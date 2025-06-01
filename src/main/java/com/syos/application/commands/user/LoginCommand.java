package com.syos.application.commands.user;

import com.syos.application.interfaces.Command;
import com.syos.application.services.UserService;
import com.syos.infrastructure.ui.presenters.UserPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.entities.User;

/**
 * Command to handle user login
 * Authenticates user and establishes session
 */
public class LoginCommand implements Command {
    private final UserService userService;
    private final UserPresenter presenter;
    private final InputReader inputReader;
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public LoginCommand(UserService userService,
                        UserPresenter presenter,
                        InputReader inputReader) {
        this.userService = userService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        // Check if already logged in
        if (userService.isLoggedIn()) {
            User currentUser = userService.getCurrentUser();
            presenter.showInfo("Already logged in as: " + currentUser.getUsername());

            if (inputReader.readBoolean("Do you want to logout first?")) {
                userService.logout();
                presenter.showLogoutSuccess();
            } else {
                return;
            }
        }

        presenter.showHeader("User Login");

        int attempts = 0;
        boolean loginSuccessful = false;

        while (attempts < MAX_LOGIN_ATTEMPTS && !loginSuccessful) {
            try {
                // Collect credentials
                String username = inputReader.readString("Username: ").trim().toLowerCase();

                // Hide password input (in real app, use Console.readPassword())
                System.out.print("Password: ");
                String password = inputReader.readString("").trim();

                // Attempt login
                loginSuccessful = userService.login(username, password);

                if (loginSuccessful) {
                    User user = userService.getCurrentUser();
                    presenter.showLoginSuccess(user);

                    // Show user permissions
                    showUserPermissions(user);

                    // Log successful login
                    logLoginActivity(user, true);
                } else {
                    attempts++;
                    int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts;

                    if (remainingAttempts > 0) {
                        presenter.showError("Invalid username or password. " +
                                remainingAttempts + " attempts remaining.");
                    } else {
                        presenter.showError("Maximum login attempts exceeded. Access denied.");
                        logLoginActivity(null, false);
                    }
                }

            } catch (Exception e) {
                presenter.showError("Login failed: " + e.getMessage());
                attempts++;
            }
        }

        if (!loginSuccessful && attempts >= MAX_LOGIN_ATTEMPTS) {
            // In a real system, might lock the account or notify admin
            presenter.showError("Please contact system administrator.");
        }
    }

    private void showUserPermissions(User user) {
        System.out.println("\nYour Permissions:");
        System.out.println("=".repeat(30));

        if (user.canProcessSales()) {
            System.out.println("✓ Process Sales");
        }
        if (user.canManageInventory()) {
            System.out.println("✓ Manage Inventory");
        }
        if (user.canViewReports()) {
            System.out.println("✓ View Reports");
        }
        if (user.canManageUsers()) {
            System.out.println("✓ Manage Users");
        }

        System.out.println("=".repeat(30));
    }

    private void logLoginActivity(User user, boolean success) {
        if (success && user != null) {
            System.out.println("Login recorded: " + user.getUsername() +
                    " at " + java.time.LocalDateTime.now());
        } else {
            System.out.println("Failed login attempt recorded at " +
                    java.time.LocalDateTime.now());
        }
    }

    @Override
    public String getDescription() {
        return "User Login";
    }
}