package server.dao;

import model.Question;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Question DAO - Data access for questions
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 03:59:16 UTC
 */
public class QuestionDAO extends BaseDAO {
    
    /**
     * Create new question
     */
    public Question create(Question question) throws SQLException {
        String sql = """
            INSERT INTO questions (subject_id, question_text, option_a, option_b, 
                                 option_c, option_d, correct_answer, difficulty, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        int generatedId = executeInsertWithGeneratedKey(sql,
            question.getSubjectId(),
            question.getQuestionText(),
            question.getOptionA(),
            question.getOptionB(),
            question.getOptionC(),
            question.getOptionD(),
            question.getCorrectAnswer(),
            question.getDifficulty(),
            question.isActive()
        );
        
        question.setQuestionId(generatedId);
        return question;
    }
    
    /**
     * Update question
     */
    public boolean update(Question question) throws SQLException {
        String sql = """
            UPDATE questions SET 
                subject_id = ?, question_text = ?, option_a = ?, option_b = ?,
                option_c = ?, option_d = ?, correct_answer = ?, difficulty = ?, is_active = ?
            WHERE question_id = ?
            """;
        
        int rowsAffected = executeUpdate(sql,
            question.getSubjectId(),
            question.getQuestionText(),
            question.getOptionA(),
            question.getOptionB(),
            question.getOptionC(),
            question.getOptionD(),
            question.getCorrectAnswer(),
            question.getDifficulty(),
            question.isActive(),
            question.getQuestionId()
        );
        
        return rowsAffected > 0;
    }
    
    /**
     * Delete question (soft delete)
     */
    public boolean delete(int questionId) throws SQLException {
        String sql = "UPDATE questions SET is_active = false WHERE question_id = ?";
        int rowsAffected = executeUpdate(sql, questionId);
        return rowsAffected > 0;
    }
    
    /**
     * Find question by ID
     */
    public Question findById(int questionId) throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.question_id = ?
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, questionId);
        if (!results.isEmpty()) {
            return mapToQuestion(results.get(0));
        }
        return null;
    }
    
    /**
     * Find all questions
     */
    public List<Question> findAll() throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.is_active = true
            ORDER BY q.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    public List<Question> getAll() throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            ORDER BY q.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    
    /**
     * Find questions by subject
     */
    public List<Question> findBySubject(int subjectId) throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.subject_id = ? AND q.is_active = true
            ORDER BY q.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, subjectId);
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    
    /**
     * Search questions
     */
    public List<Question> search(String keyword) throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.is_active = true AND (
                q.question_text LIKE ? OR 
                q.option_a LIKE ? OR 
                q.option_b LIKE ? OR 
                q.option_c LIKE ? OR 
                q.option_d LIKE ? OR
                s.subject_name LIKE ?
            )
            ORDER BY q.created_at DESC
            """;
        
        String searchPattern = "%" + keyword + "%";
        List<Map<String, Object>> results = executeQueryForList(sql, 
            searchPattern, searchPattern, searchPattern, 
            searchPattern, searchPattern, searchPattern);
        
        List<Question> questions = new ArrayList<>();
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    
    /**
     * Count questions by subject
     */
    public int countBySubject(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM questions WHERE subject_id = ? AND is_active = true";
        List<Map<String, Object>> results = executeQueryForList(sql, subjectId);
        
        if (!results.isEmpty()) {
            return getIntValue(results.get(0), "count", 0);
        }
        return 0;
    }
    
    /**
     * Get questions by difficulty
     */
    public List<Question> findByDifficulty(String difficulty) throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.difficulty = ? AND q.is_active = true
            ORDER BY q.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, difficulty);
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    
    /**
     * Get random questions for exam
     */
    public List<Question> getRandomQuestions(int subjectId, int count) throws SQLException {
        String sql = """
            SELECT q.*, s.subject_name
            FROM questions q
            JOIN subjects s ON q.subject_id = s.subject_id
            WHERE q.subject_id = ? AND q.is_active = true
            ORDER BY RAND()
            LIMIT ?
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, subjectId, count);
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            questions.add(mapToQuestion(row));
        }
        
        return questions;
    }
    
    /**
     * Map database row to Question object
     */
    private Question mapToQuestion(Map<String, Object> row) {
        Question question = new Question();
        question.setQuestionId(getIntValue(row, "question_id", 0));
        question.setSubjectId(getIntValue(row, "subject_id", 0));
        question.setSubjectName(getStringValue(row, "subject_name"));
        question.setQuestionText(getStringValue(row, "question_text"));
        question.setOptionA(getStringValue(row, "option_a"));
        question.setOptionB(getStringValue(row, "option_b"));
        question.setOptionC(getStringValue(row, "option_c"));
        question.setOptionD(getStringValue(row, "option_d"));
        question.setCorrectAnswer(getStringValue(row, "correct_answer"));
        question.setDifficulty(getStringValue(row, "difficulty"));
        question.setActive(getBooleanValue(row, "is_active", true));
        question.setCreatedAt(safeToString(row.get("created_at")));
        question.setUpdatedAt(safeToString(row.get("updated_at")));
        return question;
    }
    
    // Helper methods
    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }
    
    private int getIntValue(Map<String, Object> row, String key, int defaultValue) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    private boolean getBooleanValue(Map<String, Object> row, String key, boolean defaultValue) {
        Object value = row.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return defaultValue;
    }
    
    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}