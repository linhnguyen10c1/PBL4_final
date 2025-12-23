package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.User;
import utils.JsonUtil;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseController {
    
    protected NetworkManager networkManager;
    protected User currentUser;
    protected String sessionToken;
    
    public BaseController(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }
    
    protected ResponseData sendRequest(String action, String data) {
        try {
            boolean isStudentImportantRequest = isStudentImportantRequest(action);
            
            if (isStudentImportantRequest && currentUser != null && currentUser.isStudent()) {
                if (networkManager instanceof NetworkManager) {
                    try {
                        networkManager.getClass().getMethod("pauseHeartbeat").invoke(networkManager);
                    } catch (Exception e) {
                    }
                }
                
                return sendRequestWithRetry(action, data, 3);
            } else {
                String response = networkManager.sendRequest(action, data);
                return NetworkManager.parseResponse(response);
            }
            
        } catch (IOException e) {
            handleNetworkError(e);
            return new ResponseData(false, "Network error: " + e.getMessage(), null);
        } finally {
            // ✅ FIX: Resume heartbeat sau request
            if (isStudentImportantRequest(action) && currentUser != null && currentUser.isStudent()) {
                if (networkManager instanceof NetworkManager) {
                    try {
                        networkManager.getClass().getMethod("resumeHeartbeat").invoke(networkManager);
                    } catch (Exception e) {
                        // Không có method resumeHeartbeat, bỏ qua
                    }
                }
            }
        }
    }
    
    /**
     * ✅ FIX: Send request with retry cho student
     */
    private ResponseData sendRequestWithRetry(String action, String data, int maxRetries) throws IOException {
        IOException lastException = null;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                logAction("sendRequest", "Attempt " + (i + 1) + " for action: " + action + 
                         (currentUser != null ? " (User: " + currentUser.getRole() + ")" : ""));
                
                String response = networkManager.sendRequest(action, data);
                ResponseData responseData = NetworkManager.parseResponse(response);
                
                if (responseData.isSuccess() || i == maxRetries - 1) {
                    return responseData;
                }
                
            } catch (IOException e) {
                lastException = e;
                if (i < maxRetries - 1) {
                    logAction("sendRequest", "Retry " + (i + 1) + " failed: " + e.getMessage());
                    try {
                        Thread.sleep(1000 * (i + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw lastException != null ? lastException : new IOException("Request failed after retries");
    }
    
    /**
     * ✅ FIX: Xác định student requests quan trọng
     */
    private boolean isStudentImportantRequest(String action) {
        return action != null && (
            action.contains("ROOM") || 
            action.contains("EXAM") || 
            action.contains("JOIN") ||
            action.contains("TAKE") ||
            action.contains("SUBMIT")
        );
    }
    
    /**
     * Send request without data
     */
    protected ResponseData sendRequest(String action) {
        return sendRequest(action, "");
    }
    
    /**
     * Send request and parse JSON response
     */
    protected <T> T sendRequestForObject(String action, String data, Class<T> responseType) {
        ResponseData response = sendRequest(action, data);
        if (response.isSuccess() && response.getData() != null) {
            return JsonUtil.fromJson(response.getData(), responseType);
        }
        return null;
    }
    
    /**
     * Send request with JSON data
     */
    protected ResponseData sendJsonRequest(String action, Object data) {
        String jsonData = JsonUtil.toJson(data);
        return sendRequest(action, jsonData);
    }

    /**
     * Handle network errors
     * ✅ FIX: Cải thiện error handling cho student
     */
    protected void handleNetworkError(Exception e) {
        System.err.println("Network error: " + e.getMessage());
        
        if (e instanceof IOException) {
            String message;
            String title = "Connection Error";
            
            // ✅ FIX: Khác biệt message cho student vs admin
            if (currentUser != null && currentUser.isStudent()) {
                if (e.getMessage().contains("timeout")) {
                    title = "Request Timeout";
                    message = "Loading exam data is taking longer than expected.\n" +
                             "This may happen during peak hours.\n" +
                             "Please try again in a moment.";
                } else {
                    message = "Lost connection to exam server.\n" +
                             "Please check your network connection and login again to continue your exam.";
                }
            } else {
                message = "Lost connection to server. Please check your network connection and try again.";
            }
            
            showErrorMessage(title, message);
        }
    }
    
    /**
     * Handle server errors
     */
    protected void handleServerError(String message) {
        System.err.println("Server error: " + message);
        
        // ✅ FIX: Xử lý authentication error đặc biệt
        if (message != null && (message.toLowerCase().contains("authentication") || 
                               message.toLowerCase().contains("session") ||
                               message.toLowerCase().contains("login"))) {
            handleAuthenticationError();
        } else {
            showErrorMessage("Server Error", message);
        }
    }
    
    /**
     * Handle authentication error riêng cho student
     */
    private void handleAuthenticationError() {
        if (currentUser != null && currentUser.isStudent()) {
            showErrorMessage("Session Expired", 
                "Your exam session has expired.\n" +
                "Please login again to continue.");
        } else {
            showErrorMessage("Authentication Error", 
                "Please login first to access this feature.");
        }
        clearSession();
    }
    
    /**
     * Show error message to user
     */
    protected void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show success message to user
     */
    protected void showSuccessMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show warning message to user
     */
    protected void showWarningMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show confirmation dialog
     */
    protected boolean showConfirmDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    /**
     * Set current user and session
     */
    public void setCurrentUser(User user, String sessionToken) {
        this.currentUser = user;
        this.sessionToken = sessionToken;
        
        // ✅ FIX: Log để debug
        logAction("setCurrentUser", "User: " + (user != null ? user.getUsername() : "null") + 
                 " (" + (user != null ? user.getRole() : "null") + ")");
    }
    
    /**
     * Get current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get session token
     */
    public String getSessionToken() {
        return sessionToken;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        boolean loggedIn = currentUser != null && 
                          sessionToken != null && 
                          !sessionToken.trim().isEmpty();
        
        if (!loggedIn) {
            logAction("isLoggedIn", "Not logged in - User: " + (currentUser != null ? "exists" : "null") + 
                     ", Token: " + (sessionToken != null && !sessionToken.trim().isEmpty() ? "valid" : "invalid"));
        }
        
        return loggedIn;
    }
    
    /**
     * Clear session
     */
    public void clearSession() {
        logAction("clearSession", "Clearing session for user: " + 
                 (currentUser != null ? currentUser.getUsername() : "null"));
        this.currentUser = null;
        this.sessionToken = null;
    }
    
    /**
     * Validate network connection
     */
    protected boolean validateConnection() {
        if (!networkManager.isConnected()) {
            showErrorMessage("Connection Error", "Not connected to server. Please connect first.");
            return false;
        }
        return true;
    }
    
    /**
     * Validate session
     */
    protected boolean validateSession() {
        if (!isLoggedIn()) {
            if (currentUser != null && currentUser.isStudent()) {
                showErrorMessage("Authentication Error", 
                    "Your session is invalid. Please login again to continue your exam.");
            } else {
                showErrorMessage("Authentication Error", "Please login first.");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Log action for debugging
     */
    protected void logAction(String action, String details) {
        String timestamp = java.time.LocalDateTime.now().toString();
        String userInfo = currentUser != null ? 
            " [" + currentUser.getRole() + ":" + currentUser.getUsername() + "]" : " [NO_USER]";
        
        System.out.println("[" + timestamp + "][" + getClass().getSimpleName() + "]" + userInfo + 
            " " + action + (details != null ? ": " + details : ""));
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Check if current user is student
     */
    public boolean isStudent() {
        return currentUser != null && currentUser.isStudent();
    }
    
    /**
     * Get user role string
     */
    public String getUserRole() {
        return currentUser != null ? currentUser.getRole() : "UNKNOWN";
    }
    
    /**
     * Validate admin permissions (để các controller hiện tại dùng)
     */
    public boolean hasAdminPermissions() {
        if (!isLoggedIn()) {
            showErrorMessage("Authentication Error", "Please login first");
            return false;
        }
        
        if (!currentUser.isAdmin()) {
            showErrorMessage("Access Denied", 
                "You need administrator privileges to perform this action");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate student permissions
     */
    public boolean hasStudentPermissions() {
        if (!isLoggedIn()) {
            showErrorMessage("Authentication Error", "Please login first");
            return false;
        }
        
        if (!currentUser.isStudent()) {
            showErrorMessage("Access Denied", 
                "This feature is only available for students");
            return false;
        }
        
        return true;
    }
}