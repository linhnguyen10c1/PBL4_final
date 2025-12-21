package server.service;

import model.*;
import server.dao.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class ExamService {
    
    private final ExamSessionDAO examSessionDAO;
    private final ExamRoomDAO examRoomDAO;
    private final QuestionDAO questionDAO;
    private final UserDAO userDAO;
    
    // Question distribution ratios for balanced difficulty
    private static final double EASY_RATIO = 0.4;    // 40% easy questions
    private static final double MEDIUM_RATIO = 0.4;  // 40% medium questions  
    private static final double HARD_RATIO = 0.2;    // 20% hard questions
    
    public ExamService() {
        this.examSessionDAO = new ExamSessionDAO();
        this.examRoomDAO = new ExamRoomDAO();
        this.questionDAO = new QuestionDAO();
        this.userDAO = new UserDAO();
    }
    
	/**
	 * Get available exam rooms for student - UPDATED:  Include submitted rooms with score
	 */
    public ServiceResult<List<ExamRoom>> getAvailableExamRooms(int studentId) {
        try {
            System.out.println("🎓 [ExamService] Getting available exam rooms for student: " + studentId);
            
            // Get all active exam rooms where student is allowed
            List<ExamRoom> allRooms = examRoomDAO.findAll();
            System.out.println("🔍 [ExamService] Found " + allRooms. size() + " total rooms");
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            System.out.println("🔍 [ExamService] Current time: " + now);
            
            List<ExamRoom> availableRooms = new ArrayList<>();
            
            for (ExamRoom room : allRooms) {
                System.out.println("🔍 [ExamService] Checking room " + room.getRoomId() + ": " + room. getRoomName());
                System.out.println("  - Active: " + room. isActive());
                System.out.println("  - Allowed students: " + room.getAllowedStudentIds());
                System.out.println("  - Student " + studentId + " allowed: " + room. getAllowedStudentIds().contains(studentId));
                
                boolean isActive = room.isActive();
                boolean isAllowed = room.getAllowedStudentIds().contains(studentId);
                boolean notFinished = !isExamFinished(room, now);
                
                System.out.println("  - Filters: active=" + isActive + ", allowed=" + isAllowed + 
                                 ", notFinished=" + notFinished);
                
                if (isActive && isAllowed && notFinished) {
                    attachStudentSubmissionInfo(room, studentId);
                    
                    availableRooms.add(room);
                    System.out.println("  ✅ Room added to available list (submitted=" + room.hasStudentSubmitted() + ")");
                } else {
                    System.out.println("  ❌ Room filtered out");
                }
            }
            
            System.out.println("✅ [ExamService] Found " + availableRooms.size() + " available exam rooms");
            return ServiceResult.success("Available exam rooms retrieved successfully", availableRooms);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error getting available exam rooms: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve available exam rooms:  " + e.getMessage());
        }
    }
    
    /**
     * Attach student's submission info to ExamRoom
     */
    private void attachStudentSubmissionInfo(ExamRoom room, int studentId) {
    try {
        ExamSession session = examSessionDAO.findByRoomAndStudent(room.getRoomId(), studentId);
        
        if (session != null && session.isSubmitted()) {
            // Student đã nộp bài
            room.setStudentSubmissionStatus(session.getStatus());
            
            double percentageScore = session.getTotalScore(); // Giả sử là 72.9
            double roomMaxScore = room.getTotalScore();      // Giả sử là 50.0
            double absoluteScore = (percentageScore / 100.0) * roomMaxScore;
            
            absoluteScore = Math.round(absoluteScore * 100.0) / 100.0;
            
            room.setStudentScore(absoluteScore);
            room.setMaxScoreForStudent(roomMaxScore);
            
            System.out.println("  📝 Score converted: " + percentageScore + "% -> " + absoluteScore + "/" + roomMaxScore);
        } else {
            // Student chưa nộp bài
            room.setStudentSubmissionStatus("NOT_SUBMITTED");
            room.setStudentScore(null);
            room.setMaxScoreForStudent(null);
        }
    } catch (Exception e) {
        System.err.println("  ⚠️ Error checking submission: " + e.getMessage());
        room.setStudentSubmissionStatus("NOT_SUBMITTED");
    }
}
    
    /**
     * Join exam room with password
     */
    public ServiceResult<ExamSession> joinExamRoom(int studentId, int roomId, String password) {
        try {
            System.out.println("🎓 [ExamService] Student " + studentId + " joining exam room " + roomId);
            
            // Validate exam room
            ExamRoom room = examRoomDAO.findById(roomId);
            if (room == null || !room.isActive()) {
                return ServiceResult.error("Exam room not found or inactive");
            }
            
            // Check password
            if (!password.equals(room.getRoomPassword())) {
                System.out.println("❌ [ExamService] Invalid password for room " + roomId);
                return ServiceResult.error("Invalid room password");
            }
            System.out.println("DEBUG"+ room.getAllowedStudentIds().contains(studentId));
            
            // Check if student is allowed
            if (!room.getAllowedStudentIds().contains(studentId)) {
            	System.out.println(">>> [DEBUG] allowedStudentIds for room " + roomId + ": " + room.getAllowedStudentIds());
            	System.out.println(">>> [DEBUG] studentId: " + studentId + " type: " + ((Object)studentId).getClass());
            	for (Object s : room.getAllowedStudentIds()) {
            	    System.out.println("    - " + s + " (" + (s != null ? s.getClass() : "null") + ")");
            	}
                return ServiceResult.error("You are not registered for this exam");
            }
            
            // Check exam timing
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (!isExamAccessible(room, now)) {
                return ServiceResult.error("Exam is not currently accessible");
            }
            
            // Check if already completed
            if (hasCompletedExam(studentId, roomId)) {
                return ServiceResult.error("You have already completed this exam");
            }
            
            // Get or create exam session
            ExamSession session = examSessionDAO.findByRoomAndStudent(roomId, studentId);
            if (session == null) {
                session = examSessionDAO.createSession(roomId, studentId);
                System.out.println("✅ [ExamService] Created new exam session: " + session.getSessionId());
            } else {
                System.out.println("✅ [ExamService] Found existing exam session: " + session.getSessionId());
            }
            
            // Set exam room info
            session.setExamRoom(room);
            
            return ServiceResult.success("Successfully joined exam room", session);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error joining exam room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to join exam room: " + e.getMessage());
        }
    }
    
    /**
     * Start exam (generate questions and begin timer)
     */
    public ServiceResult<List<ExamAnswer>> startExam(String sessionToken) {
        try {
            System.out.println("🎓 [ExamService] Starting exam for session token: " + sessionToken);
            
            ExamSession session = examSessionDAO.findByToken(sessionToken);
            if (session == null) {
                return ServiceResult.error("Invalid session token");
            }
            
            if (session.isSubmitted()) {
                return ServiceResult.error("Exam already submitted");
            }
            
            // Check if exam is still accessible
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (!isExamAccessible(session.getExamRoom(), now)) {
                return ServiceResult.error("Exam time has expired");
            }
            
            // If already in progress, return existing questions
            if (session.isInProgress()) {
                List<ExamAnswer> answers = examSessionDAO.getSessionAnswers(session.getSessionId());
                System.out.println("✅ [ExamService] Resumed exam with " + answers.size() + " questions");
                return ServiceResult.success("Exam resumed successfully", answers);
            }
            
            // Generate balanced question set
            List<Question> examQuestions = generateBalancedQuestionSet(
                session.getExamRoom().getSubjectId(), 
                session.getExamRoom().getQuestionCount()
            );
            
            if (examQuestions.size() < session.getExamRoom().getQuestionCount()) {
                return ServiceResult.error("Not enough questions available for this exam");
            }
            
            // Start the session
            boolean started = examSessionDAO.startSession(session.getSessionId(), examQuestions);
            if (!started) {
                return ServiceResult.error("Failed to start exam session");
            }
            
            // Create exam answers
            List<ExamAnswer> examAnswers = new ArrayList<>();
            for (int i = 0; i < examQuestions.size(); i++) {
                ExamAnswer answer = new ExamAnswer(session.getSessionId(), 
                    examQuestions.get(i).getQuestionId(), i + 1);
                answer.setQuestion(examQuestions.get(i));
                examAnswers.add(answer);
            }
            
            System.out.println("✅ [ExamService] Started exam with " + examAnswers.size() + " questions");
            return ServiceResult.success("Exam started successfully", examAnswers);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error starting exam: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to start exam: " + e.getMessage());
        }
    }
    
    /**
     * Save student answer during exam
     */
    public ServiceResult<Boolean> saveAnswer(String sessionToken, int questionId, String answer) {
        try {
            System.out.println("🎓 [ExamService] Saving answer for session: " + sessionToken + 
                             ", question: " + questionId + ", answer: " + answer);
            
            ExamSession session = examSessionDAO.findByToken(sessionToken);
            if (session == null) {
                return ServiceResult.error("Invalid session token");
            }
            
            if (!session.isInProgress()) {
                return ServiceResult.error("Exam is not in progress");
            }
            
            // Check if exam time expired
            if (session.isTimeExpired()) {
                // Auto-submit if time expired
                examSessionDAO.submitSession(session.getSessionId(), true);
                return ServiceResult.error("Exam time has expired. Your exam has been auto-submitted.");
            }
            
            // Validate answer format
            if (answer != null && !answer.matches("[ABCD]")) {
                return ServiceResult.error("Invalid answer format. Must be A, B, C, or D");
            }
            
            boolean saved = examSessionDAO.saveAnswer(session.getSessionId(), questionId, answer);
            if (saved) {
                System.out.println("✅ [ExamService] Answer saved successfully");
                return ServiceResult.success("Answer saved successfully", true);
            } else {
                return ServiceResult.error("Failed to save answer");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error saving answer: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to save answer: " + e.getMessage());
        }
    }
    
    /**
     * Submit exam
     */
    public ServiceResult<ExamResult> submitExam(String sessionToken, boolean isAutoSubmit) {
        try {
            System.out.println("🎓 [ExamService] Submitting exam for session: " + sessionToken + 
                             ", auto: " + isAutoSubmit);
            
            ExamSession session = examSessionDAO.findByToken(sessionToken);
            if (session == null) {
                return ServiceResult.error("Invalid session token");
            }
            
            if (session.isSubmitted()) {
                return ServiceResult.error("Exam already submitted");
            }
            
            if (!session.isInProgress()) {
                return ServiceResult.error("Exam not started");
            }
            
            // Submit the session
            boolean submitted = examSessionDAO.submitSession(session.getSessionId(), isAutoSubmit);
            if (!submitted) {
                return ServiceResult.error("Failed to submit exam");
            }
            
            // Get exam result
            ExamResult result = generateExamResult(session);
            
            System.out.println("✅ [ExamService] Exam submitted successfully. Score: " + result.getTotalScore());
            return ServiceResult.success("Exam submitted successfully", result);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error submitting exam: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to submit exam: " + e.getMessage());
        }
    }
    
    /**
     * Get exam session by token
     */
    public ServiceResult<ExamSession> getExamSession(String sessionToken) {
        try {
            ExamSession session = examSessionDAO.findByToken(sessionToken);
            if (session == null) {
                return ServiceResult.error("Session not found");
            }
            
            return ServiceResult.success("Session retrieved successfully", session);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error getting exam session: " + e.getMessage());
            return ServiceResult.error("Failed to retrieve exam session: " + e.getMessage());
        }
    }
    
    /**
     * Get exam results for student
     */
    public ServiceResult<List<ExamResult>> getStudentExamResults(int studentId) {
        try {
            System.out.println("🎓 [ExamService] Getting exam results for student: " + studentId);
            
            // This would be implemented with a more complex query to get all results
            // For now, return empty list as placeholder
            List<ExamResult> results = new ArrayList<>();
            
            return ServiceResult.success("Exam results retrieved successfully", results);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error getting exam results: " + e.getMessage());
            return ServiceResult.error("Failed to retrieve exam results: " + e.getMessage());
        }
    }
    
    /**
     * Auto-submit expired exams (called by scheduler)
     */
    public ServiceResult<Integer> autoSubmitExpiredExams() {
        try {
            System.out.println("🎓 [ExamService] Auto-submitting expired exams");
            
            int count = examSessionDAO.autoSubmitExpiredSessions();
            
            System.out.println("✅ [ExamService] Auto-submitted " + count + " expired exams");
            return ServiceResult.success("Auto-submitted " + count + " expired exams", count);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error auto-submitting expired exams: " + e.getMessage());
            return ServiceResult.error("Failed to auto-submit expired exams: " + e.getMessage());
        }
    }
    
    /**
     * Generate balanced question set with proper difficulty distribution
     */
    private List<Question> generateBalancedQuestionSet(int subjectId, int totalQuestions) throws SQLException {
        // Calculate question counts for each difficulty
        int easyCount = (int) Math.round(totalQuestions * EASY_RATIO);
        int mediumCount = (int) Math.round(totalQuestions * MEDIUM_RATIO);
        int hardCount = totalQuestions - easyCount - mediumCount; // Remaining questions
        
        System.out.println("🎯 [ExamService] Generating balanced question set: " +
                         "Easy=" + easyCount + ", Medium=" + mediumCount + ", Hard=" + hardCount);
        
        List<Question> selectedQuestions = new ArrayList<>();
        
        // Get questions by difficulty
        List<Question> easyQuestions = getQuestionsBySubjectAndDifficulty(subjectId, "EASY");
        List<Question> mediumQuestions = getQuestionsBySubjectAndDifficulty(subjectId, "MEDIUM");
        List<Question> hardQuestions = getQuestionsBySubjectAndDifficulty(subjectId, "HARD");
        
        // Randomly select questions from each category
        selectedQuestions.addAll(getRandomQuestionsFromList(easyQuestions, easyCount));
        selectedQuestions.addAll(getRandomQuestionsFromList(mediumQuestions, mediumCount));
        selectedQuestions.addAll(getRandomQuestionsFromList(hardQuestions, hardCount));
        
        // If we don't have enough questions in specific difficulties, fill from available questions
        if (selectedQuestions.size() < totalQuestions) {
            List<Question> allQuestions = questionDAO.findBySubject(subjectId);
            List<Question> remainingQuestions = allQuestions.stream()
                .filter(q -> selectedQuestions.stream().noneMatch(s -> s.getQuestionId() == q.getQuestionId()))
                .collect(Collectors.toList());
            
            int needed = totalQuestions - selectedQuestions.size();
            selectedQuestions.addAll(getRandomQuestionsFromList(remainingQuestions, needed));
        }
        
        // Shuffle the final list to randomize question order
        Collections.shuffle(selectedQuestions);
        
        System.out.println("✅ [ExamService] Generated " + selectedQuestions.size() + " questions for exam");
        return selectedQuestions.subList(0, Math.min(selectedQuestions.size(), totalQuestions));
    }
    
    /**
     * Get questions by subject and difficulty
     */
    private List<Question> getQuestionsBySubjectAndDifficulty(int subjectId, String difficulty) throws SQLException {
        return questionDAO.findBySubject(subjectId).stream()
            .filter(q -> difficulty.equals(q.getDifficulty()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get random questions from a list
     */
    private List<Question> getRandomQuestionsFromList(List<Question> questions, int count) {
        if (questions.size() <= count) {
            return new ArrayList<>(questions);
        }
        
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }
    
    /**
     * Check if exam is accessible (within time window)
     */
    private boolean isExamAccessible(ExamRoom room, Timestamp now) {
        // If no start/end time set, exam is always accessible
        if (room.getStartTime() == null && room.getEndTime() == null) {
            return true;
        }
        
        // Check time window using Timestamp directly
        if (room.getStartTime() != null && room.getEndTime() != null) {
            return now.after(room.getStartTime()) && now.before(room.getEndTime());
        }
        
        // If only start time is set
        if (room.getStartTime() != null) {
            return now.after(room.getStartTime());
        }
        
        // If only end time is set
        if (room.getEndTime() != null) {
            return now.before(room.getEndTime());
        }
        
        return true; // Default to accessible// Default to accessible if parsing fails
    }
    
    /**
     * Check if exam is finished
     */
    private boolean isExamFinished(ExamRoom room, Timestamp now) {
    	 if (room.getEndTime() == null) {
    	        return false;
    	    }
    	    
    	    return now.after(room.getEndTime());
    }
    
    /**
     * Check if student has completed exam
     */
    private boolean hasCompletedExam(int studentId, int roomId) {
        try {
            ExamSession session = examSessionDAO.findByRoomAndStudent(roomId, studentId);
            return session != null && session.isSubmitted();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isExamNotStarted(ExamRoom room, Timestamp now) {
        if (room.getStartTime() == null) {
            return true; // No start time set
        }
        
        return now.before(room.getStartTime());
    }

	/**
	 * Generate exam result from session - UPDATED: Convert percentage to absolute score
	 */
	private ExamResult generateExamResult(ExamSession session) throws SQLException {
	    if (session == null) {
	        throw new SQLException("Session is null");
	    }
	    
	    // Reload session để lấy data mới nhất sau khi submit (score, status, answers)
	    ExamSession updatedSession = examSessionDAO.findByToken(session.getSessionToken());
	    if (updatedSession == null) {
	        System.err.println("⚠️ [ExamService] Could not reload session, using original data");
	        updatedSession = session;
	    }
	    
	    ExamResult result = new ExamResult();
	    result.setSessionId(updatedSession.getSessionId());
	    result.setRoomId(updatedSession.getRoomId());
	    result.setStudentId(updatedSession.getStudentId());
	    result.setStudentName(updatedSession.getStudentName());
	    result.setStatus(updatedSession.getStatus());
	    
	    // 1. Xử lý thời gian nộp bài
	    if (updatedSession.getSubmitTime() != null) {
	        result.setSubmittedAt(updatedSession.getSubmitTime().toString());
	    } else {
	        result.setSubmittedAt(new java.sql.Timestamp(System.currentTimeMillis()).toString());
	    }
	    
	    // 2. Quy đổi điểm số
	    double percentageScore = updatedSession.getTotalScore(); // Điểm hệ 100 từ DB
	    double roomMaxScore = 100.0;
	    int duration = 0;
	    
	    if (updatedSession.getExamRoom() != null) {
	        ExamRoom room = updatedSession.getExamRoom();
	        roomMaxScore = room.getTotalScore();
	        duration = room.getDurationMinutes();
	        
	        result.setRoomName(room.getRoomName());
	        result.setSubjectName(room.getSubjectName());
	    } else {
	        result.setRoomName("Unknown Exam");
	        result.setSubjectName("Unknown Subject");
	    }
	
	    double absoluteScore = (percentageScore / 100.0) * roomMaxScore;
	    absoluteScore = Math.round(absoluteScore * 100.0) / 100.0;
	    
	    result.setTotalScore(absoluteScore); 
	    result.setMaxScore(roomMaxScore);    
	    result.setPercentage(percentageScore);
	    result.setGrade(calculateGrade(percentageScore)); 
	    result.setTimeLimitMinutes(duration);
	    
	    // 3. Thống kê số câu trả lời
	    List<ExamAnswer> answers = updatedSession.getAnswers();
	    if (answers == null || answers.isEmpty()) {
	        try {
	            answers = examSessionDAO.getSessionAnswers(updatedSession.getSessionId());
	        } catch (Exception e) {
	            System.err.println("⚠️ [ExamService] Could not reload answers: " + e.getMessage());
	        }
	    }
	    
	    if (answers != null && !answers.isEmpty()) {
	        result.setTotalQuestions(answers.size());
	        result.setCorrectAnswers((int) answers.stream().filter(ExamAnswer::isCorrect).count());
	    } else {
	        result.setTotalQuestions(0);
	        result.setCorrectAnswers(0);
	    }
	    
	    // 4. Tính thời gian làm bài thực tế
	    if (updatedSession.getStartTime() != null && updatedSession.getSubmitTime() != null) {
	        long timeDiff = updatedSession.getSubmitTime().getTime() - updatedSession.getStartTime().getTime();
	        result.setTimeSpentMinutes((int) (timeDiff / (1000 * 60)));
	    } else {
	        result.setTimeSpentMinutes(0);
	    }
	    
	    System.out.println("✅ [ExamService] Result Generated - Absolute: " + absoluteScore + "/" + roomMaxScore + 
	                      " (" + String.format("%.1f", percentageScore) + "%), Grade: " + result.getGrade());
	    
	    return result;
	}
		
	/**
	 * Calculate grade based on percentage
	 */
	private String calculateGrade(double percentage) {
	    if (percentage >= 90) {
	        return "A+";
	    } else if (percentage >= 85) {
	        return "A";
	    } else if (percentage >= 80) {
	        return "B+";
	    } else if (percentage >= 70) {
	        return "B";
	    } else if (percentage >= 60) {
	        return "C";
	    } else if (percentage >= 50) {
	        return "D";
	    } else {
	        return "F";
	    }
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
    /**
     * Calculate session score - Simple method using existing DAO
     */
    private double calculateSessionScore(int sessionId) throws SQLException {
        try {
            List<ExamAnswer> answers = examSessionDAO.getSessionAnswers(sessionId);
            
            if (answers.isEmpty()) {
                return 0.0;
            }
            
            int totalAnswered = 0;
            int correctAnswers = 0;
            
            for (ExamAnswer answer : answers) {
                if (answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty()) {
                    totalAnswered++;
                    
                    Question question = answer.getQuestion();
                    if (question != null && answer.getStudentAnswer().equals(question.getCorrectAnswer())) {
                        correctAnswers++;
                    }
                }
            }
            
            return totalAnswered > 0 ? (double) correctAnswers / totalAnswered * 100.0 : 0.0;
            
        } catch (Exception e) {
            System.err.println("Error calculating session score: " + e.getMessage());
            throw new SQLException("Failed to calculate session score", e);
        }
    }
    public ServiceResult<List<ExamSession>> getAllExamSessions() {
        try {
            System.out.println("🎓 [ExamService] Getting all exam sessions");
            
            List<ExamSession> sessions = examSessionDAO.findAllSessions();
            System.out.println("✅ [ExamService] Retrieved " + sessions.size() + " exam sessions");
            
            return ServiceResult.success("Exam sessions retrieved successfully", sessions);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error getting all exam sessions: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve exam sessions: " + e.getMessage());
        }
    }

    /**
     * Get exam sessions for specific room (Admin only)
     */
    public ServiceResult<List<ExamSession>> getExamSessionsForRoom(int roomId) {
        try {
            System.out.println("🎓 [ExamService] Getting exam sessions for room: " + roomId);
            
            List<ExamSession> sessions = examSessionDAO.getActiveSessionsForRoom(roomId);
            System.out.println("✅ [ExamService] Retrieved " + sessions.size() + " sessions for room " + roomId);
            
            return ServiceResult.success("Room exam sessions retrieved successfully", sessions);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamService] Error getting exam sessions for room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve room exam sessions: " + e.getMessage());
        }
    }
}