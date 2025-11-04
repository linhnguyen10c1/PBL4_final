// client/ui/admin/dialogs/AddUserDialog.java
package client.ui.admin.dialogs;

import model.User;
import utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddUserDialog extends JDialog {
    private JTextField usernameField;
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JCheckBox activeCheckBox;
    
    private User resultUser;
    private boolean confirmed = false;
    
    public AddUserDialog(JFrame parent) {
        super(parent, "Add New User", true);
        initializeUI();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        roleComboBox = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        formPanel.add(roleComboBox, gbc);
        
        // Active
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active", true);
        formPanel.add(activeCheckBox, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> handleCancel());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Enter key to save
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane()
            .getComponent(1)).getComponent(0));
    }
    
    private void handleSave() {
        // Validate input
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();
        boolean active = activeCheckBox.isSelected();
        
        if (!ValidationUtil.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, 
                "Username must be 3-20 characters, alphanumeric and underscore only");
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(fullName)) {
            JOptionPane.showMessageDialog(this, "Full name is required");
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Password is required");
            return;
        }
        
        if (!ValidationUtil.isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters");
            return;
        }
        
        // Create user object
        resultUser = new User();
        resultUser.setUsername(username);
        resultUser.setFullName(fullName);
        resultUser.setPassword(password);
        resultUser.setRole(role);
        resultUser.setActive(active);
        
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public User getUser() {
        return resultUser;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}