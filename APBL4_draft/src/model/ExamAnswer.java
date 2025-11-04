package model;

import java.sql.Timestamp;

/**
 * ExamAnswer model - Represents a student's answer to a question
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 15:54:30 UTC
 */
public class ExamAnswer {
    private int sessionId;
    private int questionId;
    private int questionOrder;
    private String studentAnswer;
    private boolean isCorrect;
    private Timestamp answeredAt;
    
    // Related objects
    private Question question;
    
    public ExamAnswer() {}
    
    public ExamAnswer(int sessionId, int questionId, int questionOrder) {
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.questionOrder = questionOrder;
    }
    
    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    
    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
    
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { this.isCorrect = correct; }
    
    public Timestamp getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Timestamp answeredAt) { this.answeredAt = answeredAt; }
    
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    
    // Helper methods
    public boolean isAnswered() {
        return studentAnswer != null && !studentAnswer.trim().isEmpty();
    }
    
    public double getScore() {
        if (question == null || !isCorrect) {
            return 0.0;
        }
        
        // Base score is 1.0 for correct answer
        double baseScore = 1.0;
        
        // Apply difficulty multiplier
        switch (question.getDifficulty()) {
            case "EASY":
                return baseScore * 1.0;    // Easy questions worth 1x
            case "MEDIUM":
                return baseScore * 1.2;    // Medium questions worth 1.2x
            case "HARD":
                return baseScore * 1.5;    // Hard questions worth 1.5x
            default:
                return baseScore;
        }
    }
    
    /**
     * Get points for this answer (alternative scoring method)
     */
    public double getPoints() {
        if (question == null) {
            return 0.0;
        }
        
        // If correct, return points based on difficulty
        if (isCorrect) {
            switch (question.getDifficulty()) {
                case "EASY": return 1.0;
                case "MEDIUM": return 2.0;
                case "HARD": return 3.0;
                default: return 1.0;
            }
        }
        
        return 0.0; // Wrong answer = 0 points
    }
    
    /**
     * Get percentage score (0-100)
     */
    public double getPercentageScore() {
        return isCorrect ? 100.0 : 0.0;
    }
    
    
    @Override
    public String toString() {
        return "ExamAnswer{" +
                "sessionId=" + sessionId +
                ", questionId=" + questionId +
                ", questionOrder=" + questionOrder +
                ", studentAnswer='" + studentAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}