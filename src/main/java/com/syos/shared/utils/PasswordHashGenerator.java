package com.syos.shared.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to generate password hashes
 * Use this to generate hashes for database seeding
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        // Generate hashes for common passwords
        System.out.println("Password Hash Generator");
        System.out.println("=".repeat(60));

        generateAndPrint("admin123");
        generateAndPrint("cashier123");
        generateAndPrint("manager123");
        generateAndPrint("password123");
        generateAndPrint("test123");

        System.out.println("=".repeat(60));

        // Generate SQL update statements
        System.out.println("\nSQL Update Statements:");
        System.out.println("UPDATE users SET password_hash = '" + hashPassword("manager123") + "' WHERE username = 'manager1';");
    }

    private static void generateAndPrint(String password) {
        String hash = hashPassword(password);
        System.out.printf("Password: %-15s Hash: %s%n", password, hash);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}