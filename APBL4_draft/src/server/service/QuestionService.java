package server.service;

import model.Question;
import model.Subject;
import server.dao.QuestionDAO;
import server.dao.SubjectDAO;
import java.util.List;

/**
 * Question Service - Business logic for question management
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 03:59:16 UTC
 */
public class QuestionService {
    
    private final QuestionDAO questionDAO;
    private final SubjectDAO subjectDAO;
    
    public QuestionService() {
        this.questionDAO = new QuestionDAO();
        this.subjectDAO = new SubjectDAO();
    }
    
    /**
     * Create new question
     */
    public ServiceResult<Question> createQuestion(Question question) {
        try {
            System.out.println("📝 [QuestionService] Creating question for subject: " + question.getSubjectId());
            
            // Validate input
            ValidationResult validation = validateQuestionForCreation(question);
            if (!validation.isValid()) {
                System.out.println("❌ [QuestionService] Validation failed: " + validation.getMessage());
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if subject exists and is active
            Subject subject = subjectDAO.findById(question.getSubjectId());
            if (subject == null || !subject.isActive()) {
                System.out.println("❌ [QuestionService] Subject not found or inactive: " + question.getSubjectId());
                return ServiceResult.error("Subject not found or inactive");
            }
            
            // Set subject name for display
            question.setSubjectName(subject.getSubjectName());
            
            // Create question
            Question createdQuestion = questionDAO.create(question);
            if (createdQuestion != null) {
                System.out.println("✅ [QuestionService] Question created successfully with ID: " + createdQuestion.getQuestionId());
                return ServiceResult.success("Question created successfully", createdQuestion);
            } else {
                return ServiceResult.error("Failed to create question");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error creating question: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to create question: " + e.getMessage());
        }
    }
    
    /**
     * Update question
     */
    public ServiceResult<Question> updateQuestion(Question question) {
        try {
            System.out.println("📝 [QuestionService] Updating question ID: " + question.getQuestionId());
            
            // Validate input
            ValidationResult validation = validateQuestionForUpdate(question);
            if (!validation.isValid()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if question exists
            Question existing = questionDAO.findById(question.getQuestionId());
            if (existing == null) {
                System.out.println("❌ [QuestionService] Question not found: " + question.getQuestionId());
                return ServiceResult.error("Question not found");
            }
            
            // Check subject
            Subject subject = subjectDAO.findById(question.getSubjectId());
            if (subject == null || !subject.isActive()) {
                return ServiceResult.error("Subject not found or inactive");
            }
            
            // Set subject name
            question.setSubjectName(subject.getSubjectName());
            
            // Update question
            boolean updated = questionDAO.update(question);
            if (updated) {
                Question updatedQuestion = questionDAO.findById(question.getQuestionId());
                System.out.println("✅ [QuestionService] Question updated successfully");
                return ServiceResult.success("Question updated successfully", updatedQuestion);
            } else {
                return ServiceResult.error("Failed to update question");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error updating question: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to update question: " + e.getMessage());
        }
    }
    
    /**
     * Delete question (soft delete)
     */
    public ServiceResult<Boolean> deleteQuestion(int questionId) {
        try {
            System.out.println("📝 [QuestionService] Deleting question ID: " + questionId);
            
            Question question = questionDAO.findById(questionId);
            if (question == null) {
                System.out.println("❌ [QuestionService] Question not found: " + questionId);
                return ServiceResult.error("Question not found");
            }
            
            boolean deleted = questionDAO.delete(questionId);
            if (deleted) {
                System.out.println("✅ [QuestionService] Question deleted successfully");
                return ServiceResult.success("Question deleted successfully", true);
            } else {
                return ServiceResult.error("Failed to delete question");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error deleting question: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to delete question: " + e.getMessage());
        }
    }
    
    /**
     * Get all questions (Admin only)
     */
    public ServiceResult<List<Question>> getAllQuestions() {
        try {
            System.out.println("📝 [QuestionService] Getting all questions");
            
            List<Question> questions = questionDAO.findAll();
            System.out.println("✅ [QuestionService] Retrieved " + questions.size() + " questions");
            
            return ServiceResult.success("Questions retrieved successfully", questions);
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error getting questions: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve questions: " + e.getMessage());
        }
    }
    
    /**
     * Get questions by subject
     */
    public ServiceResult<List<Question>> getQuestionsBySubject(int subjectId) {
        try {
            System.out.println("📝 [QuestionService] Getting questions for subject: " + subjectId);
            
            List<Question> questions = questionDAO.findBySubject(subjectId);
            System.out.println("✅ [QuestionService] Retrieved " + questions.size() + " questions for subject");
            
            return ServiceResult.success("Questions retrieved successfully", questions);
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error getting questions by subject: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve questions: " + e.getMessage());
        }
    }
    
    /**
     * Search questions
     */
    public ServiceResult<List<Question>> searchQuestions(String keyword) {
        try {
            System.out.println("📝 [QuestionService] Searching questions with keyword: " + keyword);
            
            List<Question> questions;
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllQuestions();
            } else {
                questions = questionDAO.search(keyword.trim());
            }
            
            System.out.println("✅ [QuestionService] Search found " + questions.size() + " questions");
            return ServiceResult.success("Search completed", questions);
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error searching questions: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Search failed: " + e.getMessage());
        }
    }
    
    /**
     * Get questions by difficulty
     */
    public ServiceResult<List<Question>> getQuestionsByDifficulty(String difficulty) {
        try {
            System.out.println("📝 [QuestionService] Getting questions by difficulty: " + difficulty);
            
            List<Question> questions = questionDAO.findByDifficulty(difficulty);
            System.out.println("✅ [QuestionService] Retrieved " + questions.size() + " " + difficulty + " questions");
            
            return ServiceResult.success("Questions retrieved successfully", questions);
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error getting questions by difficulty: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve questions: " + e.getMessage());
        }
    }
    
    /**
     * Get random questions for exam
     */
    public ServiceResult<List<Question>> getRandomQuestions(int subjectId, int count) {
        try {
            System.out.println("📝 [QuestionService] Getting " + count + " random questions for subject: " + subjectId);
            
            // Check if enough questions available
            int availableCount = questionDAO.countBySubject(subjectId);
            if (availableCount < count) {
                return ServiceResult.error("Not enough questions available. Required: " + count + ", Available: " + availableCount);
            }
            
            List<Question> questions = questionDAO.getRandomQuestions(subjectId, count);
            System.out.println("✅ [QuestionService] Retrieved " + questions.size() + " random questions");
            
            return ServiceResult.success("Random questions retrieved successfully", questions);
            
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error getting random questions: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve random questions: " + e.getMessage());
        }
    }
    
    /**
     * Get question by ID
     */
    public ServiceResult<Question> getQuestionById(int questionId) {
        try {
            Question question = questionDAO.findById(questionId);
            if (question != null) {
                return ServiceResult.success("Question retrieved successfully", question);
            } else {
                return ServiceResult.error("Question not found");
            }
        } catch (Exception e) {
            System.err.println("❌ [QuestionService] Error getting question by ID: " + e.getMessage());
            return ServiceResult.error("Failed to retrieve question: " + e.getMessage());
        }
    }
    
    /**
     * Validate question for creation
     */
    private ValidationResult validateQuestionForCreation(Question question) {
        if (question == null) {
            return new ValidationResult(false, "Question data is required");
        }
        
        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
            return new ValidationResult(false, "Question text is required");
        }
        
        if (question.getQuestionText().length() > 1000) {
            return new ValidationResult(false, "Question text cannot exceed 1000 characters");
        }
        
        if (question.getSubjectId() <= 0) {
            return new ValidationResult(false, "Valid subject is required");
        }
        
        // Validate options
        if (question.getOptionA() == null || question.getOptionA().trim().isEmpty()) {
            return new ValidationResult(false, "Option A is required");
        }
        if (question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
            return new ValidationResult(false, "Option B is required");
        }
        if (question.getOptionC() == null || question.getOptionC().trim().isEmpty()) {
            return new ValidationResult(false, "Option C is required");
        }
        if (question.getOptionD() == null || question.getOptionD().trim().isEmpty()) {
            return new ValidationResult(false, "Option D is required");
        }
        
        // Validate option lengths
        if (question.getOptionA().length() > 500) {
            return new ValidationResult(false, "Option A cannot exceed 500 characters");
        }
        if (question.getOptionB().length() > 500) {
            return new ValidationResult(false, "Option B cannot exceed 500 characters");
        }
        if (question.getOptionC().length() > 500) {
            return new ValidationResult(false, "Option C cannot exceed 500 characters");
        }
        if (question.getOptionD().length() > 500) {
            return new ValidationResult(false, "Option D cannot exceed 500 characters");
        }
        
        // Validate correct answer
        if (question.getCorrectAnswer() == null || 
            !question.getCorrectAnswer().matches("[ABCD]")) {
            return new ValidationResult(false, "Correct answer must be A, B, C, or D");
        }
        
        // Validate difficulty
        if (question.getDifficulty() == null || 
            !question.getDifficulty().matches("EASY|MEDIUM|HARD")) {
            return new ValidationResult(false, "Difficulty must be EASY, MEDIUM, or HARD");
        }
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate question for update
     */
    private ValidationResult validateQuestionForUpdate(Question question) {
        if (question == null) {
            return new ValidationResult(false, "Question data is required");
        }
        
        if (question.getQuestionId() <= 0) {
            return new ValidationResult(false, "Valid question ID is required");
        }
        
        // Use same validation as creation for other fields
        return validateQuestionForCreation(question);
    }
    
    /**
     * Validation Result inner class
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}