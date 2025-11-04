// client/ui/admin/panels/AdminHeaderPanel.java
package client.ui.admin.panels;

import client.ui.admin.interfaces.AdminDashboardCallbacks;
import model.User;

import javax.swing.*;
import java.awt.*;

public class AdminHeaderPanel extends JPanel {
    
    private JLabel welcomeLabel;
    private JLabel userInfoLabel;
    private JButton logoutButton;
    private AdminDashboardCallbacks callbacks;
    
    public AdminHeaderPanel(User currentUser, AdminDashboardCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI(currentUser);
        setupEventHandlers();
    }
    
    private void initializeUI(User currentUser) {
        setLayout(new BorderLayout());
        
        // Welcome message
        welcomeLabel = new JLabel("Administrator Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(153, 51, 51));
        
        // User info
        userInfoLabel = new JLabel("Logged in as: " + 
            (currentUser != null ? currentUser.getFullName() : "Unknown"));
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userInfoLabel.setForeground(Color.GRAY);
        
        // Logout button
        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.setToolTipText("Logout from the system");
        
        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(welcomeLabel, BorderLayout.CENTER);
        leftPanel.add(userInfoLabel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        add(logoutButton, BorderLayout.EAST);
        
        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    }
    
    private void setupEventHandlers() {
        logoutButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onLogoutRequested();
            }
        });
    }
    
    public void updateUserInfo(User user) {
        userInfoLabel.setText("Logged in as: " + 
            (user != null ? user.getFullName() : "Unknown"));
    }
}