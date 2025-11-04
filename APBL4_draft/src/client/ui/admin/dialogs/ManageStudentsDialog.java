package client.ui.admin.dialogs;

import client.controller.ExamRoomController;
import model.ExamRoom;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Manage Students Dialog - Dialog for managing students in exam room
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:40:18 UTC
 */
public class ManageStudentsDialog extends JDialog {
    
    private ExamRoom examRoom;
    private List<User> allStudents;
    private ExamRoomController examRoomController;
    
    // UI Components
    private JTable availableStudentsTable;
    private JTable assignedStudentsTable;
    private DefaultTableModel availableTableModel;
    private DefaultTableModel assignedTableModel;
    
    private JButton addButton;
    private JButton removeButton;
    private JButton addAllButton;
    private JButton removeAllButton;
    private JTextField searchField;
    
    // Current state
    private List<User> currentAssignedStudents;
    
    public ManageStudentsDialog(JFrame parent, ExamRoom examRoom, 
                               List<User> allStudents, ExamRoomController examRoomController) {
        super(parent, "Manage Students - " + examRoom.getRoomName(), true);
        this.examRoom = examRoom;
        this.allStudents = allStudents != null ? allStudents : new ArrayList<>();
        this.examRoomController = examRoomController;
        this.currentAssignedStudents = new ArrayList<>();
        
        initializeUI();
        setupEventHandlers();
        loadStudentData();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Manage Students for: " + examRoom.getRoomName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchPanel.add(searchField);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Available students panel (left)
        JPanel availablePanel = createAvailableStudentsPanel();
        
        // Control buttons panel (center)
        JPanel controlPanel = createControlPanel();
        
        // Assigned students panel (right)
        JPanel assignedPanel = createAssignedStudentsPanel();
        
        // Layout
        panel.add(availablePanel, BorderLayout.WEST);
        panel.add(controlPanel, BorderLayout.CENTER);
        panel.add(assignedPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createAvailableStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Students"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Table
        String[] columnNames = {"ID", "Username", "Full Name"};
        availableTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        availableStudentsTable = new JTable(availableTableModel);
        availableStudentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Set column widths
        availableStudentsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        availableStudentsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        availableStudentsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(availableStudentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAssignedStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Assigned Students"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Table
        String[] columnNames = {"ID", "Username", "Full Name"};
        assignedTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        assignedStudentsTable = new JTable(assignedTableModel);
        assignedStudentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Set column widths
        assignedStudentsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        assignedStudentsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        assignedStudentsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(assignedStudentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(120, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        addButton = new JButton("Add >");
        gbc.gridy = 0;
        panel.add(addButton, gbc);
        
        removeButton = new JButton("< Remove");
        gbc.gridy = 1;
        panel.add(removeButton, gbc);
        
        // Separator
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 15, 5);
        panel.add(new JSeparator(), gbc);
        
        addAllButton = new JButton("Add All >>");
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(addAllButton, gbc);
        
        removeAllButton = new JButton("<< Remove All");
        gbc.gridy = 4;
        panel.add(removeAllButton, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterAvailableStudents();
            }
        });
        
        // Control buttons
        addButton.addActionListener(e -> addSelectedStudents());
        removeButton.addActionListener(e -> removeSelectedStudents());
        addAllButton.addActionListener(e -> addAllStudents());
        removeAllButton.addActionListener(e -> removeAllStudents());
        
        // Double-click handlers
        availableStudentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedStudents();
                }
            }
        });
        
        assignedStudentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeSelectedStudents();
                }
            }
        });
    }
    
    private void loadStudentData() {
        // Load currently assigned students
        currentAssignedStudents.clear();
        for (Integer studentId : examRoom.getAllowedStudentIds()) {
            User student = findStudentById(studentId);
            if (student != null) {
                currentAssignedStudents.add(student);
            }
        }
        
        updateTables();
    }
    
    private void updateTables() {
        // Update assigned students table
        assignedTableModel.setRowCount(0);
        for (User student : currentAssignedStudents) {
            Object[] row = {student.getUserId(), student.getUsername(), student.getFullName()};
            assignedTableModel.addRow(row);
        }
        
        // Update available students table
        filterAvailableStudents();
    }
    
    private void filterAvailableStudents() {
        availableTableModel.setRowCount(0);
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        for (User student : allStudents) {
            // Skip if already assigned
            if (isStudentAssigned(student.getUserId())) {
                continue;
            }
            
            // Apply search filter
            if (!searchTerm.isEmpty()) {
                String searchText = (student.getUsername() + " " + student.getFullName()).toLowerCase();
                if (!searchText.contains(searchTerm)) {
                    continue;
                }
            }
            
            Object[] row = {student.getUserId(), student.getUsername(), student.getFullName()};
            availableTableModel.addRow(row);
        }
    }
    
    private void addSelectedStudents() {
        int[] selectedRows = availableStudentsTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        for (int row : selectedRows) {
            int studentId = (Integer) availableTableModel.getValueAt(row, 0);
            User student = findStudentById(studentId);
            if (student != null && !isStudentAssigned(studentId)) {
                currentAssignedStudents.add(student);
            }
        }
        
        updateTables();
    }
    
    private void removeSelectedStudents() {
        int[] selectedRows = assignedStudentsTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        // Remove in reverse order to maintain indices
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int row = selectedRows[i];
            int studentId = (Integer) assignedTableModel.getValueAt(row, 0);
            currentAssignedStudents.removeIf(student -> student.getUserId() == studentId);
        }
        
        updateTables();
    }
    
    private void addAllStudents() {
        for (int i = 0; i < availableTableModel.getRowCount(); i++) {
            int studentId = (Integer) availableTableModel.getValueAt(i, 0);
            User student = findStudentById(studentId);
            if (student != null && !isStudentAssigned(studentId)) {
                currentAssignedStudents.add(student);
            }
        }
        
        updateTables();
    }
    
    private void removeAllStudents() {
        currentAssignedStudents.clear();
        updateTables();
    }
    
    private void saveChanges() {
        // Get current assigned student IDs
        List<Integer> studentIds = currentAssignedStudents.stream()
            .map(User::getUserId)
            .collect(Collectors.toList());
        
        // Update the exam room on server
        boolean success = examRoomController.addStudentsToRoom(examRoom.getRoomId(), studentIds);
        
        if (success) {
            dispose();
        }
    }
    
    private User findStudentById(int studentId) {
        return allStudents.stream()
            .filter(student -> student.getUserId() == studentId)
            .findFirst()
            .orElse(null);
    }
    
    private boolean isStudentAssigned(int studentId) {
        return currentAssignedStudents.stream()
            .anyMatch(student -> student.getUserId() == studentId);
    }
}