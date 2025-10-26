package com.syos.web.dao;

import com.syos.web.model.Sale;
import com.syos.web.model.SaleItem;
import com.syos.web.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {

    public long createSale(Sale sale, Connection conn) throws SQLException {
        String query = "INSERT INTO sales (sale_number, cashier_id, total_amount, discount, " +
                "tax_amount, payment_method, cash_tendered, change_amount, status, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sale.getSaleNumber());
            stmt.setLong(2, sale.getCashierId());
            stmt.setDouble(3, sale.getTotalAmount());
            stmt.setDouble(4, sale.getDiscount());
            stmt.setDouble(5, sale.getTaxAmount());
            stmt.setString(6, sale.getPaymentMethod());
            stmt.setDouble(7, sale.getCashTendered());
            stmt.setDouble(8, sale.getChangeAmount());
            stmt.setString(9, sale.getStatus());
            stmt.setInt(10, 0);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }

            throw new SQLException("Failed to get generated sale ID");
        }
    }

    public void createSaleItem(SaleItem item, Connection conn) throws SQLException {
        String query = "INSERT INTO sale_items (sale_id, item_code, item_name, quantity, " +
                "unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, item.getSaleId());
            stmt.setString(2, item.getItemCode());
            stmt.setString(3, item.getItemName());
            stmt.setInt(4, item.getQuantity());
            stmt.setDouble(5, item.getUnitPrice());
            stmt.setDouble(6, item.getSubtotal());

            stmt.executeUpdate();
        }
    }

    public Sale getSaleById(long saleId) throws SQLException {
        String query = "SELECT * FROM sales WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
            return null;
        }
    }

    public Sale getSaleById(long saleId, Connection conn) throws SQLException {
        String query = "SELECT * FROM sales WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
            return null;
        }
    }

    public Sale getSaleByNumber(String saleNumber) throws SQLException {
        String query = "SELECT * FROM sales WHERE sale_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, saleNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
            return null;
        }
    }

    public List<SaleItem> getSaleItems(long saleId) throws SQLException {
        String query = "SELECT * FROM sale_items WHERE sale_id = ?";
        List<SaleItem> items = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }
        }

        return items;
    }

    public List<SaleItem> getSaleItems(long saleId, Connection conn) throws SQLException {
        String query = "SELECT * FROM sale_items WHERE sale_id = ?";
        List<SaleItem> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(mapResultSetToSaleItem(rs));
            }
        }

        return items;
    }

    public List<Sale> getAllSales() throws SQLException {
        String query = "SELECT * FROM sales ORDER BY created_at DESC";
        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }

        return sales;
    }

    public List<Sale> getSalesByCashier(long cashierId, Date startDate, Date endDate)
            throws SQLException {
        String query = "SELECT * FROM sales WHERE cashier_id = ? AND created_at BETWEEN ? AND ? " +
                "ORDER BY created_at DESC";
        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, cashierId);
            stmt.setTimestamp(2, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(3, new Timestamp(endDate.getTime()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }

        return sales;
    }

    public List<Sale> getSalesByDate(Date date) throws SQLException {
        String query = "SELECT * FROM sales WHERE DATE(created_at) = ? ORDER BY created_at DESC";
        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }

        return sales;
    }

    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) throws SQLException {
        String query = "SELECT * FROM sales WHERE created_at BETWEEN ? AND ? " +
                "ORDER BY created_at DESC";
        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }

        return sales;
    }

    public void updateSaleStatus(long saleId, String status, Connection conn) throws SQLException {
        String query = "UPDATE sales SET status=?, version=version+1 WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setLong(2, saleId);
            stmt.executeUpdate();
        }
    }

    public int getNextSequenceForDate(String datePart) throws SQLException {
        String query = "SELECT COUNT(*) + 1 as next_seq FROM sales " +
                "WHERE DATE_FORMAT(created_at, '%Y%m%d') = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, datePart);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("next_seq");
            }
            return 1;
        }
    }

    public List<Map<String, Object>> getTopSellingProducts(Date startDate, Date endDate, int limit)
            throws SQLException {
        String query = "SELECT si.item_code, si.item_name, SUM(si.quantity) as total_quantity, " +
                "SUM(si.subtotal) as total_revenue, COUNT(DISTINCT si.sale_id) as num_sales " +
                "FROM sale_items si " +
                "JOIN sales s ON si.sale_id = s.id " +
                "WHERE s.status = 'COMPLETED' AND s.created_at BETWEEN ? AND ? " +
                "GROUP BY si.item_code, si.item_name " +
                "ORDER BY total_quantity DESC LIMIT ?";

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("itemCode", rs.getString("item_code"));
                row.put("itemName", rs.getString("item_name"));
                row.put("totalQuantity", rs.getInt("total_quantity"));
                row.put("totalRevenue", rs.getDouble("total_revenue"));
                row.put("numSales", rs.getInt("num_sales"));
                results.add(row);
            }
        }

        return results;
    }

    public Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getLong("id"));
        sale.setSaleNumber(rs.getString("sale_number"));
        sale.setCashierId(rs.getLong("cashier_id"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setDiscount(rs.getDouble("discount"));
        sale.setTaxAmount(rs.getDouble("tax_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setCashTendered(rs.getDouble("cash_tendered"));
        sale.setChangeAmount(rs.getDouble("change_amount"));
        sale.setStatus(rs.getString("status"));
        sale.setCreatedAt(rs.getTimestamp("created_at"));
        sale.setVersion(rs.getInt("version"));

        return sale;
    }

    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        SaleItem item = new SaleItem();
        item.setId(rs.getLong("id"));
        item.setSaleId(rs.getLong("sale_id"));
        item.setItemCode(rs.getString("item_code"));
        item.setItemName(rs.getString("item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setSubtotal(rs.getDouble("subtotal"));

        return item;
    }
}