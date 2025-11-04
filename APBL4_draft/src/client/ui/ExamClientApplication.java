package client.ui;

import client.network.NetworkManager;
import client.ui.auth.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

public class ExamClientApplication{
	// hàm main đặt ở đây để làm gì 
	public static void main(String[] args) {
		try {
		    UIManager.setLookAndFeel(UIManager.getLookAndFeel());
		} catch(Exception e) {
			System.err.println("Could not set system look and feel: " + e.getMessage());
		}
		//.invokeLater là gì đây?
	    SwingUtilities.invokeLater(() ->{
	    	try {
		    	System.out.println("Online exam system - client");
	            System.out.println("=====================================");
	            System.out.println("📅 Date: 2025-09-14 13:41:41 UTC");
	            System.out.println("👨‍💻 Author: claude");
	            System.out.println("🏫 Project: PBL4 - DUT");
	            System.out.println("=====================================");
	    	    // tại sao tự dưng lại tạo một network manager đây làm gì vậy cha
	            // hàm tạo ra đối tượng networkManger có những gì 
	    	    NetworkManager networkManager = new NetworkManager();
	    	    
	    	    // tại sao tạo Network Manager rồi mới tạo frame login vậy logic ở đây có vấn đề gì không
	    	    // rồi truyền đối tượng networkManager vào LoginFrame có ý nghĩa gì 
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

