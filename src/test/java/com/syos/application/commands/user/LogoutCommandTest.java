package com.syos.application.commands.user;

import com.syos.application.services.UserService;
import com.syos.domain.valueobjects.UserRole;
import com.syos.infrastructure.ui.presenters.UserPresenter;
import com.syos.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LogoutCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private UserPresenter presenter;

    @Mock
    private User mockUser;

    private LogoutCommand logoutCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutCommand = new LogoutCommand(userService, presenter);
    }

    @Test
    void should_logout_successfully_and_display_session_summary_when_user_is_logged_in() {
        // Arrange
        String username = "testuser";
        String userRole = "EMPLOYEE";
        LocalDateTime loginTime = LocalDateTime.now().minusHours(2).minusMinutes(30);

        // Mock user is logged in
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        // Mock user properties
        when(mockUser.getUsername()).thenReturn(username);
        when(mockUser.getRole()).thenReturn(UserRole.valueOf(userRole));
        when(mockUser.getLastLoginAt()).thenReturn(loginTime);

        // Act
        logoutCommand.execute();

        // Assert
        // Verify logout flow
        verify(userService).isLoggedIn();
        verify(userService).getCurrentUser();
        verify(userService).logout();

        // Verify presenter interactions
        verify(presenter).showHeader("User Logout");
        verify(presenter).showLogoutSuccess();

        // Verify user properties were accessed for session summary
        verify(mockUser).getUsername();
        verify(mockUser).getRole();
        verify(mockUser).getLastLoginAt();

        // Verify no error messages
        verify(presenter, never()).showError(anyString());
    }

    @Test
    void should_show_error_message_when_no_user_is_logged_in() {
        // Arrange
        when(userService.isLoggedIn()).thenReturn(false);

        // Act
        logoutCommand.execute();

        // Assert
        // Verify error handling
        verify(userService).isLoggedIn();
        verify(presenter).showError("No user is currently logged in.");

        // Verify logout process is not executed
        verify(userService, never()).getCurrentUser();
        verify(userService, never()).logout();
        verify(presenter, never()).showHeader("User Logout");
        verify(presenter, never()).showLogoutSuccess();
    }

    @Test
    void should_handle_missing_login_time_with_fallback_and_complete_logout_process() {
        // Arrange
        String username = "testuser";
        String userRole = "MANAGER";

        // Mock user is logged in but has no last login time
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        // Mock user properties with null login time
        when(mockUser.getUsername()).thenReturn(username);
        when(mockUser.getRole()).thenReturn(UserRole.valueOf(userRole));
        when(mockUser.getLastLoginAt()).thenReturn(null); // No login time available

        // Act
        logoutCommand.execute();

        // Assert
        // Verify complete logout flow executed
        verify(userService).isLoggedIn();
        verify(userService).getCurrentUser();
        verify(userService).logout();

        // Verify presenter interactions
        verify(presenter).showHeader("User Logout");
        verify(presenter).showLogoutSuccess();

        // Verify user properties were accessed
        verify(mockUser).getUsername();
        verify(mockUser).getRole();
        verify(mockUser).getLastLoginAt();

        // Verify graceful handling of null login time
        // (The command should still complete successfully using fallback time)
        verify(presenter, never()).showError(anyString());

        // Verify logout was called despite missing login time
        verify(userService).logout();
    }
}