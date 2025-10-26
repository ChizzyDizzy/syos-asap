package com.syos.web.dao;

import com.syos.web.model.Product;
import com.syos.web.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public Product getProductByCode(String code) throws SQLException {
        String query = "SELECT * FROM items WHERE code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        }
    }

    /**
     * Get product with pessimistic lock (creates own connection)
     */
    public Product getProductWithLock(String code) throws SQLException {
        String query = "SELECT * FROM items WHERE code = ? FOR UPDATE";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setString(1, code);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapResultSetToProduct(rs);
        }
        return null;
    }

    /**
     * Get product with pessimistic lock (uses existing connection for transactions)
     */
    public Product getProductWithLock(String code, Connection conn) throws SQLException {
        String query = "SELECT * FROM items WHERE code = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        String query = "SELECT * FROM items ORDER BY code";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }

        return products;
    }

    public List<Product> getProductsByCategory(String category) throws SQLException {
        String query = "SELECT * FROM items WHERE category = ? ORDER BY code";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }

        return products;
    }

    public boolean addProduct(Product product) throws SQLException {
        String query = "INSERT INTO items (code, name, category, price, quantity_in_store, " +
                "quantity_on_shelf, reorder_level, state, purchase_date, expiry_date, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getQuantityInStore());
            stmt.setInt(6, product.getQuantityOnShelf());
            stmt.setInt(7, product.getReorderLevel());
            stmt.setString(8, product.getState() != null ? product.getState() : "AVAILABLE");
            stmt.setDate(9, product.getPurchaseDate());
            stmt.setDate(10, product.getExpiryDate());
            stmt.setInt(11, 0);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String query = "UPDATE items SET name=?, category=?, price=?, quantity_in_store=?, " +
                "quantity_on_shelf=?, reorder_level=?, state=?, purchase_date=?, " +
                "expiry_date=? WHERE code=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantityInStore());
            stmt.setInt(5, product.getQuantityOnShelf());
            stmt.setInt(6, product.getReorderLevel());
            stmt.setString(7, product.getState());
            stmt.setDate(8, product.getPurchaseDate());
            stmt.setDate(9, product.getExpiryDate());
            stmt.setString(10, product.getCode());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProductWithVersion(Product product) throws SQLException {
        String query = "UPDATE items SET name=?, category=?, price=?, quantity_in_store=?, " +
                "quantity_on_shelf=?, reorder_level=?, state=?, purchase_date=?, " +
                "expiry_date=?, version=? WHERE code=? AND version=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int newVersion = product.getVersion() + 1;

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantityInStore());
            stmt.setInt(5, product.getQuantityOnShelf());
            stmt.setInt(6, product.getReorderLevel());
            stmt.setString(7, product.getState());
            stmt.setDate(8, product.getPurchaseDate());
            stmt.setDate(9, product.getExpiryDate());
            stmt.setInt(10, newVersion);
            stmt.setString(11, product.getCode());
            stmt.setInt(12, product.getVersion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                product.setVersion(newVersion);
                return true;
            }
            return false;
        }
    }

    public boolean updateProductStock(Product product) throws SQLException {
        String query = "UPDATE items SET quantity_in_store=?, quantity_on_shelf=?, version=? " +
                "WHERE code=? AND version=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int newVersion = product.getVersion() + 1;

            stmt.setInt(1, product.getQuantityInStore());
            stmt.setInt(2, product.getQuantityOnShelf());
            stmt.setInt(3, newVersion);
            stmt.setString(4, product.getCode());
            stmt.setInt(5, product.getVersion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                product.setVersion(newVersion);
                return true;
            }
            return false;
        }
    }

    /**
     * Update stock by reducing quantity on shelf
     * Used for billing/sales operations
     */
    public boolean updateStock(String code, int quantitySold) throws SQLException {
        String query = "UPDATE items SET quantity_on_shelf = quantity_on_shelf - ? " +
                "WHERE code = ? AND quantity_on_shelf >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, quantitySold);
            stmt.setString(2, code);
            stmt.setInt(3, quantitySold);

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;
        }
    }

    public boolean updateStockQuantity(String code, int newQuantity, int expectedVersion, Connection conn)
            throws SQLException {
        String query = "UPDATE items SET quantity_on_shelf=?, version=version+1 " +
                "WHERE code=? AND version=?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newQuantity);
            stmt.setString(2, code);
            stmt.setInt(3, expectedVersion);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(String code) throws SQLException {
        String query = "DELETE FROM items WHERE code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Product> getLowStockProducts() throws SQLException {
        String query = "SELECT * FROM items WHERE (quantity_in_store + quantity_on_shelf) <= reorder_level";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }

        return products;
    }

    public Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setCode(rs.getString("code"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getDouble("price"));
        product.setQuantityInStore(rs.getInt("quantity_in_store"));
        product.setQuantityOnShelf(rs.getInt("quantity_on_shelf"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setState(rs.getString("state"));
        product.setPurchaseDate(rs.getDate("purchase_date"));
        product.setExpiryDate(rs.getDate("expiry_date"));
        product.setVersion(rs.getInt("version"));

        try {
            product.setLockedBy(rs.getLong("locked_by"));
            if (rs.wasNull()) {
                product.setLockedBy(null);
            }
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }

        try {
            product.setLockTimestamp(rs.getTimestamp("lock_timestamp"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }

        return product;
    }
}