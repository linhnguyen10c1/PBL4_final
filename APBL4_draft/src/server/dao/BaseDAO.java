package server.dao;

import server.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base DAO with common database operations
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:31:46 UTC
 */
public abstract class BaseDAO {
	 /**
     * Execute SELECT query với try-with-resources tự động đóng
     */
    protected List<Map<String, Object>> executeQueryForList(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Execute SELECT query và return ResultSet (CHỈ dùng trong try-with-resources)
     */
    protected ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        
        setParameters(stmt, params);
        ResultSet rs = stmt.executeQuery();
        
        // ⚠️ CẢNH BÁO: Caller phải đóng rs, stmt, conn
        return rs;
    }
    /**
     * Execute SELECT query and return ResultSet
     */
//    protected ResultSet executeQuery(String sql, Object... params) throws SQLException {
//        Connection conn = DatabaseManager.getConnection();
//        PreparedStatement stmt = conn.prepareStatement(sql);
//        
//        setParameters(stmt, params);
//        return stmt.executeQuery();
//    }
    
    /**
     * Execute INSERT/UPDATE/DELETE query
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Execute INSERT and return generated key
     */
    protected int executeInsertWithGeneratedKey(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained");
                }
            }
        }
    }
    
    /**
     * Set parameters for PreparedStatement
     */
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                stmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof Timestamp) {
                stmt.setTimestamp(i + 1, (Timestamp) param);
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
    
    /**
     * Close resources safely
     */
    protected void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    


    
}