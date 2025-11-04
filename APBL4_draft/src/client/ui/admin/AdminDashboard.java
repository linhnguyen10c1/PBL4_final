// client/ui/admin/AdminDashboard.java
package client.ui.admin;
import java.util.List;
import java.util.ArrayList;
import client.controller.LoginController;
import client.controller.QuestionController;
import client.controller.UserController;
import client.network.NetworkManager;
import client.ui.admin.components.PlaceholderPanel;
import client.ui.admin.dialogs.AddUserDialog;
import client.ui.admin.dialogs.EditUserDialog;
import client.ui.admin.interfaces.AdminDashboardCallbacks;
import client.ui.admin.panels.*;
import model.User;

import javax.swing.*;
import java.awt.*;
import client.controller.ExamRoomController;
public class AdminDashboard extends JFrame implements AdminDashboardCallbacks {
    
    private NetworkManager networkManager;
    private LoginController loginController;
    private UserController userController;
    private ExamRoomController examRoomController;
    private QuestionController questionController;
    // UI Panels
    private AdminHeaderPanel headerPanel;
    private JTabbedPane mainTabbedPane;
    private AdminStatusPanel statusPanel;
    
    // Tab panels
    private UserManagementPanel userManagementPanel;
    private ExamRoomManagementPanel examRoomManagementPanel;
    private QuestionManagementPanel questionManagementPanel;
    
    public AdminDashboard(NetworkManager networkManager, LoginController loginController) {
        this.networkManager = networkManager;
        this.loginController = loginController;
        this.userController = new UserController(networkManager);
        this.examRoomController = new ExamRoomController(networkManager);
        this.questionController = new QuestionController(networkManager);
        // Set session for user controller
        userController.setCurrentUser(
            loginController.getCurrentUser(), 
            loginController.getSessionToken()
        );
        examRoomController.setCurrentUser( 
                loginController.getCurrentUser(), 
                loginController.getSessionToken()
            );
        questionController.setCurrentUser(
        		loginController.getCurrentUser(),
        		loginController.getSessionToken()
        	);
        
        initializeUI();
        setupEventHandlers();
        loadInitialData();
    }
    
    private void initializeUI() {
        User currentUser = loginController.getCurrentUser();
        
        setTitle("Administrator Dashboard - " + 
            (currentUser != null ? currentUser.getFullName() : "Admin"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create panels
        headerPanel = new AdminHeaderPanel(currentUser, this);
        mainTabbedPane = createContentTabs();
        statusPanel = new AdminStatusPanel();
        
        // Layout
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JTabbedPane createContentTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // User Management tab
        userManagementPanel = new UserManagementPanel(this);
        tabbedPane.addTab("User Management", userManagementPanel);
        
        // Other tabs (placeholders for now)
        questionManagementPanel = new QuestionManagementPanel(questionController);
        tabbedPane.addTab("Question Management", questionManagementPanel);
        examRoomManagementPanel = new ExamRoomManagementPanel(this, examRoomController);
        tabbedPane.addTab("Room Management", examRoomManagementPanel);
        tabbedPane.addTab("Results & Reports", new PlaceholderPanel("Results & Reports"));
        
        return tabbedPane;
    }
    
    private void setupEventHandlers() {
        // Tab change listener
        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            onTabChanged(selectedIndex);
        });
        
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onLogoutRequested();
            }
        });
    }
    
//    private void loadInitialData() {
//        // Load initial data for active tab
//        updateStatus("Loading data...");
//        // TODO: Load users, questions, etc.
//        updateStatus("Ready");
//    }
    
    // AdminDashboardCallbacks implementation
    @Override
    public void onLogoutRequested() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            loginController.logout();
            dispose();
            // Show login frame again
            // TODO: Navigate back to login
        }
    }
    
    @Override
    public void onTabChanged(int tabIndex) {
        String[] tabNames = {"User Management", "Question Management", "Room Management", "Results & Reports"};
        if (tabIndex >= 0 && tabIndex < tabNames.length) {
            updateStatus("Switched to " + tabNames[tabIndex]);
        }
    }
    
//    @Override
//    public void onAddUserRequested() {
//        updateStatus("Opening Add User dialog...");
//        // TODO: Show AddUserDialog
//    	
//    }
    @Override
    public void onAddUserRequested() {
        AddUserDialog dialog = new AddUserDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User newUser = dialog.getUser();
            boolean success = userController.createUser(newUser);
            if (success) {
                refreshUserList();
            }
        }
    }
//    @Override
//    public void onEditUserRequested(User user) {
//        updateStatus("Opening Edit User dialog for: " + user.getUsername());
//        // TODO: Show EditUserDialog
//    }
    @Override
    public void onEditUserRequested(User user) {
        EditUserDialog dialog = new EditUserDialog(this, user);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User updatedUser = dialog.getUpdatedUser();
            boolean success = userController.updateUser(updatedUser);
            if (success) {
                refreshUserList();
            }
        }
    }
 // client/ui/admin/AdminDashboard.java
 // client/ui/admin/AdminDashboard.java - Cải thiện refreshUserList
    private void refreshUserList() {
        try {
            updateStatus("Loading users...");
            System.out.println("🔄 AdminDashboard: Starting to refresh user list");
            
            List<User> users = userController.getAllUsers();
            System.out.println("🔄 AdminDashboard: userController returned: " + (users != null ? users.size() + " users" : "null"));
            
            if (users != null) {
                if (users.isEmpty()) {
                    updateStatus("No users found");
                    userManagementPanel.setUsers(new ArrayList<>());
                } else {
                    System.out.println("🔄 AdminDashboard: Setting " + users.size() + " users to panel");
                    userManagementPanel.setUsers(users);
                    updateStatus("Loaded " + users.size() + " users successfully");
                }
            } else {
                updateStatus("Failed to load users - Check console for details");
                System.err.println("❌ userController.getAllUsers() returned null");
                userManagementPanel.setUsers(new ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("❌ Error in refreshUserList: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Error loading users: " + e.getMessage());
            userManagementPanel.setUsers(new ArrayList<>());
        }
    }
    @Override
    public void onDeleteUserRequested(User user) {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user: " + user.getUsername() + "?",
            "Delete User Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            updateStatus("Deleting user: " + user.getUsername());
            // TODO: Implement user deletion
            boolean success = userController.deleteUser(user.getUserId(), user.getUsername());
            if (success) {
                refreshUserList();
                updateStatus("User deleted: " + user.getUsername());
            }
        }
    }
 // client/ui/admin/AdminDashboard.java - Thêm các phương thức thiếu

 // Thêm vào constructor
 private void loadInitialData() {
     // Load initial data for active tab
     updateStatus("Loading data...");
     refreshUserList(); // ✅ Load users ngay khi khởi động
     updateStatus("Ready - Click 'Refresh' to load users");
 }


 @Override
 public void onRefreshUsersRequested() {
     // ✅ Implement refresh functionality
     refreshUserList();
 }

 @Override
 public void onSearchUsersRequested(String searchTerm) {
     try {
         updateStatus("Searching for: " + searchTerm);
         
         List<User> users;
         if (searchTerm.isEmpty()) {
             users = userController.getAllUsers();
             updateStatus("Showing all users");
         } else {
             users = userController.searchUsers(searchTerm);
             updateStatus("Found " + (users != null ? users.size() : 0) + " users");
         }
         
         if (users != null) {
             userManagementPanel.setUsers(users);
         }
     } catch (Exception e) {
         System.err.println("❌ Error in search: " + e.getMessage());
         updateStatus("Search error: " + e.getMessage());
     }
 }
//    
//    @Override
//    public void onRefreshUsersRequested() {
//        updateStatus("Refreshing users list...");
//        // TODO: Reload users from server
//    }
//    
//    @Override
//    public void onSearchUsersRequested(String searchTerm) {
//        if (searchTerm.isEmpty()) {
//            updateStatus("Showing all users");
//        } else {
//            updateStatus("Searching for: " + searchTerm);
//        }
//        // TODO: Implement search functionality
//    }
    
    @Override
    public void onAddQuestionRequested() {
        updateStatus("Add Question - Coming Soon");
    }
    
    @Override
    public void onEditQuestionRequested(int questionId) {
        updateStatus("Edit Question - Coming Soon");
    }
    
    @Override
    public void onDeleteQuestionRequested(int questionId) {
        updateStatus("Delete Question - Coming Soon");
    }
    
    @Override
    public void onCreateRoomRequested() {
        updateStatus("Create Room - Coming Soon");
    }
    
    @Override
    public void onEditRoomRequested(int roomId) {
        updateStatus("Edit Room - Coming Soon");
    }
    
    @Override
    public void onDeleteRoomRequested(int roomId) {
        updateStatus("Delete Room - Coming Soon");
    }
    
    @Override
    public void updateStatus(String message) {
        if (statusPanel != null) {
            statusPanel.setStatus(message);
        }
    }
}