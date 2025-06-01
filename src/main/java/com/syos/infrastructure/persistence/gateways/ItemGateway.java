package com.syos.infrastructure.persistence.gateways;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;
import com.syos.infrastructure.persistence.mappers.ItemMapper;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ItemGateway extends OracleDatabaseGateway<Item> {
    private final ItemMapper mapper;

    public ItemGateway(DatabaseConnectionPool pool) {
        super();
        this.mapper = new ItemMapper();
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO items (code, name, price, expiry_date, state, purchase_date, quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE items SET name = ?, price = ?, quantity = ?, state = ? WHERE code = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM items WHERE id = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM items WHERE id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Item item) throws SQLException {
        stmt.setString(1, item.getCode().getValue());
        stmt.setString(2, item.getName());
        stmt.setBigDecimal(3, item.getPrice().getValue());
        stmt.setDate(4, item.getExpiryDate() != null ? Date.valueOf(item.getExpiryDate()) : null);
        stmt.setString(5, item.getState().getStateName());
        stmt.setDate(6, Date.valueOf(item.getPurchaseDate()));
        stmt.setInt(7, item.getQuantity().getValue());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Item item) throws SQLException {
        stmt.setString(1, item.getName());
        stmt.setBigDecimal(2, item.getPrice().getValue());
        stmt.setInt(3, item.getQuantity().getValue());
        stmt.setString(4, item.getState().getStateName());
        stmt.setString(5, item.getCode().getValue());
    }

    @Override
    protected Item mapResultSetToEntity(ResultSet rs) throws SQLException {
        return mapper.mapRow(rs);
    }

    @Override
    protected Item mapResultWithId(Item entity, Long id) {
        // Items use code as primary key, not auto-generated id
        return entity;
    }

    // Additional methods specific to ItemGateway
    public Item findByCode(String code) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM items WHERE code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, code);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapper.mapRow(rs);
                    }
                    return null;
                }
            }
        });
    }

    public List<Item> findAll() {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM items ORDER BY name";
            List<Item> items = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    items.add(mapper.mapRow(rs));
                }
            }
            return items;
        });
    }

    public List<Item> findLowStock(int threshold) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM items WHERE quantity < ? AND state != 'EXPIRED'";
            List<Item> items = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, threshold);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapper.mapRow(rs));
                    }
                }
            }
            return items;
        });
    }

    public List<Item> findExpiringSoon(int days) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM items WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
            List<Item> items = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, days);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapper.mapRow(rs));
                    }
                }
            }
            return items;
        });
    }
}
