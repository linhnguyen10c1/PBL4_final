package server.service;

import model.User;
import server.dao.UserDAO;
import utils.PasswordUtil;
import utils.SessionUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Timestamp;

/**
 * Authentication Service - Handles login, logout, and session management
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:33:23 UTC
 */
public class AuthService {
    
    private final UserDAO userDAO;
    private final Map<String, SessionInfo> activeSessions;
    private final Map<Integer, String> userSessions; // userId -> sessionToken (prevent multiple logins)
    
    public AuthService() {
        this.userDAO = new UserDAO();
        this.activeSessions = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
        
        // Start session cleanup thread
        startSessionCleanup();
    }
    
    /**
     * Login user with username and password
     */
    public AuthResult login(String username, String password) {
        try {
            // Find user by username
            User user = userDAO.findByUsername(username);
            if (user == null) {
                return new AuthResult(false, "Invalid username or password", null, null);
            }
            
            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                return new AuthResult(false, "Invalid username or password", null, null);
            }
            
            // Check if user is active
            if (!user.isActive()) {
                return new AuthResult(false, "Account is deactivated", null, null);
            }
            
            // Check if user is already logged in (prevent multiple sessions)
            if (userSessions.containsKey(user.getUserId())) {
                String existingToken = userSessions.get(user.getUserId());
                activeSessions.remove(existingToken);
                userSessions.remove(user.getUserId());
            }
            
            // Generate session token
            String sessionToken = SessionUtil.generateSessionToken();
            
            // Create session info
            SessionInfo sessionInfo = new SessionInfo(user, new Timestamp(System.currentTimeMillis()));
            
            // Store session
            activeSessions.put(sessionToken, sessionInfo);
            userSessions.put(user.getUserId(), sessionToken);
            
            System.out.println("✅ User logged in: " + username + " (Role: " + user.getRole() + ")");
            
            return new AuthResult(true, "Login successful", user, sessionToken);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return new AuthResult(false, "Internal server error", null, null);
        }
    }
    
    /**
     * Logout user by session token
     */
    public void logout(String sessionToken) {
        if (sessionToken != null) {
            SessionInfo sessionInfo = activeSessions.remove(sessionToken);
            if (sessionInfo != null) {
                userSessions.remove(sessionInfo.getUser().getUserId());
                System.out.println("✅ User logged out: " + sessionInfo.getUser().getUsername());
            }
        }
    }
    
    /**
     * Validate session token and return user
     */
    public User validateSession(String sessionToken) {
        if (sessionToken == null) {
            return null;
        }
        
        SessionInfo sessionInfo = activeSessions.get(sessionToken);
        if (sessionInfo == null) {
            return null;
        }
        
        // Check session timeout (4 hours)
        long currentTime = System.currentTimeMillis();
        long sessionTime = sessionInfo.getLoginTime().getTime();
        long sessionDuration = currentTime - sessionTime;
        long maxSessionDuration = 4 * 60 * 60 * 1000; // 4 hours in milliseconds
        
        if (sessionDuration > maxSessionDuration) {
            // Session expired
            activeSessions.remove(sessionToken);
            userSessions.remove(sessionInfo.getUser().getUserId());
            return null;
        }
        
        return sessionInfo.getUser();
    }
    
    /**
     * Check if user has permission for specific action
     */
    public boolean hasPermission(String sessionToken, String permission) {
        User user = validateSession(sessionToken);
        if (user == null) {
            return false;
        }
        
        switch (permission) {
            case "MANAGE_USERS":
            case "MANAGE_QUESTIONS":
            case "MANAGE_ROOMS":
            case "VIEW_RESULTS":
                return user.isAdmin();
            case "TAKE_EXAM":
            case "VIEW_OWN_RESULTS":
                return user.isStudent() || user.isAdmin();
            default:
                return false;
        }
    }
    
    /**
     * Get current user by session token
     */
    public User getCurrentUser(String sessionToken) {
        return validateSession(sessionToken);
    }
    
    /**
     * Check if user is currently logged in
     */
    public boolean isUserLoggedIn(int userId) {
        return userSessions.containsKey(userId);
    }
    
    /**
     * Start session cleanup thread
     */
    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5 * 60 * 1000); // Run every 5 minutes
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        
        System.out.println("✅ Session cleanup thread started");
    }
    
    /**
     * Cleanup expired sessions
     */
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long maxSessionDuration = 4 * 60 * 60 * 1000; // 4 hours
        
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo sessionInfo = entry.getValue();
            long sessionDuration = currentTime - sessionInfo.getLoginTime().getTime();
            
            if (sessionDuration > maxSessionDuration) {
                userSessions.remove(sessionInfo.getUser().getUserId());
                System.out.println("🔄 Expired session cleaned up: " + sessionInfo.getUser().getUsername());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Session Info inner class
     */
    private static class SessionInfo {
        private final User user;
        private final Timestamp loginTime;
        
        public SessionInfo(User user, Timestamp loginTime) {
            this.user = user;
            this.loginTime = loginTime;
        }
        
        public User getUser() { return user; }
        public Timestamp getLoginTime() { return loginTime; }
    }
    
    /**
     * Authentication Result class
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String sessionToken;
        
        public AuthResult(boolean success, String message, User user, String sessionToken) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.sessionToken = sessionToken;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public String getSessionToken() { return sessionToken; }
    }
}