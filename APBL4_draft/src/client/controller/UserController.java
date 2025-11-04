package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.User;
import utils.JsonUtil;
import utils.Protocol;
import utils.ValidationUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

/**
 * User Controller - Handles user management operations (Admin only)
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:39:25 UTC
 */
public class UserController extends BaseController {
    
    public UserController(NetworkManager networkManager) {
        super(networkManager);
    }
    
    /**
     * Get all users
     */
 // client/controller/UserController.java
 // client/controller/UserController.java
 // client/controller/UserController.java
    public List<User> getAllUsers() {
        try {
            logAction("getAllUsers", "Fetching all users from server");
            
            if (!validateConnection() || !validateSession()) {
                System.err.println("❌ Connection or session validation failed");
                return null;
            }
            
            // ✅ Debug request
            System.out.println("📤 Sending request: " + Protocol.GET_USERS);
            ResponseData response = sendRequest(Protocol.GET_USERS);
            
            // ✅ Debug response
            System.out.println("📥 Response success: " + response.isSuccess());
            System.out.println("📥 Response message: " + response.getMessage());
            System.out.println("📥 Response data: " + (response.getData() != null ? "Present (" + response.getData().length() + " chars)" : "null"));
            
            if (response.isSuccess() && response.getData() != null) {
                System.out.println("✅ Server response successful");
                
                String jsonData = response.getData();
                System.out.println("📋 JSON data to parse: " + jsonData.substring(0, Math.min(200, jsonData.length())));
                
                try {
                    List<User> users = JsonUtil.fromJsontoList(jsonData, User.class);
                    
                    if (users != null) {
                        logAction("getAllUsers", "Successfully retrieved " + users.size() + " users");
                        return users;
                    } else {
                        System.err.println("❌ JsonUtil returned null");
                        return new ArrayList<>();
                    }
                } catch (Exception e) {
                    System.err.println("❌ JSON parsing error: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            } else {
                // ✅ This is where the error happens
                String errorMsg = "Failed to retrieve users: " + response.getMessage();
                System.err.println("❌ Server error: " + errorMsg);
                handleServerError(errorMsg);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            handleNetworkError(e);
            return null;
        }
    }
    /**
     * Search users by keyword
     */
    public List<User> searchUsers(String keyword) {
        try {
            logAction("searchUsers", "Searching users with keyword: " + keyword);
            System.out.println("📅 Current Time: 2025-09-14 16:34:32 UTC");
            System.out.println("👤 User: linhnguyen10c1");
            System.out.println("🔍 Search keyword: '" + (keyword != null ? keyword : "(empty)") + "'");
            
            if (!validateConnection() || !validateSession()) {
                System.err.println("❌ Connection or session validation failed");
                return null;
            }
            
            Map<String, String> searchData = new HashMap<>();
            searchData.put("keyword", keyword != null ? keyword : "");
            
            ResponseData response = sendJsonRequest(Protocol.SEARCH_USERS, searchData);
            
            if (response.isSuccess() && response.getData() != null) {
                System.out.println("✅ Server response successful, parsing search results...");
                
                // Fixed: Use Class parameter instead of Type
                List<User> users = JsonUtil.fromJsontoList(response.getData(), User.class);
                
                if (users != null) {
                    logAction("searchUsers", "Found " + users.size() + " users matching keyword: " + keyword);
                    System.out.println("📊 Search results (" + users.size() + " users found):");
                    
                    for (User user : users) {
                        System.out.println("  - " + user.getUsername() + " (" + user.getFullName() + ") [" + user.getRole() + "]");
                    }
                    
                    return users;
                } else {
                    System.err.println("❌ Failed to parse search results from JSON");
                    return new ArrayList<>();
                }
            } else {
                String errorMsg = "Failed to search users: " + (response != null ? response.getMessage() : "No response");
                handleServerError(errorMsg);
                System.err.println("❌ " + errorMsg);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception in searchUsers: " + e.getMessage());
            e.printStackTrace();
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Create new user
     */
    public boolean createUser(User user) {
        try {
            logAction("createUser", "Creating user: " + user.getUsername());
            
            if (!validateConnection() || !validateSession()) {
                return false;
            }
            
            // Validate user data
            if (!validateUserData(user)) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.CREATE_USER, user);
            
            if (response.isSuccess()) {
                showSuccessMessage("Success", "User created successfully");
                logAction("createUser", "User created: " + user.getUsername());
                return true;
            } else {
                showErrorMessage("Error", "Failed to create user: " + response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Update existing user
     */
    public boolean updateUser(User user) {
        try {
            logAction("updateUser", "Updating user: " + user.getUsername());
            
            if (!validateConnection() || !validateSession()) {
                return false;
            }
            
            // Validate user data
            if (!validateUserDataForUpdate(user)) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.UPDATE_USER, user);
            
            if (response.isSuccess()) {
                showSuccessMessage("Success", "User updated successfully");
                logAction("updateUser", "User updated: " + user.getUsername());
                return true;
            } else {
                showErrorMessage("Error", "Failed to update user: " + response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(int userId, String username) {
        try {
            logAction("deleteUser", "Deleting user: " + username + " (ID: " + userId + ")");
            
            if (!validateConnection() || !validateSession()) {
                return false;
            }
            
            // Confirm deletion
            boolean confirmed = showConfirmDialog("Confirm Deletion", 
                "Are you sure you want to delete user '" + username + "'?\n" +
                "This action cannot be undone.");
            
            if (!confirmed) {
                return false;
            }
            
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("userId", userId);
            
            ResponseData response = sendJsonRequest(Protocol.DELETE_USER, deleteData);
            
            if (response.isSuccess()) {
                showSuccessMessage("Success", "User deleted successfully");
                logAction("deleteUser", "User deleted: " + username);
                return true;
            } else {
                showErrorMessage("Error", "Failed to delete user: " + response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Get students only
     */
    public List<User> getStudents() {
        List<User> allUsers = getAllUsers();
        if (allUsers != null) {
            return allUsers.stream()
                .filter(User::isStudent)
                .collect(java.util.stream.Collectors.toList());
        }
        return null;
    }
    
    /**
     * Validate user data for creation
     */
    private boolean validateUserData(User user) {
        if (user == null) {
            showErrorMessage("Validation Error", "User data is required");
            return false;
        }
        
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            showErrorMessage("Validation Error", 
                "Username must be 3-20 characters, alphanumeric and underscore only");
            return false;
        }
        
        if (!ValidationUtil.isNotEmpty(user.getFullName())) {
            showErrorMessage("Validation Error", "Full name is required");
            return false;
        }
        
        if (!ValidationUtil.isValidRole(user.getRole())) {
            showErrorMessage("Validation Error", "Role must be ADMIN or STUDENT");
            return false;
        }
        
        // Password is optional for creation (will be auto-generated)
        if (ValidationUtil.isNotEmpty(user.getPassword()) && 
            !ValidationUtil.isValidPassword(user.getPassword())) {
            showErrorMessage("Validation Error", "Password must be at least 6 characters");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate user data for update
     */
    private boolean validateUserDataForUpdate(User user) {
        if (user == null) {
            showErrorMessage("Validation Error", "User data is required");
            return false;
        }
        
        if (user.getUserId() <= 0) {
            showErrorMessage("Validation Error", "Valid user ID is required");
            return false;
        }
        
        if (!ValidationUtil.isNotEmpty(user.getFullName())) {
            showErrorMessage("Validation Error", "Full name is required");
            return false;
        }
        
        if (!ValidationUtil.isValidRole(user.getRole())) {
            showErrorMessage("Validation Error", "Role must be ADMIN or STUDENT");
            return false;
        }
        
        return true;
    }
    
    /**
     * Create sample user for testing
     */
    public User createSampleUser(String username, String fullName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        return user;
    }
    
    /**
     * Validate admin permissions
     */
    public boolean hasAdminPermissions() {
        if (!isLoggedIn()) {
            showErrorMessage("Authentication Error", "Please login first");
            return false;
        }
        
        if (!currentUser.isAdmin()) {
            showErrorMessage("Access Denied", "You need administrator privileges to perform this action");
            return false;
        }
        
        return true;
    }
}