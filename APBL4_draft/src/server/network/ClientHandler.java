package server.network;

import utils.Protocol;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Client Handler - Handles individual client connections
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:37:11 UTC
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final ProtocolHandler protocolHandler;
    private final String clientId;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isRunning;
    private String currentSessionToken;
    
    public ClientHandler(Socket clientSocket, ProtocolHandler protocolHandler) {
        this.clientSocket = clientSocket;
        this.protocolHandler = protocolHandler;
        this.clientId = clientSocket.getRemoteSocketAddress().toString();
        this.isRunning = true;
        
        try {
            // Set socket timeout for reading
            clientSocket.setSoTimeout(300000); // 5 minutes timeout
            
            // Initialize streams
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            System.out.println("✅ Client connected: " + clientId + " at " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
        } catch (IOException e) {
            System.err.println("Error initializing client handler: " + e.getMessage());
            closeConnection();
        }
    }
    
    @Override
    public void run() {
        try {
            // Send welcome message
            sendResponse(Protocol.SUCCESS + Protocol.DELIMITER + "Connected to Exam Server v1.0");
            
            // Main communication loop
            String inputLine;
            while (isRunning && (inputLine = reader.readLine()) != null) {
                try {
                    // Handle client disconnect
                    if (Protocol.DISCONNECT.equals(inputLine.trim())) {
                        System.out.println("🔄 Client requested disconnect: " + clientId);
                        break;
                    }
                    
                    // Process request
                    String response = processRequest(inputLine);
                    
                    // Send response
                    sendResponse(response);
                    
                     } catch (Throwable t) { // thay vì chỉ Exception
                    System.err.println("FATAL error processing request from " + clientId);
                    t.printStackTrace();
                    sendResponse(Protocol.ERROR + Protocol.DELIMITER + "Fatal server error");
                    break;
                }
            }
            
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Connection error with client " + clientId + ": " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    /**
     * Process client request
     */
    private String processRequest(String request) {
        try {
            if (request == null || request.trim().isEmpty()) {
                return Protocol.INVALID_REQUEST + Protocol.DELIMITER + "Empty request";
            }
            System.out.println("📨 Raw request: [" + request + "]");

            // Parse request: ACTION|DATA
            String[] parts = request.split("\\" + Protocol.DELIMITER, 2);
            if (parts.length < 1) {
                return Protocol.INVALID_REQUEST + Protocol.DELIMITER + "Invalid request format";
            }
            
            String action = parts[0].trim();
            String data = parts.length > 1 ? parts[1] : "";
            
            // Log request (excluding sensitive data)
            if (!action.equals(Protocol.LOGIN) && !action.equals(Protocol.PING)) {
                System.out.println("📨 Request from " + clientId + ": " + action);
            }
            
            // Handle request
            String response = protocolHandler.handleRequest(action, data, currentSessionToken);
            
            // Update session token if login was successful
            if (action.equals(Protocol.LOGIN) && response.startsWith(Protocol.LOGIN_SUCCESS)) {
                String[] responseParts = response.split("\\" + Protocol.DELIMITER);
                if (responseParts.length >= 2) {
                    currentSessionToken = responseParts[1];
                    System.out.println("🔐 Session established for client: " + clientId);
                }
            }
            
            // Clear session token on logout
            if (action.equals(Protocol.LOGOUT)) {
                currentSessionToken = null;
                System.out.println("🔓 Session cleared for client: " + clientId);
            }
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            return Protocol.ERROR + Protocol.DELIMITER + "Request processing failed";
        }
    }
    
    /**
     * Send response to client
     */
    private void sendResponse(String response) {
        try {
            if (writer != null && !clientSocket.isClosed()) {
                writer.println(response);
                writer.flush();
                
                // Log response (excluding sensitive data)
                if (!response.startsWith(Protocol.PONG) && !response.contains("password")) {
                    String logResponse = response.length() > 100 ? 
                        response.substring(0, 100) + "..." : response;
                    System.out.println("📤 Response to " + clientId + ": " + logResponse);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending response to " + clientId + ": " + e.getMessage());
            closeConnection();
        }
    }
    
    /**
     * Close client connection
     */
    public void closeConnection() {
        isRunning = false;
        
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup resources and logout user
     */
    private void cleanup() {
        // Logout user if logged in
        if (currentSessionToken != null) {
            try {
                protocolHandler.handleRequest(Protocol.LOGOUT, "", currentSessionToken);
                System.out.println("🔄 Auto-logout for client: " + clientId);
            } catch (Exception e) {
                System.err.println("Error during auto-logout: " + e.getMessage());
            }
        }
        
        closeConnection();
        System.out.println("❌ Client disconnected: " + clientId + " at " + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * Check if client is still connected
     */
    public boolean isConnected() {
        return isRunning && clientSocket != null && !clientSocket.isClosed();
    }
    
    /**
     * Get client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Get current session token
     */
    public String getCurrentSessionToken() {
        return currentSessionToken;
    }
    
    /**
     * Send heartbeat to check connection
     */
    public boolean sendHeartbeat() {
        try {
            sendResponse(Protocol.PING);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}