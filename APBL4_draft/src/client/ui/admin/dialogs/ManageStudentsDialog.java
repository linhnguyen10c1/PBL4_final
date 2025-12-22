package client.ui.admin.dialogs;

import client.controller.ExamRoomController;
import model.ExamRoom;
import model.User;
import model.StudentExamStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
    private JLabel refreshStatusLabel;
    
    // Current state
    private List<User> currentAssignedStudents;
    
    // Status tracking
    private Map<Integer, StudentExamStatus> studentStatusMap;
    private Timer autoRefreshTimer;
    private static final int REFRESH_INTERVAL_MS = 5000; // 5 seconds
    
    public ManageStudentsDialog(JFrame parent, ExamRoom examRoom, 
                               List<User> allStudents, ExamRoomController examRoomController) {
        super(parent, "Manage Students - " + examRoom.getRoomName(), true);
        this.examRoom = examRoom;
        this.allStudents = allStudents != null ? allStudents : new ArrayList<>();
        this.examRoomController = examRoomController;
        this.currentAssignedStudents = new ArrayList<>();
        this.studentStatusMap = new HashMap<>();
        
        initializeUI();
        setupEventHandlers();
        loadFreshStudentData();
        loadStudentStatuses(); // Load statuses after student data
        startAutoRefresh();
        
        setSize(900, 600); // Wider to accommodate Status column
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
    
    private void loadFreshStudentData() {
        try {
            System.out.println("🔍 [ManageStudentsDialog] Loading fresh student data for room: " + examRoom.getRoomId());
            
            // Get fresh ExamRoom data from server
            List<ExamRoom> allRooms = examRoomController.getAllExamRooms();
            if (allRooms != null) {
                ExamRoom freshRoom = allRooms.stream()
                    .filter(room -> room.getRoomId() == examRoom.getRoomId())
                    .findFirst()
                    .orElse(examRoom);
                
                this.examRoom = freshRoom;
                System.out.println("✅ [ManageStudentsDialog] Fresh room data loaded.Assigned students: " + 
                                 freshRoom.getAllowedStudentIds().size());
            }
            
            // Load currently assigned students from fresh data
            currentAssignedStudents.clear();
            for (Integer studentId : examRoom.getAllowedStudentIds()) {
                User student = findStudentById(studentId);
                if (student != null) {
                    currentAssignedStudents.add(student);
                    System.out.println("✅ [ManageStudentsDialog] Loaded assigned student: " + student.getUsername());
                } else {
                    System.err.println("❌ [ManageStudentsDialog] Student ID " + studentId + " not found in allStudents list");
                }
            }
            
            System.out.println("✅ [ManageStudentsDialog] Total assigned students loaded: " + currentAssignedStudents.size());
            
            updateTables();
            
        } catch (Exception e) {
            System.err.println("❌ [ManageStudentsDialog] Error loading fresh student data: " + e.getMessage());
            e.printStackTrace();
            loadStudentData();
        }
    }
    
    /**
     * Load student statuses from server
     */
    private void loadStudentStatuses() {
        try {
            System.out.println("📊 [ManageStudentsDialog] Loading student statuses...");
            
            List<StudentExamStatus> statuses = examRoomController.getStudentStatusesForRoom(examRoom.getRoomId());
            
            if (statuses != null) {
                studentStatusMap.clear();
                for (StudentExamStatus status : statuses) {
                    studentStatusMap.put(status.getStudentId(), status);
                }
                System.out.println("✅ [ManageStudentsDialog] Loaded " + statuses.size() + " student statuses");
                
                // Update only the status column without recreating the table
                updateStatusColumn();
            } else {
                System.err.println("⚠️ [ManageStudentsDialog] Failed to load student statuses");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ManageStudentsDialog] Error loading student statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update only the status column in assigned students table
     */
    private void updateStatusColumn() {
        SwingUtilities.invokeLater(() -> {
            for (int row = 0; row < assignedTableModel.getRowCount(); row++) {
                int studentId = (Integer) assignedTableModel.getValueAt(row, 0);
                StudentExamStatus status = studentStatusMap.get(studentId);
                
                String displayStatus = (status != null) ? status.getDisplayStatus() : "Not Started";
                assignedTableModel.setValueAt(displayStatus, row, 3); // Column 3 = Status
            }
            
            // Update refresh status label
            if (refreshStatusLabel != null) {
                refreshStatusLabel.setText("Last refresh: " + 
                    new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            }
        });
    }
    
    /**
     * Start auto-refresh timer (every 5 seconds)
     */
    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> {
            // Run in background to avoid blocking UI
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    loadStudentStatuses();
                    return null;
                }
            }.execute();
        });
        autoRefreshTimer.start();
        System.out.println("🔄 [ManageStudentsDialog] Auto-refresh started (every " + (REFRESH_INTERVAL_MS/1000) + "s)");
    }
    
    /**
     * Stop auto-refresh timer
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
            System.out.println("🛑 [ManageStudentsDialog] Auto-refresh stopped");
        }
    }
    
    @Override
    public void dispose() {
        stopAutoRefresh();
        super.dispose();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Manage Students for:  " + examRoom.getRoomName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Right side:  Search + Refresh status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Refresh status
        refreshStatusLabel = new JLabel("Auto-refresh: ON (5s)");
        refreshStatusLabel.setForeground(new Color(0, 128, 0));
        refreshStatusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        rightPanel.add(refreshStatusLabel);
        
        rightPanel.add(Box.createHorizontalStrut(20));
        
        // Search
        rightPanel.add(new JLabel("Search: "));
        searchField = new JTextField(15);
        rightPanel.add(searchField);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
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
        panel.setPreferredSize(new Dimension(280, 0));
        
        // Table - 3 columns (no status for available students)
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
        availableStudentsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        availableStudentsTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        availableStudentsTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        
        JScrollPane scrollPane = new JScrollPane(availableStudentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAssignedStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Assigned Students"));
        panel.setPreferredSize(new Dimension(420, 0)); // Wider for Status column
        
        // Table - 4 columns including Status
        String[] columnNames = {"ID", "Username", "Full Name", "Status"};
        assignedTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        assignedStudentsTable = new JTable(assignedTableModel);
        assignedStudentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Set column widths
        assignedStudentsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        assignedStudentsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        assignedStudentsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        assignedStudentsTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        
        // Custom renderer for Status column
        assignedStudentsTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(assignedStudentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info label at bottom
        JLabel infoLabel = new JLabel("<html><i>🔒 Students with status 'In Progress' or 'Submitted' cannot be removed</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Custom cell renderer for Status column
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = value != null ? value.toString() : "Not Started";
            setHorizontalAlignment(SwingConstants.LEFT);
            
            if (! isSelected) {
                if (status.equals("Not Started")) {
                    setForeground(Color.GRAY);
                    setText("⚪ " + status);
                } else if (status.equals("In Progress")) {
                    setForeground(new Color(255, 140, 0)); // Orange
                    setText("🟡 " + status);
                } else if (status.startsWith("Submitted: ")) {
                    setForeground(new Color(0, 150, 0)); // Green
                    setText("✅ " + status);
                } else if (status.startsWith("Auto-submitted:")) {
                    setForeground(new Color(255, 100, 0)); // Dark orange
                    setText("⏰ " + status);
                } else {
                    setForeground(Color.BLACK);
                    setText(status);
                }
            } else {
                setForeground(Color.WHITE);
                setText(status);
            }
            
            return this;
        }
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
        
        JButton refreshButton = new JButton("🔄 Refresh Now");
        refreshButton.addActionListener(e -> {
            loadStudentStatuses();
            JOptionPane.showMessageDialog(this, "Status refreshed!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(refreshButton);
        
        panel.add(Box.createHorizontalStrut(20));
        
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
        System.out.println("🔍 [ManageStudentsDialog] Loading student data (fallback method)");
        System.out.println("  - ExamRoom allowed students: " + examRoom.getAllowedStudentIds());
        
        currentAssignedStudents.clear();
        for (Integer studentId : examRoom.getAllowedStudentIds()) {
            User student = findStudentById(studentId);
            if (student != null) {
                currentAssignedStudents.add(student);
                System.out.println("  - Added assigned student: " + student.getUsername());
            } else {
                System.err.println("  - Student ID " + studentId + " not found");
            }
        }
        
        System.out.println("✅ [ManageStudentsDialog] Loaded " + currentAssignedStudents.size() + " assigned students");
        updateTables();
    }
    
    private void updateTables() {
        // Update assigned students table with status
        assignedTableModel.setRowCount(0);
        for (User student : currentAssignedStudents) {
            StudentExamStatus status = studentStatusMap.get(student.getUserId());
            String displayStatus = (status != null) ? status.getDisplayStatus() : "Not Started";
            
            Object[] row = {
                student.getUserId(), 
                student.getUsername(), 
                student.getFullName(),
                displayStatus
            };
            assignedTableModel.addRow(row);
        }
        
        // Update available students table
        filterAvailableStudents();
    }
    
    private void filterAvailableStudents() {
        availableTableModel.setRowCount(0);
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        int availableCount = 0;
        for (User student : allStudents) {
            if (isStudentAssigned(student.getUserId())) {
                continue;
            }
            
            if (! searchTerm.isEmpty()) {
                String searchText = (student.getUsername() + " " + student.getFullName()).toLowerCase();
                if (! searchText.contains(searchTerm)) {
                    continue;
                }
            }
            
            Object[] row = {student.getUserId(), student.getUsername(), student.getFullName()};
            availableTableModel.addRow(row);
            availableCount++;
        }
        
        System.out.println("✅ [ManageStudentsDialog] Available table updated:  " + availableCount + " rows");
    }
    
    private void addSelectedStudents() {
        int[] selectedRows = availableStudentsTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        for (int row : selectedRows) {
            int studentId = (Integer) availableTableModel.getValueAt(row, 0);
            User student = findStudentById(studentId);
            if (student != null && !isStudentAssigned(studentId)) {
                currentAssignedStudents.add(student);
                // New students have no session yet
                studentStatusMap.put(studentId, new StudentExamStatus(studentId, null, null, examRoom.getTotalScore()));
            }
        }
        
        updateTables();
    }
    
    private void removeSelectedStudents() {
        int[] selectedRows = assignedStudentsTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        List<String> cannotRemove = new ArrayList<>();
        List<Integer> toRemove = new ArrayList<>();
        
        // Check each selected student
        for (int row : selectedRows) {
            int studentId = (Integer) assignedTableModel.getValueAt(row, 0);
            String username = (String) assignedTableModel.getValueAt(row, 1);
            
            StudentExamStatus status = studentStatusMap.get(studentId);
            
            if (status != null && !status.canBeRemoved()) {
                cannotRemove.add(username + " (" + status.getDisplayStatus() + ")");
            } else {
                toRemove.add(studentId);
            }
        }
        
        // Show warning if some cannot be removed
        if (!cannotRemove.isEmpty()) {
            String message = "Cannot remove the following students who have started or submitted the exam:\n\n";
            for (String name : cannotRemove) {
                message += "• " + name + "\n";
            }
            if (!toRemove.isEmpty()) {
                message += "\n" + toRemove.size() + " other student(s) will be removed.";
            }
            
            JOptionPane.showMessageDialog(this, message, "Cannot Remove", JOptionPane.WARNING_MESSAGE);
        }
        
        // Remove allowed students
        for (Integer studentId : toRemove) {
            currentAssignedStudents.removeIf(student -> student.getUserId() == studentId);
            studentStatusMap.remove(studentId);
        }
        
        if (!toRemove.isEmpty()) {
            updateTables();
        }
    }
    
    private void addAllStudents() {
        for (int i = 0; i < availableTableModel.getRowCount(); i++) {
            int studentId = (Integer) availableTableModel.getValueAt(i, 0);
            User student = findStudentById(studentId);
            if (student != null && !isStudentAssigned(studentId)) {
                currentAssignedStudents.add(student);
                studentStatusMap.put(studentId, new StudentExamStatus(studentId, null, null, examRoom.getTotalScore()));
            }
        }
        
        updateTables();
    }
    
    private void removeAllStudents() {
        List<String> cannotRemove = new ArrayList<>();
        List<User> toRemove = new ArrayList<>();
        
        // Check each assigned student
        for (User student : currentAssignedStudents) {
            StudentExamStatus status = studentStatusMap.get(student.getUserId());
            
            if (status != null && ! status.canBeRemoved()) {
                cannotRemove.add(student.getUsername() + " (" + status.getDisplayStatus() + ")");
            } else {
                toRemove.add(student);
            }
        }
        
        // Show warning if some cannot be removed
        if (!cannotRemove.isEmpty()) {
            String message = "Cannot remove the following students who have started or submitted the exam:\n\n";
            for (String name : cannotRemove) {
                message += "• " + name + "\n";
            }
            message += "\n" + toRemove.size() + " student(s) will be removed.";
            
            int confirm = JOptionPane.showConfirmDialog(this, message, 
                "Partial Remove", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.OK_OPTION) {
                return;
            }
        }
        
        // Remove allowed students
        for (User student :  toRemove) {
            currentAssignedStudents.remove(student);
            studentStatusMap.remove(student.getUserId());
        }
        
        updateTables();
    }
    
    private void saveChanges() {
        List<Integer> studentIds = currentAssignedStudents.stream()
            .map(User:: getUserId)
            .collect(Collectors.toList());
        
        boolean success = examRoomController.addStudentsToRoom(examRoom.getRoomId(), studentIds);
        
        if (success) {
            examRoom.setAllowedStudentIds(studentIds);
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