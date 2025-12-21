package client.ui.admin.panels;

import client.controller.QuestionController;
import client.ui.admin.components.QuestionsTable;
import client.ui.admin.dialogs.AddQuestionDialog;
import client.ui.admin.dialogs.EditQuestionDialog;
import client.ui.admin.dialogs.QuestionPreviewDialog;
import model.Question;
import model.Subject;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class QuestionManagementPanel extends JPanel implements QuestionsTable.QuestionSelectionListener {
    
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
        
        initUI();
        loadQuestions();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        
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
        deleteButton.setEnabled(false);
        deleteButton.setForeground(Color.RED);
        
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
        
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        questionsTable = new QuestionsTable();
        questionsTable.setSelectionListener(this);
  
        add(questionsTable, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        
        searchField.addActionListener(e -> performSearch());
        
        // Buttons
        addButton.addActionListener(e -> addQuestion());
        editButton.addActionListener(e -> editQuestion());
        deleteButton.addActionListener(e -> deleteQuestion());
        previewButton.addActionListener(e -> previewSelectedQuestion());
        refreshButton.addActionListener(e -> loadQuestions());
        
        loadInitialData();
    }
//    ADD: Method để load subjects
    private void loadInitialData() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Loading subjects...");
            subjects = questionController.getAllSubjects();
            
            if (subjects != null && !subjects.isEmpty()) {
                // Update subject filter combobox
                subjectFilterComboBox.removeAllItems();
                subjectFilterComboBox.addItem(null); // "All subjects" option
                for (Subject subject : subjects) {
                    subjectFilterComboBox.addItem(subject);
                }
                statusLabel.setText("Subjects loaded. Loading questions...");
                loadQuestions();
            } else {
                statusLabel.setText("Failed to load subjects");
                JOptionPane.showMessageDialog(this, 
                    "Failed to load subjects. Some features may not work.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
 private void loadQuestions() {
        statusLabel.setText("Loading questions...");
        
        List<Question> questions = questionController.getAllQuestions();
        
        if (questions != null) {
            // ✅ FIX: Lưu vào currentQuestions
            this.currentQuestions = questions;
            questionsTable.setQuestions(questions);
            statusLabel.setText("Loaded " + questions.size() + " questions");
        } else {
            statusLabel.setText("Failed to load questions");
            currentQuestions = new ArrayList<>();
            questionsTable.setQuestions(currentQuestions);
        }
    }
 private void applyFilters() {
	    if (currentQuestions == null) {
	        statusLabel.setText("No questions loaded");
	        return;
	    }
	    
	    Subject selectedSubject = (Subject) subjectFilterComboBox.getSelectedItem();
	    String selectedDifficulty = (String) difficultyFilterComboBox.getSelectedItem();
	    
	    List<Question> filteredQuestions = currentQuestions.stream()
	        .filter(q -> selectedSubject == null || q.getSubjectId() == selectedSubject.getSubjectId())
	        .filter(q -> "All".equals(selectedDifficulty) || selectedDifficulty.equals(q.getDifficulty()))
	        .collect(java.util.stream.Collectors.toList());
	    
	    questionsTable.setQuestions(filteredQuestions);
	    statusLabel.setText("Showing " + filteredQuestions.size() + " of " + currentQuestions.size() + " questions");
	}
 private void performSearch() {
	    String keyword = searchField.getText().trim();
	    statusLabel.setText("Searching questions...");
	    
	    List<Question> searchResults = questionController.searchQuestions(keyword);
	    
	    if (searchResults != null) {
	        currentQuestions = searchResults;  // ✅ Update currentQuestions
	        questionsTable.setQuestions(searchResults);
	        statusLabel.setText("Found " + searchResults.size() + " questions");
	    } else {
	        statusLabel.setText("Search failed");
	    }
	}
    @Override
    public void onQuestionSelected(Question question) {
        editButton.setEnabled(true);
        deleteButton.setEnabled(question.isActive());
        previewButton.setEnabled(true);
        
    }
    
    @Override
    public void onQuestionDeselected() {
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        previewButton.setEnabled(false);
    }
    
    @Override
    public void onQuestionDoubleClicked(Question question) {
        // Double click = edit user
        editQuestion();
    }
    
    private void addQuestion() {
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
            if(questionController.createQuestion(newQuestion)) {
            	loadQuestions();
            }
            
        }
    }
    
    private void editQuestion() {
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
            if(questionController.updateQuestion(updatedQuestion)) {
            	loadQuestions();
            }
        }
    }
    
    private void deleteQuestion() {
        Question selectedQuestion = getFullSelectedQuestion();
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Deleting question...");
        if(questionController.deleteQuestion(selectedQuestion.getQuestionId(), selectedQuestion.getQuestionText())) {
        	loadQuestions();
        }
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