package model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * ExamSession model - Represents a student's exam session
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 15:54:30 UTC
 */
public class ExamSession {
    private int sessionId;
    private int roomId;
    private int studentId;
    private String sessionToken;
    private Timestamp startTime;
    private Timestamp submitTime;
    private double totalScore;
    private String status; // NOT_STARTED, IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED
    private int timeSpentMinutes;
    private String createdAt;
    private String updatedAt;
    
    // Related objects
    private ExamRoom examRoom;
    private String studentName;
    private List<ExamAnswer> answers;
    
    public ExamSession() {
        this.status = "NOT_STARTED";
        this.timeSpentMinutes = 0;
        this.totalScore = 0.0;
        this.answers = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    
    public Timestamp getSubmitTime() { return submitTime; }
    public void setSubmitTime(Timestamp submitTime) { this.submitTime = submitTime; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public ExamRoom getExamRoom() { return examRoom; }
    public void setExamRoom(ExamRoom examRoom) { this.examRoom = examRoom; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public List<ExamAnswer> getAnswers() { return answers; }
    public void setAnswers(List<ExamAnswer> answers) { this.answers = answers; }
    
    // Helper methods
    public boolean isNotStarted() { return "NOT_STARTED".equals(status); }
    public boolean isInProgress() { return "IN_PROGRESS".equals(status); }
    public boolean isSubmitted() { return "SUBMITTED".equals(status) || "AUTO_SUBMITTED".equals(status); }
    public boolean isAutoSubmitted() { return "AUTO_SUBMITTED".equals(status); }
    
    /**
     * Calculate remaining time in minutes
     */
//    public int getRemainingTimeMinutes() {
//        if (examRoom == null || startTime == null) return 0;
//        
//        long startMillis = startTime.getTime();
//        long currentMillis = System.currentTimeMillis();
//        long elapsedMillis = currentMillis - startMillis;
//        int elapsedMinutes = (int) (elapsedMillis / (1000 * 60));
//        
//        return Math.max(0, examRoom.getDurationMinutes() - elapsedMinutes);
//    }
    
    /**
     * Calculate remaining time in minutes
     */
    public int getRemainingTimeMinutes() {
        if (examRoom == null) {
            System.err.println("❌ getRemainingTimeMinutes: examRoom is null");
            return 0;
        }
        
        // ✅ FIX: Nếu chưa start exam thì return full duration
        if (startTime == null) {
            System.out.println("🔍 Exam not started yet, returning full duration: " + examRoom.getDurationMinutes());
            return examRoom.getDurationMinutes();
        }
        
        long startMillis = startTime.getTime();
        long currentMillis = System.currentTimeMillis();
        long elapsedMillis = currentMillis - startMillis;
        int elapsedMinutes = (int) (elapsedMillis / (1000 * 60));
        
        int remainingMinutes = Math.max(0, examRoom.getDurationMinutes() - elapsedMinutes);
        
        System.out.println("🔍 Timer calc: duration=" + examRoom.getDurationMinutes() + 
                          ", elapsed=" + elapsedMinutes + ", remaining=" + remainingMinutes);
        
        return remainingMinutes;
    }
    /**
     * Check if exam time is expired
     */
    public boolean isTimeExpired() {
        return getRemainingTimeMinutes() <= 0;
    }
    
    /**
     * Get answered question count
     */
    public int getAnsweredCount() {
        return (int) answers.stream().filter(a -> a.getStudentAnswer() != null).count();
    }
    
    /**
     * Get total question count
     */
    public int getTotalQuestions() {
        return examRoom != null ? examRoom.getQuestionCount() : answers.size();
    }
    
    @Override
    public String toString() {
        return "ExamSession{" +
                "sessionId=" + sessionId +
                ", roomId=" + roomId +
                ", studentId=" + studentId +
                ", status='" + status + '\'' +
                ", totalScore=" + totalScore +
                ", timeSpentMinutes=" + timeSpentMinutes +
                '}';
    }
}