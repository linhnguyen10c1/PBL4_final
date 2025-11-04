package server.dao;

import model.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Subject DAO - Database operations for subjects
 */
public class SubjectDAO extends BaseDAO {
    
    /**
     * Find all active subjects
     */
    public List<Subject> findAllActive() throws SQLException {
        String sql = "SELECT * FROM subjects WHERE is_active = true ORDER BY subject_code";
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<Subject> subjects = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            subjects.add(mapToSubject(row));
        }
        
        return subjects;
    }
    
    /**
     * Find subject by ID
     */
    public Subject findById(int subjectId) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE subject_id = ?";
        List<Map<String, Object>> results = executeQueryForList(sql, subjectId);
        
        if (!results.isEmpty()) {
            return mapToSubject(results.get(0));
        }
        return null;
    }
    
    /**
     * Count questions by subject
     */
    public int countQuestionsBySubject(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM questions WHERE subject_id = ? AND is_active = true";
        List<Map<String, Object>> results = executeQueryForList(sql, subjectId);
        
        if (!results.isEmpty()) {
            return ((Number) results.get(0).get("count")).intValue();
        }
        return 0;
    }
    
    /**
     * Map database row to Subject object
     */
    private Subject mapToSubject(Map<String, Object> row) {
        Subject subject = new Subject();
        subject.setSubjectId((Integer) row.get("subject_id"));
        subject.setSubjectCode((String) row.get("subject_code"));
        subject.setSubjectName((String) row.get("subject_name"));
        subject.setDescription((String) row.get("description"));
        subject.setActive((Boolean) row.get("is_active"));
        subject.setCreatedAt(row.get("created_at").toString());
        return subject;
    }
    
}