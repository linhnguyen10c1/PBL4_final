package client.ui.admin;

import java.util.List;
import java.util.ArrayList;
import client.controller.LoginController;
import client.controller.QuestionController;
import client.controller.UserController;
import client.network.NetworkManager;
import client.ui.admin.components.PlaceholderPanel;
import client.ui.admin.interfaces.AdminDashboardCallbacks;
import client.ui.admin.panels.*;
import client.ui.auth.LoginFrame;
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
    private LoginFrame loginFrame;
    
    // UI Panels
    private AdminHeaderPanel headerPanel;
    private JTabbedPane mainTabbedPane;
    private AdminStatusPanel statusPanel;
    
    // Tab panels
    private UserManagementPanel userManagementPanel;
    private ExamRoomManagementPanel examRoomManagementPanel;
    private QuestionManagementPanel questionManagementPanel;
    
    public AdminDashboard(NetworkManager networkManager, LoginController loginController) {
        this(networkManager, loginController, null);
    }
    
    public AdminDashboard(NetworkManager networkManager, LoginController loginController, LoginFrame loginFrame) {
        this.networkManager = networkManager;
        this.loginController = loginController;
        this.loginFrame = loginFrame;
        this.userController = new UserController(networkManager);
        this.examRoomController = new ExamRoomController(networkManager);
        this.questionController = new QuestionController(networkManager);
        
        // Set session for controllers
        User currentUser = loginController.getCurrentUser();
        String sessionToken = loginController.getSessionToken();
        
        userController.setCurrentUser(currentUser, sessionToken);
        examRoomController.setCurrentUser(currentUser, sessionToken);
        questionController.setCurrentUser(currentUser, sessionToken);
        
        initializeUI();
        setupEventHandlers();
        loadInitialData();
    }
    
    private void initializeUI() {
        User currentUser = loginController.getCurrentUser();
        
        setTitle("Administrator Dashboard - " + 
            (currentUser != null ? currentUser.getFullName() : "Admin"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout(10, 10));
        
        // Header
        headerPanel = new AdminHeaderPanel(currentUser, this);
        add(headerPanel, BorderLayout.NORTH);
        
        // MAIN CONTENT - Simplified
        mainTabbedPane = createContentTabs();
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // Status bar
        statusPanel = new AdminStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JTabbedPane createContentTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        
        userManagementPanel = new UserManagementPanel(userController);
        tabbedPane.addTab("User Management", userManagementPanel);
        
        // QUESTION MANAGEMENT  
        questionManagementPanel = new QuestionManagementPanel(questionController);
        tabbedPane.addTab("Question Management", questionManagementPanel);
        
        // EXAM ROOM MANAGEMENT
        examRoomManagementPanel = new ExamRoomManagementPanel(this, examRoomController);
        tabbedPane.addTab("Room Management", examRoomManagementPanel);
        
        // REPORTS (placeholder)
        tabbedPane.addTab("Reports", new PlaceholderPanel("Reports & Statistics"));
        
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
                handleWindowClosing();
            }
        });
    }
    
    private void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout and exit?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }
    
    private void performLogout() {
        System.out.println("🔄 AdminDashboard: Performing logout...");
        
        // Logout from server
        loginController.logout();
        
        // Clear data
        clearDashboardData();
        
        // Dispose dashboard
        dispose();
        
        // Show login frame
        if (loginFrame != null) {
            loginFrame.showAfterLogout();
        } else {
            System.exit(0);
        }
    }
    
    private void clearDashboardData() {
        System.out.println("🔄 Clearing AdminDashboard data...");
        
        try {
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
        updateStatus("Dashboard loaded - Ready to use");
    }
    
    @Override
    public void onLogoutRequested() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }
    
    @Override
    public void onTabChanged(int tabIndex) {
        String[] tabNames = {"User Management", "Question Management", "Room Management", "Reports"};
        if (tabIndex >= 0 && tabIndex < tabNames.length) {
            updateStatus("Current tab: " + tabNames[tabIndex]);
        }
    }
    
    @Override
    public void onAddUserRequested() {
    }
    
    @Override
    public void onEditUserRequested(User user) {
    }
    
    @Override
    public void onDeleteUserRequested(User user) {
    }
    
    @Override
    public void onRefreshUsersRequested() {
    }
    
    @Override
    public void onSearchUsersRequested(String searchTerm) {
    }
    
    @Override
    public void onAddQuestionRequested() {
        updateStatus("Add Question feature");
    }
    
    @Override
    public void onEditQuestionRequested(int questionId) {
        updateStatus("Edit Question feature");
    }
    
    @Override
    public void onDeleteQuestionRequested(int questionId) {
        updateStatus("Delete Question feature");
    }
    
    @Override
    public void onCreateRoomRequested() {
        updateStatus("Create Room feature");
    }
    
    @Override
    public void onEditRoomRequested(int roomId) {
        updateStatus("Edit Room feature");
    }
    
    @Override
    public void onDeleteRoomRequested(int roomId) {
        updateStatus("Delete Room feature");
    }
    
    @Override
    public void updateStatus(String message) {
        if (statusPanel != null) {
            statusPanel.setStatus(message);
        }
        System.out.println("📊 [AdminDashboard] " + message);
    }
}