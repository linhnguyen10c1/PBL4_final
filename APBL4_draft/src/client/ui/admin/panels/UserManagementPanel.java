package client.ui.admin.panels;

import client.controller.UserController;
import client.ui.admin.components.UsersTable;
import client.ui.admin.dialogs.AddUserDialog;
import client.ui.admin.dialogs.EditUserDialog;
import model.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementPanel extends JPanel implements UsersTable.UserSelectionListener {
    
    private UserController userController;
    private UsersTable usersTable;
    private List<User> currentUsers;
    
    private JButton addBtn, editBtn, deleteBtn, refreshBtn;
    private JTextField searchField;
    private JComboBox<String> statusFilterComboBox;
    
    public UserManagementPanel(UserController userController) {
        this.userController = userController;
        initUI();
        loadUsers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Row 1: Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBtn = new JButton("Add User");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Deactivate");
        refreshBtn = new JButton("Refresh");
        
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        deleteBtn.setForeground(Color.RED);
        
        actionPanel.add(addBtn); actionPanel.add(editBtn); 
        actionPanel.add(deleteBtn); actionPanel.add(refreshBtn);

        // Row 2: Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Filters"));
        
        filterPanel.add(new JLabel("Status:"));
        statusFilterComboBox = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        statusFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(statusFilterComboBox);

        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        filterPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        filterPanel.add(searchBtn);

        topContainer.add(actionPanel);
        topContainer.add(filterPanel);
        add(topContainer, BorderLayout.NORTH);
        
        usersTable = new UsersTable();
        usersTable.setSelectionListener(this); 
        add(usersTable, BorderLayout.CENTER);
        
        // Listeners
        addBtn.addActionListener(e -> addUser());
        editBtn.addActionListener(e -> editUser());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUsers());
        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
    }

    private void applyFilters() {
        if (currentUsers == null) return;
        String status = (String) statusFilterComboBox.getSelectedItem();
        
        List<User> filtered = currentUsers.stream()
            .filter(u -> {
                if ("Active".equals(status)) return u.isActive();
                if ("Inactive".equals(status)) return !u.isActive();
                return true;
            })
            .collect(Collectors.toList());
        usersTable.setUsers(filtered);
    }

    private void loadUsers() {
        new Thread(() -> {
            List<User> users = userController.getAllUsers();
            SwingUtilities.invokeLater(() -> {
                this.currentUsers = users;
                applyFilters();
            });
        }).start();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        new Thread(() -> {
            List<User> results = keyword.isEmpty() ? userController.getAllUsers() : userController.searchUsers(keyword);
            SwingUtilities.invokeLater(() -> {
                this.currentUsers = results;
                applyFilters();
            });
        }).start();
    }

    @Override public void onUserSelected(User u) { editBtn.setEnabled(true); deleteBtn.setEnabled(u.isActive()); }
    @Override public void onUserDeselected() { editBtn.setEnabled(false); deleteBtn.setEnabled(false); }
    @Override public void onUserDoubleClicked(User u) { editUser(); }

    private void addUser() {
        AddUserDialog dialog = new AddUserDialog((JFrame) getTopLevelAncestor());
        dialog.setVisible(true);
        if (dialog.isConfirmed() && userController.createUser(dialog.getUser())) loadUsers();
    }

    private void editUser() {
        User selected = usersTable.getSelectedUser();
        if (selected == null) return;
        EditUserDialog dialog = new EditUserDialog((JFrame) getTopLevelAncestor(), selected);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && userController.updateUser(dialog.getUser())) loadUsers();
    }

    private void deleteUser() {
        User selected = usersTable.getSelectedUser();
        if (selected == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Deactivate user: " + selected.getUsername() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION && userController.deleteUser(selected.getUserId(), selected.getUsername())) loadUsers();
    }
}