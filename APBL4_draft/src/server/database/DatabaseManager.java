package server.database;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DatabaseManager {
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    private static final int INITIAL_POOL_SIZE = 10;
    private static BlockingQueue<Connection> connectionPool;

    static {
        loadDatabaseConfig();
        try {
            initPool();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    private static void loadDatabaseConfig() {
        try (InputStream input = DatabaseManager.class.getResourceAsStream("database.properties")) {
            Properties props = new Properties();
            props.load(input);
            DB_URL = props.getProperty("db.url");
            DB_USERNAME = props.getProperty("db.username");
            DB_PASSWORD = props.getProperty("db.password");
            DB_DRIVER = props.getProperty("db.driver");
            Class.forName(DB_DRIVER);
        } catch (Exception e) {
            throw new RuntimeException("Database configuration failed", e);
        }
    }

    private static void initPool() throws SQLException {
        connectionPool = new LinkedBlockingQueue<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createNewConnection());
        }
        System.out.println("✅ Database Pool initialized with " + INITIAL_POOL_SIZE + " connections");
    }

    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection realConnection = connectionPool.take();

            if (realConnection.isClosed()) {
                realConnection = createNewConnection();
            }

            final Connection finalRealConn = realConnection;
            
            return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        connectionPool.offer(finalRealConn);
                        return null;
                    }
                    return method.invoke(finalRealConn, args);
                }
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}