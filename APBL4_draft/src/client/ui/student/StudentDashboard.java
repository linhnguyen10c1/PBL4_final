package client.ui.student;

import client.controller.StudentExamController;
import client.controller.LoginController;
import client.ui.student.interfaces.StudentDashboardCallbacks;
import client.ui.student.panels.*;
import client.network.NetworkManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Student Dashboard - Restructured similar to Admin
 */
public class StudentDashboard extends JFrame implements StudentDashboardCallbacks, StudentExamController.ExamListener {
    
    private NetworkManager networkManager;
    private LoginController loginController;
    private StudentExamController examController;
    
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
    
    public StudentDashboard(NetworkManager networkManager, LoginController loginController) {
        this.networkManager = networkManager;
        this.loginController = loginController;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        // Will be added dynamically when exam starts
        
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
        // Window closing
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
            } else {
                return; // Don't close
            }
        }
        
        dispose();
        System.exit(0);
    }
    
    // StudentDashboardCallbacks implementation
    @Override
    public void onLogoutRequested() {
        if (currentExamSession != null && currentExamSession.isInProgress()) {
            JOptionPane.showMessageDialog(this,
                "Cannot logout while exam is in progress.",
                "Exam Active",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            loginController.logout();
            dispose();
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
        // This will be handled by password dialog in AvailableExamsPanel
    }
    
//    @Override
//    public void onStartExamRequested(ExamSession session) {
//        this.currentExamSession = session;
//        updateStatus("Starting exam: " + session.getExamRoom().getRoomName());
//        
//        // Add exam interface tab and switch to it
//        if (tabbedPane.indexOfComponent(examInterfacePanel) == -1) {
//            tabbedPane.insertTab("🎯 Taking Exam", null, examInterfacePanel, "Currently taking exam", 1);
//        }
//        
//        examInterfacePanel.startExam(session);
//        tabbedPane.setSelectedComponent(examInterfacePanel);
//        
//        // Disable other tabs during exam
//        setTabsEnabled(false);
//    }
    @Override
    public void onStartExamRequested(ExamSession session) {
        this.currentExamSession = session;
        updateStatus("Starting exam: " + session.getExamRoom().getRoomName());
        
        System.out.println("🔍 onStartExamRequested called:");
        System.out.println("  - Session: " + session.getSessionId());
        System.out.println("  - Room: " + session.getExamRoom().getRoomName());
        System.out.println("  - Duration: " + session.getExamRoom().getDurationMinutes() + " minutes");
        
        // Add exam interface tab and switch to it
        if (tabbedPane.indexOfComponent(examInterfacePanel) == -1) {
            tabbedPane.insertTab("🎯 Taking Exam", null, examInterfacePanel, "Currently taking exam", 1);
            System.out.println("✅ Exam interface tab added");
        }
        
        examInterfacePanel.startExam(session);
        tabbedPane.setSelectedComponent(examInterfacePanel);
        
        // Disable other tabs during exam
        setTabsEnabled(false);
        
        System.out.println("✅ Exam interface activated");
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
        // Auto-save answer
        if (currentExamSession != null) {
            examController.saveAnswer(currentExamSession.getSessionToken(), questionId, answer);
        }
    }
    
    @Override
    public void onAnswerSaved(int questionId, String answer) {
        // Update status briefly
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
        // Show detailed result dialog
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
    
//    @Override
//    public void onExamStarted(List<ExamAnswer> questions) {
//        SwingUtilities.invokeLater(() -> {
//            updateStatus("Exam started with " + questions.size() + " questions");
//            if (examInterfacePanel != null) {
//                examInterfacePanel.setExamQuestions(questions);
//            }
//        });
//    }
//    
    @Override
    public void onExamStarted(List<ExamAnswer> questions) {
        System.out.println("🔍 [StudentDashboard] onExamStarted called with " + 
                          (questions != null ? questions.size() + " questions" : "NULL questions"));
        
        SwingUtilities.invokeLater(() -> {
            updateStatus("Exam started with " + questions.size() + " questions");
            
            if (examInterfacePanel != null) {
                System.out.println("🔍 [StudentDashboard] Setting " + questions.size() + " questions to examInterfacePanel");
                examInterfacePanel.setExamQuestions(questions);
                
                // ✅ FORCE REFRESH UI
                examInterfacePanel.revalidate();
                examInterfacePanel.repaint();
                
                System.out.println("✅ [StudentDashboard] Questions set and UI refreshed");
            } else {
                System.err.println("❌ [StudentDashboard] examInterfacePanel is null!");
            }
        });
    }
//    @Override
//    public void onAnswerSaved(int questionId, String answer) {
//        // Brief status update
//        updateStatus("Answer saved");
//    }
    
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