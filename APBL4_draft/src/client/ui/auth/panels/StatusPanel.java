// client/ui/panels/StatusPanel.java
package client.ui.auth.panels;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    
    private JLabel statusLabel;
    
    public StatusPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        statusLabel = new JLabel("Ready to connect", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        add(statusLabel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(new JLabel("Default Admin: admin/admin123"));
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}