// client/ui/admin/components/UserManagementToolbar.java
package client.ui.admin.components;

import client.ui.admin.interfaces.AdminDashboardCallbacks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class UserManagementToolbar extends JPanel {
    
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton refreshUsersButton;
    private JTextField searchField;
    private JButton searchButton;
    private AdminDashboardCallbacks callbacks;
    
    public UserManagementToolbar(AdminDashboardCallbacks callbacks) {
        this.callbacks = callbacks;
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Left side - Action buttons
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addUserButton = new JButton("Add User");
        addUserButton.setToolTipText("Add a new user");
        
        editUserButton = new JButton("Edit User");
        editUserButton.setEnabled(false);
        editUserButton.setToolTipText("Edit selected user");
        
        deleteUserButton = new JButton("Delete User");
        deleteUserButton.setEnabled(false);
        deleteUserButton.setForeground(Color.RED);
        deleteUserButton.setToolTipText("Delete selected user");
        
        refreshUsersButton = new JButton("Refresh");
        refreshUsersButton.setToolTipText("Refresh user list");
        
        leftPanel.add(addUserButton);
        leftPanel.add(editUserButton);
        leftPanel.add(deleteUserButton);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(refreshUsersButton);
        
        // Right side - Search
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        rightPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.setToolTipText("Search by username or full name");
        rightPanel.add(searchField);
        
        searchButton = new JButton("Search");
        rightPanel.add(searchButton);
        
        // Layout
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private void setupEventHandlers() {
        // Action buttons
        addUserButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onAddUserRequested();
            }
        });
        
        editUserButton.addActionListener(e -> {
            if (callbacks != null) {
                // Will be handled by parent with selected user
            }
        });
        
        deleteUserButton.addActionListener(e -> {
            if (callbacks != null) {
                // Will be handled by parent with selected user
            }
        });
        
        refreshUsersButton.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.onRefreshUsersRequested();
            }
        });
        
        // Search functionality
        searchButton.addActionListener(e -> performSearch());
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
    }
    
    private void performSearch() {
        if (callbacks != null) {
            String searchTerm = searchField.getText().trim();
            callbacks.onSearchUsersRequested(searchTerm);
        }
    }
    
    // Enable/disable buttons based on selection
    public void setUserSelected(boolean selected) {
        editUserButton.setEnabled(selected);
        deleteUserButton.setEnabled(selected);
    }
    
    public void clearSearch() {
        searchField.setText("");
    }
    
    public String getSearchTerm() {
        return searchField.getText().trim();
    }
}