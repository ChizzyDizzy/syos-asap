package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with valid data")
    void should_create_user_with_valid_data() {
        User user = new User("testuser", "test@example.com", "hashedPassword", UserRole.CASHIER);

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.CASHIER, user.getRole());
    }

    @Test
    @DisplayName("Should verify password correctly")
    void should_verify_password_correctly() {
        String passwordHash = "hashedPassword123";
        User user = new User("testuser", "test@example.com", passwordHash, UserRole.CASHIER);

        assertTrue(user.verifyPassword(passwordHash));
        assertFalse(user.verifyPassword("wrongPassword"));
    }

    @Test
    @DisplayName("Should check user permissions correctly")
    void should_check_user_permissions_correctly() {
        User admin = new User("admin", "admin@example.com", "hash", UserRole.ADMIN);
        User cashier = new User("cashier", "cashier@example.com", "hash", UserRole.CASHIER);
        User manager = new User("manager", "manager@example.com", "hash", UserRole.MANAGER);

        // Admin can do everything
        assertTrue(admin.canManageUsers());
        assertTrue(admin.canManageInventory());
        assertTrue(admin.canViewReports());
        assertTrue(admin.canProcessSales());

        // Cashier can only process sales
        assertFalse(cashier.canManageUsers());
        assertFalse(cashier.canManageInventory());
        assertFalse(cashier.canViewReports());
        assertTrue(cashier.canProcessSales());

        // Manager can do most things except manage users
        assertFalse(manager.canManageUsers());
        assertTrue(manager.canManageInventory());
        assertTrue(manager.canViewReports());
        assertTrue(manager.canProcessSales());
    }

    @Test
    @DisplayName("Should throw exception for invalid user data")
    void should_throw_exception_for_invalid_user_data() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("", "email@test.com", "hash", UserRole.ADMIN)
        );

        assertThrows(IllegalArgumentException.class, () ->
                new User("user", "invalid-email", "hash", UserRole.ADMIN)
        );

        assertThrows(IllegalArgumentException.class, () ->
                new User("user", "email@test.com", "", UserRole.ADMIN)
        );
    }
}