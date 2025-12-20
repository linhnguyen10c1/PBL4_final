package server.dao;

import model.ExamSession;
import model.ExamAnswer;
import model.ExamRoom;
import model.Question;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

/**
 * ExamSession DAO - Data access for exam sessions
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 15:54:30 UTC
 */
public class ExamSessionDAO extends BaseDAO {
    
    /**
     * Create new exam session for student
     */
    public ExamSession createSession(int roomId, int studentId) throws SQLException {
        // Check if session already exists
        ExamSession existing = findByRoomAndStudent(roomId, studentId);
        if (existing != null) {
            return existing; // Return existing session
        }
        
        // Generate unique session token
        String sessionToken = generateSessionToken();
        
        String sql = """
            INSERT INTO exam_sessions (room_id, student_id, session_token, status)
            VALUES (?, ?, ?, 'NOT_STARTED')
            """;
        
        int generatedId = executeInsertWithGeneratedKey(sql, roomId, studentId, sessionToken);
        
        ExamSession session = new ExamSession();
        session.setSessionId(generatedId);
        session.setRoomId(roomId);
        session.setStudentId(studentId);
        session.setSessionToken(sessionToken);
        session.setStatus("NOT_STARTED");
        
        return session;
    }
    
    /**
     * Start exam session
     */
    public boolean startSession(int sessionId, List<Question> examQuestions) throws SQLException {
    	try {
            // Update session status and start time
            String updateSessionSql = """
                UPDATE exam_sessions 
                SET status = 'IN_PROGRESS', start_time = CURRENT_TIMESTAMP 
                WHERE session_id = ?
                """;
            
            int rowsUpdated = executeUpdate(updateSessionSql, sessionId);
            
            if (rowsUpdated > 0) {
                // Create exam questions for this session
                String insertQuestionSql = """
                    INSERT INTO exam_questions (session_id, question_id, question_order)
                    VALUES (?, ?, ?)
                    """;
                
                for (int i = 0; i < examQuestions.size(); i++) {
                    executeUpdate(insertQuestionSql, sessionId, examQuestions.get(i).getQuestionId(), i + 1);
                }
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ [ExamSessionDAO] Error starting session: " + e.getMessage());
            throw new SQLException("Failed to start session", e);
        }
    }
    
    /**
     * Save student answer
     */
    public boolean saveAnswer(int sessionId, int questionId, String answer) throws SQLException {
        // First try to update existing answer
        String updateSql = """
            UPDATE exam_questions 
            SET student_answer = ?, answered_at = CURRENT_TIMESTAMP
            WHERE session_id = ? AND question_id = ?
            """;
        
        int rowsUpdated = executeUpdate(updateSql, answer, sessionId, questionId);
        
        if (rowsUpdated == 0) {
            // If no rows updated, insert new answer
            String insertSql = """
                INSERT INTO exam_questions (session_id, question_id, student_answer, answered_at, question_order)
                SELECT ?, ?, ?, CURRENT_TIMESTAMP, COALESCE(MAX(question_order), 0) + 1
                FROM exam_questions WHERE session_id = ?
                """;
            executeUpdate(insertSql, sessionId, questionId, answer, sessionId);
        }
        
        // Auto-save to backup table
        saveAutoAnswer(sessionId, questionId, answer);
        
        return true;
    }
    
    /**
     * Auto-save answer (for backup during exam)
     */
    private void saveAutoAnswer(int sessionId, int questionId, String answer) throws SQLException {
        String sql = """
            INSERT INTO auto_save_answers (session_id, question_id, answer)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE answer = VALUES(answer), saved_at = CURRENT_TIMESTAMP
            """;
        executeUpdate(sql, sessionId, questionId, answer);
    }
    
    /**
     * Submit exam session
     */
    public boolean submitSession(int sessionId, boolean isAutoSubmit) throws SQLException {
    	try {
            // Calculate score first
            double totalScore = calculateSimpleScore(sessionId);
            
            // Update session
            String status = isAutoSubmit ? "AUTO_SUBMITTED" : "SUBMITTED";
            String updateSql = """
                UPDATE exam_sessions 
                SET status = ?, submit_time = CURRENT_TIMESTAMP, total_score = ?
                WHERE session_id = ?
                """;
            
            int rowsUpdated = executeUpdate(updateSql, status, totalScore, sessionId);
            
            if (rowsUpdated > 0) {
                // Update correct answers flags
                updateCorrectAnswersSimple(sessionId);
                
                System.out.println("✅ [ExamSessionDAO] Session " + sessionId + " submitted with score: " + 
                                 String.format("%.2f", totalScore));
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ [ExamSessionDAO] Error submitting session: " + e.getMessage());
            throw new SQLException("Failed to submit session", e);
        }
    }
    private double calculateSimpleScore(int sessionId) throws SQLException {
        String sql = """
            SELECT 
                q.difficulty,
                q.correct_answer,
                eq.student_answer
            FROM exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            WHERE eq.session_id = ?
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, sessionId);
        
        if (results.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        double maxPossibleScore = 0.0;
        
        for (Map<String, Object> row : results) {
            String difficulty = safeToString(row.get("difficulty"));
            String correctAnswer = safeToString(row.get("correct_answer"));
            String studentAnswer = safeToString(row.get("student_answer"));
            
            // Calculate points based on difficulty
            double questionMaxScore;
            switch (difficulty != null ? difficulty : "MEDIUM") {
                case "EASY": 
                    questionMaxScore = 1.0; 
                    break;
                case "MEDIUM": 
                    questionMaxScore = 1.2; 
                    break;
                case "HARD": 
                    questionMaxScore = 1.5; 
                    break;
                default: 
                    questionMaxScore = 1.0; 
                    break;
            }
            
            maxPossibleScore += questionMaxScore;
            
            // Check if answer is correct
            if (studentAnswer != null && studentAnswer.equals(correctAnswer)) {
                totalScore += questionMaxScore;
            }
        }
        
        // Return percentage score
        return maxPossibleScore > 0 ? (totalScore / maxPossibleScore) * 100.0 : 0.0;
    }

    /**
     * Update correct answers flags - SIMPLE VERSION
     */
    private void updateCorrectAnswersSimple(int sessionId) throws SQLException {
        String sql = """
            UPDATE exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            SET eq.is_correct = (eq.student_answer = q.correct_answer)
            WHERE eq.session_id = ?
            """;
        executeUpdate(sql, sessionId);
    }
    
    /**
     * Calculate session score
     */
//    private double calculateSessionScore(int sessionId) throws SQLException {
//        String sql = """
//            SELECT 
//                COUNT(*) as total_questions,
//                SUM(CASE WHEN eq.student_answer = q.correct_answer THEN 1 ELSE 0 END) as correct_answers
//            FROM exam_questions eq
//            JOIN questions q ON eq.question_id = q.question_id
//            WHERE eq.session_id = ? AND eq.student_answer IS NOT NULL
//            """;
//        
//        List<Map<String, Object>> results = executeQueryForList(sql, sessionId);
//        if (!results.isEmpty()) {
//            Map<String, Object> row = results.get(0);
//            int totalQuestions = getIntValue(row, "total_questions", 0);
//            int correctAnswers = getIntValue(row, "correct_answers", 0);
//            
//            if (totalQuestions > 0) {
//                return (double) correctAnswers / totalQuestions * 100.0;
//            }
//        }
//        
//        return 0.0;
//    }
    
    /**
     * Update correct answers flags
     */
    private void updateCorrectAnswers(int sessionId) throws SQLException {
        String sql = """
            UPDATE exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            SET eq.is_correct = (eq.student_answer = q.correct_answer)
            WHERE eq.session_id = ?
            """;
        executeUpdate(sql, sessionId);
    }
    
    /**
     * Find session by room and student
     */
    public ExamSession findByRoomAndStudent(int roomId, int studentId) throws SQLException {
        String sql = """
            SELECT 
                es.session_id, 
                es.room_id, 
                es.student_id, 
                es.session_token,
                es.start_time, 
                es.submit_time, 
                es.total_score as session_score,
                es.status, 
                es.created_at,
                er.room_name, 
                er.room_password,
                er.subject_id, 
                s.subject_name, 
                er.question_count,
                er.duration_minutes, 
                er.total_score as max_score,
                er.start_time as room_start_time, 
                er.end_time as room_end_time,
                er.description,
                er.is_active as room_active,
                er.created_by,
                u.full_name as student_name
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            WHERE es.room_id = ? AND es.student_id = ?
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, roomId, studentId);
        if (! results.isEmpty()) {
            return mapToExamSession(results.get(0));
        }
        return null;
    }
    
    /**
     * Find session by session token
     */
    public ExamSession findByToken(String sessionToken) throws SQLException {
        String sql = """
            SELECT 
                es.session_id, 
                es.room_id, 
                es.student_id, 
                es.session_token,
                es.start_time, 
                es.submit_time, 
                es.total_score as session_score,
                es.status, 
                es.created_at,
                er.room_name, 
                er.room_password,
                er.subject_id, 
                s.subject_name, 
                er.question_count,
                er.duration_minutes, 
                er.total_score as max_score,
                er.start_time as room_start_time, 
                er.end_time as room_end_time,
                er.description,
                er.is_active as room_active,
                er.created_by,
                u.full_name as student_name
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            WHERE es.session_token = ? 
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, sessionToken);
        if (!results.isEmpty()) {
            ExamSession session = mapToExamSession(results.get(0));
            // Load answers
            session.setAnswers(getSessionAnswers(session.getSessionId()));
            return session;
        }
        return null;
    }
    
    /**
     * Get session answers
     */
    public List<ExamAnswer> getSessionAnswers(int sessionId) throws SQLException {
        String sql = """
            SELECT eq.*, q.question_text, q.option_a, q.option_b, q.option_c, q.option_d, 
                   q.correct_answer, q.difficulty
            FROM exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            WHERE eq.session_id = ?
            ORDER BY eq.question_order
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, sessionId);
        List<ExamAnswer> answers = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamAnswer answer = mapToExamAnswer(row);
            answers.add(answer);
        }
        
        return answers;
    }
    
    /**
     * Get active sessions for room
     */
    public List<ExamSession> getActiveSessionsForRoom(int roomId) throws SQLException {
        String sql = """
            SELECT es.*, er.room_name, er.subject_id, s.subject_name, er.question_count,
                   er.duration_minutes, er.total_score as max_score, u.full_name as student_name
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            WHERE es.room_id = ? AND es.status = 'IN_PROGRESS'
            ORDER BY es.start_time DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, roomId);
        List<ExamSession> sessions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            sessions.add(mapToExamSession(row));
        }
        
        return sessions;
    }
    
    /**
     * Auto-submit expired sessions
     */
    public int autoSubmitExpiredSessions() throws SQLException {
        String sql = """
            SELECT es.session_id
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            WHERE es.status = 'IN_PROGRESS' 
            AND TIMESTAMPDIFF(MINUTE, es.start_time, NOW()) >= er.duration_minutes
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        int count = 0;
        
        for (Map<String, Object> row : results) {
            int sessionId = getIntValue(row, "session_id", 0);
            if (submitSession(sessionId, true)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Map database row to ExamSession object
     */
    private ExamSession mapToExamSession(Map<String, Object> row) {
        ExamSession session = new ExamSession();
        session.setSessionId(getIntValue(row, "session_id", 0));
        session.setRoomId(getIntValue(row, "room_id", 0));
        session.setStudentId(getIntValue(row, "student_id", 0));
        session.setSessionToken(getStringValue(row, "session_token"));
        
        // Safe timestamp conversion
        session.setStartTime(convertToTimestamp(row.get("start_time")));
        session.setSubmitTime(convertToTimestamp(row.get("submit_time")));
        
        // Read from 'session_score' (student's actual score)
        Double sessionScore = getDoubleValueOrNull(row, "session_score");
        if (sessionScore != null) {
            session.setTotalScore(sessionScore);
        } else {
            session.setTotalScore(getDoubleValue(row, "total_score", 0.0));
        }
        
        session.setStatus(getStringValue(row, "status"));
        session.setCreatedAt(safeToString(row.get("created_at")));
        session.setStudentName(getStringValue(row, "student_name"));
        
        // Create ExamRoom with full info
        if (row.containsKey("room_name")) {
            ExamRoom room = new ExamRoom();
            room.setRoomId(getIntValue(row, "room_id", 0));
            room.setRoomName(getStringValue(row, "room_name"));
            room.setRoomPassword(getStringValue(row, "room_password"));
            room.setSubjectId(getIntValue(row, "subject_id", 0));
            room.setSubjectName(getStringValue(row, "subject_name"));
            room.setQuestionCount(getIntValue(row, "question_count", 0));
            room.setDurationMinutes(getIntValue(row, "duration_minutes", 0));
            room.setTotalScore(getDoubleValue(row, "max_score", 100.0));
            room.setDescription(getStringValue(row, "description"));
            room.setActive(getBooleanValue(row, "room_active", true));
            room.setCreatedBy(getIntValue(row, "created_by", 0));
            
            // Set start_time and end_time
            room.setStartTime(convertToTimestamp(row.get("room_start_time")));
            room.setEndTime(convertToTimestamp(row.get("room_end_time")));
            
            session.setExamRoom(room);
        }
        
        return session;
    }
    
    /**
     * Helper method to get Double value or null
     */
    private Double getDoubleValueOrNull(Map<String, Object> row, String key) {
        if (! row.containsKey(key) || row.get(key) == null) {
            return null;
        }
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Timestamp convertToTimestamp(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            if (obj instanceof Timestamp) {
                return (Timestamp) obj;
            }
            
            if (obj instanceof java.time.LocalDateTime) {
                java.time.LocalDateTime ldt = (java.time.LocalDateTime) obj;
                return Timestamp.valueOf(ldt);
            }
            
            if (obj instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) obj;
                return new Timestamp(date.getTime());
            }
            
            if (obj instanceof String) {
                // ✅ FIX: Parse string với nhiều format khác nhau
                String dateStr = (String) obj;
                
                try {
                    // Thử format chuẩn của MySQL: "yyyy-MM-dd HH:mm:ss"
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date date = sdf1.parse(dateStr);
                    return new Timestamp(date.getTime());
                } catch (java.text.ParseException e1) {
                    try {
                        // Thử format không có giây: "yyyy-MM-dd HH:mm"
                        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                        java.util.Date date = sdf2.parse(dateStr);
                        return new Timestamp(date.getTime());
                    } catch (java.text.ParseException e2) {
                        try {
                            // Thử format ISO: "yyyy-MM-ddTHH:mm:ss"
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(dateStr.replace(" ", "T"));
                            return Timestamp.valueOf(ldt);
                        } catch (Exception e3) {
                            System.err.println("❌ Error parsing datetime string: " + dateStr);
                            System.err.println("   Tried formats: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, ISO");
                            return null;
                        }
                    }
                }
            }
            
            System.err.println("⚠️ Unexpected timestamp type: " + obj.getClass().getName() + " = " + obj);
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Error converting to Timestamp: " + obj + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Map database row to ExamAnswer
     */
    private ExamAnswer mapToExamAnswer(Map<String, Object> row) {
        ExamAnswer answer = new ExamAnswer();
        answer.setSessionId(getIntValue(row, "session_id", 0));
        answer.setQuestionId(getIntValue(row, "question_id", 0));
        answer.setQuestionOrder(getIntValue(row, "question_order", 0));
        answer.setStudentAnswer(getStringValue(row, "student_answer"));
        answer.setCorrect(getBooleanValue(row, "is_correct", false));
        answer.setAnsweredAt((Timestamp) row.get("answered_at"));
        
        // Set question info
        if (row.containsKey("question_text")) {
            Question question = new Question();
            question.setQuestionId(getIntValue(row, "question_id", 0));
            question.setQuestionText(getStringValue(row, "question_text"));
            question.setOptionA(getStringValue(row, "option_a"));
            question.setOptionB(getStringValue(row, "option_b"));
            question.setOptionC(getStringValue(row, "option_c"));
            question.setOptionD(getStringValue(row, "option_d"));
            question.setCorrectAnswer(getStringValue(row, "correct_answer"));
            question.setDifficulty(getStringValue(row, "difficulty"));
            answer.setQuestion(question);
        }
        
        return answer;
    }
    private double calculateSessionScore(int sessionId) throws SQLException {
        String sql = """
            SELECT 
                q.difficulty,
                CASE WHEN eq.student_answer = q.correct_answer THEN 1 ELSE 0 END as is_correct
            FROM exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            WHERE eq.session_id = ? AND eq.student_answer IS NOT NULL
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, sessionId);
        
        if (results.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        double maxPossibleScore = 0.0;
        
        for (Map<String, Object> row : results) {
            String difficulty = getStringValue(row, "difficulty");
            boolean isCorrect = getBooleanValue(row, "is_correct", false);
            
            // Calculate points based on difficulty
            double questionMaxScore;
            switch (difficulty) {
                case "EASY": questionMaxScore = 1.0; break;
                case "MEDIUM": questionMaxScore = 1.2; break;
                case "HARD": questionMaxScore = 1.5; break;
                default: questionMaxScore = 1.0; break;
            }
            
            maxPossibleScore += questionMaxScore;
            
            if (isCorrect) {
                totalScore += questionMaxScore;
            }
        }
        
        // Return percentage score
        return maxPossibleScore > 0 ? (totalScore / maxPossibleScore) * 100.0 : 0.0;
    }
    public List<ExamSession> findAllSessions() throws SQLException {
        String sql = """
            SELECT es.*, er.room_name, er.subject_id, s.subject_name, er.question_count,
                   er.duration_minutes, er.total_score as max_score, u.full_name as student_name
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            ORDER BY es.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<ExamSession> sessions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamSession session = mapToExamSession(row);
            sessions.add(session);
        }
        
        return sessions;
    }

    /**
     * Find sessions by status
     */
    public List<ExamSession> findSessionsByStatus(String status) throws SQLException {
        String sql = """
            SELECT es.*, er.room_name, er.subject_id, s.subject_name, er.question_count,
                   er.duration_minutes, er.total_score as max_score, u.full_name as student_name
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            WHERE es.status = ?
            ORDER BY es.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, status);
        List<ExamSession> sessions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamSession session = mapToExamSession(row);
            sessions.add(session);
        }
        
        return sessions;
    }

    /**
     * Get exam sessions with statistics
     */
    public List<ExamSession> getExamSessionsWithStats() throws SQLException {
        String sql = """
            SELECT es.*, er.room_name, er.subject_id, s.subject_name, er.question_count,
                   er.duration_minutes, er.total_score as max_score, u.full_name as student_name,
                   COUNT(eq.question_id) as answered_questions,
                   SUM(CASE WHEN eq.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers
            FROM exam_sessions es
            JOIN exam_rooms er ON es.room_id = er.room_id
            JOIN subjects s ON er.subject_id = s.subject_id
            JOIN users u ON es.student_id = u.user_id
            LEFT JOIN exam_questions eq ON es.session_id = eq.session_id AND eq.student_answer IS NOT NULL
            GROUP BY es.session_id
            ORDER BY es.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<ExamSession> sessions = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamSession session = mapToExamSession(row);
            
            // Add additional stats
            int answeredQuestions = getIntValue(row, "answered_questions", 0);
            int correctAnswers = getIntValue(row, "correct_answers", 0);
            
            // You can add these to ExamSession if needed, or handle in UI
            sessions.add(session);
        }
        
        return sessions;
    }
    
    /**
     * Generate unique session token
     */
    private String generateSessionToken() {
        return "EXAM_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
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
    
    private double getDoubleValue(Map<String, Object> row, String key, double defaultValue) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
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