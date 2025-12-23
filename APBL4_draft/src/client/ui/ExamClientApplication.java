package client.ui;

import client.network.NetworkManager;
import client.ui.auth.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

public class ExamClientApplication{
	public static void main(String[] args) {
		try {
		    UIManager.setLookAndFeel(UIManager.getLookAndFeel());
		} catch(Exception e) {
			System.err.println("Could not set system look and feel: " + e.getMessage());
		}
	    SwingUtilities.invokeLater(() ->{
	    	try {
		    	System.out.println("Online exam system - client");
	            System.out.println("=====================================");
	            System.out.println("📅 Date: 2025-09-14 13:41:41 UTC");
	            System.out.println("👨‍💻 Author: Khiem - Linh");
	            System.out.println("🏫 Project: PBL4 - DUT");
	            System.out.println("=====================================");
	    	    NetworkManager networkManager = new NetworkManager();
	    	    
	    	    LoginFrame loginFrame = new LoginFrame(networkManager);
	    	    
	    	    loginFrame.setVisible(true);
	    	    System.out.println("Client application started");
	    	} catch(Exception e) {
	    		System.err.println("Failed to start client application: " + e.getMessage());
	    		e.printStackTrace();
	    	
	    	JOptionPane.showMessageDialog(null, 
	    			"Failed to start application: " + e.getMessage(),
	    			"Startup Error",
	    			JOptionPane.ERROR_MESSAGE);
	    	System.exit(1);
	    }
	    }
	    	);
	    
	    }
	}

