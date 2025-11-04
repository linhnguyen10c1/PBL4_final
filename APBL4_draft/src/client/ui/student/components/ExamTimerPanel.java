package client.ui.student.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ExamTimerPanel - Component for displaying exam timer
 */
public class ExamTimerPanel extends JPanel {
    
    private Timer timer;
    private int remainingSeconds;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private TimeExpiredListener timeExpiredListener;
    
    // Timer settings
    private int totalDurationMinutes;
    private boolean isWarningShown = false;
    private boolean isCriticalWarningShown = false;
    
    public ExamTimerPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Time Remaining"));
        setPreferredSize(new Dimension(250, 80));
        
        // Time display
        timeLabel = new JLabel("--:--", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timeLabel.setForeground(new Color(0, 100, 200));
        
        // Status
        statusLabel = new JLabel("Timer not started", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setPreferredSize(new Dimension(0, 10));
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(timeLabel, BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }
    
    public void startTimer(int durationMinutes) {
        this.totalDurationMinutes = durationMinutes;
        this.remainingSeconds = durationMinutes * 60;
        
        // Reset warnings
        isWarningShown = false;
        isCriticalWarningShown = false;
        
        // Update display
        updateDisplay();
        
        // Start timer (update every second)
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                updateDisplay();
                
                // Check for warnings
                checkTimeWarnings();
                
                // Check if time expired
                if (remainingSeconds <= 0) {
                    timer.stop();
                    handleTimeExpired();
                }
            }
        });
        
        timer.start();
        statusLabel.setText("Timer started");
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
            statusLabel.setText("Timer stopped");
        }
    }
    
    public void pauseTimer() {
        if (timer != null) {
            timer.stop();
            statusLabel.setText("Timer paused");
        }
    }
    
    public void resumeTimer() {
        if (timer != null && remainingSeconds > 0) {
            timer.start();
            statusLabel.setText("Timer resumed");
        }
    }
    
    private void updateDisplay() {
        // Calculate minutes and seconds
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        
        // Format time display
        String timeText = String.format("%02d:%02d", minutes, seconds);
        timeLabel.setText(timeText);
        
        // Update progress bar
        if (totalDurationMinutes > 0) {
            int totalSeconds = totalDurationMinutes * 60;
            int progress = (int) (((double) (totalSeconds - remainingSeconds) / totalSeconds) * 100);
            progressBar.setValue(progress);
            progressBar.setString(timeText + " remaining");
        }
        
        // Update colors based on remaining time
        updateTimerColors();
    }
    
    private void updateTimerColors() {
        int minutes = remainingSeconds / 60;
        
        if (minutes <= 2) { // Critical - Red
            timeLabel.setForeground(Color.RED);
            progressBar.setForeground(Color.RED);
            statusLabel.setText("⚠️ Time almost up!");
            statusLabel.setForeground(Color.RED);
        } else if (minutes <= 5) { // Warning - Orange
            timeLabel.setForeground(new Color(255, 140, 0));
            progressBar.setForeground(new Color(255, 140, 0));
            statusLabel.setText("⏰ Time running low");
            statusLabel.setForeground(new Color(255, 140, 0));
        } else { // Normal - Blue
            timeLabel.setForeground(new Color(0, 100, 200));
            progressBar.setForeground(new Color(0, 150, 0));
            statusLabel.setText("Timer running");
            statusLabel.setForeground(Color.GRAY);
        }
    }
    
    private void checkTimeWarnings() {
        int minutes = remainingSeconds / 60;
        
        // 5-minute warning
        if (minutes <= 5 && !isWarningShown) {
            isWarningShown = true;
            showTimeWarning("5 minutes remaining", 
                "You have 5 minutes left to complete the exam.", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        // 2-minute critical warning
        if (minutes <= 2 && !isCriticalWarningShown) {
            isCriticalWarningShown = true;
            showTimeWarning("2 minutes remaining", 
                "Critical: Only 2 minutes left! Please finish soon.", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showTimeWarning(String title, String message, int messageType) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                message,
                title,
                messageType
            );
        });
    }
    
    private void handleTimeExpired() {
        timeLabel.setText("00:00");
        timeLabel.setForeground(Color.RED);
        statusLabel.setText("⏰ Time expired!");
        statusLabel.setForeground(Color.RED);
        progressBar.setValue(100);
        progressBar.setString("Time expired");
        progressBar.setForeground(Color.RED);
        
        // Notify listener
        if (timeExpiredListener != null) {
            SwingUtilities.invokeLater(() -> {
                timeExpiredListener.onTimeExpired();
            });
        }
    }
    
    public int getRemainingMinutes() {
        return remainingSeconds / 60;
    }
    
    public int getRemainingSeconds() {
        return remainingSeconds;
    }
    
    public boolean isTimerRunning() {
        return timer != null && timer.isRunning();
    }
    
    public void setTimeExpiredListener(TimeExpiredListener listener) {
        this.timeExpiredListener = listener;
    }
    
    /**
     * Time expired listener interface
     */
    public interface TimeExpiredListener {
        void onTimeExpired();
    }
}