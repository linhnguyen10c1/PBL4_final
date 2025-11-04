package server.service;

import model.ExamRoom;
import model.Subject;
import model.User;
import server.dao.ExamRoomDAO;
import server.dao.SubjectDAO;
import server.dao.UserDAO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExamRoom Service - Business logic for exam room management
 * 
 * @author linhnguyen10c1
 * @since 2025-10-16 14:37:51 UTC
 */
public class ExamRoomService {
    
    private final ExamRoomDAO examRoomDAO;
    private final SubjectDAO subjectDAO;
    private final UserDAO userDAO;
    
    public ExamRoomService() {
        this.examRoomDAO = new ExamRoomDAO();
        this.subjectDAO = new SubjectDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Create new exam room
     */
    public ServiceResult<ExamRoom> createExamRoom(ExamRoom examRoom) {
        try {
            System.out.println("🏠 [ExamRoomService] Creating room: " + examRoom.getRoomName());
            
            // Validate input
            ValidationResult validation = validateExamRoomForCreation(examRoom);
            if (!validation.isValid()) {
                System.out.println("❌ [ExamRoomService] Validation failed: " + validation.getMessage());
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if subject exists and has enough questions
            Subject subject = subjectDAO.findById(examRoom.getSubjectId());
            if (subject == null || !subject.isActive()) {
                System.out.println("❌ [ExamRoomService] Subject not found or inactive: " + examRoom.getSubjectId());
                return ServiceResult.error("Subject not found or inactive");
            }
            
            int availableQuestions = subjectDAO.countQuestionsBySubject(examRoom.getSubjectId());
            if (availableQuestions < examRoom.getQuestionCount()) {
                System.out.println("❌ [ExamRoomService] Not enough questions. Available: " + 
                                 availableQuestions + ", Required: " + examRoom.getQuestionCount());
                return ServiceResult.error("Not enough questions in subject. Available: " + 
                                         availableQuestions + ", Required: " + examRoom.getQuestionCount());
            }
            
            // Set subject name for display
            examRoom.setSubjectName(subject.getSubjectName());
            
            // Generate password if not provided
            if (examRoom.getRoomPassword() == null || examRoom.getRoomPassword().trim().isEmpty()) {
                examRoom.setRoomPassword(generateRoomPassword());
                System.out.println("🔐 [ExamRoomService] Generated password: " + examRoom.getRoomPassword());
            }
            
            // Create exam room
            ExamRoom createdRoom = examRoomDAO.create(examRoom);
            if (createdRoom != null) {
                System.out.println("✅ [ExamRoomService] Room created successfully with ID: " + createdRoom.getRoomId());
                return ServiceResult.success("Exam room created successfully", createdRoom);
            } else {
                return ServiceResult.error("Failed to create exam room");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error creating exam room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to create exam room: " + e.getMessage());
        }
    }
    
    /**
     * Update exam room
     */
    public ServiceResult<ExamRoom> updateExamRoom(ExamRoom examRoom) {
        try {
            System.out.println("🏠 [ExamRoomService] Updating room ID: " + examRoom.getRoomId());
            
            // Validate input
            ValidationResult validation = validateExamRoomForUpdate(examRoom);
            if (!validation.isValid()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if room exists
            ExamRoom existing = examRoomDAO.findById(examRoom.getRoomId());
            if (existing == null) {
                System.out.println("❌ [ExamRoomService] Room not found: " + examRoom.getRoomId());
                return ServiceResult.error("Exam room not found");
            }
            
            // Check subject and questions
            Subject subject = subjectDAO.findById(examRoom.getSubjectId());
            if (subject == null || !subject.isActive()) {
                return ServiceResult.error("Subject not found or inactive");
            }
            
            int availableQuestions = subjectDAO.countQuestionsBySubject(examRoom.getSubjectId());
            if (availableQuestions < examRoom.getQuestionCount()) {
                return ServiceResult.error("Not enough questions in subject. Available: " + 
                                         availableQuestions + ", Required: " + examRoom.getQuestionCount());
            }
            
            // Set subject name
            examRoom.setSubjectName(subject.getSubjectName());
            
            // Preserve original creation info
            examRoom.setCreatedBy(existing.getCreatedBy());
            examRoom.setCreatedAt(existing.getCreatedAt());
            
            // Update exam room
            boolean updated = examRoomDAO.update(examRoom);
            if (updated) {
                ExamRoom updatedRoom = examRoomDAO.findById(examRoom.getRoomId());
                System.out.println("✅ [ExamRoomService] Room updated successfully");
                return ServiceResult.success("Exam room updated successfully", updatedRoom);
            } else {
                return ServiceResult.error("Failed to update exam room");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error updating exam room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to update exam room: " + e.getMessage());
        }
    }
    
    /**
     * Delete exam room (soft delete)
     */
    public ServiceResult<Boolean> deleteExamRoom(int roomId) {
        try {
            System.out.println("🏠 [ExamRoomService] Deleting room ID: " + roomId);
            
            ExamRoom room = examRoomDAO.findById(roomId);
            if (room == null) {
                System.out.println("❌ [ExamRoomService] Room not found: " + roomId);
                return ServiceResult.error("Exam room not found");
            }
            
            // Simple deletion without complex time checking
            boolean deleted = examRoomDAO.delete(roomId);
            if (deleted) {
                System.out.println("✅ [ExamRoomService] Room deleted successfully");
                return ServiceResult.success("Exam room deleted successfully", true);
            } else {
                return ServiceResult.error("Failed to delete exam room");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error deleting exam room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to delete exam room: " + e.getMessage());
        }
    }
    
    /**
     * Get all exam rooms (Admin only)
     */
    public ServiceResult<List<ExamRoom>> getAllExamRooms() {
        try {
            System.out.println("🏠 [ExamRoomService] Getting all exam rooms");
            
            List<ExamRoom> examRooms = examRoomDAO.findAll();
            System.out.println("✅ [ExamRoomService] Retrieved " + examRooms.size() + " exam rooms");
            
            return ServiceResult.success("Exam rooms retrieved successfully", examRooms);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error getting exam rooms: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve exam rooms: " + e.getMessage());
        }
    }
    
    /**
     * Get available exam rooms for student (simplified)
     */
    public ServiceResult<List<ExamRoom>> getAvailableExamRooms(int studentId) {
        try {
            System.out.println("🏠 [ExamRoomService] Getting available rooms for student: " + studentId);
            
            List<ExamRoom> allRooms = examRoomDAO.findAll();
            List<ExamRoom> availableRooms = allRooms.stream()
                .filter(room -> room.isActive())
                .filter(room -> room.getAllowedStudentIds().contains(studentId))
                .collect(Collectors.toList());
            
            System.out.println("✅ [ExamRoomService] Found " + availableRooms.size() + 
                             " available rooms for student");
            
            return ServiceResult.success("Available rooms retrieved successfully", availableRooms);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error getting available rooms: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve available rooms: " + e.getMessage());
        }
    }
    
    /**
     * Search exam rooms
     */
    public ServiceResult<List<ExamRoom>> searchExamRooms(String keyword) {
        try {
            System.out.println("🏠 [ExamRoomService] Searching rooms with keyword: " + keyword);
            
            List<ExamRoom> examRooms;
            if (keyword == null || keyword.trim().isEmpty()) {
                // If empty keyword, return all rooms
                return getAllExamRooms();
            } else {
                examRooms = examRoomDAO.search(keyword.trim());
            }
            
            System.out.println("✅ [ExamRoomService] Search found " + examRooms.size() + " rooms");
            return ServiceResult.success("Search completed", examRooms);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error searching exam rooms: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Search failed: " + e.getMessage());
        }
    }
    
    /**
     * Add students to room
     */
    public ServiceResult<Boolean> addStudentsToRoom(int roomId, List<Integer> studentIds) {
        try {
            System.out.println("👥 [ExamRoomService] Adding " + studentIds.size() + 
                             " students to room " + roomId);
            
            ExamRoom room = examRoomDAO.findById(roomId);
            if (room == null) {
                return ServiceResult.error("Exam room not found");
            }
            
            // Validate all student IDs exist and are active
            for (Integer studentId : studentIds) {
                User student = userDAO.findById(studentId);
                if (student == null || !student.isActive() || !student.isStudent()) {
                    return ServiceResult.error("Invalid student ID: " + studentId);
                }
            }
            
            boolean added = examRoomDAO.addStudentsToRoom(roomId, studentIds);
            if (added) {
                System.out.println("✅ [ExamRoomService] Students added successfully");
                return ServiceResult.success("Students added to room successfully", true);
            } else {
                return ServiceResult.error("Failed to add students to room");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error adding students to room: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to add students: " + e.getMessage());
        }
    }
    
    /**
     * Get all subjects
     */
    public ServiceResult<List<Subject>> getAllSubjects() {
        try {
            System.out.println("📚 [ExamRoomService] Getting all subjects");
            
            List<Subject> subjects = subjectDAO.findAllActive();
            System.out.println("✅ [ExamRoomService] Retrieved " + subjects.size() + " subjects");
            
            return ServiceResult.success("Subjects retrieved successfully", subjects);
            
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error getting subjects: " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.error("Failed to retrieve subjects: " + e.getMessage());
        }
    }
    
    /**
     * Get exam room by ID
     */
    public ServiceResult<ExamRoom> getExamRoomById(int roomId) {
        try {
            ExamRoom room = examRoomDAO.findById(roomId);
            if (room != null) {
                return ServiceResult.success("Exam room retrieved successfully", room);
            } else {
                return ServiceResult.error("Exam room not found");
            }
        } catch (Exception e) {
            System.err.println("❌ [ExamRoomService] Error getting exam room by ID: " + e.getMessage());
            return ServiceResult.error("Failed to retrieve exam room: " + e.getMessage());
        }
    }
    
    /**
     * Validate exam room for creation - SIMPLIFIED
     */
    private ValidationResult validateExamRoomForCreation(ExamRoom examRoom) {
        if (examRoom == null) {
            return new ValidationResult(false, "Exam room data is required");
        }
        
        if (examRoom.getRoomName() == null || examRoom.getRoomName().trim().isEmpty()) {
            return new ValidationResult(false, "Room name is required");
        }
        
        if (examRoom.getRoomName().length() > 100) {
            return new ValidationResult(false, "Room name cannot exceed 100 characters");
        }
        
        if (examRoom.getSubjectId() <= 0) {
            return new ValidationResult(false, "Valid subject is required");
        }
        
        if (examRoom.getQuestionCount() <= 0 || examRoom.getQuestionCount() > 100) {
            return new ValidationResult(false, "Question count must be between 1 and 100");
        }
        
        if (examRoom.getTotalScore() <= 0 || examRoom.getTotalScore() > 1000) {
            return new ValidationResult(false, "Total score must be between 0.1 and 1000");
        }
        
        if (examRoom.getDurationMinutes() <= 0 || examRoom.getDurationMinutes() > 300) {
            return new ValidationResult(false, "Duration must be between 1 and 300 minutes");
        }
        
        
        return new ValidationResult(true, "Valid");
    }
    
    /**
     * Validate exam room for update - SIMPLIFIED
     */
    private ValidationResult validateExamRoomForUpdate(ExamRoom examRoom) {
        if (examRoom == null) {
            return new ValidationResult(false, "Exam room data is required");
        }
        
        if (examRoom.getRoomId() <= 0) {
            return new ValidationResult(false, "Valid room ID is required");
        }
        
        // Use same validation as creation for other fields
        return validateExamRoomForCreation(examRoom);
    }
    
    /**
     * Generate random room password
     */
    private String generateRoomPassword() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    /**
     * Validation Result inner class - SAME AS UserService
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