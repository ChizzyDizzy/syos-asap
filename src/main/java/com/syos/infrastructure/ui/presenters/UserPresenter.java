package com.syos.infrastructure.ui.presenters;

import com.syos.domain.entities.User;

public class UserPresenter {

    public void showRegistrationSuccess(String username) {
        System.out.println("✓ User '" + username + "' registered successfully!");
    }

    public void showLoginSuccess(User user) {
        System.out.println("✓ Welcome, " + user.getUsername() + "!");
        System.out.println("Role: " + user.getRole());
    }

    public void showLoginFailure() {
        System.out.println("❌ Invalid username or password");
    }

    public void showLogoutSuccess() {
        System.out.println("✓ Logged out successfully");
    }

    public void showError(String message) {
        System.err.println("❌ Error: " + message);
    }

    public void showHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
    }

    public void showInfo(String message) {
        System.out.println("ℹ️  " + message);
    }
}