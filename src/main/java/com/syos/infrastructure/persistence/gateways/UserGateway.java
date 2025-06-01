package com.syos.infrastructure.persistence.gateways;

import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Table Data Gateway for User entity
 * Handles all database operations related to users
 */
public class UserGateway extends OracleDatabaseGateway<User> {

    public UserGateway(DatabaseConnectionPool pool) {
        super();
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE users SET email = ?, role = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM users WHERE id = ?";
    }

    @Override
    protected String getFindByIdSQL() {
        return "SELECT * FROM users WHERE id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPasswordHash());
        stmt.setString(4, user.getRole().name());
        stmt.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getEmail());
        stmt.setString(2, user.getRole().name());
        stmt.setLong(3, user.getId().getValue());
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        UserId id = new UserId(rs.getLong("id"));
        String username = rs.getString("username");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        UserRole role = UserRole.valueOf(rs.getString("role"));

        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        // Handle nullable last_login_at
        LocalDateTime lastLoginAt = null;
        Timestamp lastLoginTimestamp = rs.getTimestamp("last_login_at");
        if (lastLoginTimestamp != null) {
            lastLoginAt = lastLoginTimestamp.toLocalDateTime();
        }

        return new User(id, username, email, passwordHash, role, createdAt, lastLoginAt);
    }

    @Override
    protected User mapResultWithId(User user, Long id) {
        // Use the withId method to create a new User with the generated ID
        return user.withId(new UserId(id));
    }

    /**
     * Find user by username
     * @param username the username to search for
     * @return User if found, null otherwise
     */
    public User findByUsername(String username) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    }
                    return null;
                }
            }
        });
    }

    /**
     * Find user by email
     * @param email the email to search for
     * @return User if found, null otherwise
     */
    public User findByEmail(String email) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    }
                    return null;
                }
            }
        });
    }

    /**
     * Find all users by role
     * @param role the role to filter by
     * @return List of users with the specified role
     */
    public List<User> findByRole(UserRole role) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
            List<User> users = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, role.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToEntity(rs));
                    }
                }
            }
            return users;
        });
    }

    /**
     * Find all users
     * @return List of all users
     */
    public List<User> findAll() {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users ORDER BY username";
            List<User> users = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(mapResultSetToEntity(rs));
                }
            }
            return users;
        });
    }

    /**
     * Find active users (logged in within last 30 days)
     * @return List of active users
     */
    public List<User> findActiveUsers() {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users WHERE last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) ORDER BY last_login_at DESC";
            List<User> users = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(mapResultSetToEntity(rs));
                }
            }
            return users;
        });
    }

    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                }
            }
        });
    }

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                }
            }
        });
    }

    /**
     * Update user password
     * @param userId the user ID
     * @param newPasswordHash the new password hash
     */
    public void updatePassword(Long userId, String newPasswordHash) {
        connectionManager.executeWithConnection(connection -> {
            String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newPasswordHash);
                stmt.setLong(2, userId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Update last login timestamp
     * @param userId the user ID
     */
    public void updateLastLogin(Long userId) {
        connectionManager.executeWithConnection(connection -> {
            String sql = "UPDATE users SET last_login_at = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setLong(2, userId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Update user email
     * @param userId the user ID
     * @param newEmail the new email
     */
    public void updateEmail(Long userId, String newEmail) {
        connectionManager.executeWithConnection(connection -> {
            String sql = "UPDATE users SET email = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newEmail);
                stmt.setLong(2, userId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Update user role
     * @param userId the user ID
     * @param newRole the new role
     */
    public void updateRole(Long userId, UserRole newRole) {
        connectionManager.executeWithConnection(connection -> {
            String sql = "UPDATE users SET role = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newRole.name());
                stmt.setLong(2, userId);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Get total count of users
     * @return total number of users
     */
    public int getTotalUserCount() {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) FROM users";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        });
    }

    /**
     * Get count of users by role
     * @param role the role to count
     * @return number of users with the specified role
     */
    public int getCountByRole(UserRole role) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, role.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        });
    }

    /**
     * Search users by username or email
     * @param searchTerm the term to search for
     * @return List of matching users
     */
    public List<User> searchUsers(String searchTerm) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? ORDER BY username";
            List<User> users = new ArrayList<>();
            String searchPattern = "%" + searchTerm + "%";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToEntity(rs));
                    }
                }
            }
            return users;
        });
    }
}