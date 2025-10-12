package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import java.time.LocalDateTime;

/**
 * User Domain Entity
 * Represents a system user (Admin, Cashier, Manager)
 */
public class User {
    private final UserId id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Constructor for new users (without ID)
    public User(String username, String email, String passwordHash, UserRole role) {
        this(null, username, email, passwordHash, role, LocalDateTime.now(), null);
    }

    // Constructor for existing users (with ID)
    public User(UserId id, String username, String email, String passwordHash, UserRole role) {
        this(id, username, email, passwordHash, role, LocalDateTime.now(), null);
    }

    // Full constructor
    public User(UserId id, String username, String email, String passwordHash,
                UserRole role, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        validateUser(username, email, passwordHash, role);
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastLoginAt = lastLoginAt;
    }

    private void validateUser(String username, String email, String passwordHash, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
    }

    // Business methods
    public boolean verifyPassword(String passwordHash) {
        return this.passwordHash.equals(passwordHash);
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isManager() {
        return this.role == UserRole.MANAGER;
    }

    public boolean isCashier() {
        return this.role == UserRole.CASHIER;
    }

    // Permission checks
    public boolean canManageInventory() {
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

    public boolean canViewReports() {
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

    public boolean canProcessSales() {
        return true; // All roles can process sales
    }

    public boolean canManageUsers() {
        return role == UserRole.ADMIN;
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    // Factory method for creating a new user
    public static User createNewUser(String username, String email, String passwordHash, UserRole role) {
        return new User(username, email, passwordHash, role);
    }

    // Create a new instance with updated values (for immutability)
    public User withUpdatedEmail(String newEmail) {
        return new User(id, username, newEmail, passwordHash, role, createdAt, lastLoginAt);
    }

    public User withUpdatedRole(UserRole newRole) {
        return new User(id, username, email, passwordHash, newRole, createdAt, lastLoginAt);
    }

    public User withId(UserId newId) {
        return new User(newId, username, email, passwordHash, role, createdAt, lastLoginAt);
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, username=%s, email=%s, role=%s]",
                id, username, email, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : username.hashCode();
    }
}