package com.syos.web.dao;

import com.syos.web.model.Bill;
import com.syos.web.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public String saveBill(Bill bill) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String billNumber = generateBillNumber(conn);
            bill.setBillNumber(billNumber);

            // Insert bill
            String billSql = "INSERT INTO bills (bill_number, bill_date, total_amount, discount, " +
                    "cash_tendered, change_amount, transaction_type, user_id) " +
                    "VALUES (?, NOW(), ?, 0, ?, ?, ?, ?)";

            long billId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, billNumber);
                stmt.setDouble(2, bill.getTotalAmount());
                stmt.setDouble(3, bill.getCashReceived());
                stmt.setDouble(4, bill.getChangeAmount());
                stmt.setString(5, bill.getTransactionType());
                stmt.setString(6, bill.getUserId());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    billId = rs.getLong(1);
                }
            }

            // Insert bill items
            String itemSql = "INSERT INTO bill_items (bill_id, bill_number, item_code, item_name, quantity, unit_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (Bill.BillItem item : bill.getItems()) {
                    stmt.setLong(1, billId);
                    stmt.setString(2, billNumber);
                    stmt.setString(3, item.getItemCode());
                    stmt.setString(4, item.getItemName());
                    stmt.setInt(5, item.getQuantity());
                    stmt.setDouble(6, item.getUnitPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return billNumber;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT bill_number, user_id, total_amount, cash_tendered, " +
                "change_amount, transaction_type, bill_date FROM bills ORDER BY bill_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBillNumber(rs.getString("bill_number"));
                bill.setUserId(rs.getString("user_id"));
                bill.setTotalAmount(rs.getDouble("total_amount"));
                bill.setCashReceived(rs.getDouble("cash_tendered"));
                bill.setChangeAmount(rs.getDouble("change_amount"));
                bill.setTransactionType(rs.getString("transaction_type"));
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public List<Bill> getTodaysBills() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT bill_number, user_id, total_amount, cash_tendered, " +
                "change_amount, transaction_type, bill_date FROM bills " +
                "WHERE DATE(bill_date) = CURDATE() ORDER BY bill_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBillNumber(rs.getString("bill_number"));
                bill.setUserId(rs.getString("user_id"));
                bill.setTotalAmount(rs.getDouble("total_amount"));
                bill.setCashReceived(rs.getDouble("cash_tendered"));
                bill.setChangeAmount(rs.getDouble("change_amount"));
                bill.setTransactionType(rs.getString("transaction_type"));
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public Bill getBillWithItems(String billNumber) {
        String billSql = "SELECT bill_number, user_id, total_amount, cash_tendered, " +
                "change_amount, transaction_type, bill_date FROM bills WHERE bill_number = ?";
        String itemsSql = "SELECT item_code, item_name, quantity, unit_price, subtotal " +
                "FROM bill_items WHERE bill_number = ?";

        try (Connection conn = DBConnection.getConnection()) {
            Bill bill = null;

            // Get bill
            try (PreparedStatement stmt = conn.prepareStatement(billSql)) {
                stmt.setString(1, billNumber);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    bill = new Bill();
                    bill.setBillNumber(rs.getString("bill_number"));
                    bill.setUserId(rs.getString("user_id"));
                    bill.setTotalAmount(rs.getDouble("total_amount"));
                    bill.setCashReceived(rs.getDouble("cash_tendered"));
                    bill.setChangeAmount(rs.getDouble("change_amount"));
                    bill.setTransactionType(rs.getString("transaction_type"));
                }
            }

            // Get items
            if (bill != null) {
                try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                    stmt.setString(1, billNumber);
                    ResultSet rs = stmt.executeQuery();

                    List<Bill.BillItem> items = new ArrayList<>();
                    while (rs.next()) {
                        Bill.BillItem item = new Bill.BillItem();
                        item.setItemCode(rs.getString("item_code"));
                        item.setItemName(rs.getString("item_name"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getDouble("unit_price"));
                        item.setSubtotal(rs.getDouble("subtotal"));
                        items.add(item);
                    }
                    bill.setItems(items);
                }
            }

            return bill;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getTodaysTotalSales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total " +
                "FROM bills WHERE DATE(bill_date) = CURDATE()";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private String generateBillNumber(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT fn_generate_bill_number() as bill_number")) {
            if (rs.next()) {
                return rs.getString("bill_number");
            }
        } catch (SQLException e) {
            // Fallback
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM bills")) {
                if (rs.next()) {
                    int count = rs.getInt("count") + 1;
                    return "BILL" + String.format("%06d", count);
                }
            }
        }
        return "BILL000001";
    }
}