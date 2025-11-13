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
    
    // ✅ THÊM: Lưu reference dashboard hiện tại
    private JFrame currentDashboard;
    
    public LoginFrame(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.loginController = new LoginController(networkManager);
        
        // Set listeners
        loginController.setLoginListener(this);
        networkManager.setConnectionListener(this);
        
        initializeUI();
        setupEventHandlers();
        
        // Initial state
        updateConnectionStatus(networkManager.isConnected());
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
        
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * ✅ PHƯƠNG THỨC MỚI: Đặt cửa sổ chính giữa màn hình một cách chính xác
     */
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        
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
    
    // ========================================
    // ✅ PHƯƠNG THỨC MỚI: Reset LoginFrame về trạng thái ban đầu
    // ========================================
    /**
     * Reset LoginFrame về trạng thái ban đầu khi logout
     * - Clear password field
     * - Reset status
     * - Focus vào username
     */
    public void resetForLogout() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🔄 Resetting LoginFrame for logout...");
            
            // Clear password
            loginPanel.clearPassword();
            
            // Reset login state
            loginPanel.setLoginInProgress(false);
            
            // Update status
            if (networkManager.isConnected()) {
                statusPanel.setStatus("Logged out successfully - Ready to login again");
                loginPanel.setLoginEnabled(true);
            } else {
                statusPanel.setStatus("Logged out - Please reconnect to server");
                loginPanel.setLoginEnabled(false);
            }
            
            // Focus username field
            loginPanel.focusUsername();
            
            // Đưa cửa sổ lên trên cùng
            toFront();
            requestFocus();
            
            System.out.println("✅ LoginFrame reset complete");
        });
    }
    
    /**
     * ✅ PHƯƠNG THỨC MỚI: Hiển thị lại LoginFrame sau khi logout
     */
    public void showAfterLogout() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🔄 Showing LoginFrame after logout...");
            
            // Reset UI
            resetForLogout();
            
            // Hiển thị lại frame
            setVisible(true);
            
            // Đưa lên trên cùng và focus
            setState(JFrame.NORMAL);
            toFront();
            requestFocus();
            
            System.out.println("✅ LoginFrame is now visible");
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
            
            // ✅ ẨN (không dispose) login frame
            setVisible(false);
            
            // Open appropriate dashboard based on user role
            if (user.isAdmin()) {
                currentDashboard = new AdminDashboard(networkManager, loginController, this);
                currentDashboard.setVisible(true);
            } else if (user.isStudent()) {
                currentDashboard = new StudentDashboard(networkManager, loginController, this);
                currentDashboard.setVisible(true);
            }
            
            // ✅ KHÔNG dispose() login frame nữa - giữ lại để reuse
            // dispose(); // <-- REMOVED
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
        // ✅ PHƯƠNG THỨC MỚI: Được gọi từ dashboard khi logout
        SwingUtilities.invokeLater(() -> {
            System.out.println("🔄 onLogoutSuccess called in LoginFrame");
            
            // Dispose current dashboard
            if (currentDashboard != null) {
                currentDashboard.dispose();
                currentDashboard = null;
            }
            
            // Show login frame again
            showAfterLogout();
        });
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
    
    // ✅ GETTER: Cho phép dashboard access LoginFrame
    public LoginController getLoginController() {
        return loginController;
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
}