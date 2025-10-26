package com.syos.web.model;

import com.syos.pos.model.Role;
import java.sql.Timestamp;

public class User {
    private long id;
    private String userId;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private String fullName;
    private boolean isActive;
    private int version;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastLoginAt;

    public User() {
        this.isActive = true;
        this.version = 0;
    }

    public User(String userId, String username, String email, String passwordHash, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.version = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Timestamp lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean canManageProducts() {
        return role.canManageProducts();
    }

    public boolean canCreateSales() {
        return role.canCreateSales();
    }

    public boolean canGenerateReports() {
        return role.canGenerateReports();
    }

    public boolean canManageUsers() {
        return role.canManageUsers();
    }

    public boolean canViewProductsOnly() {
        return role.canViewProductsOnly();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isCashier() {
        return role == Role.CASHIER;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, userId=%s, username=%s, role=%s, active=%b]",
                id, userId, username, role, isActive);
    }
}