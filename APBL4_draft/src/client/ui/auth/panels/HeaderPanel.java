// client/ui/panels/HeaderPanel.java
package client.ui.auth.panels;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {
    
    public HeaderPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Online Exam System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 102, 153));
        
        JLabel subtitleLabel = new JLabel("Student & Administrator Portal", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        add(titleLabel, BorderLayout.CENTER);
        add(subtitleLabel, BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    }
}