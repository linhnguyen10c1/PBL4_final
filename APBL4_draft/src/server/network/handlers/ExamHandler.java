package server.network.handlers;

import server.service.AuthService;
import server.service.ExamService;
import server.service.ServiceResult;
import model.*;
import utils.JsonUtil;
import java.util.List;
import java.util.Map;

public class ExamHandler extends BaseHandler {
    private final ExamService examService;
    
    public ExamHandler(AuthService authService) {
        super(authService);
        this.examService = new ExamService();
    }
    
    /**
     * Handle get available exam rooms request (Student)
     */
    public String handleGetAvailableExamRooms(String sessionToken) {
        System.out.println("🎓 [GET_AVAILABLE_EXAM_ROOMS] Processing request");
        System.out.println("🔍 [GET_AVAILABLE_EXAM_ROOMS] Session token: " + 
                         (sessionToken != null ? sessionToken.substring(0, Math.min(10, sessionToken.length())) + "..." : "null"));
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_AVAILABLE_EXAM_ROOMS] Session expired or invalid");
            return sessionExpiredResponse();
        }
        
        System.out.println("🔍 [GET_AVAILABLE_EXAM_ROOMS] User validated: " + user.getUsername() + " (Role: " + user.getRole() + ")");
        
        if (!user.isStudent()) {
            System.out.println("❌ [GET_AVAILABLE_EXAM_ROOMS] Access denied - not a student. Role: " + user.getRole());
            return accessDeniedResponse();
        }
        
        try {
            System.out.println("🔍 [GET_AVAILABLE_EXAM_ROOMS] Calling examService.getAvailableExamRooms for user ID: " + user.getUserId());
            ServiceResult<List<ExamRoom>> result = examService.getAvailableExamRooms(user.getUserId());
            
            if (result.isSuccess()) {
                String roomsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_AVAILABLE_EXAM_ROOMS] Success - Retrieved " + result.getData().size() + " available exam rooms");
                System.out.println("🔍 [GET_AVAILABLE_EXAM_ROOMS] Response JSON length: " + roomsJson.length());
                return successResponse(roomsJson);
            } else {
                System.out.println("❌ [GET_AVAILABLE_EXAM_ROOMS] Service failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_AVAILABLE_EXAM_ROOMS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting available exam rooms: " + e.getMessage());
        }
    }

    /**
     * Handle join exam room request (Student)
     */
    public String handleJoinExamRoom(String data, String sessionToken) {
        System.out.println("🎓 [JOIN_EXAM_ROOM] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [JOIN_EXAM_ROOM] Session expired");
            return sessionExpiredResponse();
        }
        
        if (!user.isStudent()) {
            System.out.println("❌ [JOIN_EXAM_ROOM] Access denied - not a student");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("roomId") || !requestData.containsKey("password")) {
                return invalidRequestResponse("Room ID and password are required");
            }
            
            int roomId = ((Number) requestData.get("roomId")).intValue();
            String password = (String) requestData.get("password");
            
            ServiceResult<ExamSession> result = examService.joinExamRoom(user.getUserId(), roomId, password);
            if (result.isSuccess()) {
                String sessionJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [JOIN_EXAM_ROOM] Student joined exam room successfully");
                return successResponse(sessionJson);
            } else {
                System.out.println("❌ [JOIN_EXAM_ROOM] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [JOIN_EXAM_ROOM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error joining exam room: " + e.getMessage());
        }
    }

    /**
     * Handle start exam request (Student)
     */
    public String handleStartExam(String data, String sessionToken) {
        System.out.println("🎓 [START_EXAM] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [START_EXAM] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("sessionToken")) {
                return invalidRequestResponse("Session token is required");
            }
            
            String examSessionToken = (String) requestData.get("sessionToken");
            
            ServiceResult<List<ExamAnswer>> result = examService.startExam(examSessionToken);
            if (result.isSuccess()) {
                String answersJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [START_EXAM] Exam started successfully");
                return successResponse(answersJson);
            } else {
                System.out.println("❌ [START_EXAM] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [START_EXAM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error starting exam: " + e.getMessage());
        }
    }

    /**
     * Handle save exam answer request (Student)
     */
    public String handleSaveExamAnswer(String data, String sessionToken) {
        // Don't log every answer save to avoid spam
        
        User user = validateSession(sessionToken);
        if (user == null) {
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("sessionToken") || 
                !requestData.containsKey("questionId") || !requestData.containsKey("answer")) {
                return invalidRequestResponse("Session token, question ID, and answer are required");
            }
            
            String examSessionToken = (String) requestData.get("sessionToken");
            int questionId = ((Number) requestData.get("questionId")).intValue();
            String answer = (String) requestData.get("answer");
            
            ServiceResult<Boolean> result = examService.saveAnswer(examSessionToken, questionId, answer);
            if (result.isSuccess()) {
                return successResponse("Answer saved successfully");
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [SAVE_EXAM_ANSWER] Exception: " + e.getMessage());
            return errorResponse("Error saving answer: " + e.getMessage());
        }
    }

    /**
     * Handle submit exam request (Student)
     */
    public String handleSubmitExam(String data, String sessionToken) {
        System.out.println("🎓 [SUBMIT_EXAM] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [SUBMIT_EXAM] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("sessionToken")) {
                return invalidRequestResponse("Session token is required");
            }
            
            String examSessionToken = (String) requestData.get("sessionToken");
            boolean isAutoSubmit = requestData.containsKey("isAutoSubmit") ? 
                (Boolean) requestData.get("isAutoSubmit") : false;
            
            ServiceResult<ExamResult> result = examService.submitExam(examSessionToken, isAutoSubmit);
            if (result.isSuccess()) {
                String resultJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [SUBMIT_EXAM] Exam submitted successfully");
                return successResponse(resultJson);
            } else {
                System.out.println("❌ [SUBMIT_EXAM] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [SUBMIT_EXAM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error submitting exam: " + e.getMessage());
        }
    }

    /**
     * Handle get exam session request (Student)
     */
    public String handleGetExamSession(String data, String sessionToken) {
        User user = validateSession(sessionToken);
        if (user == null) {
            return sessionExpiredResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null || !requestData.containsKey("sessionToken")) {
                return invalidRequestResponse("Session token is required");
            }
            
            String examSessionToken = (String) requestData.get("sessionToken");
            
            ServiceResult<ExamSession> result = examService.getExamSession(examSessionToken);
            if (result.isSuccess()) {
                String sessionJson = JsonUtil.toJson(result.getData());
                return successResponse(sessionJson);
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_EXAM_SESSION] Exception: " + e.getMessage());
            return errorResponse("Error getting exam session: " + e.getMessage());
        }
    }

    /**
     * Handle get exam sessions request (Admin)
     */
    public String handleGetExamSessions(String data, String sessionToken) {
        System.out.println("🎓 [GET_EXAM_SESSIONS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_EXAMS")) {
            System.out.println("❌ [GET_EXAM_SESSIONS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            
            // Check if requesting sessions for specific room
            if (requestData != null && requestData.containsKey("roomId")) {
                int roomId = ((Number) requestData.get("roomId")).intValue();
                ServiceResult<List<ExamSession>> result = examService.getExamSessionsForRoom(roomId);
                
                if (result.isSuccess()) {
                    String sessionsJson = JsonUtil.toJson(result.getData());
                    System.out.println("✅ [GET_EXAM_SESSIONS] Retrieved " + result.getData().size() + " sessions for room " + roomId);
                    return successResponse(sessionsJson);
                } else {
                    System.out.println("❌ [GET_EXAM_SESSIONS] Failed: " + result.getMessage());
                    return errorResponse(result.getMessage());
                }
            } else {
                // Get all exam sessions
                ServiceResult<List<ExamSession>> result = examService.getAllExamSessions();
                
                if (result.isSuccess()) {
                    String sessionsJson = JsonUtil.toJson(result.getData());
                    System.out.println("✅ [GET_EXAM_SESSIONS] Retrieved " + result.getData().size() + " exam sessions");
                    return successResponse(sessionsJson);
                } else {
                    System.out.println("❌ [GET_EXAM_SESSIONS] Failed: " + result.getMessage());
                    return errorResponse(result.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_EXAM_SESSIONS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting exam sessions: " + e.getMessage());
        }
    }

    /**
     * Handle get student exam results request (Student)
     */
    public String handleGetStudentExamResults(String sessionToken) {
        System.out.println("🎓 [GET_STUDENT_EXAM_RESULTS] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_STUDENT_EXAM_RESULTS] Session expired");
            return sessionExpiredResponse();
        }
        
        if (!user.isStudent()) {
            System.out.println("❌ [GET_STUDENT_EXAM_RESULTS] Access denied - not a student");
            return accessDeniedResponse();
        }
        
        try {
            ServiceResult<List<ExamResult>> result = examService.getStudentExamResults(user.getUserId());
            if (result.isSuccess()) {
                String resultsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_STUDENT_EXAM_RESULTS] Retrieved exam results successfully");
                return successResponse(resultsJson);
            } else {
                System.out.println("❌ [GET_STUDENT_EXAM_RESULTS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_STUDENT_EXAM_RESULTS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting exam results: " + e.getMessage());
        }
    }

    /**
     * Handle auto-submit expired exams request (Admin)
     */
    public String handleAutoSubmitExpiredExams(String sessionToken) {
        System.out.println("🎓 [AUTO_SUBMIT_EXPIRED_EXAMS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_EXAMS")) {
            System.out.println("❌ [AUTO_SUBMIT_EXPIRED_EXAMS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            ServiceResult<Integer> result = examService.autoSubmitExpiredExams();
            if (result.isSuccess()) {
                System.out.println("✅ [AUTO_SUBMIT_EXPIRED_EXAMS] Auto-submitted " + result.getData() + " expired exams");
                return successResponse("Auto-submitted " + result.getData() + " expired exams");
            } else {
                System.out.println("❌ [AUTO_SUBMIT_EXPIRED_EXAMS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [AUTO_SUBMIT_EXPIRED_EXAMS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error auto-submitting expired exams: " + e.getMessage());
        }
    }
}