// client/ui/admin/panels/AdminStatusPanel.java
package client.ui.admin.panels;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminStatusPanel extends JPanel {
    
    private JLabel statusLabel;
    private JLabel timeLabel;
    private Timer timeUpdateTimer;
    
    public AdminStatusPanel() {
        initializeUI();
        startTimeUpdater();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        statusLabel = new JLabel("Ready", SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        updateTime();
        
        add(statusLabel, BorderLayout.WEST);
        add(timeLabel, BorderLayout.EAST);
        
        setBorder(BorderFactory.createEtchedBorder());
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
        statusLabel.setText(status);
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