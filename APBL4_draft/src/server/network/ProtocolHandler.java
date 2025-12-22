package server.network;

import server.service.AuthService;
import server.network.handlers.*;
import utils.Protocol;

public class ProtocolHandler {
    
    // Services
    private final AuthService authService;
    
    // Handlers
    private final AuthHandler authHandler;
    private final UserHandler userHandler;
    private final QuestionHandler questionHandler;
    private final ExamRoomHandler examRoomHandler;
    private final ExamHandler examHandler;
    
    public ProtocolHandler() {
        this.authService = new AuthService();
        
        // Initialize handlers
        this.authHandler = new AuthHandler(authService);
        this.userHandler = new UserHandler(authService);
        this.questionHandler = new QuestionHandler(authService);
        this.examRoomHandler = new ExamRoomHandler(authService);
        this.examHandler = new ExamHandler(authService);
    }
    
    /**
     * Main request router
     */
    public String handleRequest(String action, String data, String sessionToken) {
        try {
            String response = routeRequest(action, data, sessionToken);
            System.out.println("📤 Server sending response: " + response);
            return response;
        } catch (Exception e) {
            String errorResponse = Protocol.ERROR + Protocol.DELIMITER + "Internal server error";
            System.out.println("📤 Server sending error response: " + errorResponse);
            return errorResponse;
        }
    }
    
    /**
     * Route requests to appropriate handlers
     */
    private String routeRequest(String action, String data, String sessionToken) {
        switch (action) {
            // Authentication
            case Protocol.LOGIN:
                return authHandler.handleLogin(data);
            case Protocol.LOGOUT:
                return authHandler.handleLogout(sessionToken);
            
            // User Management
            case Protocol.CREATE_USER:
                return userHandler.handleCreateUser(data, sessionToken);
            case Protocol.UPDATE_USER:
                return userHandler.handleUpdateUser(data, sessionToken);
            case Protocol.DELETE_USER:
                return userHandler.handleDeleteUser(data, sessionToken);
            case Protocol.GET_USERS:
                return userHandler.handleGetUsers(sessionToken);
            case Protocol.SEARCH_USERS:
                return userHandler.handleSearchUsers(data, sessionToken);
            
            // Question Management
            case Protocol.CREATE_QUESTION:
                return questionHandler.handleCreateQuestion(data, sessionToken);
            case Protocol.UPDATE_QUESTION:
                return questionHandler.handleUpdateQuestion(data, sessionToken);
            case Protocol.DELETE_QUESTION:
                return questionHandler.handleDeleteQuestion(data, sessionToken);
            case Protocol.GET_QUESTIONS:
                return questionHandler.handleGetQuestions(sessionToken);
            case Protocol.SEARCH_QUESTIONS:
                return questionHandler.handleSearchQuestions(data, sessionToken);
            case Protocol.GET_QUESTIONS_BY_SUBJECT:
                return questionHandler.handleGetQuestionsBySubject(data, sessionToken);
            case Protocol.GET_QUESTIONS_BY_DIFFICULTY:
                return questionHandler.handleGetQuestionsByDifficulty(data, sessionToken);
            case Protocol.GET_RANDOM_QUESTIONS:
                return questionHandler.handleGetRandomQuestions(data, sessionToken);
            
            // Room Management
            case Protocol.CREATE_ROOM:
                return examRoomHandler.handleCreateRoom(data, sessionToken);
            case Protocol.UPDATE_ROOM:
                return examRoomHandler.handleUpdateRoom(data, sessionToken);
            case Protocol.GET_ROOMS:
                return examRoomHandler.handleGetRooms(sessionToken);
            case Protocol.DELETE_ROOM:
                return examRoomHandler.handleDeleteRoom(data, sessionToken);
            case Protocol.GET_AVAILABLE_ROOMS:
                return examRoomHandler.handleGetAvailableRooms(sessionToken);
            case Protocol.SEARCH_ROOMS:
                return examRoomHandler.handleSearchRooms(data, sessionToken);
            case Protocol.GET_SUBJECTS:
                return examRoomHandler.handleGetSubjects(sessionToken);
            case Protocol.ADD_STUDENTS_TO_ROOM:
                return examRoomHandler.handleAddStudentsToRoom(data, sessionToken);
            case Protocol.GET_STUDENT_STATUSES:
            	return examRoomHandler.handleGetStudentStatuses(data, sessionToken);
            
            // Exam Operations
            case Protocol.GET_AVAILABLE_EXAM_ROOMS:
                return examHandler.handleGetAvailableExamRooms(sessionToken);
            case Protocol.JOIN_EXAM_ROOM:
                return examHandler.handleJoinExamRoom(data, sessionToken);
            case Protocol.START_EXAM:
                return examHandler.handleStartExam(data, sessionToken);
            case Protocol.SAVE_EXAM_ANSWER:
                return examHandler.handleSaveExamAnswer(data, sessionToken);
            case Protocol.SUBMIT_EXAM:
                return examHandler.handleSubmitExam(data, sessionToken);
            case Protocol.GET_EXAM_SESSION:
                return examHandler.handleGetExamSession(data, sessionToken);
            case Protocol.GET_STUDENT_EXAM_RESULTS:
                return examHandler.handleGetStudentExamResults(sessionToken);
            case Protocol.GET_EXAM_SESSIONS:
                return examHandler.handleGetExamSessions(data, sessionToken);
            case Protocol.AUTO_SUBMIT_EXPIRED_EXAMS:
                return examHandler.handleAutoSubmitExpiredExams(sessionToken);
            
            // System
            case Protocol.PING:
                return Protocol.PONG;
            
            default:
                return Protocol.INVALID_REQUEST + Protocol.DELIMITER + "Unknown action: " + action;
        }
    }
}