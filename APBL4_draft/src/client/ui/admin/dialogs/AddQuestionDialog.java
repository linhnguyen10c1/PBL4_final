package client.ui.admin.dialogs;

import model.Question;
import model.Subject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Add Question Dialog
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 04:02:31 UTC
 */
public class AddQuestionDialog extends JDialog {
    
    private List<Subject> subjects;
    private Question resultQuestion;
    private boolean confirmed = false;
    
    // UI Components
    private JComboBox<Subject> subjectComboBox;
    private JTextArea questionTextArea;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JComboBox<String> correctAnswerComboBox;
    private JComboBox<String> difficultyComboBox;
    private JCheckBox activeCheckBox;
    
    public AddQuestionDialog(JFrame parent, List<Subject> subjects) {
        super(parent, "Add New Question", true);
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
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        subjectComboBox = new JComboBox<>();
        for (Subject subject : subjects) {
            subjectComboBox.addItem(subject);
        }
        subjectComboBox.setRenderer(new SubjectComboBoxRenderer());
        formPanel.add(subjectComboBox, gbc);
        row++;
        
        // Question Text
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Question Text:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        questionTextArea = new JTextArea(4, 40);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane questionScrollPane = new JScrollPane(questionTextArea);
        formPanel.add(questionScrollPane, gbc);
        row++;
        
        // Options
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Option A
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Option A:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        optionAField = new JTextField(40);
        formPanel.add(optionAField, gbc);
        row++;
        
        // Option B
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Option B:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        optionBField = new JTextField(40);
        formPanel.add(optionBField, gbc);
        row++;
        
        // Option C
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Option C:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        optionCField = new JTextField(40);
        formPanel.add(optionCField, gbc);
        row++;
        
        // Option D
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Option D:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        optionDField = new JTextField(40);
        formPanel.add(optionDField, gbc);
        row++;
        
        // Correct Answer
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Correct Answer:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        correctAnswerComboBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        formPanel.add(correctAnswerComboBox, gbc);
        row++;
        
        // Difficulty
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Difficulty:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        difficultyComboBox = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD"});
        difficultyComboBox.setSelectedItem("MEDIUM");
        formPanel.add(difficultyComboBox, gbc);
        row++;
        
        // Active
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setSelected(true);
        formPanel.add(activeCheckBox, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create Question");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> handleCreate());
        cancelButton.addActionListener(e -> handleCancel());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Enter key to create
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane()
            .getComponent(1)).getComponent(0));
    }
    
    private void handleCreate() {
        // Validate input
        String questionText = questionTextArea.getText().trim();
        String optionA = optionAField.getText().trim();
        String optionB = optionBField.getText().trim();
        String optionC = optionCField.getText().trim();
        String optionD = optionDField.getText().trim();
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        String correctAnswer = (String) correctAnswerComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();
        boolean active = activeCheckBox.isSelected();
        
        // Validation
        if (questionText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Question text is required");
            return;
        }
        
        if (questionText.length() > 1000) {
            JOptionPane.showMessageDialog(this, "Question text cannot exceed 1000 characters");
            return;
        }
        
        if (optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All options are required");
            return;
        }
        
        if (optionA.length() > 500 || optionB.length() > 500 || 
            optionC.length() > 500 || optionD.length() > 500) {
            JOptionPane.showMessageDialog(this, "Options cannot exceed 500 characters each");
            return;
        }
        
        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "Subject is required");
            return;
        }
        
        // Create question object
        resultQuestion = new Question();
        resultQuestion.setSubjectId(selectedSubject.getSubjectId());
        resultQuestion.setSubjectName(selectedSubject.getSubjectName());
        resultQuestion.setQuestionText(questionText);
        resultQuestion.setOptionA(optionA);
        resultQuestion.setOptionB(optionB);
        resultQuestion.setOptionC(optionC);
        resultQuestion.setOptionD(optionD);
        resultQuestion.setCorrectAnswer(correctAnswer);
        resultQuestion.setDifficulty(difficulty);
        resultQuestion.setActive(active);
        
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public Question getQuestion() {
        return resultQuestion;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Custom renderer for Subject ComboBox
     */
    private static class SubjectComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Subject) {
                Subject subject = (Subject) value;
                setText(subject.getSubjectCode() + " - " + subject.getSubjectName());
            }
            
            return this;
        }
    }
}