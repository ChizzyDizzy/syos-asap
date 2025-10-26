package com.syos.web.dao;

import com.syos.web.model.User;
import com.syos.pos.model.Role;
import com.syos.web.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND is_active = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    public User getUserById(long id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT * FROM users ORDER BY role, username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public List<User> getActiveUsers() throws SQLException {
        String query = "SELECT * FROM users WHERE is_active = true ORDER BY role, username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public List<User> getUsersByRole(Role role) throws SQLException {
        String query = "SELECT * FROM users WHERE role = ? AND is_active = true ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public boolean createUser(User user) throws SQLException {
        String query = "INSERT INTO users (user_id, username, email, password_hash, role, " +
                "full_name, is_active, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getFullName());
            stmt.setBoolean(7, user.isActive());
            stmt.setInt(8, 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE users SET email=?, full_name=?, role=?, is_active=?, " +
                "version=version+1 WHERE id=? AND version=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getRole().name());
            stmt.setBoolean(4, user.isActive());
            stmt.setLong(5, user.getId());
            stmt.setInt(6, user.getVersion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                user.setVersion(user.getVersion() + 1);
                return true;
            }
            return false;
        }
    }

    public boolean updatePassword(long userId, String newPasswordHash) throws SQLException {
        String query = "UPDATE users SET password_hash=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateLastLogin(long userId) throws SQLException {
        String query = "UPDATE users SET last_login_at=CURRENT_TIMESTAMP WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(long userId) throws SQLException {
        String query = "UPDATE users SET is_active=false WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean hardDeleteUser(long userId) throws SQLException {
        String query = "DELETE FROM users WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUserId(rs.getString("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(mapStringToRole(rs.getString("role")));
        user.setFullName(rs.getString("full_name"));
        user.setActive(rs.getBoolean("is_active"));
        user.setVersion(rs.getInt("version"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setLastLoginAt(rs.getTimestamp("last_login_at"));

        return user;
    }

    private Role mapStringToRole(String roleString) {
        switch (roleString.toUpperCase()) {
            case "ADMIN":
                return Role.ADMIN;
            case "MANAGER":
                return Role.MANAGER;
            case "CASHIER":
                return Role.CASHIER;
            case "CUSTOMER":
                return Role.CUSTOMER;
            default:
                throw new IllegalArgumentException("Unknown role: " + roleString);
        }
    }
}