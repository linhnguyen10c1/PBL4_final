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
import java.util.stream.Collectors;

public class ExamRoomManagementPanel extends JPanel implements ExamRoomController.ExamRoomListener {
    
    private AdminDashboardCallbacks callbacks;
    private ExamRoomController examRoomController;
    
    private ExamRoomsTable examRoomsTable;
    private JTextField searchField;
    private JComboBox<String> statusFilterComboBox;
    private JButton createRoomButton, editRoomButton, deleteRoomButton, manageStudentsButton, refreshButton;
    
    private List<ExamRoom> allExamRooms;
    private List<Subject> subjects = new ArrayList<>();
    
    public ExamRoomManagementPanel(AdminDashboardCallbacks callbacks, ExamRoomController examRoomController) {
        this.callbacks = callbacks;
        this.examRoomController = examRoomController;
        examRoomController.setExamRoomListener(this);
        
        initializeUI();
        loadInitialData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Row 1: Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createRoomButton = new JButton("Create Room");
        editRoomButton = new JButton("Edit Room");
        deleteRoomButton = new JButton("Delete Room");
        manageStudentsButton = new JButton("Manage Students");
        refreshButton = new JButton("Refresh");
        
        editRoomButton.setEnabled(false);
        deleteRoomButton.setEnabled(false);
        manageStudentsButton.setEnabled(false);
        
        actionPanel.add(createRoomButton); actionPanel.add(editRoomButton); 
        actionPanel.add(deleteRoomButton); actionPanel.add(manageStudentsButton);
        actionPanel.add(new JSeparator(JSeparator.VERTICAL));
        actionPanel.add(refreshButton);

        // Row 2: Filters & Search
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftFilter.add(new JLabel("Status: "));
        statusFilterComboBox = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
        statusFilterComboBox.addActionListener(e -> applyLocalFilter());
        leftFilter.add(statusFilterComboBox);

        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightSearch.add(new JLabel("Search: "));
        searchField = new JTextField(20);
        rightSearch.add(searchField);
        JButton searchBtn = new JButton("Search");
        rightSearch.add(searchBtn);

        filterPanel.add(leftFilter, BorderLayout.WEST);
        filterPanel.add(rightSearch, BorderLayout.EAST);

        topContainer.add(actionPanel);
        topContainer.add(filterPanel);
        add(topContainer, BorderLayout.NORTH);

        examRoomsTable = new ExamRoomsTable();
        add(new JScrollPane(examRoomsTable), BorderLayout.CENTER);
        
        // Listeners
        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        editRoomButton.addActionListener(e -> showEditRoomDialog());
        deleteRoomButton.addActionListener(e -> deleteSelectedRoom());
        manageStudentsButton.addActionListener(e -> showManageStudentsDialog());
        refreshButton.addActionListener(e -> loadInitialData());

        examRoomsTable.setSelectionListener(new ExamRoomsTable.ExamRoomSelectionListener() {
            @Override public void onExamRoomSelected(ExamRoom r) {
                editRoomButton.setEnabled(true); deleteRoomButton.setEnabled(true); manageStudentsButton.setEnabled(true);
            }
            @Override public void onExamRoomDeselected() {
                editRoomButton.setEnabled(false); deleteRoomButton.setEnabled(false); manageStudentsButton.setEnabled(false);
            }
            @Override public void onExamRoomDoubleClicked(ExamRoom r) { showEditRoomDialog(); }
        });
    }

    private void applyLocalFilter() {
        if (allExamRooms == null) return;
        String status = (String) statusFilterComboBox.getSelectedItem();
        List<ExamRoom> filtered = allExamRooms.stream()
            .filter(r -> {
                if ("Active".equals(status)) return r.isActive();
                if ("Inactive".equals(status)) return !r.isActive();
                return true;
            })
            .collect(Collectors.toList());
        examRoomsTable.setExamRooms(filtered);
    }

    private void loadInitialData() {
        updateStatus("Loading data...");
        examRoomController.getAllExamRooms();
        examRoomController.getAllSubjects();
        examRoomController.getAllStudents();
    }

    private void performSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) examRoomController.getAllExamRooms();
        else examRoomController.searchExamRooms(term);
    }

    @Override public void onExamRoomsLoaded(List<ExamRoom> rooms) {
        this.allExamRooms = rooms;
        SwingUtilities.invokeLater(this::applyLocalFilter);
        updateStatus("Loaded " + rooms.size() + " rooms");
    }

    @Override public void onSubjectsLoaded(List<Subject> subjects) { this.subjects = subjects; }
    @Override public void onStudentsLoaded(List<User> students) { }
    @Override public void onStudentsUpdated() { examRoomController.getAllExamRooms(); }
    @Override public void onExamRoomCreated(ExamRoom r) { loadInitialData(); }
    @Override public void onExamRoomUpdated(ExamRoom r) { loadInitialData(); }
    @Override public void onExamRoomDeleted(int id) { loadInitialData(); }
    @Override public void onError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    private void showCreateRoomDialog() {
        AddExamRoomDialog d = new AddExamRoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), subjects);
        d.setVisible(true);
        if (d.isConfirmed()) examRoomController.createExamRoom(d.getExamRoom());
    }

    private void showEditRoomDialog() {
        ExamRoom s = examRoomsTable.getSelectedExamRoom();
        if (s == null) return;
        EditExamRoomDialog d = new EditExamRoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), s, subjects);
        d.setVisible(true);
        if (d.isConfirmed()) examRoomController.updateExamRoom(d.getExamRoom());
    }

    private void deleteSelectedRoom() {
        ExamRoom s = examRoomsTable.getSelectedExamRoom();
        if (s != null) examRoomController.deleteExamRoom(s.getRoomId(), s.getRoomName());
    }

    private void showManageStudentsDialog() {
        ExamRoom s = examRoomsTable.getSelectedExamRoom();
        if (s != null) {
            List<User> stds = examRoomController.getAllStudents();
            new ManageStudentsDialog((JFrame) SwingUtilities.getWindowAncestor(this), s, stds, examRoomController).setVisible(true);
        }
    }

    private void updateStatus(String msg) { if (callbacks != null) callbacks.updateStatus(msg); }
}