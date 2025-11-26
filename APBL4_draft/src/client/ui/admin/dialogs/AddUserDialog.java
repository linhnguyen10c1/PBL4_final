package client.ui.admin.dialogs;

import model.User;
import javax.swing.*;
import java.awt.*;

public class AddUserDialog extends JDialog {
    
    private boolean confirmed = false;
    private JTextField usernameField, fullNameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JLabel usernameHintLabel;
    
    public AddUserDialog(JFrame parent) {
        super(parent, "Add User", true);
        initUI();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        form.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        form.add(roleBox);
        
        form.add(new JLabel("Username:*"));
        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernameField = new JTextField();
        usernameHintLabel = new JLabel("Enter student ID (e.g: 102230303)");
        usernameHintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        usernameHintLabel.setForeground(Color.GRAY);
        usernamePanel.add(usernameField, BorderLayout.NORTH);
        usernamePanel.add(usernameHintLabel, BorderLayout.SOUTH);
        form.add(usernamePanel);
        
        form.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        form.add(fullNameField);
        
        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        form.add(passwordField);
        
        JButton okBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");
        
        okBtn.addActionListener(e -> {
            if (validateForm()) {
                confirmed = true;
                dispose();
            }
        });
        cancelBtn.addActionListener(e -> dispose());
        
        JPanel buttons = new JPanel();
        buttons.add(okBtn);
        buttons.add(cancelBtn);
        form.add(buttons);
        
        add(form);
        setupEvents();
    }
    
    private void setupEvents() {
        roleBox.addActionListener(e -> updateHint());
        
        usernameField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if ("STUDENT".equals(roleBox.getSelectedItem())) {
                    passwordField.setText(usernameField.getText().trim());
                }
            }
        });
        
        updateHint(); // Initial setup
    }
    
    private void updateHint() {
        if ("STUDENT".equals(roleBox.getSelectedItem())) {
            usernameHintLabel.setText("Enter student ID (9 digits, e.g: 102230303)");
            usernameHintLabel.setForeground(Color.BLUE);
            passwordField.setText(usernameField.getText().trim()); // Auto-fill
        } else {
            usernameHintLabel.setText("Enter admin username");
            usernameHintLabel.setForeground(Color.GRAY);
            passwordField.setText(""); 
        }
    }
    
    private boolean validateForm() {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username required!");
            return false;
        }
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name required!");
            return false;
        }
        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Password required!");
            return false;
        }
        return true;
    }
    
    public boolean isConfirmed() { return confirmed; }
    
    public User getUser() {
        User user = new User();
        user.setUsername(usernameField.getText().trim());
        user.setFullName(fullNameField.getText().trim());
        user.setPassword(new String(passwordField.getPassword()));
        user.setRole((String) roleBox.getSelectedItem());
        user.setActive(true);
        return user;
    }
}