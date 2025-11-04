package client.ui.student.dialogs;

import javax.swing.*;
import java.awt.*;

/**
 * Exam Time Up Dialog - Shown when exam time expires
 */
public class ExamTimeUpDialog extends JDialog {
    
    public ExamTimeUpDialog(JFrame parent) {
        super(parent, "Exam Time Expired", true);
        
        initializeUI();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
        
        // Auto-close after 10 seconds
        Timer autoCloseTimer = new Timer(10000, e -> dispose());
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
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
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 220, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        
        JLabel timeUpLabel = new JLabel("⏰ TIME'S UP!");
        timeUpLabel.setFont(new Font("Arial", Font.BOLD, 32));
        timeUpLabel.setForeground(Color.RED);
        timeUpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(timeUpLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        String message = "<html><center>" +
                        "<h2>Your exam time has expired!</h2>" +
                        "<p>Your answers have been automatically submitted.</p>" +
                        "<p>Thank you for taking the exam.</p>" +
                        "<br/>" +
                        "<p><i>This dialog will close automatically in 10 seconds...</i></p>" +
                        "</center></html>";
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton okButton = new JButton("✅ OK");
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setFont(new Font("Arial", Font.BOLD, 12));
        okButton.addActionListener(e -> dispose());
        
        panel.add(okButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // ESC and Enter keys to close
        KeyStroke escapeStroke = KeyStroke.getKeyStroke("ESCAPE");
        KeyStroke enterStroke = KeyStroke.getKeyStroke("ENTER");
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, "CLOSE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterStroke, "CLOSE");
        
        getRootPane().getActionMap().put("CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        
        // Make it non-closable with X button initially
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Allow closing after 3 seconds
        Timer allowCloseTimer = new Timer(3000, e -> {
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        });
        allowCloseTimer.setRepeats(false);
        allowCloseTimer.start();
    }
}