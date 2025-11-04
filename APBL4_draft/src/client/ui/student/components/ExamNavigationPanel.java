package client.ui.student.components;

import model.ExamAnswer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamNavigationPanel - Component for navigating between exam questions
 */
public class ExamNavigationPanel extends JPanel {
    
    private List<ExamAnswer> questions;
    private List<JButton> questionButtons;
    private int currentQuestionIndex = -1;
    private NavigationListener navigationListener;
    
    // UI Components
    private JPanel buttonPanel;
    private JLabel summaryLabel;
    private JProgressBar progressBar;
    
    public ExamNavigationPanel() {
        this.questions = new ArrayList<>();
        this.questionButtons = new ArrayList<>();
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with summary
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - question buttons
        buttonPanel = new JPanel(new GridLayout(0, 4, 2, 2));
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer with progress
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Questions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        summaryLabel = new JLabel("0 questions");
        summaryLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        summaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        summaryLabel.setForeground(Color.GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(summaryLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setPreferredSize(new Dimension(0, 20));
        
        // Legend
        JPanel legendPanel = createLegendPanel();
        
        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(legendPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 2, 2));
        panel.setBorder(BorderFactory.createTitledBorder("Legend"));
        
        // Create legend items
        panel.add(createLegendItem("⚪ Not answered", Color.LIGHT_GRAY));
        panel.add(createLegendItem("🔵 Answered", new Color(100, 150, 255)));
        panel.add(createLegendItem("🟢 Current", new Color(0, 200, 0)));
        
        return panel;
    }
    
    private JLabel createLegendItem(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 9));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
//    public void setQuestions(List<ExamAnswer> questions) {
//        this.questions = questions;
//        this.questionButtons.clear();
//        buttonPanel.removeAll();
//        
//        // Create button for each question
//        for (int i = 0; i < questions.size(); i++) {
//            final int questionIndex = i;
//            ExamAnswer examAnswer = questions.get(i);
//            
//            JButton button = new JButton(String.valueOf(i + 1));
//            button.setPreferredSize(new Dimension(40, 40));
//            button.setFont(new Font("Arial", Font.BOLD, 12));
//            button.setBorder(BorderFactory.createRaisedBevelBorder());
//            
//            // Set initial state
//            updateQuestionButtonState(button, examAnswer.getStudentAnswer() != null);
//            
//            // Add click listener
//            button.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    if (navigationListener != null) {
//                        navigationListener.onQuestionClicked(questionIndex);
//                    }
//                }
//            });
//            
//            questionButtons.add(button);
//            buttonPanel.add(button);
//        }
//        
//        updateSummary();
//        buttonPanel.revalidate();
//        buttonPanel.repaint();
//    }
    public void setQuestions(List<ExamAnswer> questions) {
        // ✅ THÊM DEBUG LOG
        System.out.println("🔍 [ExamNavigationPanel] setQuestions called with " + 
                          (questions != null ? questions.size() + " questions" : "NULL"));
        
        if (questions == null) {
            System.err.println("❌ [ExamNavigationPanel] Questions list is null!");
            return;
        }
        
        this.questions = questions;
        this.questionButtons.clear();
        buttonPanel.removeAll();
        
        // Create button for each question
        for (int i = 0; i < questions.size(); i++) {
            final int questionIndex = i;
            ExamAnswer examAnswer = questions.get(i);
            
            JButton button = new JButton(String.valueOf(i + 1));
            button.setPreferredSize(new Dimension(40, 40));
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            
            // Set initial state
            updateQuestionButtonState(button, examAnswer.getStudentAnswer() != null);
            
            // ✅ FIX: Correct listener interface method
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (navigationListener != null) {
                        // ✅ FIX: Đây phải là callback từ ExamInterfacePanel
                        navigationListener.onQuestionClicked(questionIndex);
                    }
                }
            });
            
            questionButtons.add(button);
            buttonPanel.add(button);
        }
        
        updateSummary();
        buttonPanel.revalidate();
        buttonPanel.repaint();
        
        System.out.println("✅ [ExamNavigationPanel] " + questions.size() + " navigation buttons created");
    }

    // ✅ THÊM DEBUG LOG cho markQuestionAnswered
    public void markQuestionAnswered(int questionIndex, boolean isAnswered) {
        System.out.println("🔍 [ExamNavigationPanel] markQuestionAnswered: Q" + (questionIndex + 1) + " = " + isAnswered);
        
        if (questionIndex >= 0 && questionIndex < questionButtons.size()) {
            JButton button = questionButtons.get(questionIndex);
            
            // Don't override current question highlighting
            if (questionIndex != currentQuestionIndex) {
                updateQuestionButtonState(button, isAnswered);
            }
            
            updateSummary();
        }
    }
    
    public void setCurrentQuestion(int questionIndex) {
        // Reset all buttons to non-current state
        for (int i = 0; i < questionButtons.size(); i++) {
            JButton button = questionButtons.get(i);
            ExamAnswer examAnswer = questions.get(i);
            boolean isAnswered = examAnswer.getStudentAnswer() != null && 
                               !examAnswer.getStudentAnswer().trim().isEmpty();
            updateQuestionButtonState(button, isAnswered);
        }
        
        // Highlight current question
        if (questionIndex >= 0 && questionIndex < questionButtons.size()) {
            JButton currentButton = questionButtons.get(questionIndex);
            currentButton.setBackground(new Color(0, 200, 0));
            currentButton.setForeground(Color.WHITE);
            currentButton.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        
        this.currentQuestionIndex = questionIndex;
        updateSummary();
    }
    
//    public void markQuestionAnswered(int questionIndex, boolean isAnswered) {
//        if (questionIndex >= 0 && questionIndex < questionButtons.size()) {
//            JButton button = questionButtons.get(questionIndex);
//            
//            // Don't override current question highlighting
//            if (questionIndex != currentQuestionIndex) {
//                updateQuestionButtonState(button, isAnswered);
//            }
//            
//            updateSummary();
//        }
//    }
    
    private void updateQuestionButtonState(JButton button, boolean isAnswered) {
        if (isAnswered) {
            button.setBackground(new Color(100, 150, 255));
            button.setForeground(Color.WHITE);
            button.setToolTipText("Question answered");
        } else {
            button.setBackground(Color.LIGHT_GRAY);
            button.setForeground(Color.BLACK);
            button.setToolTipText("Question not answered");
        }
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }
    
    private void updateSummary() {
        if (questions.isEmpty()) {
            summaryLabel.setText("No questions");
            progressBar.setValue(0);
            progressBar.setString("0%");
            return;
        }
        
        int totalQuestions = questions.size();
        int answeredQuestions = 0;
        
        for (ExamAnswer answer : questions) {
            if (answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty()) {
                answeredQuestions++;
            }
        }
        
        summaryLabel.setText(String.format("%d / %d answered", answeredQuestions, totalQuestions));
        
        int progressPercentage = (int) ((double) answeredQuestions / totalQuestions * 100);
        progressBar.setValue(progressPercentage);
        progressBar.setString(progressPercentage + "%");
        
        // Update progress bar color
        if (progressPercentage >= 80) {
            progressBar.setForeground(new Color(0, 150, 0));
        } else if (progressPercentage >= 50) {
            progressBar.setForeground(new Color(255, 140, 0));
        } else {
            progressBar.setForeground(Color.RED);
        }
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }
    
    public int getAnsweredCount() {
        return (int) questions.stream()
            .filter(q -> q.getStudentAnswer() != null && !q.getStudentAnswer().trim().isEmpty())
            .count();
    }
    
    public int getTotalQuestions() {
        return questions.size();
    }
    
    /**
     * Navigation listener interface
     */
    public interface NavigationListener {
        void onQuestionClicked(int questionIndex);
    }
}