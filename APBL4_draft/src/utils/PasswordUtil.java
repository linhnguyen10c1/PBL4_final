package utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password Utility for hashing and verification
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:30:04 UTC
 */
public class PasswordUtil {
    private static final int COST = 12; // BCrypt cost factor
    
    /**
     * Hash password using BCrypt
     */
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }
    
    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String password, String hash) {
        try {
            return BCrypt.verifyer()
                         .verify(password.toCharArray(), hash)
                         .verified;
        } catch (RuntimeException e) {
            System.err.println("BCrypt verify runtime error: " + e.getMessage());
            return false;
        }
    }
    /**
     * Generate random password for new users
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}