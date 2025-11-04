package server.network;

import server.database.DatabaseConfig;
import server.database.DatabaseManager;
import server.service.UserService;
//import server.service.QuestionService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exam Server - Main socket server for Online Exam System
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:37:11 UTC
 */
public class ExamServer {
    
    private static final int DEFAULT_PORT = 8888;
    private static final int MAX_CLIENTS = 100;
    private static final int THREAD_POOL_SIZE = 50;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private ProtocolHandler protocolHandler;
    private boolean isRunning;
    private final int port;
    private final Map<String, ClientHandler> activeClients;
    private final AtomicInteger clientCounter;
    
    public ExamServer() {
        this(DEFAULT_PORT);
    }
    
    public ExamServer(int port) {
        this.port = port;
        this.activeClients = new ConcurrentHashMap<>();
        this.clientCounter = new AtomicInteger(0);
        this.isRunning = false;
    }
    
    /**
     * Start the server
     */
    public void start() {
        try {
            System.out.println("🚀 ONLINE EXAM SYSTEM SERVER");
            System.out.println("=====================================");
            System.out.println("📅 Date: 2025-09-14 13:37:11 UTC");
            System.out.println("👨‍💻 Author: claude need to edit");
            System.out.println("🏫 Project: PBL4");
            System.out.println("=====================================");
            
            // Initialize database
            initializeDatabase();
            
            // Initialize protocol handler
            protocolHandler = new ProtocolHandler();
            
            // Initialize thread pool
            threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            
            // Create server socket
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // 1 second timeout for accept()
            
            isRunning = true;
            
            System.out.println("🌐 Server started on port: " + port);
            System.out.println("👥 Max clients: " + MAX_CLIENTS);
            System.out.println("🧵 Thread pool size: " + THREAD_POOL_SIZE);
            System.out.println("✅ Server is ready to accept connections");
            System.out.println("=====================================");
            
            // Start client monitoring thread
            startClientMonitoring();
            
            // Main server loop
            while (isRunning) {
                try {
                    // Accept client connections
                    Socket clientSocket = serverSocket.accept();
                    
                    // Check client limit
                    if (activeClients.size() >= MAX_CLIENTS) {
                        System.out.println("⚠️ Max clients reached, rejecting connection from: " + 
                            clientSocket.getRemoteSocketAddress());
                        clientSocket.close();
                        continue;
                    }
                    
                    // Create client handler
                    ClientHandler clientHandler = new ClientHandler(clientSocket, protocolHandler);
                    String clientId = "Client-" + clientCounter.incrementAndGet();
                    
                    // Add to active clients
                    activeClients.put(clientId, clientHandler);
                    
                    // Submit to thread pool
                    threadPool.submit(() -> {
                        try {
                            clientHandler.run();
                        } finally {
                            // Remove from active clients when done
                            activeClients.remove(clientId);
                        }
                    });
                    
                } catch (SocketTimeoutException e) {
                    // Normal timeout, continue loop
                    continue;
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Server startup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        System.out.println("\n🔄 Shutting down server...");
        isRunning = false;
        
        try {
            // Close all client connections
            for (ClientHandler client : activeClients.values()) {
                client.closeConnection();
            }
            activeClients.clear();
            
            // Shutdown thread pool
            if (threadPool != null) {
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
            }
            
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            System.out.println("✅ Server shutdown completed");
            
        } catch (Exception e) {
            System.err.println("Error during server shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Initialize database and default data
     */
    private void initializeDatabase() {
        try {
            System.out.println("🔧 Initializing database...");
            
            // Test database connection
            if (!DatabaseManager.testConnection()) {
                throw new RuntimeException("Database connection failed");
            }
            
            // Initialize database schema if needed
            DatabaseConfig.initializeDatabase();
            
            // Initialize default data
            UserService userService = new UserService();
//            QuestionService questionService = new QuestionService();
            
            userService.initializeDefaultData();
//            questionService.initializeDefaultData();
            
            System.out.println("✅ Database initialized successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Cannot start server without database", e);
        }
    }
    
    /**
     * Start client monitoring thread
     */
    private void startClientMonitoring() {
        Thread monitorThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(30000); // Check every 30 seconds
                    
                    // Remove disconnected clients
                    activeClients.entrySet().removeIf(entry -> {
                        ClientHandler client = entry.getValue();
                        if (!client.isConnected()) {
                            System.out.println("🔄 Removed disconnected client: " + entry.getKey());
                            return true;
                        }
                        return false;
                    });
                    
                    // Log server status
                    if (activeClients.size() > 0) {
                        System.out.println("📊 Active clients: " + activeClients.size() + "/" + MAX_CLIENTS + 
                            " at " + java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in client monitoring: " + e.getMessage());
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("ClientMonitor");
        monitorThread.start();
        
        System.out.println("🔍 Client monitoring started");
    }
    
    /**
     * Get server status
     */
    public ServerStatus getStatus() {
        return new ServerStatus(
            isRunning,
            port,
            activeClients.size(),
            MAX_CLIENTS,
            clientCounter.get()
        );
    }
    
    /**
     * Get active client count
     */
    public int getActiveClientCount() {
        return activeClients.size();
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Server Status inner class
     */
    public static class ServerStatus {
        private final boolean running;
        private final int port;
        private final int activeClients;
        private final int maxClients;
        private final int totalConnections;
        
        public ServerStatus(boolean running, int port, int activeClients, int maxClients, int totalConnections) {
            this.running = running;
            this.port = port;
            this.activeClients = activeClients;
            this.maxClients = maxClients;
            this.totalConnections = totalConnections;
        }
        
        // Getters
        public boolean isRunning() { return running; }
        public int getPort() { return port; }
        public int getActiveClients() { return activeClients; }
        public int getMaxClients() { return maxClients; }
        public int getTotalConnections() { return totalConnections; }
        
        @Override
        public String toString() {
            return "ServerStatus{" +
                    "running=" + running +
                    ", port=" + port +
                    ", activeClients=" + activeClients +
                    ", maxClients=" + maxClients +
                    ", totalConnections=" + totalConnections +
                    '}';
        }
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        // Handle shutdown gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutdown signal received");
        }));
        
        try {
            // Parse port from command line arguments
            int serverPort = DEFAULT_PORT;
            if (args.length > 0) {
                try {
                    serverPort = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid port number, using default: " + DEFAULT_PORT);
                }
            }
            
            // Create and start server
            ExamServer server = new ExamServer(serverPort);
            server.start();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}