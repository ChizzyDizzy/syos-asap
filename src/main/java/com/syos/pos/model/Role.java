package com.syos.pos.model;

public enum Role {
    ADMIN(1),
    MANAGER(2),
    CASHIER(3),
    CUSTOMER(4);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean hasPrivilegeOf(Role other) {
        return this.level <= other.level;
    }

    public boolean canManage(Role targetRole) {
        return this.level < targetRole.level;
    }

    public boolean canManageProducts() {
        return this == ADMIN || this == MANAGER;
    }

    public boolean canCreateSales() {
        return this == ADMIN || this == MANAGER || this == CASHIER;
    }

    public boolean canGenerateReports() {
        return this == ADMIN || this == MANAGER;
    }

    public boolean canManageUsers() {
        return this == ADMIN || this == MANAGER;
    }

    public boolean canAccessCLI() {
        return this == ADMIN || this == MANAGER || this == CASHIER;
    }

    public boolean canViewProductsOnly() {
        return this == CUSTOMER;
    }

    public boolean canCRUD(String resource) {
        switch (resource.toUpperCase()) {
            case "PRODUCTS":
                return this == ADMIN || this == MANAGER;
            case "SALES":
                return this == ADMIN || this == MANAGER || this == CASHIER;
            case "BILLS":
                return this == ADMIN || this == MANAGER || this == CASHIER;
            case "USERS":
                return this == ADMIN || this == MANAGER;
            case "REPORTS":
                return this == ADMIN || this == MANAGER;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}