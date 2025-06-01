package com.syos.application.commands.user;

import com.syos.application.interfaces.Command;
import com.syos.application.services.UserService;
import com.syos.infrastructure.ui.presenters.UserPresenter;
import com.syos.domain.entities.User;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Command to handle user logout
 * Ends user session and shows session summary
 */
public class LogoutCommand implements Command {
    private final UserService userService;
    private final UserPresenter presenter;
    private LocalDateTime loginTime;

    public LogoutCommand(UserService userService, UserPresenter presenter) {
        this.userService = userService;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        // Check if user is logged in
        if (!userService.isLoggedIn()) {
            presenter.showError("No user is currently logged in.");
            return;
        }

        // Get current user before logout
        User currentUser = userService.getCurrentUser();

        // Record login time if available
        if (currentUser.getLastLoginAt() != null) {
            loginTime = currentUser.getLastLoginAt();
        } else {
            loginTime = LocalDateTime.now(); // Fallback
        }

        // Show logout confirmation
        presenter.showHeader("User Logout");

        // Display session summary
        displaySessionSummary(currentUser);

        // Perform logout
        userService.logout();

        // Show success message
        presenter.showLogoutSuccess();

        // Log the logout activity
        logLogoutActivity(currentUser);
    }

    private void displaySessionSummary(User user) {
        LocalDateTime now = LocalDateTime.now();
        Duration sessionDuration = Duration.between(loginTime, now);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Session Summary");
        System.out.println("=".repeat(50));
        System.out.println("User: " + user.getUsername());
        System.out.println("Role: " + user.getRole());
        System.out.println("Login Time: " + formatDateTime(loginTime));
        System.out.println("Logout Time: " + formatDateTime(now));
        System.out.println("Session Duration: " + formatDuration(sessionDuration));
        System.out.println("=".repeat(50));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }

    private void logLogoutActivity(User user) {
        System.out.println("Logout recorded: " + user.getUsername() +
                " at " + LocalDateTime.now());
    }

    @Override
    public String getDescription() {
        return "User Logout";
    }
}