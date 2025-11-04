package client.ui.student.panels;

import client.controller.StudentExamController;
import client.ui.student.interfaces.StudentDashboardCallbacks;
import client.ui.student.components.ExamRoomsTable;
import client.ui.student.dialogs.ExamPasswordDialog;
import model.ExamRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Available Exams Panel - Restructured to follow admin pattern
 */
public class AvailableExamsPanel extends JPanel {
    
    private StudentDashboardCallbacks callbacks;
    private StudentExamController examController;
    
    // UI Components
    private ExamRoomsTable examRoomsTable;
    private JTextField searchField;
    private JButton joinButton;
    private JButton refreshButton;
    private JButton searchButton;
    private JLabel statusLabel;
    
    // Data
    private List<ExamRoom> currentExams;
    
    public AvailableExamsPanel(StudentDashboardCallbacks callbacks, StudentExamController examController) {
        this.callbacks = callbacks;
        this.examController = examController;
        
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel with search
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        examRoomsTable = new ExamRoomsTable();
        examRoomsTable.setSelectionListener(new ExamRoomsTable.ExamRoomSelectionListener() {
            @Override
            public void onExamRoomSelected(ExamRoom examRoom) {
                joinButton.setEnabled(true);
                updateStatus("Selected: " + examRoom.getRoomName());
            }
            
            @Override
            public void onExamRoomDeselected() {
                joinButton.setEnabled(false);
                updateStatus("Ready");
            }
            
            @Override
            public void onExamRoomDoubleClicked(ExamRoom examRoom) {
                joinSelectedExam();
            }
        });
        
        add(examRoomsTable, BorderLayout.CENTER);
        
        // Bottom panel with buttons and status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Available Exams");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        searchButton = new JButton("🔍 Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> loadAvailableExams());
        searchPanel.add(refreshButton);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        joinButton = new JButton("🎯 Join Exam");
        joinButton.setEnabled(false);
        joinButton.setPreferredSize(new Dimension(150, 40));
        joinButton.setFont(joinButton.getFont().deriveFont(Font.BOLD));
        buttonPanel.add(joinButton);
        
        // Status label
        statusLabel = new JLabel("Loading available exams...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Search field Enter key
        searchField.addActionListener(e -> performSearch());
        
        // Join button
        joinButton.addActionListener(e -> joinSelectedExam());
        
        // Double-click on table
        examRoomsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    joinSelectedExam();
                }
            }
        });
    }
    
    public void loadAvailableExams() {
        updateStatus("Loading available exams...");
        refreshButton.setEnabled(false);
        joinButton.setEnabled(false);
        
        if (callbacks != null) {
            callbacks.onRefreshExamsRequested();
        }
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        updateStatus("Searching...");
        
        if (searchTerm.isEmpty()) {
            loadAvailableExams();
        } else {
            // Filter current exams
            if (currentExams != null) {
                List<ExamRoom> filteredExams = currentExams.stream()
                    .filter(room -> room.getRoomName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   room.getSubjectName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                
                examRoomsTable.setExamRooms(filteredExams);
                updateStatus("Found " + filteredExams.size() + " exams matching: " + searchTerm);
            }
        }
    }
    
    private void joinSelectedExam() {
        ExamRoom selectedRoom = examRoomsTable.getSelectedExamRoom();
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an exam to join.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show password dialog
        ExamPasswordDialog passwordDialog = new ExamPasswordDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            selectedRoom);
        passwordDialog.setVisible(true);
        
        if (passwordDialog.isConfirmed()) {
            String password = passwordDialog.getPassword();
            updateStatus("Joining exam room: " + selectedRoom.getRoomName());
            
            // Join exam through controller
            examController.joinExamRoom(selectedRoom.getRoomId(), password);
            
            if (callbacks != null) {
                callbacks.onJoinExamRequested(selectedRoom);
            }
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        if (callbacks != null) {
            callbacks.updateStatus(message);
        }
    }
    
    // Public methods for external control
    public void setExamRooms(List<ExamRoom> examRooms) {
        this.currentExams = examRooms;
        examRoomsTable.setExamRooms(examRooms);
        updateStatus("Found " + examRooms.size() + " available exams");
        refreshButton.setEnabled(true);
    }
    
    public ExamRoom getSelectedExamRoom() {
        return examRoomsTable.getSelectedExamRoom();
    }
    
    public void clearSelection() {
        examRoomsTable.clearSelection();
    }
}