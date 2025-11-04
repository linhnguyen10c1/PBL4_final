// client/ui/admin/interfaces/AdminDashboardCallbacks.java
package client.ui.admin.interfaces;

import model.User;

public interface AdminDashboardCallbacks {
    // Navigation callbacks
    void onLogoutRequested();
    void onTabChanged(int tabIndex);
    
    // User management callbacks
    void onAddUserRequested();
    void onEditUserRequested(User user);
    void onDeleteUserRequested(User user);
    void onRefreshUsersRequested();
    void onSearchUsersRequested(String searchTerm);
    
    // Question management callbacks (for future)
    void onAddQuestionRequested();
    void onEditQuestionRequested(int questionId);
    void onDeleteQuestionRequested(int questionId);
    
    // Room management callbacks (for future)
    void onCreateRoomRequested();
    void onEditRoomRequested(int roomId);
    void onDeleteRoomRequested(int roomId);
    
    // Status updates
    void updateStatus(String message);
}