package client.ui.student.panels;

import client.controller.StudentExamController;
import client.ui.student.interfaces.StudentDashboardCallbacks;
import client.ui.student.components.ExamQuestionPanel;
import client.ui.student.components.ExamTimerPanel;
import client.ui.student.components.ExamNavigationPanel;
import client.ui.student.dialogs.ExamSubmitConfirmDialog;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Exam Interface Panel - Main interface for taking exams
 */
public class ExamInterfacePanel extends JPanel {
    
    private StudentDashboardCallbacks callbacks;
    private StudentExamController examController;
    
    // UI Components
    private JLabel examTitleLabel;
    private ExamTimerPanel timerPanel;
    private ExamQuestionPanel questionPanel;
    private ExamNavigationPanel navigationPanel;
    private JButton submitButton;
    private JButton saveAndNextButton;
    private JButton previousButton;
    private JButton nextButton;
    
    // Current state
    private ExamSession currentSession;
    private List<ExamAnswer> examQuestions;
    private int currentQuestionIndex = 0;
    private Timer autoSaveTimer;
    
    public ExamInterfacePanel(StudentDashboardCallbacks callbacks, StudentExamController examController) {
        this.callbacks = callbacks;
        this.examController = examController;
        this.examQuestions = new ArrayList<>();
        
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header with exam info and timer
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content area
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Question panel (center)
        questionPanel = new ExamQuestionPanel();
        questionPanel.setAnswerChangeListener(this::onAnswerChanged);
        mainPanel.add(questionPanel, BorderLayout.CENTER);
        
        // Navigation panel (right)
        navigationPanel = new ExamNavigationPanel();
//        navigationPanel.setNavigationListener(this::navigateToQuestion);
        navigationPanel.setNavigationListener(new ExamNavigationPanel.NavigationListener() {
            @Override
            public void onQuestionClicked(int questionIndex) {
                System.out.println("🔍 [Navigation] Button clicked: Q" + (questionIndex + 1));
                navigateToQuestion(questionIndex);
            }
        });
        JScrollPane navScrollPane = new JScrollPane(navigationPanel);
        navScrollPane.setPreferredSize(new Dimension(200, 0));
        navScrollPane.setBorder(BorderFactory.createTitledBorder("Question Navigation"));
        mainPanel.add(navScrollPane, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom panel with controls
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Left side - Exam title
        examTitleLabel = new JLabel("Exam Interface");
        examTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        examTitleLabel.setForeground(new Color(0, 100, 200));
        panel.add(examTitleLabel, BorderLayout.WEST);
        
        // Right side - Timer
        timerPanel = new ExamTimerPanel();
        timerPanel.setTimeExpiredListener(() -> {
            if (callbacks != null && currentSession != null) {
                callbacks.onExamTimeExpired(currentSession);
            }
        });
        panel.add(timerPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Navigation buttons (left)
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        previousButton = new JButton("⬅️ Previous");
        nextButton = new JButton("Next ➡️");
        saveAndNextButton = new JButton("💾 Save & Next");
        
        previousButton.addActionListener(e -> navigateToPrevious());
        nextButton.addActionListener(e -> navigateToNext());
        saveAndNextButton.addActionListener(e -> saveAndNavigateNext());
        
        navButtonPanel.add(previousButton);
        navButtonPanel.add(saveAndNextButton);
        navButtonPanel.add(nextButton);
        
        // Submit button (right)
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("✅ Submit Exam");
        submitButton.setFont(submitButton.getFont().deriveFont(Font.BOLD));
        submitButton.setBackground(new Color(220, 255, 220));
        submitButton.addActionListener(e -> showSubmitConfirmation());
        submitPanel.add(submitButton);
        
        panel.add(navButtonPanel, BorderLayout.WEST);
        panel.add(submitPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Auto-save timer (every 30 seconds)
        autoSaveTimer = new Timer(30000, e -> autoSaveCurrentAnswer());
    }
    
    public void startExam(ExamSession session) {
        this.currentSession = session;
        
        // Update UI
        examTitleLabel.setText("📝 " + session.getExamRoom().getRoomName() + 
                              " - " + session.getExamRoom().getSubjectName());
        
        // Start timer
        timerPanel.startTimer(session.getRemainingTimeMinutes());
        
        // Start auto-save
        autoSaveTimer.start();
        
        // Load questions
        examController.startExam(session.getSessionToken());
        
        updateStatus("Exam started. Good luck!");
    }
    
//    public void setExamQuestions(List<ExamAnswer> questions) {
//        this.examQuestions = questions;
//        navigationPanel.setQuestions(questions);
//        
//        if (!questions.isEmpty()) {
//            currentQuestionIndex = 0;
//            displayCurrentQuestion();
//        }
//        
//        updateNavigationButtons();
//    }
    
    
    public void navigateToQuestion(int questionIndex) {
        if (questionIndex >= 0 && questionIndex < examQuestions.size()) {
            // Save current answer before navigating
            saveCurrentAnswer();
            
            currentQuestionIndex = questionIndex;
            displayCurrentQuestion();
            updateNavigationButtons();
            
            if (callbacks != null) {
                callbacks.onNavigateToQuestion(questionIndex);
            }
        }
    }
    public void setExamQuestions(List<ExamAnswer> questions) {
        System.out.println("🔍 ExamInterfacePanel.setExamQuestions called with " + questions.size() + " questions");
        
        this.examQuestions = questions;
        
        if (navigationPanel != null) {
            navigationPanel.setQuestions(questions);
            System.out.println("✅ Navigation panel updated");
        }
        
        if (!questions.isEmpty()) {
            currentQuestionIndex = 0;
            displayCurrentQuestion();
            System.out.println("✅ First question displayed");
        } else {
            System.err.println("❌ No questions to display!");
        }
        
        updateNavigationButtons();
    }
//    private void displayCurrentQuestion() {
//        if (currentQuestionIndex >= 0 && currentQuestionIndex < examQuestions.size()) {
//            ExamAnswer examAnswer = examQuestions.get(currentQuestionIndex);
//            questionPanel.setQuestion(examAnswer.getQuestion(), examAnswer.getStudentAnswer());
//            navigationPanel.setCurrentQuestion(currentQuestionIndex);
//            
//            updateStatus("Question " + (currentQuestionIndex + 1) + " of " + examQuestions.size());
//        }
//    }
    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < examQuestions.size()) {
            ExamAnswer examAnswer = examQuestions.get(currentQuestionIndex);
            
            System.out.println("🔍 Displaying question " + (currentQuestionIndex + 1) + ": " + 
                              examAnswer.getQuestion().getQuestionText());
            
            if (questionPanel != null) {
                questionPanel.setQuestion(examAnswer.getQuestion(), examAnswer.getStudentAnswer());
            }
            
            if (navigationPanel != null) {
                navigationPanel.setCurrentQuestion(currentQuestionIndex);
            }
            
            updateStatus("Question " + (currentQuestionIndex + 1) + " of " + examQuestions.size());
        } else {
            System.err.println("❌ Invalid question index: " + currentQuestionIndex + 
                              " (total: " + examQuestions.size() + ")");
        }
    }
    private void navigateToPrevious() {
        if (currentQuestionIndex > 0) {
            navigateToQuestion(currentQuestionIndex - 1);
        }
    }
    
    private void navigateToNext() {
        if (currentQuestionIndex < examQuestions.size() - 1) {
            navigateToQuestion(currentQuestionIndex + 1);
        }
    }
    
    private void saveAndNavigateNext() {
        saveCurrentAnswer();
        navigateToNext();
    }
    
    private void onAnswerChanged(String answer) {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < examQuestions.size()) {
            ExamAnswer examAnswer = examQuestions.get(currentQuestionIndex);
            examAnswer.setStudentAnswer(answer);
            
            // Mark as answered in navigation
            navigationPanel.markQuestionAnswered(currentQuestionIndex, answer != null && !answer.trim().isEmpty());
            
            if (callbacks != null) {
                callbacks.onAnswerChanged(examAnswer.getQuestionId(), answer);
            }
        }
    }
    
    private void saveCurrentAnswer() {
        String currentAnswer = questionPanel.getCurrentAnswer();
        onAnswerChanged(currentAnswer);
    }
    
    private void autoSaveCurrentAnswer() {
        saveCurrentAnswer();
        updateStatus("Auto-saved");
    }
    
    private void showSubmitConfirmation() {
        // Save current answer first
        saveCurrentAnswer();
        
        // Count answered questions
        int answeredCount = (int) examQuestions.stream()
            .filter(q -> q.getStudentAnswer() != null && !q.getStudentAnswer().trim().isEmpty())
            .count();
        
        ExamSubmitConfirmDialog dialog = new ExamSubmitConfirmDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            examQuestions.size(),
            answeredCount,
            timerPanel.getRemainingMinutes()
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            submitExam();
        }
    }
    
    private void submitExam() {
        // Stop timer and auto-save
        timerPanel.stopTimer();
        autoSaveTimer.stop();
        
        if (callbacks != null && currentSession != null) {
            callbacks.onSubmitExamRequested(currentSession, false);
        }
    }
    
    private void updateNavigationButtons() {
        previousButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setEnabled(currentQuestionIndex < examQuestions.size() - 1);
        saveAndNextButton.setEnabled(currentQuestionIndex < examQuestions.size() - 1);
    }
    
    private void updateStatus(String message) {
        if (callbacks != null) {
            callbacks.updateStatus(message);
        }
    }
    
    // Cleanup when exam ends
    public void cleanup() {
        if (timerPanel != null) {
            timerPanel.stopTimer();
        }
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
    }
}