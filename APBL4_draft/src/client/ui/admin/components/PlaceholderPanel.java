// client/ui/admin/components/PlaceholderPanel.java
package client.ui.admin.components;

import javax.swing.*;
import java.awt.*;

public class PlaceholderPanel extends JPanel {
    
    public PlaceholderPanel(String title) {
        initializeUI(title);
    }
    
    private void initializeUI(String title) {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel(title + " - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        
        add(label, BorderLayout.CENTER);
        
        // Add some decoration
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    }
}