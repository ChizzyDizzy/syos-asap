package com.syos.infrastructure.persistence.gateways;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;
import com.syos.infrastructure.persistence.mappers.BillMapper;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gateway for Bill entity database operations
 * Implements the Table Data Gateway pattern
 */
public class BillGateway extends OracleDatabaseGateway<Bill> {
    private final BillMapper mapper;

    public BillGateway(DatabaseConnectionPool pool) {
        super();
        this.mapper = new BillMapper();
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO bills (bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        throw new UnsupportedOperationException("Bills cannot be updated");
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM bills WHERE bill_number = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM bills WHERE bill_number = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Bill bill) throws SQLException {
        stmt.setTimestamp(1, Timestamp.valueOf(bill.getBillDate()));
        stmt.setBigDecimal(2, bill.getTotalAmount().getValue());
        stmt.setBigDecimal(3, bill.getDiscount().getValue());
        stmt.setBigDecimal(4, bill.getCashTendered().getValue());
        stmt.setBigDecimal(5, bill.getChange().getValue());
        stmt.setString(6, bill.getTransactionType().name());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Bill bill) throws SQLException {
        throw new UnsupportedOperationException("Bills cannot be updated");
    }

    @Override
    protected Bill mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapper.mapRow(rs);
    }

    @Override
    protected Bill mapResultWithId(Bill bill, Long id) {
        // Create new bill with generated ID
        return new Bill.Builder()
                .withBillNumber(id.intValue())
                .withDate(bill.getBillDate())
                .withDiscount(bill.getDiscount().getValue())
                .withCashTendered(bill.getCashTendered().getValue())
                .withTransactionType(bill.getTransactionType())
                .build();
    }

    /**
     * Save bill with its items in a transaction
     * @param bill The bill to save with items
     */
    public void saveBillWithItems(Bill bill) {
        connectionManager.executeWithTransaction(connection -> {
            // Save bill
            String billSql = "INSERT INTO bills (bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            long billId;
            try (PreparedStatement stmt = connection.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, Timestamp.valueOf(bill.getBillDate()));
                stmt.setBigDecimal(2, bill.getTotalAmount().getValue());
                stmt.setBigDecimal(3, bill.getDiscount().getValue());
                stmt.setBigDecimal(4, bill.getCashTendered().getValue());
                stmt.setBigDecimal(5, bill.getChange().getValue());
                stmt.setString(6, bill.getTransactionType().name());

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Failed to get generated bill ID");
                    }
                }
            }

            // Save bill items
            String itemSql = "INSERT INTO bill_items (bill_number, item_code, quantity, unit_price, total_price) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(itemSql)) {
                for (BillItem billItem : bill.getItems()) {
                    stmt.setLong(1, billId);
                    stmt.setString(2, billItem.getItem().getCode().getValue());
                    stmt.setInt(3, billItem.getQuantity().getValue());
                    stmt.setBigDecimal(4, billItem.getItem().getPrice().getValue());
                    stmt.setBigDecimal(5, billItem.getTotalPrice().getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        });
    }

    /**
     * Find bills by date
     * @param date The date to search for
     * @return List of bills from that date
     */
    public List<Bill> findByDate(LocalDate date) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT b.*, bi.*, i.* FROM bills b " +
                    "LEFT JOIN bill_items bi ON b.bill_number = bi.bill_number " +
                    "LEFT JOIN items i ON bi.item_code = i.code " +
                    "WHERE DATE(b.bill_date) = ? " +
                    "ORDER BY b.bill_number, bi.id";

            List<Bill> bills = new ArrayList<>();
            Bill currentBill = null;
            int lastBillNumber = -1;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int billNumber = rs.getInt("bill_number");

                        // If new bill, create it
                        if (billNumber != lastBillNumber) {
                            currentBill = mapper.mapRow(rs);
                            bills.add(currentBill);
                            lastBillNumber = billNumber;
                        }

                        // Add bill item if exists
                        if (rs.getString("item_code") != null) {
                            BillItem billItem = mapper.mapBillItem(rs);
                            currentBill.getItems().add(billItem);
                        }
                    }
                }
            }
            return bills;
        });
    }

    /**
     * Find all bills
     * @return List of all bills
     */
    public List<Bill> findAll() {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT b.*, bi.*, i.* FROM bills b " +
                    "LEFT JOIN bill_items bi ON b.bill_number = bi.bill_number " +
                    "LEFT JOIN items i ON bi.item_code = i.code " +
                    "ORDER BY b.bill_date DESC, b.bill_number, bi.id";

            List<Bill> bills = new ArrayList<>();
            Bill currentBill = null;
            int lastBillNumber = -1;

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int billNumber = rs.getInt("bill_number");

                    // If new bill, create it
                    if (billNumber != lastBillNumber) {
                        currentBill = mapper.mapRow(rs);
                        bills.add(currentBill);
                        lastBillNumber = billNumber;
                    }

                    // Add bill item if exists
                    if (rs.getString("item_code") != null) {
                        BillItem billItem = mapper.mapBillItem(rs);
                        currentBill.getItems().add(billItem);
                    }
                }
            }
            return bills;
        });
    }

    /**
     * Find a bill by its number
     * @param billNumber The bill number to search for
     * @return The bill if found, null otherwise
     */
    public Bill findByBillNumber(int billNumber) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT b.*, bi.*, i.* FROM bills b " +
                    "LEFT JOIN bill_items bi ON b.bill_number = bi.bill_number " +
                    "LEFT JOIN items i ON bi.item_code = i.code " +
                    "WHERE b.bill_number = ?";

            Bill bill = null;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, billNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (bill == null) {
                            bill = mapper.mapRow(rs);
                        }

                        // Add bill item if exists
                        if (rs.getString("item_code") != null) {
                            BillItem billItem = mapper.mapBillItem(rs);
                            bill.getItems().add(billItem);
                        }
                    }
                }
            }
            return bill;
        });
    }

    /**
     * Get total sales for a date
     * @param date The date to calculate total for
     * @return Total sales amount
     */
    public Money getTotalSalesForDate(LocalDate date) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT SUM(total_amount) as total FROM bills WHERE DATE(bill_date) = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        java.math.BigDecimal total = rs.getBigDecimal("total");
                        return new Money(total != null ? total : java.math.BigDecimal.ZERO);
                    }
                    return new Money(java.math.BigDecimal.ZERO);
                }
            }
        });
    }

    /**
     * Count bills for a date
     * @param date The date to count bills for
     * @return Number of bills
     */
    public int countBillsForDate(LocalDate date) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) as count FROM bills WHERE DATE(bill_date) = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            }
        });
    }

    /**
     * Find bills within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of bills in the date range
     */
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT b.*, bi.*, i.* FROM bills b " +
                    "LEFT JOIN bill_items bi ON b.bill_number = bi.bill_number " +
                    "LEFT JOIN items i ON bi.item_code = i.code " +
                    "WHERE DATE(b.bill_date) BETWEEN ? AND ? " +
                    "ORDER BY b.bill_date DESC, b.bill_number, bi.id";

            List<Bill> bills = new ArrayList<>();
            Bill currentBill = null;
            int lastBillNumber = -1;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int billNumber = rs.getInt("bill_number");

                        // If new bill, create it
                        if (billNumber != lastBillNumber) {
                            currentBill = mapper.mapRow(rs);
                            bills.add(currentBill);
                            lastBillNumber = billNumber;
                        }

                        // Add bill item if exists
                        if (rs.getString("item_code") != null) {
                            BillItem billItem = mapper.mapBillItem(rs);
                            currentBill.getItems().add(billItem);
                        }
                    }
                }
            }
            return bills;
        });
    }
}