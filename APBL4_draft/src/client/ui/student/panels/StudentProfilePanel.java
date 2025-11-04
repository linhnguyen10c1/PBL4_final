package client.ui.student.panels;

import client.ui.student.interfaces.StudentDashboardCallbacks;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Student Profile Panel - Shows and manages student profile
 */
public class StudentProfilePanel extends JPanel {
    
    private StudentDashboardCallbacks callbacks;
    private User currentUser;
    
    // UI Components
    private JLabel profilePictureLabel;
    private JLabel nameLabel;
    private JLabel usernameLabel;
    private JLabel emailLabel;
    private JLabel roleLabel;
    private JLabel statusLabel;
    private JLabel memberSinceLabel;
    private JButton changePasswordButton;
    private JButton editProfileButton;
    
    public StudentProfilePanel(StudentDashboardCallbacks callbacks, User currentUser) {
        this.callbacks = callbacks;
        this.currentUser = currentUser;
        
        initializeUI();
        setupEventHandlers();
        populateData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header
        JLabel titleLabel = new JLabel("Student Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        
        // Profile picture section (left)
        JPanel picturePanel = createPicturePanel();
        mainPanel.add(picturePanel, BorderLayout.WEST);
        
        // Profile info section (center)
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createPicturePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Profile Picture"));
        panel.setPreferredSize(new Dimension(200, 250));
        
        // Placeholder profile picture
        profilePictureLabel = new JLabel();
        profilePictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setVerticalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        profilePictureLabel.setBackground(Color.LIGHT_GRAY);
        profilePictureLabel.setOpaque(true);
        profilePictureLabel.setText("<html><center>👤<br/>Profile<br/>Picture</center></html>");
        profilePictureLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        
        panel.add(profilePictureLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Profile Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        int row = 0;
        
        // Full Name
        addInfoRow(panel, gbc, row++, "Full Name:", 
            nameLabel = new JLabel(), new Font("Arial", Font.BOLD, 14));
        
        // Username
        addInfoRow(panel, gbc, row++, "Username:", 
            usernameLabel = new JLabel(), new Font("Arial", Font.PLAIN, 14));
        
        // Email
        addInfoRow(panel, gbc, row++, "Email:", 
            emailLabel = new JLabel(), new Font("Arial", Font.PLAIN, 14));
        
        // Role
        addInfoRow(panel, gbc, row++, "Role:", 
            roleLabel = new JLabel(), new Font("Arial", Font.PLAIN, 14));
        
        // Status
        addInfoRow(panel, gbc, row++, "Status:", 
            statusLabel = new JLabel(), new Font("Arial", Font.PLAIN, 14));
        
        // Member Since
        addInfoRow(panel, gbc, row++, "Member Since:", 
            memberSinceLabel = new JLabel(), new Font("Arial", Font.PLAIN, 14));
        
        return panel;
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, 
                           String labelText, JLabel valueLabel, Font font) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(font.deriveFont(Font.BOLD));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        valueLabel.setFont(font);
        panel.add(valueLabel, gbc);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        editProfileButton = new JButton("✏️ Edit Profile");
        editProfileButton.setEnabled(false); // Coming soon
        editProfileButton.setToolTipText("Feature coming soon");
        
        changePasswordButton = new JButton("🔒 Change Password");
        changePasswordButton.setEnabled(false); // Coming soon
        changePasswordButton.setToolTipText("Feature coming soon");
        
        panel.add(editProfileButton);
        panel.add(changePasswordButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        editProfileButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Profile editing feature will be available in a future update.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        changePasswordButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Password change feature will be available in a future update.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    private void populateData() {
        if (currentUser != null) {
            nameLabel.setText(currentUser.getFullName());
            usernameLabel.setText(currentUser.getUsername());
//            emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Not provided");
            roleLabel.setText("Student");
            statusLabel.setText(currentUser.isActive() ? "✅ Active" : "❌ Inactive");
            statusLabel.setForeground(currentUser.isActive() ? Color.GREEN : Color.RED);
            memberSinceLabel.setText(currentUser.getCreatedAt() != null ? 
                currentUser.getCreatedAt() : "Unknown");
        } else {
            nameLabel.setText("Unknown");
            usernameLabel.setText("Unknown");
            emailLabel.setText("Unknown");
            roleLabel.setText("Unknown");
            statusLabel.setText("Unknown");
            memberSinceLabel.setText("Unknown");
        }
    }
    
    public void updateUserInfo(User user) {
        this.currentUser = user;
        populateData();
    }
}