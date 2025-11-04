package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.User;
import utils.JsonUtil;
import utils.Protocol;
import utils.ValidationUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Controller - Handles authentication operations
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:39:25 UTC
 */
public class LoginController extends BaseController {
    
    private LoginListener loginListener;
    
    public LoginController(NetworkManager networkManager) {
        super(networkManager);
    }
    
    /**
     * Login with username and password
     */
 // client/controller/LoginController.java
    public void login(String username, String password) {
        try {
            logAction("login", "Attempting login for user: " + username);
            
            // Validate input
            if (!validateLoginInput(username, password)) {
                return;
            }
            
            // Check network connection
            if (!validateConnection()) {
                return;
            }
            
            // Prepare login data
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);
            
            // Send login request
            ResponseData response = sendJsonRequest(Protocol.LOGIN, loginData);
            
            if (response.isSuccess()) {
                // ✅ FIX: Parse data correctly
                String data = response.getData();  // "sessionToken|userJson"
                System.out.println("🔍 Login success data: " + data);
                
                if (data != null && data.contains("|")) {
                    String[] loginParts = data.split("\\|", 2);
                    if (loginParts.length >= 2) {
                        String sessionToken = loginParts[0];
                        String userJson = loginParts[1];
                        
                        System.out.println("🔐 Session token: " + sessionToken);
                        System.out.println("👤 User JSON: " + userJson.substring(0, Math.min(100, userJson.length())));
                        
                        User user = JsonUtil.fromJson(userJson, User.class);
                        if (user != null) {
                            // Set session
                            setCurrentUser(user, sessionToken);
                            
                            logAction("login", "Login successful for: " + username + " (Role: " + user.getRole() + ")");
                            
                            // Notify listener
                            if (loginListener != null) {
                                loginListener.onLoginSuccess(user);
                            }
                            return;
                        } else {
                            handleServerError("Failed to parse user data");
                        }
                    } else {
                        handleServerError("Invalid login response format");
                    }
                } else {
                    handleServerError("Invalid login response");
                }
            } else {
                // Login failed
                String errorMessage = response.getMessage();
                logAction("login", "Login failed: " + errorMessage);
                
                if (loginListener != null) {
                    loginListener.onLoginFailed(errorMessage);
                }
            }
            
        } catch (Exception e) {
            logAction("login", "Login error: " + e.getMessage());
            e.printStackTrace();
            handleNetworkError(e);
            
            if (loginListener != null) {
                loginListener.onLoginFailed("Login failed: " + e.getMessage());
            }
        }
    }
    /**
     * Logout current user
     */
    public void logout() {
        try {
            logAction("logout", "Logging out user: " + (currentUser != null ? currentUser.getUsername() : "unknown"));
            
            if (!validateConnection()) {
                return;
            }
            
            // Send logout request
            ResponseData response = sendRequest(Protocol.LOGOUT);
            
            // Clear session regardless of response
            clearSession();
            
            if (response.isSuccess()) {
                logAction("logout", "Logout successful");
            } else {
                logAction("logout", "Logout response: " + response.getMessage());
            }
            
            // Notify listener
            if (loginListener != null) {
                loginListener.onLogoutSuccess();
            }
            
        } catch (Exception e) {
            logAction("logout", "Logout error: " + e.getMessage());
            
            // Clear session even on error
            clearSession();
            
            if (loginListener != null) {
                loginListener.onLogoutSuccess();
            }
        }
    }
    
    /**
     * Validate login input
     */
    private boolean validateLoginInput(String username, String password) {
        if (!ValidationUtil.isNotEmpty(username)) {
            showErrorMessage("Validation Error", "Username is required");
            return false;
        }
        
        if (!ValidationUtil.isNotEmpty(password)) {
            showErrorMessage("Validation Error", "Password is required");
            return false;
        }
        
        if (!ValidationUtil.isValidUsername(username)) {
            showErrorMessage("Validation Error", "Invalid username format");
            return false;
        }
        
        return true;
    }
    
    /**
     * Auto-login for testing purposes
     */
    public void autoLogin(String username, String password) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Small delay
                login(username, password);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Check if admin user
     */
    public boolean isAdminUser() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Check if student user
     */
    public boolean isStudentUser() {
        return currentUser != null && currentUser.isStudent();
    }
    
    /**
     * Get user role display name
     */
    public String getUserRoleDisplayName() {
        if (currentUser == null) return "Not logged in";
        
        switch (currentUser.getRole()) {
            case "ADMIN":
                return "Administrator";
            case "STUDENT":
                return "Student";
            default:
                return currentUser.getRole();
        }
    }
    
    /**
     * Set login listener
     */
    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }
    
    /**
     * Login Listener interface
     */
    public interface LoginListener {
        void onLoginSuccess(User user);
        void onLoginFailed(String message);
        void onLogoutSuccess();
    }
}