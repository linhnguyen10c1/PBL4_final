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

public class EditExamRoomDialog extends JDialog {
    
    private ExamRoom originalExamRoom;
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
    
    // Buttons
    private JButton setStartNowButton;
    private JButton clearStartButton;
    private JButton setEndNowButton;
    private JButton clearEndButton;
    
    private JTextArea descriptionArea;
    private JCheckBox activeCheckBox;
    private JButton generatePasswordButton;
    
    // Formats
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public EditExamRoomDialog(JFrame parent, ExamRoom examRoom, List<Subject> subjects) {
        super(parent, "Edit Exam Room", true);
        this.originalExamRoom = examRoom;
        this.subjects = subjects;
        
        initializeUI();
        setupEventHandlers();
        // Gọi populateFields cuối cùng để đảm bảo UI đã sẵn sàng nhận dữ liệu
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
        
        // --- CÁC TRƯỜNG CƠ BẢN (Giữ nguyên như cũ) ---
        
        // Subject
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        subjectComboBox = new JComboBox<>();
        for (Subject subject : subjects) subjectComboBox.addItem(subject);
        formPanel.add(subjectComboBox, gbc);
        row++;
        
        // Class
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Class:*"), gbc);
        gbc.gridx = 1;
        classStudy = new JTextField(20);
        formPanel.add(classStudy, gbc);
        row++;
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Room Name:*"), gbc);
        gbc.gridx = 1;
        roomNameField = new JTextField(20);
        roomNameField.setEditable(false);
        formPanel.add(roomNameField, gbc);
        row++;
        
        // Password
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Room Password:*"), gbc);
        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        roomPasswordField = new JTextField(15);
        generatePasswordButton = new JButton("Generate");
        passwordPanel.add(roomPasswordField, BorderLayout.CENTER);
        passwordPanel.add(generatePasswordButton, BorderLayout.EAST);
        formPanel.add(passwordPanel, gbc);
        row++;
        
        // Question Count
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Question Count:*"), gbc);
        gbc.gridx = 1;
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        formPanel.add(questionCountSpinner, gbc);
        row++;
        
        // Total Score
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Total Score:*"), gbc);
        gbc.gridx = 1;
        totalScoreSpinner = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 1000.0, 5.0));
        formPanel.add(totalScoreSpinner, gbc);
        row++;
        
        // Duration
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Duration (minutes):*"), gbc);
        gbc.gridx = 1;
        durationSpinner = new JSpinner(new SpinnerNumberModel(60, 5, 300, 5));
        formPanel.add(durationSpinner, gbc);
        row++;
        
        // --- START TIME SECTION ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableStartTimeCheckBox = new JCheckBox("Set Start Time (Optional)", false);
        enableStartTimeCheckBox.setFont(enableStartTimeCheckBox.getFont().deriveFont(Font.BOLD));
        formPanel.add(enableStartTimeCheckBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("  Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel startPanel = new JPanel(new BorderLayout(5, 0));
        JPanel startDateTimePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        // Tạo models mới cho Spinners
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        
        startDateTimePanel.add(startDateSpinner);
        startDateTimePanel.add(startTimeSpinner);
        
        JPanel startButtonsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        setStartNowButton = new JButton("Now");
        clearStartButton = new JButton("Clear"); // Đổi tên thành Reset/Disable thì đúng hơn
        
        // Mặc định disable
        startDateSpinner.setEnabled(false);
        startTimeSpinner.setEnabled(false);
        setStartNowButton.setEnabled(false);
        clearStartButton.setEnabled(false);
        
        startButtonsPanel.add(setStartNowButton);
        startButtonsPanel.add(clearStartButton);
        startPanel.add(startDateTimePanel, BorderLayout.CENTER);
        startPanel.add(startButtonsPanel, BorderLayout.EAST);
        formPanel.add(startPanel, gbc);
        row++;
        
        // --- END TIME SECTION ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        enableEndTimeCheckBox = new JCheckBox("Set End Time (Optional)", false);
        enableEndTimeCheckBox.setFont(enableEndTimeCheckBox.getFont().deriveFont(Font.BOLD));
        formPanel.add(enableEndTimeCheckBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("  Date & Time:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel endPanel = new JPanel(new BorderLayout(5, 0));
        JPanel endDateTimePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
        
        endDateTimePanel.add(endDateSpinner);
        endDateTimePanel.add(endTimeSpinner);
        
        JPanel endButtonsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        setEndNowButton = new JButton("Now");
        clearEndButton = new JButton("Clear");
        
        // Mặc định disable
        endDateSpinner.setEnabled(false);
        endTimeSpinner.setEnabled(false);
        setEndNowButton.setEnabled(false);
        clearEndButton.setEnabled(false);
        
        endButtonsPanel.add(setEndNowButton);
        endButtonsPanel.add(clearEndButton);
        endPanel.add(endDateTimePanel, BorderLayout.CENTER);
        endPanel.add(endButtonsPanel, BorderLayout.EAST);
        formPanel.add(endPanel, gbc);
        row++;
        
        // Active & Description
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active", true);
        formPanel.add(activeCheckBox, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Buttons
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
    
    // Hàm này quan trọng: Load dữ liệu từ Object vào UI
    private void populateFields() {
        if (originalExamRoom == null) return;
        System.out.println("Dialog Editing Room: " + originalExamRoom.getRoomName());
        System.out.println("Original StartTime: " + originalExamRoom.getStartTime());
        System.out.println("Original EndTime: " + originalExamRoom.getEndTime());
        // 1. Load Subject & Class (Tách tên lớp từ RoomName)
        for (int i = 0; i < subjectComboBox.getItemCount(); i++) {
            if (subjectComboBox.getItemAt(i).getSubjectId() == originalExamRoom.getSubjectId()) {
                subjectComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        // Tách tên lớp
        String roomName = originalExamRoom.getRoomName();
        String subjectName = originalExamRoom.getSubjectName();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String className = "";
        if (roomName != null && subjectName != null && roomName.startsWith(subjectName)) {
            String temp = roomName.substring(subjectName.length());
            if (temp.endsWith(String.valueOf(year))) {
                className = temp.substring(0, temp.length() - 4);
            } else {
                className = temp;
            }
        }
        classStudy.setText(className);
        
        // 2. Load Basic Info
        roomNameField.setText(originalExamRoom.getRoomName());
        roomPasswordField.setText(originalExamRoom.getRoomPassword());
        questionCountSpinner.setValue(originalExamRoom.getQuestionCount());
        totalScoreSpinner.setValue(originalExamRoom.getTotalScore());
        durationSpinner.setValue(originalExamRoom.getDurationMinutes());
        activeCheckBox.setSelected(originalExamRoom.isActive());
        if (originalExamRoom.getDescription() != null) descriptionArea.setText(originalExamRoom.getDescription());

        if (originalExamRoom.getStartTime() != null) {
            // Có dữ liệu -> Check box & Set Value
            enableStartTimeCheckBox.setSelected(true);
            setStartTimeState(true); // Helper method enable controls
            
            // Convert từ SQL Timestamp sang Java Util Date cho Spinner
            Date start = new Date(originalExamRoom.getStartTime().getTime());
            startDateSpinner.setValue(start);
            startTimeSpinner.setValue(start);
        } else {
            // Không có dữ liệu -> Uncheck & Disable
            enableStartTimeCheckBox.setSelected(false);
            setStartTimeState(false); // Helper method disable controls
            
            // Set giá trị mặc định là Now để nếu user tích vào thì có giờ ngay
            Date now = new Date();
            startDateSpinner.setValue(now);
            startTimeSpinner.setValue(now);
        }
        
        // 4. Load END TIME
        if (originalExamRoom.getEndTime() != null) {
            enableEndTimeCheckBox.setSelected(true);
            setEndTimeState(true);
            
            Date end = new Date(originalExamRoom.getEndTime().getTime());
            endDateSpinner.setValue(end);
            endTimeSpinner.setValue(end);
        } else {
            enableEndTimeCheckBox.setSelected(false);
            setEndTimeState(false);
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            Date defaultEnd = cal.getTime();
            endDateSpinner.setValue(defaultEnd);
            endTimeSpinner.setValue(defaultEnd);
        }
    }
    
    private void setupEventHandlers() {
        // Auto update Room Name
        subjectComboBox.addActionListener(e -> updateRoomName());
        classStudy.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRoomName(); }
        });

        generatePasswordButton.addActionListener(e -> roomPasswordField.setText(String.format("%06d", (int)(Math.random() * 1000000))));
        
        // Start Time Checkbox
        enableStartTimeCheckBox.addActionListener(e -> {
            boolean isSelected = enableStartTimeCheckBox.isSelected();
            setStartTimeState(isSelected);
        });
        
        // Start Time Buttons
        setStartNowButton.addActionListener(e -> {
            Date now = new Date();
            startDateSpinner.setValue(now);
            startTimeSpinner.setValue(now);
        });
        
        clearStartButton.addActionListener(e -> {
            enableStartTimeCheckBox.setSelected(false);
            setStartTimeState(false);
            // Không setStartName(null) vào object gốc ở đây để tránh lỗi logic nếu user cancel
        });
        
        // End Time Checkbox
        enableEndTimeCheckBox.addActionListener(e -> {
            boolean isSelected = enableEndTimeCheckBox.isSelected();
            setEndTimeState(isSelected);
        });
        
        // End Time Buttons
        setEndNowButton.addActionListener(e -> {
            Date now = new Date();
            endDateSpinner.setValue(now);
            endTimeSpinner.setValue(now);
        });
        
        clearEndButton.addActionListener(e -> {
            enableEndTimeCheckBox.setSelected(false);
            setEndTimeState(false);
        });
    }
    
    // Helper để bật tắt UI Start Time
    private void setStartTimeState(boolean enabled) {
        startDateSpinner.setEnabled(enabled);
        startTimeSpinner.setEnabled(enabled);
        setStartNowButton.setEnabled(enabled);
        clearStartButton.setEnabled(enabled);
    }
    
    // Helper để bật tắt UI End Time
    private void setEndTimeState(boolean enabled) {
        endDateSpinner.setEnabled(enabled);
        endTimeSpinner.setEnabled(enabled);
        setEndNowButton.setEnabled(enabled);
        clearEndButton.setEnabled(enabled);
    }

    private void updateRoomName() {
        if (classStudy == null || roomNameField == null) return;
        Subject subject = (Subject) subjectComboBox.getSelectedItem();
        String className = classStudy.getText().trim();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String subjectPart = (subject != null) ? subject.getSubjectName() : "";
        roomNameField.setText(subjectPart + className + year);
    }
    
    private void handleSave() {
        // ... (Validate tên, pass, subject giống cũ) ...
        String roomName = roomNameField.getText().trim();
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        
        if (roomName.isEmpty()) { JOptionPane.showMessageDialog(this, "Room name required"); return; }
        
        // --- XỬ LÝ TIME (Phần quan trọng đã sửa) ---
        String startTimeText = null;
        String endTimeText = null;
        
        // Logic mới: Chỉ quan tâm checkbox.
        // - Checkbox ON -> Lấy giá trị từ Spinner.
        // - Checkbox OFF -> Null (Xóa giờ).
        
        if (enableStartTimeCheckBox.isSelected()) {
            Date d = (Date) startDateSpinner.getValue();
            Date t = (Date) startTimeSpinner.getValue();
            startTimeText = combineDateAndTime(d, t);
        }
        // Else: startTimeText = null (đúng ý đồ xóa giờ)
        
        if (enableEndTimeCheckBox.isSelected()) {
            Date d = (Date) endDateSpinner.getValue();
            Date t = (Date) endTimeSpinner.getValue();
            endTimeText = combineDateAndTime(d, t);
        }
        
        // Validate logic Start < End
        if (startTimeText != null && endTimeText != null) {
            try {
                Date s = DATETIME_FORMAT.parse(startTimeText);
                Date e = DATETIME_FORMAT.parse(endTimeText);
                if (!s.before(e)) {
                    JOptionPane.showMessageDialog(this, "Start time must be before End time!");
                    return;
                }
            } catch (Exception ex) { return; }
        }
        
        // Tạo object kết quả
        resultExamRoom = new ExamRoom();
        resultExamRoom.setRoomId(originalExamRoom.getRoomId());
        resultExamRoom.setRoomName(roomName);
        resultExamRoom.setRoomPassword(roomPasswordField.getText().trim());
        resultExamRoom.setSubjectId(selectedSubject.getSubjectId());
        resultExamRoom.setSubjectName(selectedSubject.getSubjectName());
        resultExamRoom.setQuestionCount((Integer) questionCountSpinner.getValue());
        resultExamRoom.setTotalScore((Double) totalScoreSpinner.getValue());
        resultExamRoom.setDurationMinutes((Integer) durationSpinner.getValue());
        
        // Set Time
        resultExamRoom.setStartTimeFromString(startTimeText);
        resultExamRoom.setEndTimeFromString(endTimeText);
        
        resultExamRoom.setActive(activeCheckBox.isSelected());
        resultExamRoom.setDescription(descriptionArea.getText().trim());
        resultExamRoom.setCreatedBy(originalExamRoom.getCreatedBy());
        resultExamRoom.setAllowedStudentIds(originalExamRoom.getAllowedStudentIds());
        
        confirmed = true;
        dispose();
    }
    
    private String combineDateAndTime(Date date, Date time) {
        Calendar c1 = Calendar.getInstance(); c1.setTime(date);
        Calendar c2 = Calendar.getInstance(); c2.setTime(time);
        c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
        c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
        c1.set(Calendar.SECOND, 0);
        return DATETIME_FORMAT.format(c1.getTime());
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public ExamRoom getExamRoom() { return resultExamRoom; }
    public boolean isConfirmed() { return confirmed; }
}