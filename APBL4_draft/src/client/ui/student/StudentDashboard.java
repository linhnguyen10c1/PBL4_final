package client.ui.student;

import client.controller.StudentExamController;
import client.controller.LoginController;
import client.ui.student.interfaces.StudentDashboardCallbacks;
import client.ui.student.panels.*;
import client.ui.auth.LoginFrame; // ✅ THÊM IMPORT
import client.network.NetworkManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Student Dashboard
 */
public class StudentDashboard extends JFrame implements StudentDashboardCallbacks, StudentExamController.ExamListener {
    
    private NetworkManager networkManager;
    private LoginController loginController;
    private StudentExamController examController;
    
    // ✅ THÊM: Reference đến LoginFrame
    private LoginFrame loginFrame;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private StudentHeaderPanel headerPanel;
    private StudentStatusPanel statusPanel;
    
    // Panels
    private AvailableExamsPanel availableExamsPanel;
    private ExamInterfacePanel examInterfacePanel;
    private ExamResultsPanel examResultsPanel;
    private StudentProfilePanel profilePanel;
    
    // Current state
    private ExamSession currentExamSession;
    
    // ✅ CONSTRUCTOR CŨ (giữ nguyên để tương thích)
    public StudentDashboard(NetworkManager networkManager, LoginController loginController) {
        this(networkManager, loginController, null);
    }
    
    // ✅ CONSTRUCTOR MỚI (với LoginFrame reference)
    public StudentDashboard(NetworkManager networkManager, LoginController loginController, LoginFrame loginFrame) {
        this.networkManager = networkManager;
        this.loginController = loginController;
        this.loginFrame = loginFrame; // ✅ Lưu reference
        this.examController = new StudentExamController(networkManager);
        this.examController.setExamListener(this);
        
        User currentUser = loginController.getCurrentUser();
        String sessionToken = loginController.getSessionToken();
        examController.setCurrentUser(currentUser, sessionToken);
        
        initializeUI();
        setupEventHandlers();
        loadInitialData();
        
        setVisible(true);
    }
    
    private void initializeUI() {
        User currentUser = loginController.getCurrentUser();
        setTitle("Student Dashboard - " + (currentUser != null ? currentUser.getFullName() : "Student"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // ✅ CHANGED
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Header
        headerPanel = new StudentHeaderPanel(currentUser, this);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with tabs
        createMainContent();
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        statusPanel = new StudentStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13f));
        
        // Available Exams Tab
        availableExamsPanel = new AvailableExamsPanel(this, examController);
        tabbedPane.addTab("📝 Available Exams", availableExamsPanel);
        
        // Exam Interface Tab (initially hidden)
        examInterfacePanel = new ExamInterfacePanel(this, examController);
        
        // Results Tab
        examResultsPanel = new ExamResultsPanel(this, examController);
        tabbedPane.addTab("📊 My Results", examResultsPanel);
        
        // Profile Tab
        profilePanel = new StudentProfilePanel(this, loginController.getCurrentUser());
        tabbedPane.addTab("👤 Profile", profilePanel);
        
        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            onTabChanged(selectedIndex);
        });
    }
    
    private void setupEventHandlers() {
        // ✅ THAY ĐỔI: Window closing handler
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    private void loadInitialData() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Loading available exams...");
            availableExamsPanel.loadAvailableExams();
        });
    }
    
    private void handleWindowClosing() {
        if (currentExamSession != null && currentExamSession.isInProgress()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "You have an active exam in progress. Are you sure you want to exit?\n" +
                "Your exam will be automatically submitted.",
                "Exam in Progress",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (choice == JOptionPane.YES_OPTION) {
                // Auto-submit exam
                onSubmitExamRequested(currentExamSession, true);
                // Sau khi submit xong sẽ logout
                performLogout();
            }
        } else {
            // Không có exam đang diễn ra
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout and return to login screen?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);
                
            if (choice == JOptionPane.YES_OPTION) {
                performLogout();
            }
        }
    }
    
    // ✅ PHƯƠNG THỨC MỚI: Thực hiện logout
    private void performLogout() {
        System.out.println("🔄 StudentDashboard: Performing logout...");
        
        // Logout từ server
        loginController.logout();
        
        // Clear local data
        clearDashboardData();
        
        // Dispose dashboard
        dispose();
        
        // ✅ Hiển thị lại LoginFrame
        if (loginFrame != null) {
            loginFrame.showAfterLogout();
        } else {
            System.err.println("⚠️ LoginFrame reference is null");
            System.exit(0); // Fallback
        }
    }
    
    // ✅ PHƯƠNG THỨC MỚI: Clear data của dashboard (CÁCH 1 - Không có clearData())
    private void clearDashboardData() {
        System.out.println("🔄 Clearing StudentDashboard data...");
        
        try {
            // Clear current exam session
            currentExamSession = null;
            
            // Note: Panels sẽ tự reset khi login lại
            // Không cần gọi clearData() vì không có method đó
            
            System.out.println("✅ StudentDashboard data cleared");
        } catch (Exception e) {
            System.err.println("⚠️ Error clearing dashboard data: " + e.getMessage());
        }
    }
    
    // StudentDashboardCallbacks implementation
    @Override
    public void onLogoutRequested() {
        if (currentExamSession != null && currentExamSession.isInProgress()) {
            JOptionPane.showMessageDialog(this,
                "Cannot logout while exam is in progress.\n" +
                "Please submit or finish your exam first.",
                "Exam Active",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            performLogout(); // ✅ THAY ĐỔI
        }
    }
    
    @Override
    public void onTabChanged(int tabIndex) {
        String[] tabNames = {"Available Exams", "My Results", "Profile"};
        if (tabIndex < tabNames.length) {
            updateStatus(tabNames[tabIndex] + " - " + getTabDescription(tabIndex));
        }
    }
    
    private String getTabDescription(int tabIndex) {
        switch (tabIndex) {
            case 0: return "Join and take exams";
            case 1: return "View your exam history and results";
            case 2: return "Manage your profile settings";
            default: return "";
        }
    }
    
    @Override
    public void onRefreshExamsRequested() {
        updateStatus("Refreshing available exams...");
        examController.getAvailableExamRooms();
    }
    
    @Override
    public void onJoinExamRequested(ExamRoom examRoom) {
        updateStatus("Joining exam room: " + examRoom.getRoomName());
    }
    
    @Override
    public void onStartExamRequested(ExamSession session) {
        this.currentExamSession = session;
        updateStatus("Starting exam: " + session.getExamRoom().getRoomName());
        
        // Add exam interface tab and switch to it
        if (tabbedPane.indexOfComponent(examInterfacePanel) == -1) {
            tabbedPane.insertTab("🎯 Taking Exam", null, examInterfacePanel, "Currently taking exam", 1);
        }
        
        examInterfacePanel.startExam(session);
        tabbedPane.setSelectedComponent(examInterfacePanel);
        
        // Disable other tabs during exam
        setTabsEnabled(false);
    }
    
    @Override
    public void onSubmitExamRequested(ExamSession session, boolean isAutoSubmit) {
        updateStatus("Submitting exam...");
        examController.submitExam(session.getSessionToken(), isAutoSubmit);
    }
    
    @Override
    public void onExamTimeExpired(ExamSession session) {
        JOptionPane.showMessageDialog(this,
            "Time's up! Your exam has been automatically submitted.",
            "Exam Time Expired",
            JOptionPane.INFORMATION_MESSAGE);
        onSubmitExamRequested(session, true);
    }
    
    @Override
    public void onAnswerChanged(int questionId, String answer) {
        if (currentExamSession != null) {
            examController.saveAnswer(currentExamSession.getSessionToken(), questionId, answer);
        }
    }
    
    @Override
    public void onAnswerSaved(int questionId, String answer) {
        updateStatus("Answer saved");
    }
    
    @Override
    public void onNavigateToQuestion(int questionIndex) {
        if (examInterfacePanel != null) {
            examInterfacePanel.navigateToQuestion(questionIndex);
        }
    }
    
    @Override
    public void onViewResultsRequested() {
        tabbedPane.setSelectedComponent(examResultsPanel);
        examResultsPanel.loadResults();
    }
    
    @Override
    public void onRefreshResultsRequested() {
        updateStatus("Refreshing exam results...");
        examController.getExamResults();
    }
    
    @Override
    public void onResultDetailRequested(ExamResult result) {
        client.ui.student.dialogs.ExamResultDialog dialog = 
            new client.ui.student.dialogs.ExamResultDialog(this, result);
        dialog.setVisible(true);
    }
    
    @Override
    public void updateStatus(String message) {
        updateStatus(message, false);
    }
    
    @Override
    public void updateStatus(String message, boolean isError) {
        if (statusPanel != null) {
            statusPanel.setStatus(message, isError ? Color.RED : Color.BLACK);
        }
    }
    
    // StudentExamController.ExamListener implementation
    @Override
    public void onAvailableRoomsLoaded(List<ExamRoom> rooms) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Loaded " + rooms.size() + " available exams");
            if (availableExamsPanel != null) {
                availableExamsPanel.setExamRooms(rooms);
            }
        });
    }
    
    @Override
    public void onExamRoomJoined(ExamSession session) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Successfully joined exam room");
            onStartExamRequested(session);
        });
    }
    
    @Override
    public void onExamStarted(List<ExamAnswer> questions) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Exam started with " + questions.size() + " questions");
            
            if (examInterfacePanel != null) {
                examInterfacePanel.setExamQuestions(questions);
                examInterfacePanel.revalidate();
                examInterfacePanel.repaint();
            }
        });
    }
    
    @Override
    public void onExamSubmitted(ExamResult result) {
        SwingUtilities.invokeLater(() -> {
            this.currentExamSession = null;
            
            // Remove exam interface tab
            int examTabIndex = tabbedPane.indexOfComponent(examInterfacePanel);
            if (examTabIndex != -1) {
                tabbedPane.removeTabAt(examTabIndex);
            }
            
            // Re-enable other tabs
            setTabsEnabled(true);
            
            // Show result
            updateStatus("Exam submitted successfully. Score: " + String.format("%.1f%%", result.getPercentage()));
            
            // Show result dialog
            onResultDetailRequested(result);
            
            // Switch to results tab
            onViewResultsRequested();
        });
    }
    
    @Override
    public void onExamResultsLoaded(List<ExamResult> results) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Loaded " + results.size() + " exam results");
            if (examResultsPanel != null) {
                examResultsPanel.setExamResults(results);
            }
        });
    }
    
    @Override
    public void onExamTimeExpired() {
        SwingUtilities.invokeLater(() -> {
            if (currentExamSession != null) {
                onExamTimeExpired(currentExamSession);
            }
        });
    }
    
    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Error: " + message, true);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    // Helper methods
    private void setTabsEnabled(boolean enabled) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tab = tabbedPane.getComponentAt(i);
            if (tab != examInterfacePanel) {
                tabbedPane.setEnabledAt(i, enabled);
            }
        }
    }
    
    // Getters
    public StudentExamController getExamController() {
        return examController;
    }
    
    public ExamSession getCurrentExamSession() {
        return currentExamSession;
    }
}