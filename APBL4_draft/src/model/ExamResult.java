package model;

/**
 * ExamResult model - Represents exam results and statistics
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 15:54:30 UTC
 */
public class ExamResult {
    private int sessionId;
    private int roomId;
    private int studentId;
    private String studentName;
    private String roomName;
    private String subjectName;
    private double totalScore;
    private double maxScore;
    private int correctAnswers;
    private int totalQuestions;
    private int timeSpentMinutes;
    private int timeLimitMinutes;
    private String status;
    private String submittedAt;
    
    // Statistics
    private double percentage;
    private String grade;
    private int ranking;
    private int totalParticipants;
    
    public ExamResult() {}
    
    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { 
        this.totalScore = totalScore;
        calculatePercentageAndGrade();
    }
    
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { 
        this.maxScore = maxScore;
        calculatePercentageAndGrade();
    }
    
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
    
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public int getRanking() { return ranking; }
    public void setRanking(int ranking) { this.ranking = ranking; }
    
    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }
    
    // Helper methods
    private void calculatePercentageAndGrade() {
        if (maxScore > 0) {
            this.percentage = (totalScore / maxScore) * 100;
            this.grade = calculateGrade(this.percentage);
        }
    }
    
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
    
    public boolean isPassed() {
        return percentage >= 60; // Assuming 60% is passing grade
    }
    
    public String getFormattedTimeSpent() {
        int hours = timeSpentMinutes / 60;
        int minutes = timeSpentMinutes % 60;
        if (hours > 0) {
            return String.format("%d giờ %d phút", hours, minutes);
        } else {
            return String.format("%d phút", minutes);
        }
    }
    
    @Override
    public String toString() {
        return "ExamResult{" +
                "studentName='" + studentName + '\'' +
                ", roomName='" + roomName + '\'' +
                ", totalScore=" + totalScore +
                ", percentage=" + String.format("%.1f", percentage) + "%" +
                ", grade='" + grade + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}