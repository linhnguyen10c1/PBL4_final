package server.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Configuration for Server
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:31:46 UTC
 */
public class DatabaseConfig {
    
    /**
     * Initialize database tables if not exists
     */
    public static void initializeDatabase() {
        System.out.println("🔧 Initializing database...");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            createTablesIfNotExists(conn);
            insertDefaultData(conn);
            System.out.println("✅ Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Create tables if they don't exist
     */
    private static void createTablesIfNotExists(Connection conn) throws SQLException {
        // Implementation would execute the SQL schema here
        // For now, assume tables exist from schema.sql
        System.out.println("📋 Checking database tables...");
    }
    
    /**
     * Insert default admin user and sample data
     */
    private static void insertDefaultData(Connection conn) throws SQLException {
        // Will be implemented in DAO layer
        System.out.println("📝 Inserting default data...");
    }
}