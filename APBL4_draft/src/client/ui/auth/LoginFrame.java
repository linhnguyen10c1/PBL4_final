// client/ui/LoginFrame.java
package client.ui.auth;

import client.controller.LoginController;
import client.network.NetworkManager;
import client.ui.admin.AdminDashboard;
import client.ui.auth.interfaces.LoginViewCallbacks;
import client.ui.auth.panels.*;
import client.ui.student.StudentDashboard;
import model.User;
import utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame implements 
    LoginController.LoginListener, 
    NetworkManager.ConnectionListener, 
    LoginViewCallbacks {
    
    private NetworkManager networkManager;
    private LoginController loginController;
    
    // UI Panels
    private HeaderPanel headerPanel;
    private ConnectionPanel connectionPanel;
    private LoginPanel loginPanel;
    private StatusPanel statusPanel;
    
    public LoginFrame(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.loginController = new LoginController(networkManager);
        
        // Set listeners
        loginController.setLoginListener(this);
        networkManager.setConnectionListener(this);
        
        initializeUI();
        setupEventHandlers();
        
        // Initial state
        updateConnectionStatus(false);
    }
    
    private void initializeUI() {
        setTitle("Online Exam System - Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // Create panels
        headerPanel = new HeaderPanel();
        connectionPanel = new ConnectionPanel(this); // Pass callbacks
        loginPanel = new LoginPanel(this); // Pass callbacks
        statusPanel = new StatusPanel();
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Layout panels
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(connectionPanel, BorderLayout.NORTH);
        centerPanel.add(loginPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // ✅ THAY ĐỔI 1: Pack trước để xác định kích thước cửa sổ
        pack();
        
        // ✅ THAY ĐỔI 2: Đặt cửa sổ vào chính giữa màn hình SAU KHI đã pack()
        setLocationRelativeTo(null);
        
        // ✅ THAY ĐỔI 3 (TÙY CHỌN): Có thể set vị trí chính xác theo tọa độ màn hình
        // centerOnScreen(); // Bỏ comment dòng này nếu muốn dùng cách tính toán chính xác hơn
    }
    
    /**
     * ✅ PHƯƠNG THỨC MỚI: Đặt cửa sổ chính giữa màn hình một cách chính xác
     * Phương thức này tính toán vị trí dựa trên kích thước màn hình và kích thước cửa sổ
     */
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        
        // Tính toán vị trí x, y để cửa sổ nằm chính giữa
        int x = (screenSize.width - frameSize.width) / 2;
        int y = (screenSize.height - frameSize.height) / 2;
        
        setLocation(x, y);
        
        System.out.println("🖥️ Screen size: " + screenSize.width + "x" + screenSize.height);
        System.out.println("📐 Frame size: " + frameSize.width + "x" + frameSize.height);
        System.out.println("📍 Frame position: (" + x + ", " + y + ")");
    }
    
    private void setupEventHandlers() {
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onExitRequested();
            }
        });
    }
    
    // LoginViewCallbacks implementation
    @Override
    public void onConnectRequested(String host, String port) {
        handleConnect(host, port);
    }
    
    @Override
    public void onLoginRequested(String username, String password) {
        handleLogin(username, password);
    }
    
    @Override
    public void onExitRequested() {
        handleExit();
    }
    
    // Handler methods
    private void handleConnect(String host, String port) {
        if (networkManager.isConnected()) {
            networkManager.disconnect();
            return;
        }
        
        // Validate connection settings
        if (!ValidationUtil.isNotEmpty(host)) {
            showError("Server host is required");
            return;
        }
        
        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            showError("Invalid port number");
            return;
        }
        
        // Update network manager settings
        networkManager = new NetworkManager(host, portNumber);
        networkManager.setConnectionListener(this);
        loginController = new LoginController(networkManager);
        loginController.setLoginListener(this);
        
        // Connect in background thread
        connectionPanel.setConnectionInProgress(true);
        statusPanel.setStatus("Connecting to server...");
        
        new Thread(() -> {
            boolean connected = networkManager.connect();
            SwingUtilities.invokeLater(() -> {
                connectionPanel.setConnectionInProgress(false);
                if (!connected) {
                    updateConnectionStatus(false);
                }
            });
        }).start();
    }
    
    private void handleLogin(String username, String password) {
        if (!networkManager.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(username)) {
            showError("Username is required");
            loginPanel.focusUsername();
            return;
        }
        
        if (!ValidationUtil.isNotEmpty(password)) {
            showError("Password is required");
            loginPanel.focusPassword();
            return;
        }
        
        // Login in background thread
        loginPanel.setLoginInProgress(true);
        statusPanel.setStatus("Authenticating...");
        
        new Thread(() -> {
            loginController.login(username, password);
        }).start();
    }
    
    private void handleExit() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit?",
            "Exit Application",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            if (networkManager.isConnected()) {
                networkManager.disconnect();
            }
            System.exit(0);
        }
    }
    
    private void updateConnectionStatus(boolean connected) {
        String serverAddress = connected ? networkManager.getServerAddress() : null;
        connectionPanel.updateConnectionStatus(connected, serverAddress);
        loginPanel.setLoginEnabled(connected);
        
        if (connected) {
            statusPanel.setStatus("Connected - Ready to login");
            loginPanel.focusUsername();
        } else {
            statusPanel.setStatus("Not connected to server");
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // LoginController.LoginListener implementation
    @Override
    public void onLoginSuccess(User user) {
        SwingUtilities.invokeLater(() -> {
            loginPanel.setLoginInProgress(false);
            
            System.out.println("✅ Login successful: " + user.getUsername() + " (Role: " + user.getRole() + ")");
            
            // Hide login frame
            setVisible(false);
            
            // Open appropriate dashboard based on user role
            if (user.isAdmin()) {
                AdminDashboard adminDashboard = new AdminDashboard(networkManager, loginController);
                adminDashboard.setVisible(true);
            } else if (user.isStudent()) {
                StudentDashboard studentDashboard = new StudentDashboard(networkManager, loginController);
                studentDashboard.setVisible(true);
            }
            
            // Dispose login frame
            dispose();
        });
    }
    
    @Override
    public void onLoginFailed(String message) {
        SwingUtilities.invokeLater(() -> {
            loginPanel.setLoginInProgress(false);
            statusPanel.setStatus("Login failed: " + message);
            showError("Login failed: " + message);
            loginPanel.clearPassword();
            loginPanel.focusPassword();
        });
    }
    
    @Override
    public void onLogoutSuccess() {
        // Not used in login frame
    }
    
    // NetworkManager.ConnectionListener implementation
    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus(true);
        });
    }
    
    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus(false);
        });
    }
    
    @Override
    public void onConnectionFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus(false);
            statusPanel.setStatus("Connection failed: " + reason);
            showError("Connection failed: " + reason);
        });
    }
    
    @Override
    public void onConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus(false);
            statusPanel.setStatus("Connection lost");
            showError("Connection to server was lost");
        });
    }
}