// client/ui/admin/dialogs/EditUserDialog.java
package client.ui.admin.dialogs;

import model.User;
import utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class EditUserDialog extends JDialog {
    private User originalUser;
    private User resultUser;
    private boolean confirmed = false;
    
    private JTextField usernameField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox;
    private JCheckBox activeCheckBox;
    
    public EditUserDialog(JFrame parent, User user) {
        super(parent, "Edit User", true);
        this.originalUser = user;
        initializeUI();
        populateFields();
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
        
        // Username (read-only)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setEditable(false);
        usernameField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(usernameField, gbc);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        roleComboBox = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        formPanel.add(roleComboBox, gbc);
        
        // Active
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active");
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
    
    private void populateFields() {
        if (originalUser != null) {
            usernameField.setText(originalUser.getUsername());
            fullNameField.setText(originalUser.getFullName());
            roleComboBox.setSelectedItem(originalUser.getRole());
            activeCheckBox.setSelected(originalUser.isActive());
        }
    }
    
    private void setupEventHandlers() {
        // Enter key to save
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane()
            .getComponent(1)).getComponent(0));
    }
    
    private void handleSave() {
        // Validate input
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();
        boolean active = activeCheckBox.isSelected();
        
        if (!ValidationUtil.isNotEmpty(fullName)) {
            JOptionPane.showMessageDialog(this, "Full name is required");
            return;
        }
        
        // Create updated user object
        resultUser = new User();
        resultUser.setUserId(originalUser.getUserId());
        resultUser.setUsername(originalUser.getUsername()); // Keep original username
        resultUser.setFullName(fullName);
        resultUser.setRole(role);
        resultUser.setActive(active);
        
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public User getUpdatedUser() {
        return resultUser;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}