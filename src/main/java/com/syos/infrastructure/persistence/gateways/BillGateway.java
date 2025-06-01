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

    // Save bill with items
    public void saveBillWithItems(Bill bill) {
        connectionManager.executeWithTransaction(connection -> {
            // Save bill
            String billSql = "INSERT INTO bills (bill_date, total_amount, discount, cash_tendered, change_amount, transaction_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            long billId;
            try (PreparedStatement stmt = connection.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                setInsertParameters(stmt, bill);
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Failed to get bill ID");
                    }
                }
            }

            // Save bill items
            String itemSql = "INSERT INTO bill_items (bill_number, item_code, quantity, total_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(itemSql)) {
                for (BillItem item : bill.getItems()) {
                    stmt.setLong(1, billId);
                    stmt.setString(2, item.getItem().getCode().getValue());
                    stmt.setInt(3, item.getQuantity().getValue());
                    stmt.setBigDecimal(4, item.getTotalPrice().getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        });
    }

    public List<Bill> findByDate(LocalDate date) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM bills WHERE DATE(bill_date) = ?";
            List<Bill> bills = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(mapper.mapRow(rs));
                    }
                }
            }
            return bills;
        });
    }
}