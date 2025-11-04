// client/ui/admin/components/UsersTable.java
package client.ui.admin.components;

import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class UsersTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private UserSelectionListener selectionListener;
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public interface UserSelectionListener {
        void onUserSelected(User user);
        void onUserDeselected();
        void onUserDoubleClicked(User user);
    }
    
    public UsersTable() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {"ID", "Username", "Full Name", "Role", "Active", "Created Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Integer.class;
                    case 4: return Boolean.class;
                    default: return String.class;
                }
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Full Name
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Role
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Active
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Created Date
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Users List"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    User user = getUserFromRow(selectedRow);
                    selectionListener.onUserSelected(user);
                } else if (selectionListener != null) {
                    selectionListener.onUserDeselected();
                }
            }
        });
        
        // Double-click listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0 && selectionListener != null) {
                        User user = getUserFromRow(selectedRow);
                        selectionListener.onUserDoubleClicked(user);
                    }
                }
            }
        });
    }
    
    public void setUsers(List<User> users) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add users
        for (User user : users) {
            Object[] rowData = {
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),  // ✅ Sử dụng isActive()
                formatDate(user.getCreatedAt())   // ✅ Display as string
            };
            tableModel.addRow(rowData);
        }
    }
    
    public User getSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return getUserFromRow(selectedRow);
        }
        return null;
    }
    
    private User getUserFromRow(int row) {
        User user = new User();
        user.setUserId((Integer) tableModel.getValueAt(row, 0));
        user.setUsername((String) tableModel.getValueAt(row, 1));
        user.setFullName((String) tableModel.getValueAt(row, 2));
        user.setRole((String) tableModel.getValueAt(row, 3));
        user.setActive((Boolean) tableModel.getValueAt(row, 4));  // ✅ Sử dụng setActive
        // Created date as string for display
        return user;
    }
    
 // client/ui/admin/components/UsersTable.java
    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "N/A";
        }
        
        try {
            // ✅ Modern approach - no Date class needed
            LocalDateTime dateTime = LocalDateTime.parse(dateString, INPUT_FORMATTER);
            return dateTime.format(OUTPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            // Fallback
            return dateString.length() >= 16 ? dateString.substring(0, 16) : dateString;
        }
    }
    
    public void setSelectionListener(UserSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }
    
    public void clearSelection() {
        table.clearSelection();
    }
}