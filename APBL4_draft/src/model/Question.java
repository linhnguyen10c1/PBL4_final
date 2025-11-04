package model;

/**
 * Question model class
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 03:59:16 UTC
 */
public class Question {
    private int questionId;
    private int subjectId;
    private String subjectName;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String difficulty;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
    
    public Question() {
        this.isActive = true;
        this.difficulty = "MEDIUM";
    }
    
    // Getters and Setters
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public void setIsActive(boolean active) { this.isActive = active; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isEasy() { return "EASY".equals(difficulty); }
    public boolean isMedium() { return "MEDIUM".equals(difficulty); }
    public boolean isHard() { return "HARD".equals(difficulty); }
    
    public String getCorrectOptionText() {
        switch (correctAnswer) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default: return "";
        }
    }
    
    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", subjectName='" + subjectName + '\'' +
                ", questionText='" + questionText.substring(0, Math.min(50, questionText.length())) + "...'" +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}