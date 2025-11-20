package client.ui.admin.panels;

import client.controller.ExamRoomController;
import client.ui.admin.interfaces.AdminDashboardCallbacks;
import client.ui.admin.dialogs.AddExamRoomDialog;
import client.ui.admin.dialogs.EditExamRoomDialog;
import client.ui.admin.dialogs.ManageStudentsDialog;
import client.ui.admin.components.ExamRoomsTable;
import model.ExamRoom;
import model.Subject;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * ExamRoom Management Panel - UI for managing exam rooms
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:36:16 UTC
 */
public class ExamRoomManagementPanel extends JPanel implements ExamRoomController.ExamRoomListener {
    
    private AdminDashboardCallbacks callbacks;
    private ExamRoomController examRoomController;
    
    // UI Components
    private ExamRoomsTable examRoomsTable;
    private JTextField searchField;
    private JButton createRoomButton;
    private JButton editRoomButton;
    private JButton deleteRoomButton;
    private JButton manageStudentsButton;
    private JButton refreshButton;
    
    // Data
    private List<ExamRoom> examRooms;
    private List<Subject> subjects;
    private List<User> students;
    
    public ExamRoomManagementPanel(AdminDashboardCallbacks callbacks, ExamRoomController examRoomController) {
        this.callbacks = callbacks;
        this.examRoomController = examRoomController;
        this.examRooms = new ArrayList<>();
        this.subjects = new ArrayList<>();
        this.students = new ArrayList<>();
        
        // Set listener
        examRoomController.setExamRoomListener(this);
        
        initializeUI();
        setupEventHandlers();
        loadInitialData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        examRoomsTable = new ExamRoomsTable();
        add(examRoomsTable, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Exam Room Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        createRoomButton = new JButton("Create Room");
        createRoomButton.setIcon(new ImageIcon("resources/icons/add.png"));
        panel.add(createRoomButton);
        
        editRoomButton = new JButton("Edit Room");
        editRoomButton.setIcon(new ImageIcon("resources/icons/edit.png"));
        editRoomButton.setEnabled(false);
        panel.add(editRoomButton);
        
        deleteRoomButton = new JButton("Delete Room");
        deleteRoomButton.setIcon(new ImageIcon("resources/icons/delete.png"));
        deleteRoomButton.setEnabled(false);
        panel.add(deleteRoomButton);
        
        manageStudentsButton = new JButton("Manage Students");
        manageStudentsButton.setIcon(new ImageIcon("resources/icons/users.png"));
        manageStudentsButton.setEnabled(false);
        panel.add(manageStudentsButton);
        
        // Separator
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        refreshButton = new JButton("Refresh");
        refreshButton.setIcon(new ImageIcon("resources/icons/refresh.png"));
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Table selection listener
        examRoomsTable.setSelectionListener(new ExamRoomsTable.ExamRoomSelectionListener() {
            @Override
            public void onExamRoomSelected(ExamRoom examRoom) {
                boolean hasSelection = examRoom != null;
                editRoomButton.setEnabled(hasSelection);
                deleteRoomButton.setEnabled(hasSelection);
                manageStudentsButton.setEnabled(hasSelection);
            }
            
            @Override
            public void onExamRoomDeselected() {
                editRoomButton.setEnabled(false);
                deleteRoomButton.setEnabled(false);
                manageStudentsButton.setEnabled(false);
            }
            
            @Override
            public void onExamRoomDoubleClicked(ExamRoom examRoom) {
                showEditRoomDialog();
            }
        });
        
        // Search functionality
        searchField.addActionListener(e -> performSearch());
        
        // Button listeners
        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        editRoomButton.addActionListener(e -> showEditRoomDialog());
        deleteRoomButton.addActionListener(e -> deleteSelectedRoom());
        manageStudentsButton.addActionListener(e -> showManageStudentsDialog());
        refreshButton.addActionListener(e -> loadInitialData());
    }
    
    private void loadInitialData() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Loading exam rooms...");
            examRoomController.getAllExamRooms();
            examRoomController.getAllSubjects();
            examRoomController.getAllStudents();
        });
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        updateStatus("Searching...");
        
        if (searchTerm.isEmpty()) {
            examRoomController.getAllExamRooms();
        } else {
            examRoomController.searchExamRooms(searchTerm);
        }
    }
    
    private void showCreateRoomDialog() {
        AddExamRoomDialog dialog = new AddExamRoomDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            subjects
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ExamRoom newRoom = dialog.getExamRoom();
            examRoomController.createExamRoom(newRoom);
        }
    }
    
    private void showEditRoomDialog() {
        ExamRoom selectedRoom = examRoomsTable.getSelectedExamRoom();
        if (selectedRoom == null) return;
        
        EditExamRoomDialog dialog = new EditExamRoomDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            selectedRoom,
            subjects
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ExamRoom updatedRoom = dialog.getExamRoom();
            examRoomController.updateExamRoom(updatedRoom);
        }
    }
    
    private void deleteSelectedRoom() {
        ExamRoom selectedRoom = examRoomsTable.getSelectedExamRoom();
        if (selectedRoom == null) return;
        
        examRoomController.deleteExamRoom(selectedRoom.getRoomId(), selectedRoom.getRoomName());
    }
    
    private void showManageStudentsDialog() {
        ExamRoom selectedRoom = examRoomsTable.getSelectedExamRoom();
        if (selectedRoom == null) return;
        
        List<User> students = examRoomController.getAllStudents();
        ManageStudentsDialog dialog = new ManageStudentsDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            selectedRoom,
            students,
            examRoomController
        );
        
        dialog.setVisible(true);
    }
    
    private void updateStatus(String message) {
        if (callbacks != null) {
            callbacks.updateStatus(message);
        }
    }
    
    // ExamRoomController.ExamRoomListener implementation
    @Override
    public void onExamRoomsLoaded(List<ExamRoom> examRooms) {
        this.examRooms = examRooms;
        SwingUtilities.invokeLater(() -> {
            examRoomsTable.setExamRooms(examRooms);
            updateStatus("Loaded " + examRooms.size() + " exam rooms");
        });
    }
    
    @Override
    public void onExamRoomCreated(ExamRoom examRoom) {
        examRooms.add(examRoom);
        SwingUtilities.invokeLater(() -> {
            examRoomsTable.setExamRooms(examRooms);
            updateStatus("Exam room created: " + examRoom.getRoomName());
        });
    }
    
    @Override
    public void onExamRoomUpdated(ExamRoom examRoom) {
        // Find and update the room in the list
        for (int i = 0; i < examRooms.size(); i++) {
            if (examRooms.get(i).getRoomId() == examRoom.getRoomId()) {
                examRooms.set(i, examRoom);
                break;
            }
        }
        SwingUtilities.invokeLater(() -> {
            examRoomsTable.setExamRooms(examRooms);
            updateStatus("Exam room updated: " + examRoom.getRoomName());
        });
    }
    
    @Override
    public void onExamRoomDeleted(int roomId) {
        examRooms.removeIf(room -> room.getRoomId() == roomId);
        SwingUtilities.invokeLater(() -> {
            examRoomsTable.setExamRooms(examRooms);
            updateStatus("Exam room deleted");
        });
    }
    
    @Override
    public void onSubjectsLoaded(List<Subject> subjects) {
        this.subjects = subjects;
    }
    
    @Override
    public void onStudentsLoaded(List<User> students) {
        this.students = students;
    }
    
    @Override
    public void onStudentsUpdated() {
        // Reload exam rooms to get updated student lists
        examRoomController.getAllExamRooms();
    }
    
    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            updateStatus("Error: " + message);
        });
    }
}