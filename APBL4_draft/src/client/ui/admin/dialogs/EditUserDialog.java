package client.ui.admin.dialogs;

import model.User;
import javax.swing.*;
import java.awt.*;

public class EditUserDialog extends JDialog {
    
    private boolean confirmed = false;
    private User originalUser;
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JCheckBox reactivateCheckBox;
    
    public EditUserDialog(JFrame parent, User user) {
        super(parent, "Edit User", true);
        this.originalUser = user;
        initUI();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // ✅ THAY ĐỔI: Tăng rows từ 5 lên 6 để có chỗ cho checkbox
        int rows = originalUser.isActive() ? 5 : 6; // 6 rows nếu inactive
        JPanel form = new JPanel(new GridLayout(rows, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        form.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(originalUser.getUsername());
        usernameField.setEnabled(false);
        form.add(usernameField);
        
        form.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        form.add(fullNameField);
        
        form.add(new JLabel("New Password:"));
        passwordField = new JPasswordField();
        form.add(passwordField);
        
        form.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        form.add(roleBox);
        
        // ✅ THÊM: Checkbox reactivate chỉ hiện khi user inactive
        if (!originalUser.isActive()) {
            form.add(new JLabel("Status:"));
            reactivateCheckBox = new JCheckBox("Reactivate User");
            reactivateCheckBox.setForeground(Color.BLUE);
            form.add(reactivateCheckBox);
        }
        
        JButton okBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        okBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());
        
        JPanel buttons = new JPanel();
        buttons.add(okBtn);
        buttons.add(cancelBtn);
        form.add(buttons);
        
        add(form);
    }
    
    private void loadData() {
        fullNameField.setText(originalUser.getFullName());
        roleBox.setSelectedItem(originalUser.getRole());
    }
    
    public boolean isConfirmed() { return confirmed; }
    
    public User getUser() {
        User user = new User();
        user.setUserId(originalUser.getUserId());
        user.setUsername(originalUser.getUsername());
        user.setFullName(fullNameField.getText().trim());
        user.setRole((String) roleBox.getSelectedItem());
        
        // ✅ XỬ LÝ REACTIVATE
        if (!originalUser.isActive() && reactivateCheckBox != null && reactivateCheckBox.isSelected()) {
            user.setActive(true); // Reactivate
        } else {
            user.setActive(originalUser.isActive()); // Keep original status
        }
        
        String password = new String(passwordField.getPassword());
        if (!password.trim().isEmpty()) {
            user.setPassword(password);
        }
        
        return user;
    }
}