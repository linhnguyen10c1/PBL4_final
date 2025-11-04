// model/User.java - Simplified version for testing
package model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean isActive;  // ✅ Đổi tên field này
    private String createdAt;
    private String updatedAt;
    
    // Default constructor
    public User() {}
    
    // ✅ Getters/Setters - ĐẢM BẢO ĐẦY ĐỦ
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActive() { return isActive; }  // ✅ isActive() method
    public void setActive(boolean active) { this.isActive = active; }  // ✅ setActive()
    public void setIsActive(boolean active) { this.isActive = active; }  // ✅ THÊM cái này
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isStudent() { return "STUDENT".equals(role); }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}