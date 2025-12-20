package model;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * ExamRoom model class
 */
public class ExamRoom {
    private int roomId;
    private String roomName;
    private String roomPassword;
    private int subjectId;
    private String subjectName; // For display purposes
    private int questionCount;
    private double totalScore;
    private int durationMinutes;
    private Timestamp startTime;    // ✅ Dùng Timestamp
    private Timestamp endTime;
    private String description;
    private boolean isActive;
    private int createdBy;
    private String createdAt;
    private String updatedAt;
    
    // For managing students
    private List<Integer> allowedStudentIds;
    
    public ExamRoom() {
        this.allowedStudentIds = new ArrayList<>();
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getRoomPassword() { return roomPassword; }
    public void setRoomPassword(String roomPassword) { this.roomPassword = roomPassword; }
    
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    
    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    
    // ... rest of getters/setters ...
    
    // ✅ THÊM: Helper methods để làm việc với UI
    public String getStartTimeAsString() {
        if (startTime == null) return null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(startTime);
    }
    public void setStartTimeFromString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            this.startTime = null;
            return;
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date date = sdf.parse(timeString);
            this.startTime = new Timestamp(date.getTime());
        } catch (Exception e) {
            System.err.println("Error parsing start time: " + timeString);
            this.startTime = null;
        }
    }
    
    public String getEndTimeAsString() {
        if (endTime == null) return null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(endTime);
    }
    
    public void setEndTimeFromString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            this.endTime = null;
            return;
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date date = sdf.parse(timeString);
            this.endTime = new Timestamp(date.getTime());
        } catch (Exception e) {
            System.err.println("Error parsing end time: " + timeString);
            this.endTime = null;
        }
    }
    
    // ✅ THÊM: Business logic methods cho thời gian
    public boolean isExamActive() {
        if (startTime == null || endTime == null) return false;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.after(startTime) && now.before(endTime);
    }
    
    public boolean isExamNotStarted() {
        if (startTime == null) return true;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.before(startTime);
    }
    
    public boolean isExamFinished() {
        if (endTime == null) return false;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.after(endTime);
    }
    
    public String getExamStatus() {
        if (!isActive) return "Inactive";
        if (startTime == null || endTime == null) return "Draft";
        
        if (isExamNotStarted()) return "Scheduled";
        if (isExamActive()) return "Active";
        if (isExamFinished()) return "Finished";
        return "Draft";
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public void setIsActive(boolean active) { this.isActive = active; }
    
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Integer> getAllowedStudentIds() { return allowedStudentIds; }
    public void setAllowedStudentIds(List<Integer> allowedStudentIds) { 
        this.allowedStudentIds = allowedStudentIds; 
    }
    
    @Override
    public String toString() {
        return "ExamRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", questionCount=" + questionCount +
                ", totalScore=" + totalScore +
                ", durationMinutes=" + durationMinutes +
                ", isActive=" + isActive +
                '}';
    }
    // Các field này được server gắn vào khi trả về cho student cụ thể
    private String studentSubmissionStatus;  // "NOT_SUBMITTED", "SUBMITTED", "AUTO_SUBMITTED"
    private Double studentScore;             // Điểm của student (null nếu chưa nộp)
    private Double maxScoreForStudent;       // Điểm tối đa
    
    // Getters and Setters for new fields
    public String getStudentSubmissionStatus() { 
        return studentSubmissionStatus; 
    }
    
    public void setStudentSubmissionStatus(String studentSubmissionStatus) { 
        this. studentSubmissionStatus = studentSubmissionStatus; 
    }
    
    public Double getStudentScore() { 
        return studentScore; 
    }
    
    public void setStudentScore(Double studentScore) { 
        this.studentScore = studentScore; 
    }
    
    public Double getMaxScoreForStudent() { 
        return maxScoreForStudent; 
    }
    
    public void setMaxScoreForStudent(Double maxScoreForStudent) { 
        this.maxScoreForStudent = maxScoreForStudent; 
    }
    
    // Helper methods
    /**
     * Check if current student has submitted this exam
     */
    public boolean hasStudentSubmitted() {
        return "SUBMITTED".equals(studentSubmissionStatus) || 
               "AUTO_SUBMITTED". equals(studentSubmissionStatus);
    }
    
    /**
     * Get formatted score display:  "8.5/10.0" or "-"
     */
    public String getStudentScoreDisplay() {
        if (! hasStudentSubmitted() || studentScore == null || maxScoreForStudent == null) {
            return "-";
        }
        return String.format("%.1f/%. 1f", studentScore, maxScoreForStudent);
    }
}