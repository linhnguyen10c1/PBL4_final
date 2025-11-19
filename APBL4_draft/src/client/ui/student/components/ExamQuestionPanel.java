package client.ui.student.components;

import model.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * ExamQuestionPanel - Component for displaying and answering exam questions
 */
public class ExamQuestionPanel extends JPanel {
    
    private Question currentQuestion;
    private String currentAnswer;
    private AnswerChangeListener answerChangeListener;
    
    // UI Components
    private JLabel questionNumberLabel;
    private JLabel difficultyLabel;
    private JTextArea questionTextArea;
    private ButtonGroup optionGroup;
    private JRadioButton optionA;
    private JRadioButton optionB;
    private JRadioButton optionC;
    private JRadioButton optionD;
    private JButton clearAnswerButton;
    
    public ExamQuestionPanel() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Question"));
        
        // Header with question info
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Question text
        questionTextArea = new JTextArea();
        questionTextArea.setEditable(false);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setLineWrap(true);
        questionTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        questionTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        questionTextArea.setBackground(new Color(248, 248, 255));
        
        JScrollPane questionScrollPane = new JScrollPane(questionTextArea);
        questionScrollPane.setPreferredSize(new Dimension(0, 120));
        questionScrollPane.setBorder(BorderFactory.createTitledBorder("Question Text"));
        mainPanel.add(questionScrollPane, BorderLayout.NORTH);
        
        // Options panel
        JPanel optionsPanel = createOptionsPanel();
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom panel with controls
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        questionNumberLabel = new JLabel("Question #");
        questionNumberLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionNumberLabel.setForeground(new Color(0, 100, 200));
        
        difficultyLabel = new JLabel("Difficulty: Unknown");
        difficultyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        panel.add(questionNumberLabel, BorderLayout.WEST);
        panel.add(difficultyLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Select Your Answer"));
        
        // Create radio buttons
        optionGroup = new ButtonGroup();
        
        optionA = new JRadioButton("A. ");
        optionB = new JRadioButton("B. ");
        optionC = new JRadioButton("C. ");
        optionD = new JRadioButton("D. ");
        
        // Style radio buttons
        JRadioButton[] options = {optionA, optionB, optionC, optionD};
        for (JRadioButton option : options) {
            option.setFont(new Font("Arial", Font.PLAIN, 13));
            option.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            option.setBackground(Color.WHITE);
            option.setOpaque(true);
            optionGroup.add(option);
            panel.add(option);
        }
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        clearAnswerButton = new JButton("🗑️ Clear Answer");
        clearAnswerButton.addActionListener(e -> clearAnswer());
        panel.add(clearAnswerButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        ActionListener answerListener = e -> {
            updateCurrentAnswer();
            if (answerChangeListener != null) {
                answerChangeListener.onAnswerChanged(currentAnswer);
            }
        };
        
        optionA.addActionListener(answerListener);
        optionB.addActionListener(answerListener);
        optionC.addActionListener(answerListener);
        optionD.addActionListener(answerListener);
    }
    
    /**
     * ✅ FIX #1: Set question with listener disabled temporarily
     */
    public void setQuestion(Question question, String existingAnswer) {
        System.out.println("🔍 [ExamQuestionPanel] setQuestion called:");
        System.out.println("  - Question: " + (question != null ? question.getQuestionText() : "NULL"));
        System.out.println("  - Existing answer: " + existingAnswer);
        
        this.currentQuestion = question;
        
        if (question != null) {
            SwingUtilities.invokeLater(() -> {
                // ✅ FIX #1: TẮT LISTENER TẠM THỜI để tránh trigger khi set answer
                AnswerChangeListener tempListener = this.answerChangeListener;
                this.answerChangeListener = null;
                
                try {
                    // Update question info
                    questionNumberLabel.setText("Question #" + question.getQuestionId());
                    System.out.println("✅ [ExamQuestionPanel] Question number set: " + question.getQuestionId());
                    
                    // Update difficulty with styling
                    String difficulty = question.getDifficulty();
                    difficultyLabel.setText("Difficulty: " + difficulty);
                    switch (difficulty) {
                        case "EASY":
                            difficultyLabel.setForeground(new Color(0, 150, 0));
                            difficultyLabel.setText("Difficulty: 🟢 Easy");
                            break;
                        case "MEDIUM":
                            difficultyLabel.setForeground(new Color(255, 140, 0));
                            difficultyLabel.setText("Difficulty: 🟡 Medium");
                            break;
                        case "HARD":
                            difficultyLabel.setForeground(Color.RED);
                            difficultyLabel.setText("Difficulty: 🔴 Hard");
                            break;
                        default:
                            difficultyLabel.setForeground(Color.BLACK);
                            break;
                    }
                    
                    // Update question text
                    if (questionTextArea != null) {
                        questionTextArea.setText(question.getQuestionText());
                        System.out.println("✅ [ExamQuestionPanel] Question text set");
                    } else {
                        System.err.println("❌ [ExamQuestionPanel] questionTextArea is null!");
                    }
                    
                    // Update options
                    if (optionA != null) {
                        optionA.setText("A. " + question.getOptionA());
                        System.out.println("✅ [ExamQuestionPanel] Options set");
                    } else {
                        System.err.println("❌ [ExamQuestionPanel] optionA is null!");
                    }
                    
                    if (optionB != null) optionB.setText("B. " + question.getOptionB());
                    if (optionC != null) optionC.setText("C. " + question.getOptionC());
                    if (optionD != null) optionD.setText("D. " + question.getOptionD());
                    
                    // ✅ Set existing answer (KHÔNG trigger callback vì listener = null)
                    setSelectedAnswer(existingAnswer);
                    
                    // Force UI refresh
                    this.revalidate();
                    this.repaint();
                    
                    System.out.println("✅ [ExamQuestionPanel] Question displayed successfully");
                    
                } finally {
                    // ✅ FIX #1: BẬT LẠI LISTENER
                    this.answerChangeListener = tempListener;
                    System.out.println("✅ [ExamQuestionPanel] Listener re-enabled");
                }
            });
        } else {
            System.err.println("❌ [ExamQuestionPanel] Question is null!");
            clearQuestion();
        }
    }

    private void setSelectedAnswer(String answer) {
        System.out.println("🔍 [ExamQuestionPanel] setSelectedAnswer: " + answer);
        
        optionGroup.clearSelection();
        currentAnswer = answer;
        
        if (answer != null) {
            switch (answer.toUpperCase()) {
                case "A":
                    optionA.setSelected(true);
                    System.out.println("✅ [ExamQuestionPanel] Option A selected");
                    break;
                case "B":
                    optionB.setSelected(true);
                    System.out.println("✅ [ExamQuestionPanel] Option B selected");
                    break;
                case "C":
                    optionC.setSelected(true);
                    System.out.println("✅ [ExamQuestionPanel] Option C selected");
                    break;
                case "D":
                    optionD.setSelected(true);
                    System.out.println("✅ [ExamQuestionPanel] Option D selected");
                    break;
            }
        }
    }
    
    private void updateCurrentAnswer() {
        if (optionA.isSelected()) {
            currentAnswer = "A";
        } else if (optionB.isSelected()) {
            currentAnswer = "B";
        } else if (optionC.isSelected()) {
            currentAnswer = "C";
        } else if (optionD.isSelected()) {
            currentAnswer = "D";
        } else {
            currentAnswer = null;
        }
    }
    
    private void clearAnswer() {
        optionGroup.clearSelection();
        currentAnswer = null;
        
        if (answerChangeListener != null) {
            answerChangeListener.onAnswerChanged(currentAnswer);
        }
    }
    
    private void clearQuestion() {
        questionNumberLabel.setText("Question #");
        difficultyLabel.setText("Difficulty: Unknown");
        difficultyLabel.setForeground(Color.BLACK);
        questionTextArea.setText("");
        optionA.setText("A. ");
        optionB.setText("B. ");
        optionC.setText("C. ");
        optionD.setText("D. ");
        optionGroup.clearSelection();
        currentAnswer = null;
    }
    
    public String getCurrentAnswer() {
        return currentAnswer;
    }
    
    public Question getCurrentQuestion() {
        return currentQuestion;
    }
    
    public void setAnswerChangeListener(AnswerChangeListener listener) {
        this.answerChangeListener = listener;
    }
    
    /**
     * Answer change listener interface
     */
    public interface AnswerChangeListener {
        void onAnswerChanged(String answer);
    }
}