// client/ui/panels/ConnectionPanel.java
package client.ui.auth.panels;

import client.ui.auth.interfaces.LoginViewCallbacks;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConnectionPanel extends JPanel {
    
    private JTextField serverHostField;
    private JTextField serverPortField;
    private JButton connectButton;
    private JLabel connectionStatusLabel;
    private LoginViewCallbacks callbacks;
    
    public ConnectionPanel(LoginViewCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Server Connection"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Server host
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Server Host:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        serverHostField = new JTextField("localhost", 15);
        add(serverHostField, gbc);
        
        // Server port
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Port:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        serverPortField = new JTextField("8888", 8);
        add(serverPortField, gbc);
        
        // Connect button
        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(100, 25));
        add(connectButton, gbc);
        
        // Connection status
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        connectionStatusLabel = new JLabel("Not connected", SwingConstants.CENTER);
        connectionStatusLabel.setOpaque(true);
        connectionStatusLabel.setBackground(Color.LIGHT_GRAY);
        connectionStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(connectionStatusLabel, gbc);
    }
    
    private void setupEventHandlers() {
        // Connect button action
        connectButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onConnectRequested(getServerHost(), getServerPort());
            }
        });
        
        // Enter key handler
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && callbacks != null) {
                    callbacks.onConnectRequested(getServerHost(), getServerPort());
                }
            }
        };
        
        serverHostField.addKeyListener(enterKeyAdapter);
        serverPortField.addKeyListener(enterKeyAdapter);
    }
    
    // Getters
    public String getServerHost() {
        return serverHostField.getText().trim();
    }
    
    public String getServerPort() {
        return serverPortField.getText().trim();
    }
    
    // Update methods
    public void updateConnectionStatus(boolean connected, String serverAddress) {
        if (connected) {
            connectionStatusLabel.setText("Connected to " + serverAddress);
            connectionStatusLabel.setBackground(new Color(144, 238, 144)); // Light green
            connectButton.setText("Disconnect");
        } else {
            connectionStatusLabel.setText("Not connected");
            connectionStatusLabel.setBackground(Color.LIGHT_GRAY);
            connectButton.setText("Connect");
        }
        
        // Enable/disable connection fields
        serverHostField.setEnabled(!connected);
        serverPortField.setEnabled(!connected);
    }
    
    public void setConnectionInProgress(boolean inProgress) {
        connectButton.setEnabled(!inProgress);
        connectButton.setText(inProgress ? "Connecting..." : "Connect");
    }
}