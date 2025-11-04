package client.ui.student.panels;

import client.ui.student.interfaces.StudentDashboardCallbacks;
import model.User;

import javax.swing.*;
import java.awt.*;

public class StudentHeaderPanel extends JPanel {
    
    private JLabel titleLabel;
    private JLabel userInfoLabel;
    private JButton logoutButton;
    private StudentDashboardCallbacks callbacks;
    
    public StudentHeaderPanel(User currentUser, StudentDashboardCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI(currentUser);
        setupEventHandlers();
    }
    
    private void initializeUI(User currentUser) {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Left side - Title and user info
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        titleLabel = new JLabel("🎓 Student Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 100, 200));
        
        userInfoLabel = new JLabel("Welcome, " + 
            (currentUser != null ? currentUser.getFullName() : "Student"));
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userInfoLabel.setForeground(Color.DARK_GRAY);
        
        leftPanel.add(titleLabel, BorderLayout.CENTER);
        leftPanel.add(userInfoLabel, BorderLayout.SOUTH);
        
        // Right side - Logout button
        logoutButton = new JButton("🚪 Logout");
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.setToolTipText("Logout from the system");
        
        add(leftPanel, BorderLayout.WEST);
        add(logoutButton, BorderLayout.EAST);
    }
    
    private void setupEventHandlers() {
        logoutButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onLogoutRequested();
            }
        });
    }
    
    public void updateUserInfo(User user) {
        userInfoLabel.setText("Welcome, " + 
            (user != null ? user.getFullName() : "Student"));
    }
}