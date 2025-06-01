package com.syos.application.services;

import com.syos.domain.entities.User;
import com.syos.domain.exceptions.UserAlreadyExistsException;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.gateways.UserGateway;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {
    private final UserGateway userGateway;
    private User currentUser;

    public UserService(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public void registerUser(String username, String email, String password, UserRole role) {
        // Check if username already exists
        if (userGateway.findByUsername(username) != null) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        String passwordHash = hashPassword(password);
        User user = new User(null, username, email, passwordHash, role);
        userGateway.insert(user);
    }

    public boolean login(String username, String password) {
        try {
            System.out.println("DEBUG: Attempting login for user: " + username);

            User user = userGateway.findByUsername(username);
            if (user == null) {
                System.out.println("DEBUG: User not found in database");
                return false;
            }

            System.out.println("DEBUG: User found - " + user.getUsername());

            String passwordHash = hashPassword(password);
            System.out.println("DEBUG: Input password hash: " + passwordHash);
            System.out.println("DEBUG: Stored password hash: " + user.getPasswordHash());

            if (user.verifyPassword(passwordHash)) {
                currentUser = user;
                System.out.println("DEBUG: Password match - login successful");

                // Update last login time
                userGateway.updateLastLogin(user.getId().getValue());

                return true;
            }

            System.out.println("DEBUG: Password mismatch - login failed");
            return false;

        } catch (Exception e) {
            System.err.println("DEBUG: Login error - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }


    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }



}