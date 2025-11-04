package client.ui.student.dialogs;

import model.ExamResult;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;

/**
 * Exam Result Dialog - Enhanced version for showing detailed results
 */
public class ExamResultDialog extends JDialog {
    
    private ExamResult result;
    
    public ExamResultDialog(JFrame parent, ExamResult result) {
        super(parent, "Exam Result Details", true);
        this.result = result;
        
        initializeUI();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setResizable(true);
        
        // Header with result status
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with details
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer with buttons
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        setPreferredSize(new Dimension(600, 700));
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        boolean passed = result.isPassed();
        Color bgColor = passed ? new Color(220, 255, 220) : new Color(255, 220, 220);
        panel.setBackground(bgColor);
        panel.setOpaque(true);
        
        // Result status
        String statusIcon = passed ? "🎉" : "📚";
        String statusText = passed ? "CONGRATULATIONS!" : "KEEP STUDYING!";
        
        JLabel statusLabel = new JLabel(statusIcon + " " + statusText);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 24));
        statusLabel.setForeground(passed ? new Color(0, 150, 0) : Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Score display
        JLabel scoreLabel = new JLabel(String.format("%.1f%%", result.getPercentage()));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 36));
        scoreLabel.setForeground(passed ? new Color(0, 150, 0) : Color.RED);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Grade display
        JLabel gradeLabel = new JLabel("Grade: " + result.getGrade());
        gradeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(statusLabel, BorderLayout.NORTH);
        centerPanel.add(scoreLabel, BorderLayout.CENTER);
        centerPanel.add(gradeLabel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Create tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Exam Info tab
        JPanel examInfoPanel = createExamInfoPanel();
        tabbedPane.addTab("📝 Exam Info", examInfoPanel);
        
        // Performance tab
        JPanel performancePanel = createPerformancePanel();
        tabbedPane.addTab("📊 Performance", performancePanel);
        
        // Statistics tab
        JPanel statisticsPanel = createStatisticsPanel();
        tabbedPane.addTab("📈 Statistics", statisticsPanel);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExamInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        addInfoRow(panel, gbc, row++, "Exam:", result.getRoomName());
        addInfoRow(panel, gbc, row++, "Subject:", result.getSubjectName());
        addInfoRow(panel, gbc, row++, "Student:", result.getStudentName());
        addInfoRow(panel, gbc, row++, "Date:", result.getSubmittedAt());
        
        // Add separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        addInfoRow(panel, gbc, row++, "Status:", getStatusDisplay(result.getStatus()));
        addInfoRow(panel, gbc, row++, "Time Spent:", result.getFormattedTimeSpent());
        addInfoRow(panel, gbc, row++, "Time Limit:", result.getTimeLimitMinutes() + " minutes");
        
        return panel;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Score section
        addInfoRow(panel, gbc, row++, "Your Score:", 
                  String.format("%.1f / %.1f points", result.getTotalScore(), result.getMaxScore()));
        addInfoRow(panel, gbc, row++, "Percentage:", 
                  String.format("%.1f%%", result.getPercentage()));
        addInfoRow(panel, gbc, row++, "Grade:", result.getGrade());
        addInfoRow(panel, gbc, row++, "Result:", result.isPassed() ? "✅ PASSED" : "❌ FAILED");
        
        // Add separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Question analysis
        addInfoRow(panel, gbc, row++, "Total Questions:", String.valueOf(result.getTotalQuestions()));
        addInfoRow(panel, gbc, row++, "Correct Answers:", 
                  String.format("%d (%.1f%%)", result.getCorrectAnswers(), 
                  ((double)result.getCorrectAnswers() / result.getTotalQuestions() * 100)));
        addInfoRow(panel, gbc, row++, "Wrong Answers:", 
                  String.format("%d (%.1f%%)", (result.getTotalQuestions() - result.getCorrectAnswers()),
                  ((double)(result.getTotalQuestions() - result.getCorrectAnswers()) / result.getTotalQuestions() * 100)));
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Performance message
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Performance Feedback"));
        
        String performanceMessage = getPerformanceMessage(result.getPercentage());
        JLabel messageLabel = new JLabel(performanceMessage);
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        
        // Ranking (if available)
        if (result.getRanking() > 0) {
            JPanel rankingPanel = new JPanel(new BorderLayout());
            rankingPanel.setBorder(BorderFactory.createTitledBorder("Class Ranking"));
            
            String rankingText = String.format(
                "<html><center>Your Rank: <b>%d</b> out of <b>%d</b> students<br/>" +
                "You performed better than <b>%.1f%%</b> of students</center></html>",
                result.getRanking(), result.getTotalParticipants(),
                ((double)(result.getTotalParticipants() - result.getRanking()) / result.getTotalParticipants() * 100)
            );
            
            JLabel rankingLabel = new JLabel(rankingText);
            rankingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            rankingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rankingLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            rankingPanel.add(rankingLabel, BorderLayout.CENTER);
            
            panel.add(rankingPanel, BorderLayout.NORTH);
        }
        
        panel.add(messagePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(valueComponent, gbc);
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        
        JButton printButton = new JButton("🖨️ Print Result");
        printButton.setPreferredSize(new Dimension(130, 35));
        printButton.addActionListener(e -> printResult());
        
        JButton closeButton = new JButton("✅ Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());
        
        panel.add(printButton);
        panel.add(closeButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // ESC key to close
        KeyStroke escapeStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }
    
    private String getStatusDisplay(String status) {
        switch (status) {
            case "SUBMITTED":
                return "✅ Manually Submitted";
            case "AUTO_SUBMITTED":
                return "⏰ Auto-submitted (Time expired)";
            default:
                return status;
        }
    }
    
    private String getPerformanceMessage(double percentage) {
        if (percentage >= 95) {
            return "🌟 Outstanding! Excellent mastery of the subject matter!";
        } else if (percentage >= 85) {
            return "🎉 Excellent work! You have a strong understanding of the material.";
        } else if (percentage >= 75) {
            return "👍 Good job! You demonstrate solid knowledge with room for improvement.";
        } else if (percentage >= 60) {
            return "✓ You passed! Continue studying to strengthen your understanding.";
        } else {
            return "📚 Don't give up! Review the material and try again when possible.";
        }
    }
    
    private void printResult() {
        try {
            // Create a simple text area with result summary
            JTextArea printArea = new JTextArea();
            printArea.setText(generatePrintableResult());
            printArea.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to print result: " + e.getMessage(), 
                "Print Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generatePrintableResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EXAM RESULT REPORT ===\n\n");
        sb.append("Student: ").append(result.getStudentName()).append("\n");
        sb.append("Exam: ").append(result.getRoomName()).append("\n");
        sb.append("Subject: ").append(result.getSubjectName()).append("\n");
        sb.append("Date: ").append(result.getSubmittedAt()).append("\n\n");
        
        sb.append("=== PERFORMANCE ===\n");
        sb.append("Score: ").append(String.format("%.1f / %.1f", result.getTotalScore(), result.getMaxScore())).append("\n");
        sb.append("Percentage: ").append(String.format("%.1f%%", result.getPercentage())).append("\n");
        sb.append("Grade: ").append(result.getGrade()).append("\n");
        sb.append("Result: ").append(result.isPassed() ? "PASSED" : "FAILED").append("\n\n");
        
        sb.append("=== STATISTICS ===\n");
        sb.append("Correct Answers: ").append(result.getCorrectAnswers()).append(" / ").append(result.getTotalQuestions()).append("\n");
        sb.append("Time Spent: ").append(result.getFormattedTimeSpent()).append("\n");
        sb.append("Time Limit: ").append(result.getTimeLimitMinutes()).append(" minutes\n");
        
        if (result.getRanking() > 0) {
            sb.append("Class Ranking: ").append(result.getRanking()).append(" / ").append(result.getTotalParticipants()).append("\n");
        }
        
        sb.append("\n=== END OF REPORT ===");
        
        return sb.toString();
    }
}