package com.syos.application.services;

import com.syos.domain.entities.User;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.UserAlreadyExistsException;
import com.syos.infrastructure.persistence.gateways.UserGateway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock private UserGateway userGateway;
    @InjectMocks private UserService userService;

    private User testUser;
    private String testPassword = "testPassword123";
    private String expectedPasswordHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"; // SHA-256 of "testPassword123"

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test user with proper password hash
        testUser = new User(
                new UserId(1L),
                "testuser",
                "test@example.com",
                expectedPasswordHash,
                UserRole.CASHIER
        );
    }

    @Test
    @DisplayName("Should register user successfully when username does not exist")
    void should_register_user_successfully_when_username_does_not_exist() {
        // Arrange
        when(userGateway.findByUsername("newuser")).thenReturn(null);

        // Act
        userService.registerUser("newuser", "new@example.com", testPassword, UserRole.MANAGER);

        // Assert
        verify(userGateway).findByUsername("newuser");
        verify(userGateway).insert(any(User.class));

        // Verify the user object passed to insert has correct properties
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userGateway).insert(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("newuser", capturedUser.getUsername());
        assertEquals("new@example.com", capturedUser.getEmail());
        assertEquals(UserRole.MANAGER, capturedUser.getRole());
        assertEquals(expectedPasswordHash, capturedUser.getPasswordHash()); // Password should be hashed
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username already exists")
    void should_throw_user_already_exists_exception_when_username_already_exists() {
        // Arrange
        when(userGateway.findByUsername("existinguser")).thenReturn(testUser);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser("existinguser", "test@example.com", testPassword, UserRole.ADMIN));

        assertEquals("Username already taken", exception.getMessage());
        verify(userGateway).findByUsername("existinguser");
        verify(userGateway, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully when valid credentials provided")
    void should_login_successfully_when_valid_credentials_provided() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);

        // Act
        boolean result = userService.login("testuser", testPassword);

        // Assert
        assertTrue(result);
        assertTrue(userService.isLoggedIn());
        assertEquals(testUser, userService.getCurrentUser());

        verify(userGateway).findByUsername("testuser");
        verify(userGateway).updateLastLogin(testUser.getId().getValue());
        verify(testUser).verifyPassword(expectedPasswordHash);
    }

    @Test
    @DisplayName("Should fail login when user does not exist")
    void should_fail_login_when_user_does_not_exist() {
        // Arrange
        when(userGateway.findByUsername("nonexistent")).thenReturn(null);

        // Act
        boolean result = userService.login("nonexistent", testPassword);

        // Assert
        assertFalse(result);
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getCurrentUser());

        verify(userGateway).findByUsername("nonexistent");
        verify(userGateway, never()).updateLastLogin(anyLong());
    }

    @Test
    @DisplayName("Should fail login when password is incorrect")
    void should_fail_login_when_password_is_incorrect() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(any())).thenReturn(false);

        // Act
        boolean result = userService.login("testuser", "wrongpassword");

        // Assert
        assertFalse(result);
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getCurrentUser());

        verify(userGateway).findByUsername("testuser");
        verify(userGateway, never()).updateLastLogin(anyLong());
        verify(testUser).verifyPassword(any());
    }

    @Test
    @DisplayName("Should throw RuntimeException when gateway throws exception during login")
    void should_throw_runtime_exception_when_gateway_throws_exception_during_login() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.login("testuser", testPassword));

        assertTrue(exception.getMessage().contains("Login failed"));
        assertTrue(exception.getMessage().contains("Database error"));
        assertFalse(userService.isLoggedIn());
    }

    @Test
    @DisplayName("Should logout successfully and clear current user state")
    void should_logout_successfully_and_clear_current_user_state() {
        // Arrange - First login a user
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);
        userService.login("testuser", testPassword);

        // Verify user is logged in
        assertTrue(userService.isLoggedIn());

        // Act
        userService.logout();

        // Assert
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getCurrentUser());
    }

    @Test
    @DisplayName("Should return correct current user when logged in")
    void should_return_correct_current_user_when_logged_in() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);

        // Act
        userService.login("testuser", testPassword);
        User currentUser = userService.getCurrentUser();

        // Assert
        assertEquals(testUser, currentUser);
        assertEquals("testuser", currentUser.getUsername());
        assertEquals(UserRole.CASHIER, currentUser.getRole());
    }

    @Test
    @DisplayName("Should return null for current user when not logged in")
    void should_return_null_for_current_user_when_not_logged_in() {
        // Arrange - No login performed

        // Act
        User currentUser = userService.getCurrentUser();

        // Assert
        assertNull(currentUser);
        assertFalse(userService.isLoggedIn());
    }

    @Test
    @DisplayName("Should return false for is logged in when no user logged in")
    void should_return_false_for_is_logged_in_when_no_user_logged_in() {
        // Arrange - No login performed

        // Act
        boolean isLoggedIn = userService.isLoggedIn();

        // Assert
        assertFalse(isLoggedIn);
    }

    @Test
    @DisplayName("Should return true for has role when user has specified role")
    void should_return_true_for_has_role_when_user_has_specified_role() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);
        userService.login("testuser", testPassword);

        // Act
        boolean hasRole = userService.hasRole(UserRole.CASHIER);

        // Assert
        assertTrue(hasRole);
    }

    @Test
    @DisplayName("Should return false for has role when user has different role")
    void should_return_false_for_has_role_when_user_has_different_role() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);
        userService.login("testuser", testPassword);

        // Act
        boolean hasRole = userService.hasRole(UserRole.ADMIN);

        // Assert
        assertFalse(hasRole);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    @DisplayName("Should return false for has role when no user is logged in")
    void should_return_false_for_has_role_when_no_user_is_logged_in(UserRole role) {
        // Arrange - No login performed

        // Act
        boolean hasRole = userService.hasRole(role);

        // Assert
        assertFalse(hasRole);
    }

    @Test
    @DisplayName("Should maintain user session state across multiple operations")
    void should_maintain_user_session_state_across_multiple_operations() {
        // Arrange
        when(userGateway.findByUsername("testuser")).thenReturn(testUser);
        when(testUser.verifyPassword(expectedPasswordHash)).thenReturn(true);

        // Act - Login
        userService.login("testuser", testPassword);

        // Assert - Verify state is maintained across multiple calls
        assertTrue(userService.isLoggedIn());
        assertEquals(testUser, userService.getCurrentUser());
        assertTrue(userService.hasRole(UserRole.CASHIER));
        assertFalse(userService.hasRole(UserRole.ADMIN));

        // Act - Logout
        userService.logout();

        // Assert - Verify state is cleared
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getCurrentUser());
        assertFalse(userService.hasRole(UserRole.CASHIER));
    }
}