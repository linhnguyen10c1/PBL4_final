package server.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseManager{
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    static {
        loadDatabaseConfig();
    }
    private static void loadDatabaseConfig() {
        try (InputStream input = DatabaseManager.class.getResourceAsStream("database.properties")) {
            Properties props = new Properties();
            props.load(input);
            
            DB_URL = props.getProperty("db.url");
            DB_USERNAME = props.getProperty("db.username");
            DB_PASSWORD = props.getProperty("db.password");
            DB_DRIVER = props.getProperty("db.driver");
            
            // Load driver
            Class.forName(DB_DRIVER);
            
            System.out.println("✅ Database configuration loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to load database configuration: " + e.getMessage());
            throw new RuntimeException("Database configuration failed", e);
        }
    }
    public static Connection getConnection() throws SQLException {
        System.out.println("🔗 Trying to connect DB: " + DB_URL + " with user " + DB_USERNAME);
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
}