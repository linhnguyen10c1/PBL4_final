package model;

/**
 * DTO for student exam status in admin's ManageStudentsDialog
 * 
 * @author nck345
 * @since 2025-12-22
 */
public class StudentExamStatus {
    
    private int studentId;
    private String status;      // NOT_STARTED, IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED
    private Double score;       // Actual score (null if not submitted)
    private Double maxScore;    // Room's max score
    
    public StudentExamStatus() {}
    
    public StudentExamStatus(int studentId, String status, Double score, Double maxScore) {
        this.studentId = studentId;
        this.status = status;
        this.score = score;
        this.maxScore = maxScore;
    }
    
    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    
    /**
     * Get display string for UI
     * @return formatted status string for table display
     */
    public String getDisplayStatus() {
        if (status == null) {
            return "Not Started";
        }
        
        switch (status) {
            case "NOT_STARTED": 
                return "Not Started";
            case "IN_PROGRESS": 
                return "In Progress";
            case "SUBMITTED": 
                if (score != null && maxScore != null) {
                    return String.format("Submitted:  %.1f/%.1f", score, maxScore);
                }
                return "Submitted";
            case "AUTO_SUBMITTED":
                if (score != null && maxScore != null) {
                    return String.format("Auto-submitted: %.1f/%.1f", score, maxScore);
                }
                return "Auto-submitted";
            default: 
                return status;
        }
    }
    
    /**
     * Check if student can be removed from room
     * Only students who haven't started can be removed
     */
    public boolean canBeRemoved() {
        return status == null || "NOT_STARTED".equals(status);
    }
    
    /**
     * Check if student has submitted (manually or auto)
     */
    public boolean hasSubmitted() {
        return "SUBMITTED".equals(status) || "AUTO_SUBMITTED".equals(status);
    }
    
    @Override
    public String toString() {
        return "StudentExamStatus{" +
                "studentId=" + studentId +
                ", status='" + status + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                '}';
    }
}