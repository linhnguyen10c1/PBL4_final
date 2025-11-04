package client.ui.student.dialogs;

import model.ExamRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Exam Password Dialog - Updated with better UI
 */
public class ExamPasswordDialog extends JDialog {
    
    private ExamRoom examRoom;
    private JPasswordField passwordField;
    private JButton joinButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    
    public ExamPasswordDialog(JFrame parent, ExamRoom examRoom) {
        super(parent, "Enter Exam Password", true);
        this.examRoom = examRoom;
        
        initializeUI();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
        
        // Focus on password field
        SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Header panel with exam info
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Input panel
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🎓 Join Exam Room");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 100, 200));
        
        // Exam info
        String examInfo = String.format(
            "<html><b>%s</b><br/>Subject: %s<br/>Duration: %d minutes | Questions: %d | Score: %.0f points</html>",
            examRoom.getRoomName(),
            examRoom.getSubjectName(),
            examRoom.getDurationMinutes(),
            examRoom.getQuestionCount(),
            examRoom.getTotalScore()
        );
        
        JLabel infoLabel = new JLabel(examInfo);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Password label
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel passwordLabel = new JLabel("🔒 Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passwordLabel, gbc);
        
        // Password field
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        panel.add(passwordField, gbc);
        
        // Info label
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel infoLabel = new JLabel("Enter the password provided by your instructor");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        joinButton = new JButton("🎯 Join Exam");
        joinButton.setPreferredSize(new Dimension(120, 35));
        joinButton.setFont(new Font("Arial", Font.BOLD, 12));
        joinButton.setBackground(new Color(220, 255, 220));
        
        cancelButton = new JButton("❌ Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(joinButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Button listeners
        joinButton.addActionListener(e -> handleJoin());
        cancelButton.addActionListener(e -> handleCancel());
        
        // Enter key in password field
        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleJoin();
                }
            }
            
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {}
        });
        
        // Password field change listener
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateJoinButton(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateJoinButton(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateJoinButton(); }
        });
        
        // Set default button
        getRootPane().setDefaultButton(joinButton);
        
        // Initially disable join button
        updateJoinButton();
    }
    
    private void updateJoinButton() {
        String password = new String(passwordField.getPassword()).trim();
        joinButton.setEnabled(!password.isEmpty());
    }
    
    private void handleJoin() {
        String password = new String(passwordField.getPassword()).trim();
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the exam password.", 
                "Password Required", 
                JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }
        
        confirmed = true;
        dispose();
    }
    
    private void handleCancel() {
        confirmed = false;
        dispose();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}