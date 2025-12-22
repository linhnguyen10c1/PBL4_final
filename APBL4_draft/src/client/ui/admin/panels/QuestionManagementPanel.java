package client.ui.admin.panels;

import client.controller.QuestionController;
import client.ui.admin.components.QuestionsTable;
import client.ui.admin.dialogs.AddQuestionDialog;
import client.ui.admin.dialogs.EditQuestionDialog;
import client.ui.admin.dialogs.QuestionPreviewDialog;
import model.Question;
import model.Subject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class QuestionManagementPanel extends JPanel implements QuestionsTable.QuestionSelectionListener {
    
    private QuestionController questionController;
    private QuestionsTable questionsTable;
    private List<Subject> subjects;
    private List<Question> currentQuestions;
    
    private JTextField searchField;
    private JComboBox<Subject> subjectFilterComboBox;
    private JComboBox<String> difficultyFilterComboBox;
    private JButton addButton, editButton, deleteButton, previewButton, refreshButton;
    private JLabel statusLabel;
    
    public QuestionManagementPanel(QuestionController questionController) {
        this.questionController = questionController;
        initUI();
        loadInitialData(); // Load subjects trước, sau đó tự động load questions
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // --- TOP PANEL: Controls ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Row 1: Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Question");
        editButton = new JButton("Edit Question");
        deleteButton = new JButton("Delete Question");
        previewButton = new JButton("Preview");
        refreshButton = new JButton("Refresh");
        
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        previewButton.setEnabled(false);
        deleteButton.setForeground(Color.RED);
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(previewButton);
        actionPanel.add(new JSeparator(JSeparator.VERTICAL));
        actionPanel.add(refreshButton);

        // Row 2: Filters and Search
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Filters & Search", TitledBorder.LEFT, TitledBorder.TOP));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Subject Filter
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        subjectFilterComboBox = new JComboBox<>();
        subjectFilterComboBox.setPreferredSize(new Dimension(150, 25));
        subjectFilterComboBox.setRenderer(new SubjectFilterRenderer());
        subjectFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(subjectFilterComboBox, gbc);

        // Difficulty Filter (Bổ sung thiết kế tương tự Subject)
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 3;
        difficultyFilterComboBox = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD"});
        difficultyFilterComboBox.setPreferredSize(new Dimension(100, 25));
        difficultyFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(difficultyFilterComboBox, gbc);

        // Search Field
        gbc.gridx = 4;
        filterPanel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0;
        searchField = new JTextField();
        filterPanel.add(searchField, gbc);
        
        gbc.gridx = 6; gbc.weightx = 0;
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> performSearch());
        filterPanel.add(searchBtn, gbc);

        topPanel.add(actionPanel);
        topPanel.add(filterPanel);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Table ---
        questionsTable = new QuestionsTable();
        questionsTable.setSelectionListener(this);
        add(questionsTable, BorderLayout.CENTER);
        
        // --- SOUTH PANEL: Status ---
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // Event listeners
        searchField.addActionListener(e -> performSearch());
        addButton.addActionListener(e -> addQuestion());
        editButton.addActionListener(e -> editQuestion());
        deleteButton.addActionListener(e -> deleteQuestion());
        previewButton.addActionListener(e -> previewSelectedQuestion());
        refreshButton.addActionListener(e -> loadQuestions());
    }

    private void loadInitialData() {
        statusLabel.setText("Loading subjects...");
        // Sử dụng Worker hoặc Thread để tránh block UI
        new Thread(() -> {
            subjects = questionController.getAllSubjects(); //
            SwingUtilities.invokeLater(() -> {
                if (subjects != null) {
                    subjectFilterComboBox.removeAllItems();
                    subjectFilterComboBox.addItem(null); // Option "All Subjects"
                    for (Subject s : subjects) subjectFilterComboBox.addItem(s);
                    loadQuestions();
                } else {
                    statusLabel.setText("Error loading subjects");
                }
            });
        }).start();
    }

    private void loadQuestions() {
        statusLabel.setText("Fetching questions...");
        new Thread(() -> {
            List<Question> questions = questionController.getAllQuestions(); //
            SwingUtilities.invokeLater(() -> {
                if (questions != null) {
                    this.currentQuestions = questions;
                    applyFilters(); // Áp dụng filter ngay sau khi load
                } else {
                    statusLabel.setText("Failed to load questions");
                }
            });
        }).start();
    }

    private void applyFilters() {
        if (currentQuestions == null) return;
        
        Subject selectedSub = (Subject) subjectFilterComboBox.getSelectedItem();
        String selectedDiff = (String) difficultyFilterComboBox.getSelectedItem();
        
        List<Question> filtered = currentQuestions.stream()
            .filter(q -> selectedSub == null || q.getSubjectId() == selectedSub.getSubjectId())
            .filter(q -> selectedDiff == null || "All".equals(selectedDiff) || selectedDiff.equals(q.getDifficulty()))
            .collect(Collectors.toList());
            
        questionsTable.setQuestions(filtered); //
        statusLabel.setText("Showing " + filtered.size() + " of " + currentQuestions.size() + " questions");
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        statusLabel.setText("Searching...");
        new Thread(() -> {
            List<Question> results = questionController.searchQuestions(keyword); //
            SwingUtilities.invokeLater(() -> {
                if (results != null) {
                    this.currentQuestions = results; // Cập nhật tập dữ liệu hiện tại
                    applyFilters(); // Sau khi search vẫn áp dụng filter ComboBox
                }
            });
        }).start();
    }

    // --- Interface Callbacks ---
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
        editQuestion();
    }

    // Các phương thức hỗ trợ khác (getFullSelectedQuestion, add, edit, delete...) 
    // giữ nguyên logic xử lý thông qua questionController
    
    private Question getFullSelectedQuestion() {
        Question tableQ = questionsTable.getSelectedQuestion();
        if (tableQ == null || currentQuestions == null) return null;
        return currentQuestions.stream()
            .filter(q -> q.getQuestionId() == tableQ.getQuestionId())
            .findFirst().orElse(null);
    }

    private void addQuestion() {
        if (subjects == null || subjects.isEmpty()) return;
        AddQuestionDialog dialog = new AddQuestionDialog((JFrame) SwingUtilities.getWindowAncestor(this), subjects);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && questionController.createQuestion(dialog.getQuestion())) loadQuestions();
    }

    private void editQuestion() {
        Question selected = getFullSelectedQuestion();
        if (selected == null) return;
        EditQuestionDialog dialog = new EditQuestionDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected, subjects);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && questionController.updateQuestion(dialog.getQuestion())) loadQuestions();
    }

    private void deleteQuestion() {
        Question selected = getFullSelectedQuestion();
        if (selected != null && questionController.deleteQuestion(selected.getQuestionId(), selected.getQuestionText())) loadQuestions();
    }

    private void previewSelectedQuestion() {
        Question selected = getFullSelectedQuestion();
        if (selected != null) new QuestionPreviewDialog((JFrame) SwingUtilities.getWindowAncestor(this), selected).setVisible(true);
    }

    private static class SubjectFilterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) setText("All Subjects");
            else if (value instanceof Subject) {
                Subject s = (Subject) value;
                setText(s.getSubjectCode() + " - " + s.getSubjectName());
            }
            return this;
        }
    }
}