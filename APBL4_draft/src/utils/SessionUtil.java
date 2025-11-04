package utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Session Utility for token generation and management
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:30:04 UTC
 */
public class SessionUtil {
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate secure session token
     */
    public static String generateSessionToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Generate exam session token
     */
    public static String generateExamSessionToken() {
        return "EXAM_" + System.currentTimeMillis() + "_" + generateSessionToken();
    }
    
    /**
     * Validate token format
     */
    public static boolean isValidToken(String token) {
        return token != null && token.length() >= 32 && !token.contains(" ");
    }
}