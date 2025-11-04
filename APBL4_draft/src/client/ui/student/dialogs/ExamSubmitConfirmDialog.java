package client.ui.student.dialogs;

import javax.swing.*;
import java.awt.*;

/**
 * Exam Submit Confirm Dialog - Confirmation before submitting exam
 */
public class ExamSubmitConfirmDialog extends JDialog {
    
    private boolean confirmed = false;
    private int totalQuestions;
    private int answeredQuestions;
    private int remainingMinutes;
    
    public ExamSubmitConfirmDialog(JFrame parent, int totalQuestions, int answeredQuestions, int remainingMinutes) {
        super(parent, "Confirm Exam Submission", true);
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.remainingMinutes = remainingMinutes;
        
        initializeUI();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 248, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("⚠️ Confirm Exam Submission");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(200, 100, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Please review your answers before submitting");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        panel.add(summaryPanel, BorderLayout.CENTER);
        
        // Warning panel
        JPanel warningPanel = createWarningPanel();
        panel.add(warningPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Exam Summary"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Total questions
        addSummaryRow(panel, gbc, row++, "Total Questions:", String.valueOf(totalQuestions));
        
        // Answered questions
        String answeredText = String.format("%d (%d%%)", answeredQuestions, 
                                           (int)((double)answeredQuestions / totalQuestions * 100));
        addSummaryRow(panel, gbc, row++, "Answered Questions:", answeredText);
        
        // Unanswered questions
        int unansweredQuestions = totalQuestions - answeredQuestions;
        String unansweredText = String.format("%d (%d%%)", unansweredQuestions,
                                            (int)((double)unansweredQuestions / totalQuestions * 100));
        addSummaryRow(panel, gbc, row++, "Unanswered Questions:", unansweredText);
        
        // Time remaining
        String timeText = remainingMinutes > 0 ? 
                         String.format("%d minutes", remainingMinutes) : "Time expired";
        addSummaryRow(panel, gbc, row++, "Time Remaining:", timeText);
        
        return panel;
    }
    
    private void addSummaryRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Color code based on content
        if (label.contains("Unanswered") && !value.startsWith("0")) {
            valueComponent.setForeground(Color.RED);
        } else if (label.contains("Time") && remainingMinutes <= 5) {
            valueComponent.setForeground(Color.RED);
        }
        
        panel.add(valueComponent, gbc);
    }
    
    private JPanel createWarningPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        String warningText;
        Color warningColor;
        
        if (answeredQuestions < totalQuestions) {
            warningText = String.format(
                "<html><center><b>⚠️ Warning:</b> You have %d unanswered questions.<br/>" +
                "Once submitted, you cannot change your answers.</center></html>",
                totalQuestions - answeredQuestions
            );
            warningColor = Color.RED;
        } else {
            warningText = "<html><center><b>✅ All questions answered!</b><br/>" +
                         "Once submitted, you cannot change your answers.</center></html>";
            warningColor = new Color(0, 150, 0);
        }
        
        JLabel warningLabel = new JLabel(warningText);
        warningLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        warningLabel.setForeground(warningColor);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(warningLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        
        JButton submitButton = new JButton("✅ Submit Exam");
        submitButton.setPreferredSize(new Dimension(140, 40));
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));
        submitButton.setBackground(new Color(220, 255, 220));
        submitButton.addActionListener(e -> handleSubmit());
        
        JButton cancelButton = new JButton("📝 Continue Exam");
        cancelButton.setPreferredSize(new Dimension(140, 40));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> handleCancel());
        
        panel.add(cancelButton);
        panel.add(submitButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // ESC key to cancel
        KeyStroke escapeStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handleCancel();
            }
        });
    }
    
    private void handleSubmit() {
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}