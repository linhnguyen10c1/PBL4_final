package client.ui.admin.panels;

import client.controller.QuestionController;
import client.ui.admin.components.QuestionsTable;
import client.ui.admin.dialogs.AddQuestionDialog;
import client.ui.admin.dialogs.EditQuestionDialog;
import client.ui.admin.dialogs.QuestionPreviewDialog;
import model.Question;
import model.Subject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Question Management Panel - Main panel for managing questions
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 04:06:50 UTC
 */
public class QuestionManagementPanel extends JPanel implements QuestionController.QuestionListener {
    
    private QuestionController questionController;
    private QuestionsTable questionsTable;
    private List<Subject> subjects;
    private List<Question> currentQuestions;
    
    // UI Components
    private JTextField searchField;
    private JComboBox<Subject> subjectFilterComboBox;
    private JComboBox<String> difficultyFilterComboBox;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton previewButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    
    public QuestionManagementPanel(QuestionController questionController) {
        this.questionController = questionController;
        this.questionController.setQuestionListener(this);
        
        initializeUI();
        setupEventHandlers();
        loadInitialData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with search and filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        questionsTable = new QuestionsTable();
        questionsTable.setSelectionListener(new QuestionsTable.QuestionSelectionListener() {
            @Override
            public void onQuestionSelected(Question question) {
                updateButtonStates(true);
            }
            
            @Override
            public void onQuestionDeselected() {
                updateButtonStates(false);
            }
            
            @Override
            public void onQuestionDoubleClicked(Question question) {
                editSelectedQuestion();
            }
        });
        add(questionsTable, BorderLayout.CENTER);
        
        // Bottom panel with status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Initial button states
        updateButtonStates(false);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Subject:"));
        subjectFilterComboBox = new JComboBox<>();
        subjectFilterComboBox.addItem(null); // "All subjects" option
        subjectFilterComboBox.setRenderer(new SubjectFilterRenderer());
        subjectFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(subjectFilterComboBox);
        
        filterPanel.add(new JLabel("Difficulty:"));
        difficultyFilterComboBox = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD"});
        difficultyFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(difficultyFilterComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add Question");
        editButton = new JButton("Edit Question");
        deleteButton = new JButton("Delete Question");
        previewButton = new JButton("Preview");
        refreshButton = new JButton("Refresh");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(refreshButton);
        
        // Combine panels
        JPanel filtersAndSearch = new JPanel(new BorderLayout());
        filtersAndSearch.add(searchPanel, BorderLayout.WEST);
        filtersAndSearch.add(filterPanel, BorderLayout.CENTER);
        
        topPanel.add(filtersAndSearch, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Ready");
        bottomPanel.add(statusLabel);
        return bottomPanel;
    }
    
    private void setupEventHandlers() {
        // Search field Enter key
        searchField.addActionListener(e -> performSearch());
        
        // Buttons
        addButton.addActionListener(e -> addNewQuestion());
        editButton.addActionListener(e -> editSelectedQuestion());
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        previewButton.addActionListener(e -> previewSelectedQuestion());
        refreshButton.addActionListener(e -> refreshQuestions());
    }
    
    private void loadInitialData() {
        // Load subjects first
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Loading subjects...");
            questionController.getAllSubjects();
        });
    }
    
    private void addNewQuestion() {
        if (subjects == null || subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects available. Please add subjects first.", 
                                        "No Subjects", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AddQuestionDialog dialog = new AddQuestionDialog((JFrame) SwingUtilities.getWindowAncestor(this), subjects);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Question newQuestion = dialog.getQuestion();
            statusLabel.setText("Creating question...");
            questionController.createQuestion(newQuestion);
        }
    }
    
    private void editSelectedQuestion() {
        Question selectedQuestion = getFullSelectedQuestion();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Please select a question to edit.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (subjects == null || subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot edit question: subjects not loaded.", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        EditQuestionDialog dialog = new EditQuestionDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), selectedQuestion, subjects);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Question updatedQuestion = dialog.getQuestion();
            statusLabel.setText("Updating question...");
            questionController.updateQuestion(updatedQuestion);
        }
    }
    
    private void deleteSelectedQuestion() {
        Question selectedQuestion = getFullSelectedQuestion();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Deleting question...");
        questionController.deleteQuestion(selectedQuestion.getQuestionId(), selectedQuestion.getQuestionText());
    }
    
    private void previewSelectedQuestion() {
        Question selectedQuestion = getFullSelectedQuestion();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Please select a question to preview.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        QuestionPreviewDialog dialog = new QuestionPreviewDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), selectedQuestion);
        dialog.setVisible(true);
    }
    
    private void refreshQuestions() {
        statusLabel.setText("Refreshing questions...");
        questionController.getAllQuestions();
    }
    
    private void performSearch() {
        String keyword = searchField.getText().trim();
        statusLabel.setText("Searching questions...");
        questionController.searchQuestions(keyword);
    }
    
    private void applyFilters() {
        if (currentQuestions == null) return;
        
        Subject selectedSubject = (Subject) subjectFilterComboBox.getSelectedItem();
        String selectedDifficulty = (String) difficultyFilterComboBox.getSelectedItem();
        
        List<Question> filteredQuestions = currentQuestions.stream()
            .filter(q -> selectedSubject == null || q.getSubjectId() == selectedSubject.getSubjectId())
            .filter(q -> "All".equals(selectedDifficulty) || selectedDifficulty.equals(q.getDifficulty()))
            .collect(java.util.stream.Collectors.toList());
        
        questionsTable.setQuestions(filteredQuestions);
        updateStatusLabel(filteredQuestions.size(), currentQuestions.size());
    }
    
    private Question getFullSelectedQuestion() {
        Question tableQuestion = questionsTable.getSelectedQuestion();
        if (tableQuestion == null || currentQuestions == null) {
            return null;
        }
        
        // Find full question from current list
        return currentQuestions.stream()
            .filter(q -> q.getQuestionId() == tableQuestion.getQuestionId())
            .findFirst()
            .orElse(null);
    }
    
    private void updateButtonStates(boolean hasSelection) {
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        previewButton.setEnabled(hasSelection);
    }
    
    private void updateStatusLabel(int displayed, int total) {
        if (displayed == total) {
            statusLabel.setText("Showing " + total + " questions");
        } else {
            statusLabel.setText("Showing " + displayed + " of " + total + " questions");
        }
    }
    
    // QuestionController.QuestionListener implementation
    @Override
    public void onQuestionsLoaded(List<Question> questions) {
        SwingUtilities.invokeLater(() -> {
            this.currentQuestions = questions;
            questionsTable.setQuestions(questions);
            updateStatusLabel(questions.size(), questions.size());
            statusLabel.setText("Questions loaded successfully");
        });
    }
    
    @Override
    public void onQuestionCreated(Question question) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Question created successfully");
            refreshQuestions(); // Reload to get updated list
        });
    }
    
    @Override
    public void onQuestionUpdated(Question question) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Question updated successfully");
            refreshQuestions(); // Reload to get updated list
        });
    }
    
    @Override
    public void onQuestionDeleted(int questionId) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Question deleted successfully");
            refreshQuestions(); // Reload to get updated list
        });
    }
    
    @Override
    public void onSubjectsLoaded(List<Subject> subjects) {
        SwingUtilities.invokeLater(() -> {
            this.subjects = subjects;
            
            // Update subject filter combobox
            subjectFilterComboBox.removeAllItems();
            subjectFilterComboBox.addItem(null); // "All subjects" option
            for (Subject subject : subjects) {
                subjectFilterComboBox.addItem(subject);
            }
            
            statusLabel.setText("Subjects loaded successfully");
            
            // Now load questions
            questionController.getAllQuestions();
        });
    }
    
    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Error: " + message);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Custom renderer for Subject filter ComboBox
     */
    private static class SubjectFilterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value == null) {
                setText("All Subjects");
            } else if (value instanceof Subject) {
                Subject subject = (Subject) value;
                setText(subject.getSubjectCode() + " - " + subject.getSubjectName());
            }
            
            return this;
        }
    }
}