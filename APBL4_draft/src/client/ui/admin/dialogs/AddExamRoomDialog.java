package client.ui.admin.dialogs;

import model.ExamRoom;
import model.Subject;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddExamRoomDialog extends JDialog {
    
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
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        int row = 0;
        
        // Subject
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        subjectComboBox = new JComboBox<>();
        for (Subject subject : subjects) subjectComboBox.addItem(subject);
        formPanel.add(subjectComboBox, gbc);
        row++;
        
        // Class
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Class:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        classStudy = new JTextField(20);
        formPanel.add(classStudy, gbc);
        row++;
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Room Name:"), gbc);
        gbc.gridx = 1;
        roomNameField = new JTextField(20);
        roomNameField.setEditable(false);
        formPanel.add(roomNameField, gbc);
        row++;
        
        // Password
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
        
        // Spinners
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Questions:*"), gbc);
        gbc.gridx = 1;
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        formPanel.add(questionCountSpinner, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Total Score:*"), gbc);
        gbc.gridx = 1;
        totalScoreSpinner = new JSpinner(new SpinnerNumberModel(10.0, 1.0, 100.0, 0.5));
        formPanel.add(totalScoreSpinner, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Duration (m):*"), gbc);
        gbc.gridx = 1;
        durationSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 480, 5));
        formPanel.add(durationSpinner, gbc);
        row++;
        
        // Start Time (Mandatory & Same Row)
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
        
        // End Time (Mandatory & Same Row)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("End Time:*"), gbc);
        gbc.gridx = 1;
        JPanel endPanel = new JPanel(new BorderLayout(5, 0));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
        // Default: +7 days
        Calendar cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_MONTH, 7);
        endDateSpinner.setValue(cal.getTime());
        JPanel endSpinners = new JPanel(new GridLayout(1, 2, 5, 0));
        endSpinners.add(endDateSpinner); endSpinners.add(endTimeSpinner);
        setEndNowButton = new JButton("Now");
        endPanel.add(endSpinners, BorderLayout.CENTER); endPanel.add(setEndNowButton, BorderLayout.EAST);
        formPanel.add(endPanel, gbc);
        row++;
        
        // Active
        gbc.gridx = 1; gbc.gridy = row;
        activeCheckBox = new JCheckBox("Is Active", true);
        formPanel.add(activeCheckBox, gbc);
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.5;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Final Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Create Room");
        JButton cancelButton = new JButton("Cancel");
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> { confirmed = false; dispose(); });
        buttonPanel.add(saveButton); buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
        String name = roomNameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Room name is required!"); return; }
        
        String startText = combineDateAndTime((Date)startDateSpinner.getValue(), (Date)startTimeSpinner.getValue());
        String endText = combineDateAndTime((Date)endDateSpinner.getValue(), (Date)endTimeSpinner.getValue());
        
        try {
            Date start = DATETIME_FORMAT.parse(startText);
            Date end = DATETIME_FORMAT.parse(endText);
            if (!start.before(end)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time!");
                return;
            }
        } catch (ParseException ex) { return; }
        
        resultExamRoom = new ExamRoom();
        resultExamRoom.setRoomName(name);
        resultExamRoom.setRoomPassword(roomPasswordField.getText().trim());
        Subject s = (Subject) subjectComboBox.getSelectedItem();
        resultExamRoom.setSubjectId(s.getSubjectId());
        resultExamRoom.setSubjectName(s.getSubjectName());
        resultExamRoom.setQuestionCount((Integer) questionCountSpinner.getValue());
        resultExamRoom.setTotalScore((Double) totalScoreSpinner.getValue());
        resultExamRoom.setDurationMinutes((Integer) durationSpinner.getValue());
        resultExamRoom.setStartTimeFromString(startText);
        resultExamRoom.setEndTimeFromString(endText);
        resultExamRoom.setActive(activeCheckBox.isSelected());
        resultExamRoom.setDescription(descriptionArea.getText().trim());
        
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