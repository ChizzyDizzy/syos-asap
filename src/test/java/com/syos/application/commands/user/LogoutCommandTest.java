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

}