package com.syos.web.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password Utility for secure password hashing and verification
 * Uses BCrypt algorithm for one-way encryption
 */
public class PasswordUtil {

    // BCrypt work factor (cost factor) - higher = more secure but slower
    // 10-12 is recommended for production
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hash a plain text password using BCrypt
     *
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a plain text password against a hashed password
     *
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to check against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }

    /**
     * Check if a password needs rehashing (e.g., if work factor changed)
     *
     * @param hashedPassword The hashed password to check
     * @return true if password should be rehashed, false otherwise
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }

        try {
            // BCrypt hashes start with $2a$, $2b$, or $2y$ followed by cost factor
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int currentRounds = Integer.parseInt(parts[2]);
                return currentRounds < BCRYPT_ROUNDS;
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

    /**
     * Validate password strength
     *
     * @param password The password to validate
     * @return true if password meets minimum requirements, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        // Require at least 3 out of 4 categories
        int categories = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) +
                (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        return categories >= 3;
    }

    /**
     * Get password strength message
     *
     * @param password The password to check
     * @return A message describing password strength
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        int categories = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) +
                (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        if (categories < 3) {
            StringBuilder msg = new StringBuilder("Password needs ");
            if (!hasUpper) msg.append("uppercase letters, ");
            if (!hasLower) msg.append("lowercase letters, ");
            if (!hasDigit) msg.append("digits, ");
            if (!hasSpecial) msg.append("special characters, ");
            return msg.substring(0, msg.length() - 2);
        }

        return "Password is strong";
    }

    /**
     * Generate a random password
     *
     * @param length The length of the password
     * @return A randomly generated password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upperCase + lowerCase + digits + special;

        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        // Ensure at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    // Test method (for development only - remove in production)
    public static void main(String[] args) {
        // Test password hashing
        String plainPassword = "password123";
        String hashed = hashPassword(plainPassword);

        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Hashed Password: " + hashed);
        System.out.println("Verification: " + verifyPassword(plainPassword, hashed));
        System.out.println("Wrong Password: " + verifyPassword("wrongpass", hashed));

        // Test password strength
        System.out.println("\nPassword Strength Tests:");
        String[] testPasswords = {
                "weak",
                "12345678",
                "Password",
                "Pass123",
                "Password123",
                "P@ssw0rd123"
        };

        for (String pwd : testPasswords) {
            System.out.println(pwd + " -> " + getPasswordStrengthMessage(pwd));
        }

        // Generate random password
        System.out.println("\nGenerated Password: " + generateRandomPassword(12));
    }
}