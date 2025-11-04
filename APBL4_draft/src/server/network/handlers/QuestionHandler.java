package server.network.handlers;

import server.service.AuthService;
import server.service.QuestionService;
import server.service.ServiceResult;
import model.Question;
import model.User;
import utils.JsonUtil;
import java.util.List;
import java.util.Map;

public class QuestionHandler extends BaseHandler {
    private final QuestionService questionService;
    
    public QuestionHandler(AuthService authService) {
        super(authService);
        this.questionService = new QuestionService();
    }
    
    /**
     * Handle create question request (Admin only)
     */
    public String handleCreateQuestion(String data, String sessionToken) {
        System.out.println("📝 [CREATE_QUESTION] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [CREATE_QUESTION] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Question question = JsonUtil.fromJson(data, Question.class);
            if (question == null) {
                return invalidRequestResponse("Invalid question data");
            }
            
            ServiceResult<Question> result = questionService.createQuestion(question);
            if (result.isSuccess()) {
                String questionJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [CREATE_QUESTION] Question created successfully");
                return successResponse(questionJson);
            } else {
                System.out.println("❌ [CREATE_QUESTION] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [CREATE_QUESTION] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error creating question: " + e.getMessage());
        }
    }

    /**
     * Handle update question request (Admin only)
     */
    public String handleUpdateQuestion(String data, String sessionToken) {
        System.out.println("📝 [UPDATE_QUESTION] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [UPDATE_QUESTION] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Question question = JsonUtil.fromJson(data, Question.class);
            if (question == null) {
                return invalidRequestResponse("Invalid question data");
            }
            
            ServiceResult<Question> result = questionService.updateQuestion(question);
            if (result.isSuccess()) {
                String questionJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [UPDATE_QUESTION] Question updated successfully");
                return successResponse(questionJson);
            } else {
                System.out.println("❌ [UPDATE_QUESTION] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [UPDATE_QUESTION] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error updating question: " + e.getMessage());
        }
    }

    /**
     * Handle delete question request (Admin only)
     */
    public String handleDeleteQuestion(String data, String sessionToken) {
        System.out.println("📝 [DELETE_QUESTION] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [DELETE_QUESTION] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("questionId")) {
                return invalidRequestResponse("Question ID is required");
            }
            
            int questionId = ((Number) requestData.get("questionId")).intValue();
            ServiceResult<Boolean> result = questionService.deleteQuestion(questionId);
            
            if (result.isSuccess()) {
                System.out.println("✅ [DELETE_QUESTION] Question deleted successfully");
                return successResponse("Question deleted successfully");
            } else {
                System.out.println("❌ [DELETE_QUESTION] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [DELETE_QUESTION] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error deleting question: " + e.getMessage());
        }
    }

    /**
     * Handle get questions request (Admin only)
     */
    public String handleGetQuestions(String sessionToken) {
        System.out.println("📝 [GET_QUESTIONS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [GET_QUESTIONS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            ServiceResult<List<Question>> result = questionService.getAllQuestions();
            if (result.isSuccess()) {
                String questionsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_QUESTIONS] Retrieved " + result.getData().size() + " questions");
                return successResponse(questionsJson);
            } else {
                System.out.println("❌ [GET_QUESTIONS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_QUESTIONS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting questions: " + e.getMessage());
        }
    }

    /**
     * Handle search questions request (Admin only)
     */
    public String handleSearchQuestions(String data, String sessionToken) {
        System.out.println("📝 [SEARCH_QUESTIONS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [SEARCH_QUESTIONS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("keyword")) {
                return invalidRequestResponse("Search keyword is required");
            }
            
            String keyword = (String) requestData.get("keyword");
            ServiceResult<List<Question>> result = questionService.searchQuestions(keyword);
            
            if (result.isSuccess()) {
                String questionsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [SEARCH_QUESTIONS] Found " + result.getData().size() + " questions");
                return successResponse(questionsJson);
            } else {
                System.out.println("❌ [SEARCH_QUESTIONS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [SEARCH_QUESTIONS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error searching questions: " + e.getMessage());
        }
    }

    /**
     * Handle get questions by subject request
     */
    public String handleGetQuestionsBySubject(String data, String sessionToken) {
        System.out.println("📝 [GET_QUESTIONS_BY_SUBJECT] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_QUESTIONS_BY_SUBJECT] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("subjectId")) {
                return invalidRequestResponse("Subject ID is required");
            }
            
            int subjectId = ((Number) requestData.get("subjectId")).intValue();
            ServiceResult<List<Question>> result = questionService.getQuestionsBySubject(subjectId);
            
            if (result.isSuccess()) {
                String questionsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_QUESTIONS_BY_SUBJECT] Retrieved " + result.getData().size() + " questions for subject " + subjectId);
                return successResponse(questionsJson);
            } else {
                System.out.println("❌ [GET_QUESTIONS_BY_SUBJECT] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_QUESTIONS_BY_SUBJECT] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting questions by subject: " + e.getMessage());
        }
    }

    /**
     * Handle get questions by difficulty request (Admin only)
     */
    public String handleGetQuestionsByDifficulty(String data, String sessionToken) {
        System.out.println("📝 [GET_QUESTIONS_BY_DIFFICULTY] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_QUESTIONS")) {
            System.out.println("❌ [GET_QUESTIONS_BY_DIFFICULTY] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("difficulty")) {
                return invalidRequestResponse("Difficulty is required");
            }
            
            String difficulty = (String) requestData.get("difficulty");
            ServiceResult<List<Question>> result = questionService.getQuestionsByDifficulty(difficulty);
            
            if (result.isSuccess()) {
                String questionsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_QUESTIONS_BY_DIFFICULTY] Retrieved " + result.getData().size() + " " + difficulty + " questions");
                return successResponse(questionsJson);
            } else {
                System.out.println("❌ [GET_QUESTIONS_BY_DIFFICULTY] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_QUESTIONS_BY_DIFFICULTY] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting questions by difficulty: " + e.getMessage());
        }
    }

    /**
     * Handle get random questions request (For exam generation)
     */
    public String handleGetRandomQuestions(String data, String sessionToken) {
        System.out.println("📝 [GET_RANDOM_QUESTIONS] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_RANDOM_QUESTIONS] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("subjectId") || !requestData.containsKey("count")) {
                return invalidRequestResponse("Subject ID and count are required");
            }
            
            int subjectId = ((Number) requestData.get("subjectId")).intValue();
            int count = ((Number) requestData.get("count")).intValue();
            
            ServiceResult<List<Question>> result = questionService.getRandomQuestions(subjectId, count);
            
            if (result.isSuccess()) {
                String questionsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_RANDOM_QUESTIONS] Retrieved " + result.getData().size() + " random questions");
                return successResponse(questionsJson);
            } else {
                System.out.println("❌ [GET_RANDOM_QUESTIONS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_RANDOM_QUESTIONS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting random questions: " + e.getMessage());
        }
    }
}