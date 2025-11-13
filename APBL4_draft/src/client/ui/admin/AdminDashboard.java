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
import client.ui.auth.LoginFrame; // ✅ THÊM IMPORT
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
    
    // ✅ THÊM: Reference đến LoginFrame
    private LoginFrame loginFrame;
    
    // UI Panels
    private AdminHeaderPanel headerPanel;
    private JTabbedPane mainTabbedPane;
    private AdminStatusPanel statusPanel;
    
    // Tab panels
    private UserManagementPanel userManagementPanel;
    private ExamRoomManagementPanel examRoomManagementPanel;
    private QuestionManagementPanel questionManagementPanel;
    
    // ✅ CONSTRUCTOR CŨ (giữ nguyên để tương thích)
    public AdminDashboard(NetworkManager networkManager, LoginController loginController) {
        this(networkManager, loginController, null);
    }
    
    // ✅ CONSTRUCTOR MỚI (với LoginFrame reference)
    public AdminDashboard(NetworkManager networkManager, LoginController loginController, LoginFrame loginFrame) {
        this.networkManager = networkManager;
        this.loginController = loginController;
        this.loginFrame = loginFrame; // ✅ Lưu reference
        this.userController = new UserController(networkManager);
        this.examRoomController = new ExamRoomController(networkManager);
        this.questionController = new QuestionController(networkManager);
        
        // Set session for controllers
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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // ✅ CHANGED: Không exit ngay
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
        
        // Other tabs
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
        
        // ✅ THAY ĐỔI: Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing(); // ✅ Gọi method mới
            }
        });
    }
    
    // ✅ PHƯƠNG THỨC MỚI: Xử lý đóng cửa sổ
    private void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout and return to login screen?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }
    
    // ✅ PHƯƠNG THỨC MỚI: Thực hiện logout
    private void performLogout() {
        System.out.println("🔄 AdminDashboard: Performing logout...");
        
        // Logout từ server
        loginController.logout();
        
        // Clear local data
        clearDashboardData();
        
        // Dispose dashboard
        dispose();
        
        // ✅ Hiển thị lại LoginFrame nếu có
        if (loginFrame != null) {
            loginFrame.showAfterLogout();
        } else {
            System.err.println("⚠️ LoginFrame reference is null - cannot return to login screen");
            System.exit(0); // Fallback: thoát ứng dụng
        }
    }
    
    // ✅ PHƯƠNG THỨC MỚI: Clear data của dashboard
    private void clearDashboardData() {
        System.out.println("🔄 Clearing AdminDashboard data...");
        
        try {
            // Clear user list
            if (userManagementPanel != null) {
                userManagementPanel.setUsers(new ArrayList<>());
            }
            
            // Clear controllers
            if (userController != null) {
                userController.clearSession();
            }
            if (examRoomController != null) {
                examRoomController.clearSession();
            }
            if (questionController != null) {
                questionController.clearSession();
            }
            
            System.out.println("✅ AdminDashboard data cleared");
        } catch (Exception e) {
            System.err.println("⚠️ Error clearing dashboard data: " + e.getMessage());
        }
    }
    
    private void loadInitialData() {
        updateStatus("Loading data...");
        refreshUserList();
        updateStatus("Ready - Click 'Refresh' to load users");
    }
    
    // AdminDashboardCallbacks implementation
    @Override
    public void onLogoutRequested() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            performLogout(); // ✅ THAY ĐỔI: Gọi performLogout() thay vì dispose()
        }
    }
    
    @Override
    public void onTabChanged(int tabIndex) {
        String[] tabNames = {"User Management", "Question Management", "Room Management", "Results & Reports"};
        if (tabIndex >= 0 && tabIndex < tabNames.length) {
            updateStatus("Switched to " + tabNames[tabIndex]);
        }
    }
    
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
            boolean success = userController.deleteUser(user.getUserId(), user.getUsername());
            if (success) {
                refreshUserList();
                updateStatus("User deleted: " + user.getUsername());
            }
        }
    }
    
    @Override
    public void onRefreshUsersRequested() {
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