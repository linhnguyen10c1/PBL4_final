package client.ui.admin.panels;

import client.controller.UserController;
import client.ui.admin.components.UsersTable;
import client.ui.admin.dialogs.AddUserDialog;
import client.ui.admin.dialogs.EditUserDialog;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel implements UsersTable.UserSelectionListener  {
    
    private UserController userController;
    private UsersTable usersTable;
    private DefaultTableModel tableModel;
    private JButton addBtn, editBtn, deleteBtn, refreshBtn;
    private JTextField searchField;
    
    public UserManagementPanel(UserController userController) {
        this.userController = userController;
        initUI();
        loadUsers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(5, 10));
        
        // Top buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        refreshBtn = new JButton("Refresh");
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        deleteBtn.setForeground(Color.RED);
        
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(deleteBtn);
        topPanel.add(refreshBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        usersTable = new UsersTable();
        usersTable.setSelectionListener(this); 
        add(usersTable, BorderLayout.CENTER);
        
        addBtn.addActionListener(e -> addUser());
        editBtn.addActionListener(e -> editUser());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUsers());
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
    }
    
    @Override
    public void onUserSelected(User user) {
        editBtn.setEnabled(true);
        deleteBtn.setEnabled(user.isActive());
    }
    
    @Override
    public void onUserDeselected() {
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }
    
    @Override
    public void onUserDoubleClicked(User user) {
        // Double click = edit user
        editUser();
    }
    
    private void addUser() {
        AddUserDialog dialog = new AddUserDialog((JFrame) getTopLevelAncestor());
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (userController.createUser(dialog.getUser())) {
//                JOptionPane.showMessageDialog(this, "User created!");
                loadUsers();
            }
        }
    }
    
    private void editUser() {
    	 User selectedUser = usersTable.getSelectedUser();
    	 if (selectedUser == null) return;
        
        EditUserDialog dialog = new EditUserDialog((JFrame) getTopLevelAncestor(), selectedUser);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            if (userController.updateUser(dialog.getUser())) {
//                JOptionPane.showMessageDialog(this, "User updated!");
                loadUsers();
            }
        }
    }
    
    private void deleteUser() {
    	User selectedUser = usersTable.getSelectedUser();
    	if (selectedUser == null) return;
        
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Delete user: " + selectedUser.getUsername() + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            if (userController.deleteUser(selectedUser.getUserId(), selectedUser.getUsername())) {
//                JOptionPane.showMessageDialog(this, "User deleted!");
                loadUsers();
            }
        }
    }
    
    private void search() {
        String keyword = searchField.getText().trim();
        List<User> users = keyword.isEmpty() ? 
            userController.getAllUsers() : 
            userController.searchUsers(keyword);
        usersTable.setUsers(users);
    }
    
    private void loadUsers() {
    	List<User> users = userController.getAllUsers();
        usersTable.setUsers(users);
    }
    
}