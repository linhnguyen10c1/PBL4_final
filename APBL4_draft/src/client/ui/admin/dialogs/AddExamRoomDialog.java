package client.ui.admin.dialogs;

import model.ExamRoom;
import model.Subject;
import utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Add ExamRoom Dialog - IMPROVED VERSION with Date/Time Pickers
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:36:16 UTC
 * @modified 2025-11-12 10:07:27 UTC (Added DateTime Pickers with Set Now & Clear buttons)
 */
public class AddExamRoomDialog extends JDialog {
    
    private List<Subject> subjects;
    private ExamRoom resultExamRoom;
    private boolean confirmed = false;
    
    // UI Components
    private JComboBox<Subject> subjectComboBox;
    private JTextField classStudy; 
    private JTextField roomNameField;
    private JTextField roomPasswordField;
    private JSpinner questionCountSpinner;
    private JSpinner totalScoreSpinner;
    private JSpinner durationSpinner;
    
    // Date/Time Pickers
    private JSpinner startDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endDateSpinner;
    private JSpinner endTimeSpinner;
    private JCheckBox enableStartTimeCheckBox;
    private JCheckBox enableEndTimeCheckBox;
    
    // ✅ TÍNH NĂNG MỚI 1: Buttons for Start Time
    private JButton setStartNowButton;
    private JButton clearStartButton;
    
    // ✅ TÍNH NĂNG MỚI 2: Buttons for End Time
    private JButton setEndNowButton;
    private JButton clearEndButton;
    
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JButton generatePasswordButton;
    
    // Date/Time formats
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public AddExamRoomDialog(JFrame parent, List<Subject> subjects) {
        super(parent, "Create New Exam Room", true);
        this.subjects = subjects;
        
        initializeUI();
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
        
        // classStudy 
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Class:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        classStudy = new JTextField(20);
        formPanel.add(classStudy, gbc);
        row++;
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Room Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        roomNameField = new JTextField(20);
        roomNameField.setEditable(false);
        formPanel.add(roomNameField, gbc);
        row++;
        
        subjectComboBox.addActionListener(e -> updateRoomName());
        classStudy.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
        });
        
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
        
        // ========== START TIME SECTION ==========
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableStartTimeCheckBox = new JCheckBox("Set Start Time (Optional)", false);
        enableStartTimeCheckBox.setFont(enableStartTimeCheckBox.getFont().deriveFont(Font.BOLD));
        formPanel.add(enableStartTimeCheckBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("  Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        // ✅ START DATE/TIME PANEL với buttons
        JPanel startPanel = new JPanel(new BorderLayout(5, 0));
        
        // Date/Time Spinners
        JPanel startDateTimePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setValue(cal.getTime());
        startDateSpinner.setEnabled(false);
        
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startTimeEditor);
        startTimeSpinner.setValue(cal.getTime());
        startTimeSpinner.setEnabled(false);
        
        startDateTimePanel.add(startDateSpinner);
        startDateTimePanel.add(startTimeSpinner);
        
        // Buttons Panel
        JPanel startButtonsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        setStartNowButton = new JButton("Now");
        setStartNowButton.setFont(new Font("Arial", Font.PLAIN, 10));
        setStartNowButton.setEnabled(false);
        
        clearStartButton = new JButton("Clear");
        clearStartButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearStartButton.setEnabled(false);
        
        startButtonsPanel.add(setStartNowButton);
        startButtonsPanel.add(clearStartButton);
        
        startPanel.add(startDateTimePanel, BorderLayout.CENTER);
        startPanel.add(startButtonsPanel, BorderLayout.EAST);
        
        formPanel.add(startPanel, gbc);
        row++;
        
        // ========== END TIME SECTION ==========
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableEndTimeCheckBox = new JCheckBox("Set End Time (Optional)", false);
        enableEndTimeCheckBox.setFont(enableEndTimeCheckBox.getFont().deriveFont(Font.BOLD));
        formPanel.add(enableEndTimeCheckBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("  Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        // ✅ END DATE/TIME PANEL với buttons
        JPanel endPanel = new JPanel(new BorderLayout(5, 0));
        
        // Date/Time Spinners
        JPanel endDateTimePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        cal.add(Calendar.DAY_OF_MONTH, 7); // Default: 7 days from now
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setValue(cal.getTime());
        endDateSpinner.setEnabled(false);
        
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endTimeEditor);
        endTimeSpinner.setValue(cal.getTime());
        endTimeSpinner.setEnabled(false);
        
        endDateTimePanel.add(endDateSpinner);
        endDateTimePanel.add(endTimeSpinner);
        
        // Buttons Panel
        JPanel endButtonsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        setEndNowButton = new JButton("Now");
        setEndNowButton.setFont(new Font("Arial", Font.PLAIN, 10));
        setEndNowButton.setEnabled(false);
        
        clearEndButton = new JButton("Clear");
        clearEndButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearEndButton.setEnabled(false);
        
        endButtonsPanel.add(setEndNowButton);
        endButtonsPanel.add(clearEndButton);
        
        endPanel.add(endDateTimePanel, BorderLayout.CENTER);
        endPanel.add(endButtonsPanel, BorderLayout.EAST);
        
        formPanel.add(endPanel, gbc);
        row++;
        
        // Active
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active", true);
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
        JButton saveButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> handleCancel());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateRoomName() {
        Subject subject = (Subject) subjectComboBox.getSelectedItem();
        String className = classStudy.getText().trim();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String subjectPart = (subject != null) ? subject.getSubjectName() : "";
        String roomName = subjectPart + className + year;  // Có thể nối bằng dấu _ nếu thích
        roomNameField.setText(roomName);
    }
    
    private void setupEventHandlers() {
        // Generate password button
        generatePasswordButton.addActionListener(e -> {
            String password = String.format("%06d", (int)(Math.random() * 1000000));
            roomPasswordField.setText(password);
        });
        
        // ========== START TIME HANDLERS ==========
        
        // Enable/Disable Start Time components
        enableStartTimeCheckBox.addActionListener(e -> {
            boolean enabled = enableStartTimeCheckBox.isSelected();
            startDateSpinner.setEnabled(enabled);
            startTimeSpinner.setEnabled(enabled);
            setStartNowButton.setEnabled(enabled);
            clearStartButton.setEnabled(enabled);
        });
        
        // ✅ TÍNH NĂNG 1: Set Start Time to Now
        setStartNowButton.addActionListener(e -> {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            startDateSpinner.setValue(now.getTime());
            startTimeSpinner.setValue(now.getTime());
            
            // Show feedback
            JOptionPane.showMessageDialog(this, 
                "Start time set to: " + DATETIME_FORMAT.format(now.getTime()),
                "Time Set", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // ✅ TÍNH NĂNG 2: Clear Start Time
        clearStartButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to disable start time?",
                "Clear Start Time",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                enableStartTimeCheckBox.setSelected(false);
                startDateSpinner.setEnabled(false);
                startTimeSpinner.setEnabled(false);
                setStartNowButton.setEnabled(false);
                clearStartButton.setEnabled(false);
                
                // Reset to default
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDateSpinner.setValue(cal.getTime());
                startTimeSpinner.setValue(cal.getTime());
            }
        });
        
        // ========== END TIME HANDLERS ==========
        
        // Enable/Disable End Time components
        enableEndTimeCheckBox.addActionListener(e -> {
            boolean enabled = enableEndTimeCheckBox.isSelected();
            endDateSpinner.setEnabled(enabled);
            endTimeSpinner.setEnabled(enabled);
            setEndNowButton.setEnabled(enabled);
            clearEndButton.setEnabled(enabled);
        });
        
        // ✅ TÍNH NĂNG 1: Set End Time to Now
        setEndNowButton.addActionListener(e -> {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            endDateSpinner.setValue(now.getTime());
            endTimeSpinner.setValue(now.getTime());
            
            // Show feedback
            JOptionPane.showMessageDialog(this, 
                "End time set to: " + DATETIME_FORMAT.format(now.getTime()),
                "Time Set", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // ✅ TÍNH NĂNG 2: Clear End Time
        clearEndButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to disable end time?",
                "Clear End Time",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                enableEndTimeCheckBox.setSelected(false);
                endDateSpinner.setEnabled(false);
                endTimeSpinner.setEnabled(false);
                setEndNowButton.setEnabled(false);
                clearEndButton.setEnabled(false);
                
                // Reset to default (7 days from now, 23:59)
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                endDateSpinner.setValue(cal.getTime());
                endTimeSpinner.setValue(cal.getTime());
            }
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
            JOptionPane.showMessageDialog(this, "Room name is required", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            roomNameField.requestFocus();
            return;
        }
        
        if (roomPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room password is required", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            roomPasswordField.requestFocus();
            return;
        }
        
        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "Subject is required", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Parse Date/Time from Spinners
        String startTimeText = null;
        String endTimeText = null;
        
        if (enableStartTimeCheckBox.isSelected()) {
            Date startDate = (Date) startDateSpinner.getValue();
            Date startTime = (Date) startTimeSpinner.getValue();
            startTimeText = combineDateAndTime(startDate, startTime);
        }
        
        if (enableEndTimeCheckBox.isSelected()) {
            Date endDate = (Date) endDateSpinner.getValue();
            Date endTime = (Date) endTimeSpinner.getValue();
            endTimeText = combineDateAndTime(endDate, endTime);
        }
        
        // Validate time logic
        if (startTimeText != null && endTimeText != null) {
            try {
                Date start = DATETIME_FORMAT.parse(startTimeText);
                Date end = DATETIME_FORMAT.parse(endTimeText);
                
                if (!start.before(end)) {
                    JOptionPane.showMessageDialog(this, 
                        "Start time must be before end time\n\n" +
                        "Start: " + startTimeText + "\n" +
                        "End:   " + endTimeText, 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid date/time format: " + e.getMessage(), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Create exam room object
        resultExamRoom = new ExamRoom();
        resultExamRoom.setRoomName(roomName);
        resultExamRoom.setRoomPassword(roomPassword);
        resultExamRoom.setSubjectId(selectedSubject.getSubjectId());
        resultExamRoom.setSubjectName(selectedSubject.getSubjectName());
        resultExamRoom.setQuestionCount(questionCount);
        resultExamRoom.setTotalScore(totalScore);
        resultExamRoom.setDurationMinutes(duration);
        
        if (startTimeText != null) {
            resultExamRoom.setStartTimeFromString(startTimeText);
        }
        
        if (endTimeText != null) {
            resultExamRoom.setEndTimeFromString(endTimeText);
        }
        
        resultExamRoom.setDescription(description);
        resultExamRoom.setActive(active);
        
        confirmed = true;
        dispose();
    }
    
    /**
     * Combine Date and Time into DateTime string
     */
    private String combineDateAndTime(Date date, Date time) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);
        
        // Combine date from dateCal and time from timeCal
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
        
        return DATETIME_FORMAT.format(dateCal.getTime());
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
}