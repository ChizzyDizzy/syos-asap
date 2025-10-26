package com.syos.web.dao;

import com.syos.web.model.Bill;
import com.syos.web.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public Bill getBillById(long id) throws SQLException {
        String query = "SELECT * FROM bills WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBill(rs);
            }
            return null;
        }
    }

    public Bill getBillByNumber(String billNumber) throws SQLException {
        String query = "SELECT * FROM bills WHERE bill_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, billNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBill(rs);
            }
            return null;
        }
    }

    public List<Bill> getAllBills() throws SQLException {
        String query = "SELECT * FROM bills ORDER BY created_at DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }

        return bills;
    }

    public List<Bill> getBillsByUser(long userId) throws SQLException {
        String query = "SELECT * FROM bills WHERE user_id = ? ORDER BY created_at DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }

        return bills;
    }

    public List<Bill> getBillsByStatus(String status) throws SQLException {
        String query = "SELECT * FROM bills WHERE status = ? ORDER BY created_at DESC";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }

        return bills;
    }

    public boolean createBill(Bill bill) throws SQLException {
        String query = "INSERT INTO bills (bill_number, user_id, total_amount, discount, " +
                "tax_amount, net_amount, status, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, bill.getBillNumber());
            stmt.setLong(2, bill.getUserId());
            stmt.setDouble(3, bill.getTotalAmount());
            stmt.setDouble(4, bill.getDiscount());
            stmt.setDouble(5, bill.getTaxAmount());
            stmt.setDouble(6, bill.getNetAmount());
            stmt.setString(7, bill.getStatus());
            stmt.setInt(8, 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    bill.setId(rs.getLong(1));
                }
                return true;
            }
            return false;
        }
    }

    // NEW METHOD: saveBill
    public String saveBill(Bill bill) throws SQLException {
        // Generate bill number if not exists
        if (bill.getBillNumber() == null || bill.getBillNumber().isEmpty()) {
            String billNumber = generateBillNumber();
            bill.setBillNumber(billNumber);
        }

        // Set default status if not set
        if (bill.getStatus() == null || bill.getStatus().isEmpty()) {
            bill.setStatus("COMPLETED");
        }

        // Use existing createBill method
        boolean created = createBill(bill);

        if (created) {
            return bill.getBillNumber();
        }
        return null;
    }

    // NEW METHOD: generateBillNumber
    private String generateBillNumber() {
        return "BILL-" + System.currentTimeMillis();
    }

    public boolean updateBill(Bill bill) throws SQLException {
        String query = "UPDATE bills SET total_amount=?, discount=?, tax_amount=?, " +
                "net_amount=?, status=?, version=? WHERE id=? AND version=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int newVersion = bill.getVersion() + 1;

            stmt.setDouble(1, bill.getTotalAmount());
            stmt.setDouble(2, bill.getDiscount());
            stmt.setDouble(3, bill.getTaxAmount());
            stmt.setDouble(4, bill.getNetAmount());
            stmt.setString(5, bill.getStatus());
            stmt.setInt(6, newVersion);
            stmt.setLong(7, bill.getId());
            stmt.setInt(8, bill.getVersion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                bill.setVersion(newVersion);
                return true;
            }
            return false;
        }
    }

    public boolean updateBillStatus(long billId, String status) throws SQLException {
        String query = "UPDATE bills SET status=?, version=version+1 WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setLong(2, billId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBill(long billId) throws SQLException {
        String query = "DELETE FROM bills WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, billId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setId(rs.getLong("id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setUserId(rs.getLong("user_id"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setDiscount(rs.getDouble("discount"));
        bill.setTaxAmount(rs.getDouble("tax_amount"));
        bill.setNetAmount(rs.getDouble("net_amount"));
        bill.setStatus(rs.getString("status"));
        bill.setCreatedAt(rs.getTimestamp("created_at"));
        bill.setUpdatedAt(rs.getTimestamp("updated_at"));
        bill.setVersion(rs.getInt("version"));

        return bill;
    }
}