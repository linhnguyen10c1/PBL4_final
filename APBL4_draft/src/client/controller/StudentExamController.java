package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.*;
import utils.JsonUtil;
import utils.Protocol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student Exam Controller - Handles exam operations for students
 * 
 * @author linhnguyen10c1
 * @since 2025-10-29 15:56:30 UTC
 */
public class StudentExamController extends BaseController {
    
    private ExamListener listener;
    
    public StudentExamController(NetworkManager networkManager) {
        super(networkManager);
    }
    
    /**
     * Get available exam rooms for student
     */
    public List<ExamRoom> getAvailableExamRooms() {
        try {
            logAction("getAvailableExamRooms", "Fetching available exam rooms");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_AVAILABLE_EXAM_ROOMS);
            
            if (response.isSuccess()) {
                List<ExamRoom> rooms = JsonUtil.fromJsontoList(response.getData(), ExamRoom.class);
                logAction("getAvailableExamRooms", "Retrieved " + (rooms != null ? rooms.size() : 0) + " available rooms");
                
                if (listener != null && rooms != null) {
                    listener.onAvailableRoomsLoaded(rooms);
                }
                return rooms;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Join exam room with password
     */
    public boolean joinExamRoom(int roomId, String password) {
        try {
            logAction("joinExamRoom", "Joining exam room: " + roomId);
            
            if (!validateSession()) {
                return false;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("roomId", roomId);
            requestData.put("password", password);
            
            ResponseData response = sendJsonRequest(Protocol.JOIN_EXAM_ROOM, requestData);
            
            if (response.isSuccess()) {
                logAction("joinExamRoom", "Successfully joined exam room");
                
                // ✅ FIX: Xử lý response data trực tiếp
                String responseData = response.getData();
                System.out.println("🔍 [DEBUG] Response data: " + responseData);
                
                ExamSession session;
                
                // Kiểm tra xem có prefix "SUCCESS|" không
                if (responseData.startsWith("SUCCESS|")) {
                    String[] parts = responseData.split("\\|", 2);
                    if (parts.length >= 2) {
                        session = JsonUtil.fromJson(parts[1], ExamSession.class);
                    } else {
                        session = null;
                    }
                } else {
                    // Nếu không có prefix, parse trực tiếp
                    session = JsonUtil.fromJson(responseData, ExamSession.class);
                }
                
                System.out.println("🔍 [DEBUG] Parsed session: " + (session != null ? "SUCCESS" : "NULL"));
                System.out.println("🔍 [DEBUG] Listener: " + (listener != null ? "SET" : "NULL"));
                
                if (session != null && listener != null) {
                    System.out.println("🔍 [DEBUG] Calling listener.onExamRoomJoined()");
                    listener.onExamRoomJoined(session);
                    return true;
                } else {
                    System.out.println("❌ [DEBUG] Cannot call listener - session or listener is null");
                    return false;
                }
            } else {
                handleServerError(response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Start exam
     */
//    public boolean startExam(String sessionToken) {
//        try {
//            logAction("startExam", "Starting exam with token: " + sessionToken);
//            
//            if (!validateSession()) {
//                return false;
//            }
//            
//            Map<String, Object> requestData = new HashMap<>();
//            requestData.put("sessionToken", sessionToken);
//            
//            ResponseData response = sendJsonRequest(Protocol.START_EXAM, requestData);
//            
//            if (response.isSuccess()) {
//                logAction("startExam", "Exam started successfully");
//                
//                // Parse exam questions from response
//                String[] parts = response.getData().split("\\|", 2);
//                if (parts.length >= 2) {
//                    List<ExamAnswer> examAnswers = JsonUtil.fromJsontoList(parts[1], ExamAnswer.class);
//                    if (examAnswers != null && listener != null) {
//                        listener.onExamStarted(examAnswers);
//                    }
//                }
//                return true;
//            } else {
//                handleServerError(response.getMessage());
//                return false;
//            }
//            
//        } catch (Exception e) {
//            handleNetworkError(e);
//            return false;
//        }
//    }
    /**
     * Start exam
     */
    public boolean startExam(String sessionToken) {
        try {
            logAction("startExam", "Starting exam with token: " + sessionToken);
            
            if (!validateSession()) {
                return false;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("sessionToken", sessionToken);
            
            ResponseData response = sendJsonRequest(Protocol.START_EXAM, requestData);
            
            if (response.isSuccess()) {
                logAction("startExam", "Exam started successfully");
                
                // ✅ FIX: Parse exam questions from response - QUAN TRỌNG!
                String responseData = response.getData();
                System.out.println("🔍 [DEBUG] START_EXAM response data: " + 
                                  responseData.substring(0, Math.min(200, responseData.length())) + "...");
                
                List<ExamAnswer> examAnswers;
                
                // ✅ FIX: Check if response has SUCCESS| prefix
                if (responseData.startsWith("SUCCESS|")) {
                    String[] parts = responseData.split("\\|", 2);
                    if (parts.length >= 2) {
                        System.out.println("🔍 [DEBUG] Parsing JSON from parts[1]: " + parts[1].substring(0, Math.min(100, parts[1].length())) + "...");
                        examAnswers = JsonUtil.fromJsontoList(parts[1], ExamAnswer.class);
                    } else {
                        System.err.println("❌ [DEBUG] Response split failed, parts length: " + parts.length);
                        examAnswers = null;
                    }
                } else {
                    // ✅ FIX: Parse directly if no prefix
                    System.out.println("🔍 [DEBUG] Parsing JSON directly: " + responseData.substring(0, Math.min(100, responseData.length())) + "...");
                    examAnswers = JsonUtil.fromJsontoList(responseData, ExamAnswer.class);
                }
                
                System.out.println("🔍 [DEBUG] Parsed examAnswers: " + (examAnswers != null ? examAnswers.size() + " questions" : "NULL"));
                
                if (examAnswers != null && listener != null) {
                    System.out.println("🔍 [DEBUG] Calling listener.onExamStarted() with " + examAnswers.size() + " questions");
                    listener.onExamStarted(examAnswers);
                    return true;
                } else {
                    System.err.println("❌ [DEBUG] Cannot call onExamStarted - examAnswers: " + 
                                     (examAnswers != null ? "OK" : "NULL") + ", listener: " + (listener != null ? "OK" : "NULL"));
                    return false;
                }
            } else {
                handleServerError(response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Exception in startExam: " + e.getMessage());
            e.printStackTrace();
            handleNetworkError(e);
            return false;
        }
    }
    /**
     * Save answer during exam
     */
    public boolean saveAnswer(String sessionToken, int questionId, String answer) {
        try {
            // Don't log every answer save to avoid spam
            if (!validateSession()) {
                return false;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("sessionToken", sessionToken);
            requestData.put("questionId", questionId);
            requestData.put("answer", answer);
            
            ResponseData response = sendJsonRequest(Protocol.SAVE_EXAM_ANSWER, requestData);
            
            if (response.isSuccess()) {
                if (listener != null) {
                    listener.onAnswerSaved(questionId, answer);
                }
                return true;
            } else {
                // Only show error if it's not a simple validation error
                if (!response.getMessage().contains("Invalid answer format")) {
                    handleServerError(response.getMessage());
                }
                return false;
            }
            
        } catch (Exception e) {
            // Don't show network errors for answer saves (too disruptive)
            System.err.println("Error saving answer: " + e.getMessage());
            return false;
        }
    }
    
    /**
	 * Submit exam
	 */
	public boolean submitExam(String sessionToken, boolean isAutoSubmit) {
	    try {
	        logAction("submitExam", "Submitting exam, auto:  " + isAutoSubmit);
	        
	        if (! validateSession()) {
	            return false;
	        }
	        
	        Map<String, Object> requestData = new HashMap<>();
	        requestData.put("sessionToken", sessionToken);
	        requestData.put("isAutoSubmit", isAutoSubmit);
	        
	        ResponseData response = sendJsonRequest(Protocol. SUBMIT_EXAM, requestData);
	        
	        if (response.isSuccess()) {
	            logAction("submitExam", "Exam submitted successfully");
	            
	            // ✅ FIX: Parse exam result from response - xử lý cả 2 format
	            String responseData = response.getData();
	            System.out.println("🔍 [DEBUG] SUBMIT_EXAM response data: " + 
	                              responseData.substring(0, Math.min(200, responseData.length())) + "...");
	            
	            ExamResult result = null;
	            
	            // Check if response has SUCCESS| prefix
	            if (responseData.startsWith("SUCCESS|")) {
	                String[] parts = responseData. split("\\|", 2);
	                if (parts.length >= 2) {
	                    System.out.println("🔍 [DEBUG] Parsing JSON from parts[1]");
	                    result = JsonUtil.fromJson(parts[1], ExamResult.class);
	                }
	            } else {
	                // Parse directly if no prefix (JSON thuần)
	                System.out.println("🔍 [DEBUG] Parsing JSON directly");
	                result = JsonUtil.fromJson(responseData, ExamResult.class);
	            }
	            
	            System.out.println("🔍 [DEBUG] Parsed ExamResult: " + (result != null ? "SUCCESS" : "NULL"));
	            
	            if (result != null) {
	                System.out.println("🔍 [DEBUG] ExamResult - Score: " + result. getTotalScore() + 
	                                  ", Correct: " + result. getCorrectAnswers() + "/" + result.getTotalQuestions());
	                
	                if (listener != null) {
	                    System.out.println("🔍 [DEBUG] Calling listener. onExamSubmitted()");
	                    listener.onExamSubmitted(result);
	                }
	            } else {
	                System.err.println("❌ [DEBUG] Failed to parse ExamResult from response");
	            }
	            
	            showSuccessMessage("Success", "Exam submitted successfully!");
	            return true;
	        } else {
	            handleServerError(response.getMessage());
	            return false;
	        }
	        
	    } catch (Exception e) {
	        System.err.println("❌ [DEBUG] Exception in submitExam: " + e.getMessage());
	        e.printStackTrace();
	        handleNetworkError(e);
	        return false;
	    }
	}
	
    /**
     * Get exam session info
     */
    public ExamSession getExamSession(String sessionToken) {
        try {
            if (!validateSession()) {
                return null;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("sessionToken", sessionToken);
            
            ResponseData response = sendJsonRequest(Protocol.GET_EXAM_SESSION, requestData);
            
            if (response.isSuccess()) {
                String[] parts = response.getData().split("\\|", 2);
                if (parts.length >= 2) {
                    return JsonUtil.fromJson(parts[1], ExamSession.class);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Error getting exam session: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get student exam results
     */
    public List<ExamResult> getExamResults() {
        try {
            logAction("getExamResults", "Fetching exam results");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_STUDENT_EXAM_RESULTS);
            
            if (response.isSuccess()) {
                List<ExamResult> results = JsonUtil.fromJsontoList(response.getData(), ExamResult.class);
                logAction("getExamResults", "Retrieved " + (results != null ? results.size() : 0) + " exam results");
                
                if (listener != null && results != null) {
                    listener.onExamResultsLoaded(results);
                }
                return results;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Set exam listener
     */
    public void setExamListener(ExamListener listener) {
        this.listener = listener;
    }
    
    /**
     * Exam Listener interface
     */
    public interface ExamListener {
        void onAvailableRoomsLoaded(List<ExamRoom> rooms);
        void onExamRoomJoined(ExamSession session);
        void onExamStarted(List<ExamAnswer> questions);
        void onAnswerSaved(int questionId, String answer);
        void onExamSubmitted(ExamResult result);
        void onExamResultsLoaded(List<ExamResult> results);
        void onExamTimeExpired();
        void onError(String message);
    }
}