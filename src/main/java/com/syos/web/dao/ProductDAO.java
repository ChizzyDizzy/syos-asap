package com.syos.web.dao;

import com.syos.web.model.Product;
import com.syos.web.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT code, name, category, price, quantity_in_store, quantity_on_shelf, " +
                "reorder_level, state, expiry_date FROM items ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> getAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT code, name, category, price, quantity_in_store, quantity_on_shelf, " +
                "reorder_level, state, expiry_date FROM items " +
                "WHERE quantity_on_shelf > 0 AND state = 'ON_SHELF' ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductByCode(String itemCode) {
        String sql = "SELECT code, name, category, price, quantity_in_store, quantity_on_shelf, " +
                "reorder_level, state, expiry_date FROM items WHERE code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO items (code, name, category, price, quantity_in_store, " +
                "quantity_on_shelf, reorder_level, state, purchase_date, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getItemCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getQuantityInStore());
            stmt.setInt(6, product.getQuantityOnShelf());
            stmt.setInt(7, product.getReorderLevel());
            stmt.setString(8, product.getState());

            if (product.getExpiryDate() != null) {
                stmt.setDate(9, product.getExpiryDate());
            } else {
                stmt.setNull(9, Types.DATE);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE items SET name=?, category=?, price=?, quantity_in_store=?, " +
                "quantity_on_shelf=?, reorder_level=?, state=?, expiry_date=? WHERE code=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantityInStore());
            stmt.setInt(5, product.getQuantityOnShelf());
            stmt.setInt(6, product.getReorderLevel());
            stmt.setString(7, product.getState());

            if (product.getExpiryDate() != null) {
                stmt.setDate(8, product.getExpiryDate());
            } else {
                stmt.setNull(8, Types.DATE);
            }

            stmt.setString(9, product.getItemCode());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProduct(String itemCode) {
        String sql = "DELETE FROM items WHERE code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStock(String itemCode, int quantity) {
        String sql = "UPDATE items SET quantity_on_shelf = quantity_on_shelf - ? " +
                "WHERE code = ? AND quantity_on_shelf >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setString(2, itemCode);
            stmt.setInt(3, quantity);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                checkSoldOut(conn, itemCode);
            }

            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT code, name, category, price, quantity_in_store, quantity_on_shelf, " +
                "reorder_level, state, expiry_date FROM items " +
                "WHERE (quantity_in_store + quantity_on_shelf) < reorder_level " +
                "AND state NOT IN ('EXPIRED', 'SOLD_OUT') " +
                "ORDER BY (quantity_in_store + quantity_on_shelf) ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private void checkSoldOut(Connection conn, String itemCode) throws SQLException {
        String sql = "UPDATE items SET state = 'SOLD_OUT' " +
                "WHERE code = ? AND (quantity_in_store + quantity_on_shelf) = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode);
            stmt.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setItemCode(rs.getString("code"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getDouble("price"));
        product.setQuantityInStore(rs.getInt("quantity_in_store"));
        product.setQuantityOnShelf(rs.getInt("quantity_on_shelf"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setState(rs.getString("state"));

        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            product.setExpiryDate(expiryDate);
        }

        return product;
    }
}