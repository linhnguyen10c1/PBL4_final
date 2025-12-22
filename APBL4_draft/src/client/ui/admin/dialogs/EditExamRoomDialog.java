package client.ui.admin.dialogs;

import model.ExamRoom;
import model.Subject;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EditExamRoomDialog extends JDialog {
    
    private ExamRoom originalExamRoom;
    private List<Subject> subjects;
    private ExamRoom resultExamRoom;
    private boolean confirmed = false;
    
    private JComboBox<Subject> subjectComboBox;
    private JTextField classStudy;
    private JTextField roomNameField;
    private JTextField roomPasswordField;
    private JSpinner questionCountSpinner;
    private JSpinner totalScoreSpinner;
    private JSpinner durationSpinner;
    
    private JSpinner startDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endDateSpinner;
    private JSpinner endTimeSpinner;
    private JButton setStartNowButton;
    private JButton setEndNowButton;
    
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JButton generatePasswordButton;
    
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public EditExamRoomDialog(JFrame parent, ExamRoom examRoom, List<Subject> subjects) {
        super(parent, "Edit Exam Room", true);
        this.originalExamRoom = examRoom;
        this.subjects = subjects;
        
        initializeUI();
        setupEventHandlers();
        populateFields();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        int row = 0;
        
        // Subject & Basic Info
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        subjectComboBox = new JComboBox<>();
        for (Subject s : subjects) subjectComboBox.addItem(s);
        formPanel.add(subjectComboBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Class:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        classStudy = new JTextField(20);
        formPanel.add(classStudy, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Room Name:"), gbc);
        gbc.gridx = 1;
        roomNameField = new JTextField(20);
        roomNameField.setEditable(false);
        formPanel.add(roomNameField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        roomPasswordField = new JTextField(15);
        generatePasswordButton = new JButton("Generate");
        passwordPanel.add(roomPasswordField, BorderLayout.CENTER);
        passwordPanel.add(generatePasswordButton, BorderLayout.EAST);
        formPanel.add(passwordPanel, gbc);
        row++;
        
        // Numeric Fields
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Questions:*"), gbc);
        gbc.gridx = 1;
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        formPanel.add(questionCountSpinner, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Score:*"), gbc);
        gbc.gridx = 1;
        totalScoreSpinner = new JSpinner(new SpinnerNumberModel(10.0, 1.0, 100.0, 0.5));
        formPanel.add(totalScoreSpinner, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Duration:*"), gbc);
        gbc.gridx = 1;
        durationSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 480, 5));
        formPanel.add(durationSpinner, gbc);
        row++;
        
        // Start Time (Mandatory & Short Label)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Start Time:*"), gbc);
        gbc.gridx = 1;
        JPanel startPanel = new JPanel(new BorderLayout(5, 0));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        JPanel startSpinners = new JPanel(new GridLayout(1, 2, 5, 0));
        startSpinners.add(startDateSpinner); startSpinners.add(startTimeSpinner);
        setStartNowButton = new JButton("Now");
        startPanel.add(startSpinners, BorderLayout.CENTER); startPanel.add(setStartNowButton, BorderLayout.EAST);
        formPanel.add(startPanel, gbc);
        row++;

        // End Time (Mandatory & Short Label)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("End Time:*"), gbc);
        gbc.gridx = 1;
        JPanel endPanel = new JPanel(new BorderLayout(5, 0));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
        JPanel endSpinners = new JPanel(new GridLayout(1, 2, 5, 0));
        endSpinners.add(endDateSpinner); endSpinners.add(endTimeSpinner);
        setEndNowButton = new JButton("Now");
        endPanel.add(endSpinners, BorderLayout.CENTER); endPanel.add(setEndNowButton, BorderLayout.EAST);
        formPanel.add(endPanel, gbc);
        row++;
        
        // Active Status
        gbc.gridx = 1; gbc.gridy = row;
        activeCheckBox = new JCheckBox("Is Active", true);
        formPanel.add(activeCheckBox, gbc);
        row++;
        
        // Description Area
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.5;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Save/Cancel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Update Room");
        JButton cancelButton = new JButton("Cancel");
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> { confirmed = false; dispose(); });
        buttonPanel.add(saveButton); buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void populateFields() {
        if (originalExamRoom == null) return;
        
        for (int i = 0; i < subjectComboBox.getItemCount(); i++) {
            if (subjectComboBox.getItemAt(i).getSubjectId() == originalExamRoom.getSubjectId()) {
                subjectComboBox.setSelectedIndex(i); break;
            }
        }
        
        // Parse Class Name
        String rName = originalExamRoom.getRoomName();
        String sName = originalExamRoom.getSubjectName();
        String cls = (rName != null && sName != null && rName.startsWith(sName)) 
                ? rName.substring(sName.length()).replaceAll("\\d{4}$", "") : "";
        classStudy.setText(cls);
        
        roomNameField.setText(originalExamRoom.getRoomName());
        roomPasswordField.setText(originalExamRoom.getRoomPassword());
        questionCountSpinner.setValue(originalExamRoom.getQuestionCount());
        totalScoreSpinner.setValue(originalExamRoom.getTotalScore());
        durationSpinner.setValue(originalExamRoom.getDurationMinutes());
        activeCheckBox.setSelected(originalExamRoom.isActive());
        descriptionArea.setText(originalExamRoom.getDescription());
        
        // Date Spinners - Mandatory
        Date start = (originalExamRoom.getStartTime() != null) 
                ? new Date(originalExamRoom.getStartTime().getTime()) : new Date();
        startDateSpinner.setValue(start); startTimeSpinner.setValue(start);
        
        Date end = (originalExamRoom.getEndTime() != null) 
                ? new Date(originalExamRoom.getEndTime().getTime()) : new Date(System.currentTimeMillis() + 86400000);
        endDateSpinner.setValue(end); endTimeSpinner.setValue(end);
    }
    
    private void setupEventHandlers() {
        subjectComboBox.addActionListener(e -> updateRoomName());
        classStudy.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
        });
        generatePasswordButton.addActionListener(e -> roomPasswordField.setText(String.format("%06d", (int)(Math.random() * 1000000))));
        setStartNowButton.addActionListener(e -> { Date n = new Date(); startDateSpinner.setValue(n); startTimeSpinner.setValue(n); });
        setEndNowButton.addActionListener(e -> { Date n = new Date(); endDateSpinner.setValue(n); endTimeSpinner.setValue(n); });
    }

    private void updateRoomName() {
        Subject s = (Subject) subjectComboBox.getSelectedItem();
        String cls = classStudy.getText().trim();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        roomNameField.setText(((s != null) ? s.getSubjectName() : "") + cls + year);
    }
    
    private void handleSave() {
        if (roomNameField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Room name is required!"); return; }
        
        String sStr = combineDateAndTime((Date)startDateSpinner.getValue(), (Date)startTimeSpinner.getValue());
        String eStr = combineDateAndTime((Date)endDateSpinner.getValue(), (Date)endTimeSpinner.getValue());
        
        try {
            Date s = DATETIME_FORMAT.parse(sStr);
            Date e = DATETIME_FORMAT.parse(eStr);
            if (!s.before(e)) { JOptionPane.showMessageDialog(this, "Start time must be before end time!"); return; }
        } catch (Exception ex) { return; }
        
        resultExamRoom = new ExamRoom();
        resultExamRoom.setRoomId(originalExamRoom.getRoomId());
        resultExamRoom.setRoomName(roomNameField.getText().trim());
        resultExamRoom.setRoomPassword(roomPasswordField.getText().trim());
        Subject s = (Subject) subjectComboBox.getSelectedItem();
        resultExamRoom.setSubjectId(s.getSubjectId());
        resultExamRoom.setSubjectName(s.getSubjectName());
        resultExamRoom.setQuestionCount((Integer) questionCountSpinner.getValue());
        resultExamRoom.setTotalScore((Double) totalScoreSpinner.getValue());
        resultExamRoom.setDurationMinutes((Integer) durationSpinner.getValue());
        resultExamRoom.setStartTimeFromString(sStr);
        resultExamRoom.setEndTimeFromString(eStr);
        resultExamRoom.setActive(activeCheckBox.isSelected());
        resultExamRoom.setDescription(descriptionArea.getText().trim());
        resultExamRoom.setCreatedBy(originalExamRoom.getCreatedBy());
        
        confirmed = true;
        dispose();
    }
    
    private String combineDateAndTime(Date d, Date t) {
        Calendar c1 = Calendar.getInstance(); c1.setTime(d);
        Calendar c2 = Calendar.getInstance(); c2.setTime(t);
        c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
        c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
        c1.set(Calendar.SECOND, 0);
        return DATETIME_FORMAT.format(c1.getTime());
    }
    
    public ExamRoom getExamRoom() { return resultExamRoom; }
    public boolean isConfirmed() { return confirmed; }
}