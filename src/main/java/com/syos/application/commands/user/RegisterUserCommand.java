package com.syos.application.commands.user;

import com.syos.application.interfaces.Command;
import com.syos.application.services.UserService;
import com.syos.infrastructure.ui.presenters.UserPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.valueobjects.UserRole;
import com.syos.domain.exceptions.UserAlreadyExistsException;

/**
 * Command to register a new user in the system
 * Only admins can register new users
 */
public class RegisterUserCommand implements Command {
    private final UserService userService;
    private final UserPresenter presenter;
    private final InputReader inputReader;

    public RegisterUserCommand(UserService userService,
                               UserPresenter presenter,
                               InputReader inputReader) {
        this.userService = userService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        // Check if user is logged in and is admin
        if (!userService.isLoggedIn()) {
            presenter.showError("You must be logged in to register new users.");
            return;
        }

        if (!userService.hasRole(UserRole.ADMIN)) {
            presenter.showError("Only administrators can register new users.");
            return;
        }

        presenter.showHeader("Register New User");

        try {
            // Collect user information
            String username = collectUsername();
            String email = collectEmail();
            String password = collectPassword();
            UserRole role = collectRole();

            // Confirm registration
            if (confirmRegistration(username, email, role)) {
                // Register the user
                userService.registerUser(username, email, password, role);
                presenter.showRegistrationSuccess(username);

                // Log the action
                logUserRegistration(username, role);
            } else {
                presenter.showInfo("User registration cancelled.");
            }

        } catch (UserAlreadyExistsException e) {
            presenter.showError(e.getMessage());
        } catch (Exception e) {
            presenter.showError("Failed to register user: " + e.getMessage());
        }
    }

    private String collectUsername() {
        while (true) {
            String username = inputReader.readString("Enter username: ").trim().toLowerCase();

            if (username.length() < 3) {
                presenter.showError("Username must be at least 3 characters long.");
            } else if (username.length() > 20) {
                presenter.showError("Username must not exceed 20 characters.");
            } else if (!username.matches("^[a-z0-9_]+$")) {
                presenter.showError("Username can only contain lowercase letters, numbers, and underscores.");
            } else {
                return username;
            }
        }
    }

    private String collectEmail() {
        while (true) {
            String email = inputReader.readString("Enter email: ").trim().toLowerCase();

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                presenter.showError("Invalid email format.");
            } else {
                return email;
            }
        }
    }

    private String collectPassword() {
        while (true) {
            System.out.print("Enter password: ");
            String password = inputReader.readString("").trim();

            if (password.length() < 6) {
                presenter.showError("Password must be at least 6 characters long.");
                continue;
            }

            System.out.print("Confirm password: ");
            String confirmPassword = inputReader.readString("").trim();

            if (!password.equals(confirmPassword)) {
                presenter.showError("Passwords do not match.");
            } else {
                return password;
            }
        }
    }

    private UserRole collectRole() {
        System.out.println("\nSelect user role:");
        System.out.println("1. Cashier");
        System.out.println("2. Manager");
        System.out.println("3. Admin");

        while (true) {
            int choice = inputReader.readInt("Enter choice (1-3): ");

            switch (choice) {
                case 1:
                    return UserRole.CASHIER;
                case 2:
                    return UserRole.MANAGER;
                case 3:
                    return UserRole.ADMIN;
                default:
                    presenter.showError("Invalid choice. Please select 1-3.");
            }
        }
    }

    private boolean confirmRegistration(String username, String email, UserRole role) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Confirm User Registration:");
        System.out.println("=".repeat(50));
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
        System.out.println("=".repeat(50));

        return inputReader.readBoolean("Confirm registration?");
    }

    private void logUserRegistration(String username, UserRole role) {
        System.out.println("✓ User '" + username + "' registered as " + role +
                " by " + userService.getCurrentUser().getUsername());
    }

    @Override
    public String getDescription() {
        return "Register New User";
    }
}