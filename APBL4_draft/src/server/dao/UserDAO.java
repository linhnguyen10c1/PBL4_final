
package server.dao;
import model.User;
import server.dao.BaseDAO;
import utils.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class UserDAO extends BaseDAO {
    
    /**
     * Create new user - SỬ DỤNG BaseDAO
     */
    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        
        // ✅ Sử dụng BaseDAO method
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        
        int userId = executeInsertWithGeneratedKey(sql,
            user.getUsername(),
            hashedPassword,
            user.getFullName(),
            user.getRole(),
            user.isActive()
        );
        
        user.setUserId(userId);
        return getUserById(userId);
    }
    
    /**
     * Find user by username - SỬ DỤNG BaseDAO
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = true";
        
        try (ResultSet rs = executeQuery(sql, username)) {
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Error in findByUsername: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by ID - SỬ DỤNG BaseDAO
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (ResultSet rs = executeQuery(sql, userId)) {
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }
    public User findById(int userId) {
        try {
            return getUserById(userId);
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all users - SỬ DỤNG BaseDAO
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        
        try (ResultSet rs = executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Search users - SỬ DỤNG BaseDAO
     */
    public List<User> searchUsers(String keyword) {
        String sql = "SELECT * FROM users WHERE " +
                    "(username LIKE ? OR full_name LIKE ?) ORDER BY full_name";
        List<User> users = new ArrayList<>();
        String searchPattern = "%" + keyword + "%";
        
        try (ResultSet rs = executeQuery(sql, searchPattern, searchPattern)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Update user - SỬ DỤNG BaseDAO
     */
    public User updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, role = ?, is_active = ?, updated_at = NOW() " +
                    "WHERE user_id = ?";
        
        // ✅ Sử dụng BaseDAO method
        int rowsAffected = executeUpdate(sql,
            user.getFullName(),
            user.getRole(),
            user.isActive(),
            user.getUserId()
        );
        
        if (rowsAffected == 0) {
            throw new SQLException("No user found with ID: " + user.getUserId());
        }
        
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            updatePassword(user.getUserId(), user.getPassword());
        }
        return getUserById(user.getUserId());
    }
    
    /**
     * Delete user (soft delete) - SỬ DỤNG BaseDAO
     */
    public boolean deleteUser(int userId) {
        String sql = "UPDATE users SET is_active = false, updated_at = NOW() WHERE user_id = ?";
        
        try {
            // ✅ Sử dụng BaseDAO method
            int rowsAffected = executeUpdate(sql, userId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if username exists - SỬ DỤNG BaseDAO
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (ResultSet rs = executeQuery(sql, username)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username exists: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update user password - SỬ DỤNG BaseDAO
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE user_id = ?";
        
        try {
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            // ✅ Sử dụng BaseDAO method
            int rowsAffected = executeUpdate(sql, hashedPassword, userId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get students only - SỬ DỤNG BaseDAO
     */
    public List<User> getStudents() {
        String sql = "SELECT * FROM users WHERE role = 'STUDENT' AND is_active = true ORDER BY full_name";
        List<User> students = new ArrayList<>();
        
        try (ResultSet rs = executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting students: " + e.getMessage());
        }
        
        return students;
    }
    
    /**
     * Map ResultSet to User object
     */
 // server/dao/UserDAO.java - Đơn giản nhất
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        
        // ✅ Convert Timestamp to String using toString()
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        
        if (createdTimestamp != null) {
            // Format: "2025-10-06 08:49:10.0" -> cắt bỏ .0
            String createdStr = createdTimestamp.toString();
            if (createdStr.endsWith(".0")) {
                createdStr = createdStr.substring(0, createdStr.length() - 2);
            }
            user.setCreatedAt(createdStr);
        }
        
        if (updatedTimestamp != null) {
            String updatedStr = updatedTimestamp.toString();
            if (updatedStr.endsWith(".0")) {
                updatedStr = updatedStr.substring(0, updatedStr.length() - 2);
            }
            user.setUpdatedAt(updatedStr);
        }
        
        return user;
    }
    /**
     * Create default admin - SỬ DỤNG SETTER METHODS
     */
    public boolean createDefaultAdmin() {
        try {
            if (findByUsername("admin") != null) {
                System.out.println("ℹ️ Default admin already exists");
                return true;
            }
            
            System.out.println("🔧 Creating default admin user...");
            
            // ✅ Sử dụng setter methods thay vì constructor
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setFullName("System Administrator");
            admin.setRole("ADMIN");
            admin.setActive(true);
            
            User createdAdmin = createUser(admin);
            
            if (createdAdmin != null) {
                System.out.println("✅ Default admin created successfully with ID: " + createdAdmin.getUserId());
                return true;
            } else {
                System.err.println("❌ Failed to create default admin");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error creating default admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}