package client.network;

//import utils.Protocol;
import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import utils.Protocol;

public class NetworkManager{
	
	// tại sao mấy thuộc tính private static final này không để vào Constants để đồng nhất cách gọi với server-side
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8888; 
	private static final int CONNECTION_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 30000;
	
	private Socket socket;
	// tại sao cần phải các thuộc tính này rứa chứ không phải Input, Output 
	private BufferedReader reader;
	private PrintWriter writer;
	private boolean isConnected;
	private String serverHost;
	private int serverPort;
	
	// thuộc tính ConnectionListener đây để làm gì 
	private ConnectionListener connectionListener;
	
	public NetworkManager() {
		// lại gọi hàm tạo chính nó nữa à
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	public NetworkManager(String host, int port) {
		this.serverHost = host;
		this.serverPort = port;
		this.isConnected = false;
	}
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String reason);
        void onConnectionLost();
    }
    
	
	// connect to server 
//	public boolean connect() {
//		try {
//			System.out.println("Connecting to server: " + serverHost + ":" + serverPort);
//			
//			// tạo socket nhưng có time out? 
//			socket = new Socket();
//			socket.connect(new java.net.InetSocketAddress(serverHost, serverPort), CONNECTION_TIMEOUT);
//			socket.setSoTimeout(READ_TIMEOUT);
//			
//			// khởi tạo kiểu luồng nhận, gửi ấy 
//			// reader là gì? 
//			// writer là gì?
//			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			writer = new PrintWriter(socket.getOutputStream(), true);
//			
//			// server phản hồi lại client
//			String welcomeMessage = reader.readLine();
//			if(welcomeMessage != null && welcomeMessage.startsWith(Protocol.SUCCESS)) {
//				isConnected = true;
//				System.out.println("Connected to server successfully");
//				
//				// hàm này có ý nghĩa gì 
//				startHeartbeat();
//				if(connectionListener != null) {
//					
//				}
//			}
//		}
//	}
    public boolean connect() {
        try {
            System.out.println("🔌 Connecting to server: " + serverHost + ":" + serverPort);
            
            // Create socket with timeout
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(serverHost, serverPort), CONNECTION_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);
            
            // Initialize streams
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            // Read welcome message
            String welcomeMessage = reader.readLine();
            if (welcomeMessage != null && welcomeMessage.startsWith(Protocol.SUCCESS)) {
                isConnected = true;
                System.out.println("✅ Connected to server successfully");
                
                // Start heartbeat thread
                startHeartbeat();
                
                // Notify listener
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
                
                return true;
            } else {
                throw new IOException("Invalid welcome message from server");
            }
            
        } catch (ConnectException e) {
            System.err.println("❌ Cannot connect to server: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Server is not available");
            }
            return false;
        } catch (SocketTimeoutException e) {
            System.err.println("❌ Connection timeout");
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Connection timeout");
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Connection failed: " + e.getMessage());
            }
            return false;
        }
    }
    
    private void startHeartbeat() {
    	 Thread heartbeatThread = new Thread(() -> {
             while (isConnected) {
                 try {
                     Thread.sleep(60000); // Send heartbeat every minute
                     
                     if (isConnected) {
                         String response = sendRequest(Protocol.PING);
                         if (!Protocol.PONG.equals(response)) {
                             System.out.println("⚠️ Invalid heartbeat response");
                             break;
                         }
                     }
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     break;
                 } catch (IOException e) {
                     System.out.println("💔 Heartbeat failed: " + e.getMessage());
                     break;
                 }
             }
         });
         
         heartbeatThread.setDaemon(true);
         heartbeatThread.setName("Heartbeat");
         heartbeatThread.start();
		
	}
    public synchronized String sendRequest(String action, String data) throws IOException {
        if (!isConnected) {
            throw new IOException("Not connected to server");
        }
        
        try {
            // Build request message
            String request = action;
            if (data != null && !data.isEmpty()) {
                request += Protocol.DELIMITER + data;
            }
            System.out.println(">>> SENT: " + request.replace("\n", "\\n"));

            
            // Send request
            writer.println(request);
            writer.flush();
            
            // Read response
            String response = reader.readLine();
            if (response == null) {
                throw new IOException("Server closed connection");
            }
            
            return response;
            
        } catch (SocketTimeoutException e) {
            throw new IOException("Request timeout");
        } catch (IOException e) {
            // Connection lost
            isConnected = false;
            if (connectionListener != null) {
                connectionListener.onConnectionLost();
            }
            throw e;
        }
    }
    public String sendRequest(String action) throws IOException {
        return sendRequest(action, "");
    }
	public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }
	public String getServerAddress() {
		// TODO Auto-generated method stub
		   return serverHost + ":" + serverPort;
	}
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
    public void disconnect() {
        try {
            if (isConnected) {
                // Send disconnect signal
                sendRawMessage(Protocol.DISCONNECT);
                
                isConnected = false;
                
                // Close streams
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (socket != null) {
                    socket.close();
                }
                
                System.out.println("🔌 Disconnected from server");
                
                // Notify listener
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
            }
        } catch (Exception e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    private void sendRawMessage(String message) {
        try {
            if (writer != null) {
                writer.println(message);
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println("Error sending raw message: " + e.getMessage());
        }
    }
 // client/network/NetworkManager.java
    public static ResponseData parseResponse(String response) {
        try {
            System.out.println("🔍 Raw server response: " + response);
            
            if (response == null || response.trim().isEmpty()) {
                return new ResponseData(false, "Empty response", null);
            }
            
            // ✅ FIX: Split đúng cách với DELIMITER
            String[] parts = response.split("\\|", 2); // ✅ ESCAPE | character và limit 2 parts
            
            if (parts.length < 2) {
                System.err.println("❌ Invalid response format: " + response);
                return new ResponseData(false, "Invalid response format", null);
            }
            
            String status = parts[0];  // ✅ Giờ sẽ là "LOGIN_SUCCESS"
            String data = parts[1];    // ✅ Giờ sẽ là "sessionToken|userJson"
            
            System.out.println("📋 Parsed - Status: '" + status + "'");
            System.out.println("📋 Parsed - Data length: " + (data != null ? data.length() : 0));
            System.out.println("📋 Parsed - Data preview: " + (data != null ? data.substring(0, Math.min(100, data.length())) : "null"));
            
            boolean success = Protocol.SUCCESS.equals(status) || 
                             Protocol.LOGIN_SUCCESS.equals(status) ||
                             "SUCCESS".equals(status) ||
                             "LOGIN_SUCCESS".equals(status);
            
            if (success) {
                // SUCCESS case - data is the actual content
                return new ResponseData(true, "Success", data);
            } else {
                // ERROR case - data is the error message
                return new ResponseData(false, data, null);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing response: " + e.getMessage());
            e.printStackTrace();
            return new ResponseData(false, "Response parsing error: " + e.getMessage(), null);
        }
    }
    public static class ResponseData {
        private final boolean success;
        private final String message;
        private final String data;
        
        public ResponseData(boolean success, String message, String data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getData() { return data; }
        
        @Override
        public String toString() {
            return "ResponseData{success=" + success + ", message='" + message + "'}";
        }
}
}