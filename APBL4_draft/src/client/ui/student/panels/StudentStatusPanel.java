package client.ui.student.panels;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudentStatusPanel extends JPanel {
    
    private JLabel statusLabel;
    private JLabel timeLabel;
    private Timer timeUpdateTimer;
    
    public StudentStatusPanel() {
        initializeUI();
        startTimeUpdater();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        
        statusLabel = new JLabel("Ready", SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        updateTime();
        
        add(statusLabel, BorderLayout.WEST);
        add(timeLabel, BorderLayout.EAST);
    }
    
    private void startTimeUpdater() {
        timeUpdateTimer = new Timer(1000, e -> updateTime());
        timeUpdateTimer.start();
    }
    
    private void updateTime() {
        String currentTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        timeLabel.setText("Current Time: " + currentTime);
    }
    
    public void setStatus(String status) {
        setStatus(status, Color.BLACK);
    }
    
    public void setStatus(String status, Color color) {
        statusLabel.setText(status);
        statusLabel.setForeground(color);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timeUpdateTimer != null) {
            timeUpdateTimer.stop();
        }
    }
}