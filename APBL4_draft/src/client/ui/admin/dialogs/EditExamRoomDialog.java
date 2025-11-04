package client.ui.admin.dialogs;

import model.ExamRoom;
import model.Subject;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Edit ExamRoom Dialog
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:40:18 UTC
 */
public class EditExamRoomDialog extends JDialog {
    
    private ExamRoom originalExamRoom;
    private List<Subject> subjects;
    private ExamRoom resultExamRoom;
    private boolean confirmed = false;
    
    // UI Components
    private JTextField roomNameField;
    private JTextField roomPasswordField;
    private JComboBox<Subject> subjectComboBox;
    private JSpinner questionCountSpinner;
    private JSpinner totalScoreSpinner;
    private JSpinner durationSpinner;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JButton generatePasswordButton;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public EditExamRoomDialog(JFrame parent, ExamRoom examRoom, List<Subject> subjects) {
        super(parent, "Edit Exam Room", true);
        this.originalExamRoom = examRoom;
        this.subjects = subjects;
        
        initializeUI();
        populateFields();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        int row = 0;
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Room Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        roomNameField = new JTextField(20);
        formPanel.add(roomNameField, gbc);
        row++;
        
        // Room Password
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Room Password:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        roomPasswordField = new JTextField(15);
        generatePasswordButton = new JButton("Generate");
        passwordPanel.add(roomPasswordField, BorderLayout.CENTER);
        passwordPanel.add(generatePasswordButton, BorderLayout.EAST);
        formPanel.add(passwordPanel, gbc);
        row++;
        
        // Subject
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        subjectComboBox = new JComboBox<>();
        for (Subject subject : subjects) {
            subjectComboBox.addItem(subject);
        }
        formPanel.add(subjectComboBox, gbc);
        row++;
        
        // Question Count
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Question Count:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        formPanel.add(questionCountSpinner, gbc);
        row++;
        
        // Total Score
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Total Score:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        totalScoreSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 5.0));
        formPanel.add(totalScoreSpinner, gbc);
        row++;
        
        // Duration
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Duration (minutes):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        durationSpinner = new JSpinner(new SpinnerNumberModel(60, 5, 300, 5));
        formPanel.add(durationSpinner, gbc);
        row++;
        
        // Start Time
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        startTimeField = new JTextField(20);
        formPanel.add(startTimeField, gbc);
        row++;
        
        // End Time
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        endTimeField = new JTextField(20);
        formPanel.add(endTimeField, gbc);
        row++;
        
        // Active
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active");
        formPanel.add(activeCheckBox, gbc);
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descScrollPane, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> handleCancel());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void populateFields() {
        if (originalExamRoom != null) {
            roomNameField.setText(originalExamRoom.getRoomName());
            roomPasswordField.setText(originalExamRoom.getRoomPassword());
            
            // Select subject
            for (int i = 0; i < subjectComboBox.getItemCount(); i++) {
                Subject subject = subjectComboBox.getItemAt(i);
                if (subject.getSubjectId() == originalExamRoom.getSubjectId()) {
                    subjectComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            questionCountSpinner.setValue(originalExamRoom.getQuestionCount());
            totalScoreSpinner.setValue(originalExamRoom.getTotalScore());
            durationSpinner.setValue(originalExamRoom.getDurationMinutes());
            
            if (originalExamRoom.getStartTime() != null) {
                startTimeField.setText(DATE_FORMAT.format(originalExamRoom.getStartTime()));
            }
            
            if (originalExamRoom.getEndTime() != null) {
                endTimeField.setText(DATE_FORMAT.format(originalExamRoom.getEndTime()));
            }
            
            activeCheckBox.setSelected(originalExamRoom.isActive());
            
            if (originalExamRoom.getDescription() != null) {
                descriptionArea.setText(originalExamRoom.getDescription());
            }
        }
    }
    
    private void setupEventHandlers() {
        // Generate password button
        generatePasswordButton.addActionListener(e -> {
            String password = String.format("%06d", (int)(Math.random() * 1000000));
            roomPasswordField.setText(password);
        });
        
        // Enter key to save
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane()
            .getComponent(1)).getComponent(0));
    }
    
    private void handleSave() {
        // Validate input
        String roomName = roomNameField.getText().trim();
        String roomPassword = roomPasswordField.getText().trim();
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        int questionCount = (Integer) questionCountSpinner.getValue();
        double totalScore = (Double) totalScoreSpinner.getValue();
        int duration = (Integer) durationSpinner.getValue();
        boolean active = activeCheckBox.isSelected();
        String description = descriptionArea.getText().trim();
        
        // Validation
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room name is required");
            return;
        }
        
        if (roomPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room password is required");
            return;
        }
        
        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "Subject is required");
            return;
        }
        
        String startTimeText = startTimeField.getText().trim();
        String endTimeText = endTimeField.getText().trim();
        
        // Validate format nếu có nhập
        if (!startTimeText.isEmpty() && !startTimeText.equals("yyyy-MM-dd HH:mm")) {
            if (!isValidDateTimeFormat(startTimeText)) {
                JOptionPane.showMessageDialog(this, "Invalid start time format. Use: yyyy-MM-dd HH:mm");
                return;
            }
        } else {
            startTimeText = null; // Set null nếu empty
        }
        
        if (!endTimeText.isEmpty() && !endTimeText.equals("yyyy-MM-dd HH:mm")) {
            if (!isValidDateTimeFormat(endTimeText)) {
                JOptionPane.showMessageDialog(this, "Invalid end time format. Use: yyyy-MM-dd HH:mm");
                return;
            }
        } else {
            endTimeText = null; // Set null nếu empty
        }
        
        // Validate time logic
        if (startTimeText != null && endTimeText != null) {
            if (!isStartTimeBeforeEndTime(startTimeText, endTimeText)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time");
                return;
            }
        }
        
        // Create updated exam room object
        resultExamRoom = new ExamRoom();
        resultExamRoom.setRoomId(originalExamRoom.getRoomId());
        resultExamRoom.setRoomName(roomName);
        resultExamRoom.setRoomPassword(roomPassword);
        resultExamRoom.setSubjectId(selectedSubject.getSubjectId());
        resultExamRoom.setSubjectName(selectedSubject.getSubjectName());
        resultExamRoom.setQuestionCount(questionCount);
        resultExamRoom.setTotalScore(totalScore);
        resultExamRoom.setDurationMinutes(duration);
        if (!startTimeText.isEmpty() && !startTimeText.equals("yyyy-MM-dd HH:mm")) {
            resultExamRoom.setStartTimeFromString(startTimeText);
        }
        
        if (!endTimeText.isEmpty() && !endTimeText.equals("yyyy-MM-dd HH:mm")) {
            resultExamRoom.setEndTimeFromString(endTimeText);
        }
        
        // Validate time logic using Timestamp methods
        if (resultExamRoom.getStartTime() != null && resultExamRoom.getEndTime() != null) {
            if (!resultExamRoom.getStartTime().before(resultExamRoom.getEndTime())) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time");
                return;
            }
        }
        resultExamRoom.setDescription(description);
        resultExamRoom.setActive(active);
        resultExamRoom.setCreatedBy(originalExamRoom.getCreatedBy());
        resultExamRoom.setAllowedStudentIds(originalExamRoom.getAllowedStudentIds());
        
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public ExamRoom getExamRoom() {
        return resultExamRoom;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
 // Helper methods
    private boolean isValidDateTimeFormat(String dateTime) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.parse(dateTime);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }

    private boolean isStartTimeBeforeEndTime(String startTime, String endTime) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date start = sdf.parse(startTime);
            java.util.Date end = sdf.parse(endTime);
            return start.before(end);
        } catch (java.text.ParseException e) {
            return false;
        }
    }
}