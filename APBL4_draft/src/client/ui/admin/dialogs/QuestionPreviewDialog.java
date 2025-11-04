package client.ui.admin.dialogs;

import model.Question;

import javax.swing.*;
import java.awt.*;

/**
 * Question Preview Dialog - Shows question in exam format
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 04:06:50 UTC
 */
public class QuestionPreviewDialog extends JDialog {
    
    private Question question;
    
    public QuestionPreviewDialog(JFrame parent, Question question) {
        super(parent, "Question Preview", true);
        this.question = question;
        
        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Question Preview");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel infoLabel = new JLabel(String.format(
            "Subject: %s | Difficulty: %s | Correct Answer: %s",
            question.getSubjectName(),
            question.getDifficulty(),
            question.getCorrectAnswer()
        ));
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC, 12f));
        infoLabel.setForeground(Color.GRAY);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        // Question panel
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBorder(BorderFactory.createTitledBorder("Question"));
        
        // Question text
        JTextArea questionTextArea = new JTextArea(question.getQuestionText());
        questionTextArea.setEditable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setBackground(getBackground());
        questionTextArea.setFont(questionTextArea.getFont().deriveFont(14f));
        questionTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        questionPanel.add(questionTextArea);
        
        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        
        // Option A
        JPanel optionAPanel = createOptionPanel("A", question.getOptionA(), 
                                              "A".equals(question.getCorrectAnswer()));
        optionsPanel.add(optionAPanel);
        optionsPanel.add(Box.createVerticalStrut(8));
        
        // Option B
        JPanel optionBPanel = createOptionPanel("B", question.getOptionB(), 
                                              "B".equals(question.getCorrectAnswer()));
        optionsPanel.add(optionBPanel);
        optionsPanel.add(Box.createVerticalStrut(8));
        
        // Option C
        JPanel optionCPanel = createOptionPanel("C", question.getOptionC(), 
                                              "C".equals(question.getCorrectAnswer()));
        optionsPanel.add(optionCPanel);
        optionsPanel.add(Box.createVerticalStrut(8));
        
        // Option D
        JPanel optionDPanel = createOptionPanel("D", question.getOptionD(), 
                                              "D".equals(question.getCorrectAnswer()));
        optionsPanel.add(optionDPanel);
        
        questionPanel.add(optionsPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(questionPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setPreferredSize(new Dimension(600, 500));
    }
    
    private JPanel createOptionPanel(String letter, String text, boolean isCorrect) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create radio button (just for visual, not functional)
        JRadioButton radioButton = new JRadioButton();
        radioButton.setEnabled(false);
        
        // Option label
        JLabel letterLabel = new JLabel(letter + ".");
        letterLabel.setFont(letterLabel.getFont().deriveFont(Font.BOLD));
        letterLabel.setPreferredSize(new Dimension(20, letterLabel.getPreferredSize().height));
        
        // Option text
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(getBackground());
        textArea.setOpaque(false);
        
        // Highlight correct answer
        if (isCorrect) {
            panel.setBackground(new Color(220, 255, 220)); // Light green
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 0), 2),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
            ));
            letterLabel.setForeground(new Color(0, 120, 0));
            letterLabel.setText(letter + ". ✓");
        } else {
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        }
        
        panel.add(radioButton, BorderLayout.WEST);
        panel.add(letterLabel, BorderLayout.CENTER);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(textArea, BorderLayout.CENTER);
        panel.add(textPanel, BorderLayout.EAST);
        
        return panel;
    }
}