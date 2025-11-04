package utils;

/**
 * Protocol Constants for client-server communication
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:30:04 UTC
 */
public class Protocol {
    
    // Message delimiter
    public static final String DELIMITER = "|";
    
    // Response status codes
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    
    // Authentication
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
    
    // User Management (Admin only)
    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String GET_USERS = "GET_USERS";
    public static final String SEARCH_USERS = "SEARCH_USERS";
    
    // Subject Management
    public static final String GET_SUBJECTS = "GET_SUBJECTS";
    public static final String CREATE_SUBJECT = "CREATE_SUBJECT";
    public static final String UPDATE_SUBJECT = "UPDATE_SUBJECT";
    public static final String DELETE_SUBJECT = "DELETE_SUBJECT";
    
    // Question Management (Admin only)
 // Question Management (Admin only)
    public static final String CREATE_QUESTION = "CREATE_QUESTION";
    public static final String UPDATE_QUESTION = "UPDATE_QUESTION"; 
    public static final String DELETE_QUESTION = "DELETE_QUESTION";
    public static final String GET_QUESTIONS = "GET_QUESTIONS";
    public static final String SEARCH_QUESTIONS = "SEARCH_QUESTIONS";
    public static final String GET_QUESTIONS_BY_SUBJECT = "GET_QUESTIONS_BY_SUBJECT";
    public static final String GET_QUESTIONS_BY_DIFFICULTY = "GET_QUESTIONS_BY_DIFFICULTY";
    public static final String GET_RANDOM_QUESTIONS = "GET_RANDOM_QUESTIONS";
    
    // Room Management (Admin only)
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String UPDATE_ROOM = "UPDATE_ROOM";
    public static final String DELETE_ROOM = "DELETE_ROOM";
    public static final String GET_ROOMS = "GET_ROOMS";
    public static final String GET_AVAILABLE_ROOMS = "GET_AVAILABLE_ROOMS";
    public static final String ADD_STUDENTS_TO_ROOM = "ADD_STUDENTS_TO_ROOM";
    public static final String REMOVE_STUDENTS_FROM_ROOM = "REMOVE_STUDENTS_FROM_ROOM";
    public static final String SEARCH_ROOMS = "SEARCH_ROOMS";
    
    // Exam Operations (Student)
    public static final String GET_AVAILABLE_EXAM_ROOMS = "GET_AVAILABLE_EXAM_ROOMS";
    public static final String JOIN_EXAM_ROOM = "JOIN_EXAM_ROOM";
    public static final String START_EXAM = "START_EXAM";
    public static final String SAVE_EXAM_ANSWER = "SAVE_EXAM_ANSWER";
    public static final String SUBMIT_EXAM = "SUBMIT_EXAM";
    public static final String GET_EXAM_SESSION = "GET_EXAM_SESSION";
    public static final String GET_STUDENT_EXAM_RESULTS = "GET_STUDENT_EXAM_RESULTS";

    // Admin Exam Operations  
    public static final String GET_EXAM_SESSIONS = "GET_EXAM_SESSIONS";
    public static final String AUTO_SUBMIT_EXPIRED_EXAMS = "AUTO_SUBMIT_EXPIRED_EXAMS";
    
    // Results
    public static final String GET_EXAM_RESULTS = "GET_EXAM_RESULTS";
    public static final String GET_STUDENT_RESULTS = "GET_STUDENT_RESULTS";
    public static final String GET_ROOM_RESULTS = "GET_ROOM_RESULTS";
    
    // System
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String DISCONNECT = "DISCONNECT";
    
    private Protocol() {
        // Utility class - no instantiation
    }
}