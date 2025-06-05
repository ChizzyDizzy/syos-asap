package com.syos.application.commands.user;

import com.syos.application.services.UserService;
import com.syos.infrastructure.ui.presenters.UserPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.valueobjects.UserRole;
import com.syos.domain.exceptions.UserAlreadyExistsException;
import com.syos.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RegisterUserCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private UserPresenter presenter;

    @Mock
    private InputReader inputReader;

    @Mock
    private User currentUser;

    private RegisterUserCommand registerUserCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerUserCommand = new RegisterUserCommand(userService, presenter, inputReader);
    }

    @Test
    void should_register_user_successfully_when_admin_provides_valid_inputs() {
        // Arrange
        String username = "newuser";
        String email = "newuser@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        UserRole selectedRole = UserRole.CASHIER;
        String adminUsername = "admin";

        // Mock admin user is logged in
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(true);

        // Mock input collection
        when(inputReader.readString("Enter username: ")).thenReturn(username);
        when(inputReader.readString("Enter email: ")).thenReturn(email);
        when(inputReader.readString("")).thenReturn(password, confirmPassword);
        when(inputReader.readInt("Enter choice (1-3): ")).thenReturn(1); // Cashier role
        when(inputReader.readBoolean("Confirm registration?")).thenReturn(true);

        // Mock current user for logging
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getUsername()).thenReturn(adminUsername);

        // Act
        registerUserCommand.execute();

        // Assert
        // Verify authorization checks
        verify(userService).isLoggedIn();
        verify(userService).hasRole(UserRole.ADMIN);

        // Verify input collection
        verify(inputReader).readString("Enter username: ");
        verify(inputReader).readString("Enter email: ");
        verify(inputReader, times(2)).readString(""); // Password and confirm password
        verify(inputReader).readInt("Enter choice (1-3): ");
        verify(inputReader).readBoolean("Confirm registration?");

        // Verify user registration
        verify(userService).registerUser(username, email, password, selectedRole);
        verify(presenter).showRegistrationSuccess(username);

        // Verify UI interactions
        verify(presenter).showHeader("Register New User");
        verify(presenter, never()).showError(anyString());

        // Verify logging
        verify(userService).getCurrentUser();
    }

    @Test
    void should_deny_registration_when_user_is_not_admin() {
        // Arrange - Mock non-admin user
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        // Act
        registerUserCommand.execute();

        // Assert
        // Verify authorization checks
        verify(userService).isLoggedIn();
        verify(userService).hasRole(UserRole.ADMIN);

        // Verify appropriate error message
        verify(presenter).showError("Only administrators can register new users.");

        // Verify no registration process occurs
        verify(inputReader, never()).readString(anyString());
        verify(inputReader, never()).readInt(anyString());
        verify(inputReader, never()).readBoolean(anyString());
        verify(userService, never()).registerUser(anyString(), anyString(), anyString(), any(UserRole.class));
        verify(presenter, never()).showRegistrationSuccess(anyString());
        verify(presenter, never()).showHeader(anyString());
    }

    @Test
    void should_handle_user_already_exists_exception_gracefully() {
        // Arrange
        String duplicateUsername = "existinguser";
        String email = "existing@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        UserRole selectedRole = UserRole.MANAGER;

        // Mock admin user is logged in
        when(userService.isLoggedIn()).thenReturn(true);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(true);

        // Mock input collection
        when(inputReader.readString("Enter username: ")).thenReturn(duplicateUsername);
        when(inputReader.readString("Enter email: ")).thenReturn(email);
        when(inputReader.readString("")).thenReturn(password, confirmPassword);
        when(inputReader.readInt("Enter choice (1-3): ")).thenReturn(2); // Manager role
        when(inputReader.readBoolean("Confirm registration?")).thenReturn(true);

        // Mock user already exists exception
        String errorMessage = "User with username 'existinguser' already exists";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage);
        doThrow(exception)
                .when(userService)
                .registerUser(duplicateUsername, email, password, selectedRole);

        // Act
        registerUserCommand.execute();

        // Assert
        // Verify authorization checks passed
        verify(userService).isLoggedIn();
        verify(userService).hasRole(UserRole.ADMIN);

        // Verify complete input collection occurred
        verify(inputReader).readString("Enter username: ");
        verify(inputReader).readString("Enter email: ");
        verify(inputReader, times(2)).readString(""); // Password and confirm password
        verify(inputReader).readInt("Enter choice (1-3): ");
        verify(inputReader).readBoolean("Confirm registration?");

        // Verify registration was attempted
        verify(userService).registerUser(duplicateUsername, email, password, selectedRole);

        // Verify proper error handling
        verify(presenter).showError(exception.getMessage());
        verify(presenter, never()).showRegistrationSuccess(anyString());

        // Verify header was shown (indicating process started)
        verify(presenter).showHeader("Register New User");
    }
}