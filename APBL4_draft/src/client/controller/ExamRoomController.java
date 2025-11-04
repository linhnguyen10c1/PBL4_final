package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.ExamRoom;
import model.Subject;
import model.User;
import utils.JsonUtil;
import utils.Protocol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExamRoom Controller - Client-side controller for exam room operations
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:36:16 UTC
 */
public class ExamRoomController extends BaseController {
    
    private ExamRoomListener listener;
    
    public ExamRoomController(NetworkManager networkManager) {
        super(networkManager);
    }
    
    /**
     * Create new exam room
     */
    public boolean createExamRoom(ExamRoom examRoom) {
        try {
            logAction("createExamRoom", "Creating room: " + examRoom.getRoomName());
            
            if (!validateSession()) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.CREATE_ROOM, examRoom);
            
            if (response.isSuccess()) {
                logAction("createExamRoom", "Room created successfully");
                showSuccessMessage("Success", "Exam room created successfully!");
                
                if (listener != null) {
                    // Parse created room from response data
                    String[] parts = response.getData().split("\\|", 2);
                    if (parts.length >= 2) {
                        ExamRoom createdRoom = JsonUtil.fromJson(parts[1], ExamRoom.class);
                        listener.onExamRoomCreated(createdRoom);
                    }
                }
                return true;
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
     * Update existing exam room
     */
    public boolean updateExamRoom(ExamRoom examRoom) {
        try {
            logAction("updateExamRoom", "Updating room ID: " + examRoom.getRoomId());
            
            if (!validateSession()) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.UPDATE_ROOM, examRoom);
            
            if (response.isSuccess()) {
                logAction("updateExamRoom", "Room updated successfully");
                showSuccessMessage("Success", "Exam room updated successfully!");
                
                if (listener != null) {
                    // Parse updated room from response data
                    String[] parts = response.getData().split("\\|", 2);
                    if (parts.length >= 2) {
                        ExamRoom updatedRoom = JsonUtil.fromJson(parts[1], ExamRoom.class);
                        listener.onExamRoomUpdated(updatedRoom);
                    }
                }
                return true;
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
     * Delete exam room
     */
    public boolean deleteExamRoom(int roomId, String roomName) {
        try {
            logAction("deleteExamRoom", "Deleting room ID: " + roomId);
            
            if (!validateSession()) {
                return false;
            }
            
            // Confirm deletion
            boolean confirmed = showConfirmDialog("Delete Exam Room", 
                "Are you sure you want to delete room: " + roomName + "?");
            if (!confirmed) {
                return false;
            }
            
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("roomId", roomId);
            
            ResponseData response = sendJsonRequest(Protocol.DELETE_ROOM, deleteData);
            
            if (response.isSuccess()) {
                logAction("deleteExamRoom", "Room deleted successfully");
                showSuccessMessage("Success", "Exam room deleted successfully!");
                
                if (listener != null) {
                    listener.onExamRoomDeleted(roomId);
                }
                return true;
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
     * Get all exam rooms
     */
    public List<ExamRoom> getAllExamRooms() {
        try {
            logAction("getAllExamRooms", "Fetching all exam rooms");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_ROOMS);
            
            if (response.isSuccess()) {
                List<ExamRoom> examRooms = JsonUtil.fromJsontoList(response.getData(), ExamRoom.class);
                logAction("getAllExamRooms", "Retrieved " + examRooms.size() + " exam rooms");
                
                if (listener != null) {
                    listener.onExamRoomsLoaded(examRooms);
                }
                return examRooms;
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
     * Search exam rooms
     */
    public List<ExamRoom> searchExamRooms(String keyword) {
        try {
            logAction("searchExamRooms", "Searching with keyword: " + keyword);
            
            if (!validateSession()) {
                return null;
            }
            
            Map<String, Object> searchData = new HashMap<>();
            searchData.put("keyword", keyword);
            
            ResponseData response = sendJsonRequest(Protocol.SEARCH_ROOMS, searchData);
            
            if (response.isSuccess()) {
                List<ExamRoom> examRooms = JsonUtil.fromJsontoList(response.getData(), ExamRoom.class);
                logAction("searchExamRooms", "Found " + examRooms.size() + " exam rooms");
                
                if (listener != null) {
                    listener.onExamRoomsLoaded(examRooms);
                }
                return examRooms;
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
     * Get all subjects
     */
    public List<Subject> getAllSubjects() {
        try {
            logAction("getAllSubjects", "Fetching all subjects");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_SUBJECTS);
            
            if (response.isSuccess()) {
                List<Subject> subjects = JsonUtil.fromJsontoList(response.getData(), Subject.class);
                logAction("getAllSubjects", "Retrieved " + subjects.size() + " subjects");
                
                if (listener != null) {
                    listener.onSubjectsLoaded(subjects);
                }
                return subjects;
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
     * Get all students (for room assignment)
     */
    public List<User> getAllStudents() {
        try {
            logAction("getAllStudents", "Fetching all students");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_USERS);
            
            if (response.isSuccess()) {
                List<User> allUsers = JsonUtil.fromJsontoList(response.getData(), User.class);
                // Filter only students
                List<User> students = allUsers.stream()
                    .filter(User::isStudent)
                    .collect(java.util.stream.Collectors.toList());
                
                logAction("getAllStudents", "Retrieved " + students.size() + " students");
                
                if (listener != null) {
                    listener.onStudentsLoaded(students);
                }
                return students;
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
     * Add students to exam room
     */
    public boolean addStudentsToRoom(int roomId, List<Integer> studentIds) {
        try {
            logAction("addStudentsToRoom", "Adding " + studentIds.size() + " students to room " + roomId);
            
            if (!validateSession()) {
                return false;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("roomId", roomId);
            requestData.put("studentIds", studentIds);
            
            ResponseData response = sendJsonRequest(Protocol.ADD_STUDENTS_TO_ROOM, requestData);
            
            if (response.isSuccess()) {
                logAction("addStudentsToRoom", "Students added successfully");
                showSuccessMessage("Success", "Students added to room successfully!");
                
                if (listener != null) {
                    listener.onStudentsUpdated();
                }
                return true;
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
     * Set exam room listener
     */
    public void setExamRoomListener(ExamRoomListener listener) {
        this.listener = listener;
    }
    
    /**
     * Exam Room Listener interface
     */
    public interface ExamRoomListener {
        void onExamRoomsLoaded(List<ExamRoom> examRooms);
        void onExamRoomCreated(ExamRoom examRoom);
        void onExamRoomUpdated(ExamRoom examRoom);
        void onExamRoomDeleted(int roomId);
        void onSubjectsLoaded(List<Subject> subjects);
        void onStudentsLoaded(List<User> students);
        void onStudentsUpdated();
        void onError(String message);
    }
}