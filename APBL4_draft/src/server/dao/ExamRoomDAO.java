package server.dao;

import model.ExamRoom;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExamRoom DAO - Database operations for exam rooms
 */
public class ExamRoomDAO extends BaseDAO {
    
    /**
     * Create new exam room
     */
    public ExamRoom create(ExamRoom examRoom) throws SQLException {
        String sql = """
            INSERT INTO exam_rooms (room_name, room_password, subject_id, question_count, 
                                   total_score, duration_minutes, start_time, end_time, 
                                   description, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        int generatedId = executeInsertWithGeneratedKey(sql,
            examRoom.getRoomName(),
            examRoom.getRoomPassword(),
            examRoom.getSubjectId(),
            examRoom.getQuestionCount(),
            examRoom.getTotalScore(),
            examRoom.getDurationMinutes(),
            examRoom.getStartTime(),
            examRoom.getEndTime(),
            examRoom.getDescription(),
            examRoom.isActive(),
            examRoom.getCreatedBy()
        );
        
        examRoom.setRoomId(generatedId);
        return examRoom;
    }
    
    /**
     * Update exam room
     */
    public boolean update(ExamRoom examRoom) throws SQLException {
        String sql = """
            UPDATE exam_rooms SET 
                room_name = ?, room_password = ?, subject_id = ?, question_count = ?,
                total_score = ?, duration_minutes = ?, start_time = ?, end_time = ?,
                description = ?, is_active = ?
            WHERE room_id = ?
            """;
        
        int rowsAffected = executeUpdate(sql,
            examRoom.getRoomName(),
            examRoom.getRoomPassword(),
            examRoom.getSubjectId(),
            examRoom.getQuestionCount(),
            examRoom.getTotalScore(),
            examRoom.getDurationMinutes(),
            examRoom.getStartTime(),  // ✅ Convert String to Timestamp
            examRoom.getEndTime(), 
            examRoom.getDescription(),
            examRoom.isActive(),
            examRoom.getRoomId()
        );
        
        return rowsAffected > 0;
    }
    
    /**
     * Delete exam room
     */
    public boolean delete(int roomId) throws SQLException {
        String sql = "UPDATE exam_rooms SET is_active = false WHERE room_id = ?";
        int rowsAffected = executeUpdate(sql, roomId);
        return rowsAffected > 0;
    }
    
    /**
     * Find exam room by ID
     */
    public ExamRoom findById(int roomId) throws SQLException {
        String sql = """
            SELECT er.*, s.subject_name
            FROM exam_rooms er
            JOIN subjects s ON er.subject_id = s.subject_id
            WHERE er.room_id = ?
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql, roomId);
        if (!results.isEmpty()) {
        	ExamRoom room = mapToExamRoom(results.get(0));
            // ✅ THÊM DÒNG NÀY:
            room.setAllowedStudentIds(getStudentsInRoom(room.getRoomId()));
            return room;
        }
        return null;
    }
    
    /**
     * Find all exam rooms
     */
    public List<ExamRoom> findAll() throws SQLException {
        String sql = """
            SELECT er.*, s.subject_name
            FROM exam_rooms er
            JOIN subjects s ON er.subject_id = s.subject_id
            ORDER BY er.created_at DESC
            """;
        
        List<Map<String, Object>> results = executeQueryForList(sql);
        List<ExamRoom> examRooms = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamRoom room = mapToExamRoom(row);
            // Load allowed students
            room.setAllowedStudentIds(getStudentsInRoom(room.getRoomId()));
            examRooms.add(room);
        }
        
        return examRooms;
    }
    
    /**
     * Search exam rooms
     */
    public List<ExamRoom> search(String keyword) throws SQLException {
        String sql = """
            SELECT er.*, s.subject_name
            FROM exam_rooms er
            JOIN subjects s ON er.subject_id = s.subject_id
            WHERE (er.room_name LIKE ? OR s.subject_name LIKE ?) AND er.is_active = true
            ORDER BY er.created_at DESC
            """;
        
        String searchPattern = "%" + keyword + "%";
        List<Map<String, Object>> results = executeQueryForList(sql, searchPattern, searchPattern);
        List<ExamRoom> examRooms = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            ExamRoom room = mapToExamRoom(row);
            room.setAllowedStudentIds(getStudentsInRoom(room.getRoomId()));
            examRooms.add(room);
        }
        
        return examRooms;
    }
    
    /**
     * Add students to room
     */
    public boolean addStudentsToRoom(int roomId, List<Integer> studentIds) throws SQLException {
        // First, remove existing students
        String deleteSql = "DELETE FROM room_students WHERE room_id = ?";
        executeUpdate(deleteSql, roomId);
        
        // Then add new students
        if (!studentIds.isEmpty()) {
            StringBuilder sql = new StringBuilder("INSERT INTO room_students (room_id, student_id) VALUES ");
            for (int i = 0; i < studentIds.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("(?, ?)");
            }
            
            Object[] params = new Object[studentIds.size() * 2];
            for (int i = 0; i < studentIds.size(); i++) {
                params[i * 2] = roomId;
                params[i * 2 + 1] = studentIds.get(i);
            }
            
            executeUpdate(sql.toString(), params);
        }
        
        return true;
    }
    
    /**
     * Get students in room
     */
    public List<Integer> getStudentsInRoom(int roomId) throws SQLException {
        String sql = "SELECT student_id FROM room_students WHERE room_id = ?";
        List<Map<String, Object>> results = executeQueryForList(sql, roomId);
        List<Integer> studentIds = new ArrayList<>();
        for (Map<String, Object> row : results) {
            Object value = row.get("student_id");
            // Ép kiểu về Integer an toàn cho mọi trường hợp
            if (value instanceof Number) {
                studentIds.add(((Number) value).intValue());
            } else {
                studentIds.add(Integer.parseInt(value.toString()));
            }
        }
        System.out.println(">>> [DEBUG] SQL Results for room_id=" + roomId + ": " + results.size());
        return studentIds;
    }
    
    /**
     * Map database row to ExamRoom object
     */
    private ExamRoom mapToExamRoom(Map<String, Object> row) {
        ExamRoom examRoom = new ExamRoom();
        examRoom.setRoomId(getIntValue(row, "room_id", 0));
        examRoom.setRoomName(getStringValue(row, "room_name"));
        examRoom.setRoomPassword(getStringValue(row, "room_password"));
        examRoom.setSubjectId(getIntValue(row, "subject_id", 0));
        examRoom.setSubjectName(getStringValue(row, "subject_name"));
        examRoom.setQuestionCount(getIntValue(row, "question_count", 0));
        // ✅ NULL SAFE: Numeric fields
        Object totalScoreObj = row.get("total_score");
        if (totalScoreObj instanceof Number) {
            examRoom.setTotalScore(((Number) totalScoreObj).doubleValue());
        } else {
            examRoom.setTotalScore(0.0);
        }
        
        examRoom.setDurationMinutes(getIntValue(row, "duration_minutes", 0));
        
        // ✅ NULL SAFE: Timestamps
        examRoom.setStartTime(convertToTimestamp(row.get("start_time")));
        examRoom.setEndTime(convertToTimestamp(row.get("end_time")));
        
        // ✅ NULL SAFE: Other fields
        examRoom.setDescription(getStringValue(row, "description"));
        
        Object isActiveObj = row.get("is_active");
        if (isActiveObj instanceof Boolean) {
            examRoom.setActive((Boolean) isActiveObj);
        } else if (isActiveObj instanceof Number) {
            examRoom.setActive(((Number) isActiveObj).intValue() == 1);
        } else {
            examRoom.setActive(true); // Default
        }
        
        examRoom.setCreatedBy(getIntValue(row, "created_by", 0));
        
        // ✅ NULL SAFE: Created/Updated timestamps
        examRoom.setCreatedAt(safeToString(row.get("created_at")));
        examRoom.setUpdatedAt(safeToString(row.get("updated_at")));
//        examRoom.setRoomId((Integer) row.get("room_id"));
//        examRoom.setRoomName((String) row.get("room_name"));
//        examRoom.setRoomPassword((String) row.get("room_password"));
//        examRoom.setSubjectId((Integer) row.get("subject_id"));
//        examRoom.setSubjectName((String) row.get("subject_name"));
//        examRoom.setQuestionCount((Integer) row.get("question_count"));
//        examRoom.setTotalScore(((Number) row.get("total_score")).doubleValue());
//        examRoom.setDurationMinutes((Integer) row.get("duration_minutes"));
//        examRoom.setStartTime(convertToTimestamp(row.get("start_time")));
//        examRoom.setEndTime(convertToTimestamp(row.get("end_time")));
//        examRoom.setDescription((String) row.get("description"));
//        examRoom.setActive((Boolean) row.get("is_active"));
//        examRoom.setCreatedBy((Integer) row.get("created_by"));
//        examRoom.setCreatedAt(row.get("created_at").toString());
//        examRoom.setUpdatedAt(row.get("updated_at").toString());
        return examRoom;
    }
    private String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Safely get int value from Map
     */
    private int getIntValue(Map<String, Object> row, String key, int defaultValue) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Safely convert object to string
     */
    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    /**
     * Convert any date object to Timestamp safely
     */
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
                // Parse string to timestamp
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date date = sdf.parse((String) obj);
                return new Timestamp(date.getTime());
            }
            
            System.err.println("⚠️ Unexpected date type: " + obj.getClass().getName() + " = " + obj);
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Error converting to Timestamp: " + obj + " - " + e.getMessage());
            return null;
        }
    }
}