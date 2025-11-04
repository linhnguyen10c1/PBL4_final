// client/ui/admin/panels/UserManagementPanel.java
package client.ui.admin.panels;

import client.ui.admin.interfaces.AdminDashboardCallbacks;
import client.ui.admin.components.UserManagementToolbar;
import client.ui.admin.components.UsersTable;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel implements UsersTable.UserSelectionListener {
    
    private UserManagementToolbar toolbar;
    private UsersTable usersTable;
    private AdminDashboardCallbacks callbacks;
    
    public UserManagementPanel(AdminDashboardCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Create components
        toolbar = new UserManagementToolbar(new ToolbarCallbacksImpl());
        usersTable = new UsersTable();
        usersTable.setSelectionListener(this);
        
        // Layout
        add(toolbar, BorderLayout.NORTH);
        add(usersTable, BorderLayout.CENTER);
    }
    
    // UsersTable.UserSelectionListener implementation
    @Override
    public void onUserSelected(User user) {
        toolbar.setUserSelected(true);
        if (callbacks != null) {
            callbacks.updateStatus("Selected user: " + user.getUsername());
        }
    }
    
    @Override
    public void onUserDeselected() {
        toolbar.setUserSelected(false);
        if (callbacks != null) {
            callbacks.updateStatus("No user selected");
        }
    }
    
    @Override
    public void onUserDoubleClicked(User user) {
        if (callbacks != null) {
            callbacks.onEditUserRequested(user);
        }
    }
    
    // Public methods for external control
    public void setUsers(List<User> users) {
        usersTable.setUsers(users);
    }
    
    public User getSelectedUser() {
        return usersTable.getSelectedUser();
    }
    
    public void refreshUsers() {
        usersTable.refreshTable();
    }
    
    public void clearSelection() {
        usersTable.clearSelection();
    }
    
    // Inner class to handle toolbar callbacks
    private class ToolbarCallbacksImpl implements AdminDashboardCallbacks {
        @Override
        public void onLogoutRequested() {
            if (callbacks != null) callbacks.onLogoutRequested();
        }
        
        @Override
        public void onTabChanged(int tabIndex) {
            if (callbacks != null) callbacks.onTabChanged(tabIndex);
        }
        
        @Override
        public void onAddUserRequested() {
            if (callbacks != null) callbacks.onAddUserRequested();
        }
        
        @Override
        public void onEditUserRequested(User user) {
            User selectedUser = getSelectedUser();
            if (selectedUser != null && callbacks != null) {
                callbacks.onEditUserRequested(selectedUser);
            }
        }
        
        @Override
        public void onDeleteUserRequested(User user) {
            User selectedUser = getSelectedUser();
            if (selectedUser != null && callbacks != null) {
                callbacks.onDeleteUserRequested(selectedUser);
            }
        }
        
        @Override
        public void onRefreshUsersRequested() {
            if (callbacks != null) callbacks.onRefreshUsersRequested();
        }
        
        @Override
        public void onSearchUsersRequested(String searchTerm) {
            if (callbacks != null) callbacks.onSearchUsersRequested(searchTerm);
        }
        
        @Override
        public void onAddQuestionRequested() {}
        
        @Override
        public void onEditQuestionRequested(int questionId) {}
        
        @Override
        public void onDeleteQuestionRequested(int questionId) {}
        
        @Override
        public void onCreateRoomRequested() {}
        
        @Override
        public void onEditRoomRequested(int roomId) {}
        
        @Override
        public void onDeleteRoomRequested(int roomId) {}
        
        @Override
        public void updateStatus(String message) {
            if (callbacks != null) callbacks.updateStatus(message);
        }
    }
}