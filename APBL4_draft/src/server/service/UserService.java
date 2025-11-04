// server/service/UserService.java
package server.service;

import model.User;
import server.dao.UserDAO;
import utils.ValidationUtil;
import utils.PasswordUtil;
import java.util.List;

/**
 * User Service - Handles user management operations
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:35:08 UTC
 */
public class UserService {
    
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * ✅ SỬA: Create new user with validation
     */
    public ServiceResult<User> createUser(User user) {
        try {
            // Validate input
            ValidationResult validation = validateUserForCreation(user);
            if (!validation.isValid()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if username already exists
            if (userDAO.usernameExists(user.getUsername())) {
                return ServiceResult.error("Username already exists");
            }
            
            // Generate password if not provided
            if (ValidationUtil.isEmpty(user.getPassword())) {
                String generatedPassword = PasswordUtil.generateRandomPassword(8);
                user.setPassword(generatedPassword);
                System.out.println("📝 Generated password for " + user.getUsername() + ": " + generatedPassword);
            }
            
            // ✅ SỬA: userDAO.createUser() trả về User object
            User createdUser = userDAO.createUser(user);
            if (createdUser != null) {
                System.out.println("✅ User created: " + user.getUsername() + " (Role: " + user.getRole() + ")");
                // Remove password from response for security
                createdUser.setPassword("");
                return ServiceResult.success("User created successfully", createdUser);
            } else {
                return ServiceResult.error("Failed to create user");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * ✅ SỬA: Update user with validation
     */
    public ServiceResult<User> updateUser(User user) {
        try {
            // Validate input
            ValidationResult validation = validateUserForUpdate(user);
            if (!validation.isValid()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if user exists
            User existingUser = userDAO.findById(user.getUserId());
            if (existingUser == null) {
                return ServiceResult.error("User not found");
            }
            
            // ✅ SỬA: userDAO.updateUser() trả về User object
            User updatedUser = userDAO.updateUser(user);
            if (updatedUser != null) {
                System.out.println("✅ User updated: " + user.getUsername());
                // Remove password from response for security
                updatedUser.setPassword("");
                return ServiceResult.success("User updated successfully", updatedUser);
            } else {
                return ServiceResult.error("Failed to update user");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Delete user (soft delete)
     */
    public ServiceResult<Boolean> deleteUser(int userId) {
        try {
            // Check if user exists
            User existingUser = userDAO.findById(userId);
            if (existingUser == null) {
                return ServiceResult.error("User not found");
            }
            
            // Prevent deleting admin users if it's the last admin
            if (existingUser.isAdmin()) {
                List<User> allUsers = userDAO.getAllUsers();
                long adminCount = allUsers.stream()
                    .filter(u -> u.isAdmin() && u.isActive())  // ✅ THÊM: check active
                    .count();
                if (adminCount <= 1) {
                    return ServiceResult.error("Cannot delete the last active admin user");
                }
            }
            
            // Delete user
            boolean success = userDAO.deleteUser(userId);
            if (success) {
                System.out.println("✅ User deleted: " + existingUser.getUsername());
                return ServiceResult.success("User deleted successfully", true);
            } else {
                return ServiceResult.error("Failed to delete user");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Get all users
     */
    public ServiceResult<List<User>> getAllUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            // Remove password from response for security
            users.forEach(user -> user.setPassword(""));
            
            System.out.println("📊 Retrieved " + users.size() + " users from database");
            return ServiceResult.success("Users retrieved successfully", users);
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Search users by keyword
     */
    public ServiceResult<List<User>> searchUsers(String keyword) {
        try {
            List<User> users;
            
            if (ValidationUtil.isEmpty(keyword)) {
                // If empty keyword, return all users
                return getAllUsers();
            } else {
                users = userDAO.searchUsers(keyword.trim());
                System.out.println("🔍 Search for '" + keyword + "' found " + users.size() + " users");
            }
            
            // Remove password from response for security
            users.forEach(user -> user.setPassword(""));
            return ServiceResult.success("Search completed", users);
            
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Get user by ID
     */
    public ServiceResult<User> getUserById(int userId) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return ServiceResult.error("User not found");
            }
            
            // Remove password from response for security
            user.setPassword("");
            return ServiceResult.success("User found", user);
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Get students only
     */
    public ServiceResult<List<User>> getStudents() {
        try {
            List<User> students = userDAO.getStudents();
            // Remove password from response for security
            students.forEach(user -> user.setPassword(""));
            return ServiceResult.success("Students retrieved successfully", students);
        } catch (Exception e) {
            System.err.println("Error getting students: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Change user password
     */
    public ServiceResult<Boolean> changePassword(int userId, String currentPassword, String newPassword) {
        try {
            // Get user with password for verification
            User user = userDAO.findById(userId);
            if (user == null) {
                return ServiceResult.error("User not found");
            }
            
            // Verify current password
            if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
                return ServiceResult.error("Current password is incorrect");
            }
            
            // Validate new password
            if (!ValidationUtil.isValidPassword(newPassword)) {
                return ServiceResult.error("New password must be at least 6 characters");
            }
            
            // Update password
            boolean success = userDAO.updatePassword(userId, newPassword);
            if (success) {
                System.out.println("✅ Password changed for user: " + user.getUsername());
                return ServiceResult.success("Password changed successfully", true);
            } else {
                return ServiceResult.error("Failed to change password");
            }
            
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Reset user password (admin only)
     */
    public ServiceResult<String> resetPassword(int userId) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return ServiceResult.error("User not found");
            }
            
            // Generate new password
            String newPassword = PasswordUtil.generateRandomPassword(8);
            
            // Update password
            boolean success = userDAO.updatePassword(userId, newPassword);
            if (success) {
                System.out.println("✅ Password reset for user: " + user.getUsername());
                return ServiceResult.success("Password reset successfully", newPassword);
            } else {
                return ServiceResult.error("Failed to reset password");
            }
            
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Validate user for creation
     */
    private ValidationResult validateUserForCreation(User user) {
        if (user == null) {
            return new ValidationResult(false, "User data is required");
        }
        
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            return new ValidationResult(false, "Username must be 3-20 characters, alphanumeric and underscore only");
        }
        
        if (!ValidationUtil.isNotEmpty(user.getFullName())) {
            return new ValidationResult(false, "Full name is required");
        }
        
        if (!ValidationUtil.isValidRole(user.getRole())) {
            return new ValidationResult(false, "Role must be ADMIN or STUDENT");
        }
        
        if (ValidationUtil.isNotEmpty(user.getPassword()) && !ValidationUtil.isValidPassword(user.getPassword())) {
            return new ValidationResult(false, "Password must be at least 6 characters");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate user for update
     */
    private ValidationResult validateUserForUpdate(User user) {
        if (user == null) {
            return new ValidationResult(false, "User data is required");
        }
        
        if (user.getUserId() <= 0) {
            return new ValidationResult(false, "User ID is required");
        }
        
        if (!ValidationUtil.isNotEmpty(user.getFullName())) {
            return new ValidationResult(false, "Full name is required");
        }
        
        if (!ValidationUtil.isValidRole(user.getRole())) {
            return new ValidationResult(false, "Role must be ADMIN or STUDENT");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Initialize default data
     */
    public boolean initializeDefaultData() {
        try {
            boolean result = userDAO.createDefaultAdmin();
            if (result) {
                System.out.println("✅ Default admin initialization completed");
            } else {
                System.err.println("❌ Failed to initialize default admin");
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error initializing default user data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validation Result inner class
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}