// client/ui/panels/LoginPanel.java
package client.ui.auth.panels;

import client.ui.auth.interfaces.LoginViewCallbacks;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPanel extends JPanel {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JProgressBar progressBar;
    private LoginViewCallbacks callbacks;
    
    public LoginPanel(LoginViewCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("User Authentication"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setToolTipText("Enter your student ID or admin username");
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setToolTipText("Enter your password");
        add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0;
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setEnabled(false); // Disabled until connected
        add(loginButton, gbc);
        
        // Progress bar
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Logging in...");
        add(progressBar, gbc);
    }
    
    private void setupEventHandlers() {
        // Login button action
        loginButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onLoginRequested(getUsername(), getPassword());
            }
        });
        
        // Enter key handler
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && callbacks != null) {
                    callbacks.onLoginRequested(getUsername(), getPassword());
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);
    }
    
    // Getters
    public String getUsername() {
        return usernameField.getText().trim();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    // Update methods
    public void setLoginInProgress(boolean inProgress) {
        loginButton.setEnabled(!inProgress);
        usernameField.setEnabled(!inProgress);
        passwordField.setEnabled(!inProgress);
        progressBar.setVisible(inProgress);
    }
    
    public void setLoginEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
    }
    
    public void clearPassword() {
        passwordField.setText("");
    }
    
    public void focusUsername() {
        usernameField.requestFocus();
    }
    
    public void focusPassword() {
        passwordField.requestFocus();
    }
}