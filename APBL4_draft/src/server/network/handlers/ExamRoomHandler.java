package server.network.handlers;

import server.service.AuthService;
import server.service.ExamRoomService;
import server.service.ServiceResult;
import model.ExamRoom;
import model.Subject;
import model.User;
import utils.JsonUtil;
import java.util.List;
import java.util.Map;

public class ExamRoomHandler extends BaseHandler {
    private final ExamRoomService examRoomService;
    
    public ExamRoomHandler(AuthService authService) {
        super(authService);
        this.examRoomService = new ExamRoomService();
    }
    
    /**
     * Handle create room request (Admin only)
     */
    public String handleCreateRoom(String data, String sessionToken) {
        System.out.println("🏠 [CREATE_ROOM] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [CREATE_ROOM] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            ExamRoom room = JsonUtil.fromJson(data, ExamRoom.class);
            if (room == null) {
                System.out.println("❌ [CREATE_ROOM] Invalid room data");
                return invalidRequestResponse("Invalid room data");
            }
            
            // Set creator
            User creator = getCurrentUser(sessionToken);
            if (creator != null) {
                room.setCreatedBy(creator.getUserId());
                System.out.println("✅ [CREATE_ROOM] Creator set: " + creator.getUsername());
            }
            
            ServiceResult<ExamRoom> result = examRoomService.createExamRoom(room);
            if (result.isSuccess()) {
                String roomJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [CREATE_ROOM] Room created successfully: " + room.getRoomName());
                return successResponse(result.getMessage() + "|" + roomJson);
            } else {
                System.out.println("❌ [CREATE_ROOM] Creation failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [CREATE_ROOM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error creating room: " + e.getMessage());
        }
    }
    
    /**
     * Handle update room request (Admin only)
     */
    public String handleUpdateRoom(String data, String sessionToken) {
        System.out.println("🏠 [UPDATE_ROOM] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [UPDATE_ROOM] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            ExamRoom room = JsonUtil.fromJson(data, ExamRoom.class);
            if (room == null) {
                System.out.println("❌ [UPDATE_ROOM] Invalid room data");
                return invalidRequestResponse("Invalid room data");
            }
            
            ServiceResult<ExamRoom> result = examRoomService.updateExamRoom(room);
            if (result.isSuccess()) {
                String roomJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [UPDATE_ROOM] Room updated successfully: " + room.getRoomName());
                return successResponse(result.getMessage() + "|" + roomJson);
            } else {
                System.out.println("❌ [UPDATE_ROOM] Update failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [UPDATE_ROOM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error updating room: " + e.getMessage());
        }
    }
    
    /**
     * Handle get rooms request (Admin only)
     */
    public String handleGetRooms(String sessionToken) {
        System.out.println("🏠 [GET_ROOMS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [GET_ROOMS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            ServiceResult<List<ExamRoom>> result = examRoomService.getAllExamRooms();
            if (result.isSuccess()) {
                String roomsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_ROOMS] Retrieved " + result.getData().size() + " rooms");
                return successResponse(roomsJson);
            } else {
                System.out.println("❌ [GET_ROOMS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_ROOMS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting rooms: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete room request (Admin only)
     */
    public String handleDeleteRoom(String data, String sessionToken) {
        System.out.println("🏠 [DELETE_ROOM] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [DELETE_ROOM] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> deleteData = JsonUtil.fromJsonToMap(data);
            if (deleteData == null || !deleteData.containsKey("roomId")) {
                System.out.println("❌ [DELETE_ROOM] Room ID is required");
                return invalidRequestResponse("Room ID is required");
            }
            
            int roomId = ((Double) deleteData.get("roomId")).intValue();
            System.out.println("🏠 [DELETE_ROOM] Deleting room ID: " + roomId);
            
            ServiceResult<Boolean> result = examRoomService.deleteExamRoom(roomId);
            
            if (result.isSuccess()) {
                System.out.println("✅ [DELETE_ROOM] Room deleted successfully");
                return successResponse(result.getMessage());
            } else {
                System.out.println("❌ [DELETE_ROOM] Deletion failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [DELETE_ROOM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error deleting room: " + e.getMessage());
        }
    }
    
    /**
     * Handle get available rooms request (For students)
     */
    public String handleGetAvailableRooms(String sessionToken) {
        System.out.println("🏠 [GET_AVAILABLE_ROOMS] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_AVAILABLE_ROOMS] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            ServiceResult<List<ExamRoom>> result = examRoomService.getAvailableExamRooms(user.getUserId());
            if (result.isSuccess()) {
                String roomsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_AVAILABLE_ROOMS] Retrieved " + result.getData().size() + 
                                 " available rooms for user: " + user.getUsername());
                return successResponse(roomsJson);
            } else {
                System.out.println("❌ [GET_AVAILABLE_ROOMS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_AVAILABLE_ROOMS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting available rooms: " + e.getMessage());
        }
    }
    
    /**
     * Handle search rooms request (Admin only)
     */
    public String handleSearchRooms(String data, String sessionToken) {
        System.out.println("🏠 [SEARCH_ROOMS] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [SEARCH_ROOMS] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> searchData = JsonUtil.fromJsonToMap(data);
            String keyword = (searchData != null && searchData.get("keyword") != null)
                    ? searchData.get("keyword").toString()
                    : "";
            
            System.out.println("🏠 [SEARCH_ROOMS] Searching with keyword: " + keyword);
            
            ServiceResult<List<ExamRoom>> result = examRoomService.searchExamRooms(keyword);
            if (result.isSuccess()) {
                String roomsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [SEARCH_ROOMS] Found " + result.getData().size() + " rooms");
                return successResponse(roomsJson);
            } else {
                System.out.println("❌ [SEARCH_ROOMS] Search failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [SEARCH_ROOMS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error searching rooms: " + e.getMessage());
        }
    }
    
    /**
     * Handle get subjects request
     */
    public String handleGetSubjects(String sessionToken) {
        System.out.println("📚 [GET_SUBJECTS] Processing request");
        
        User user = validateSession(sessionToken);
        if (user == null) {
            System.out.println("❌ [GET_SUBJECTS] Session expired");
            return sessionExpiredResponse();
        }
        
        try {
            ServiceResult<List<Subject>> result = examRoomService.getAllSubjects();
            if (result.isSuccess()) {
                String subjectsJson = JsonUtil.toJson(result.getData());
                System.out.println("✅ [GET_SUBJECTS] Retrieved " + result.getData().size() + " subjects");
                return successResponse(subjectsJson);
            } else {
                System.out.println("❌ [GET_SUBJECTS] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [GET_SUBJECTS] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error getting subjects: " + e.getMessage());
        }
    }
    
    /**
     * Handle add students to room request (Admin only)
     */
    public String handleAddStudentsToRoom(String data, String sessionToken) {
        System.out.println("👥 [ADD_STUDENTS_TO_ROOM] Processing request");
        
        if (!hasPermission(sessionToken, "MANAGE_ROOMS")) {
            System.out.println("❌ [ADD_STUDENTS_TO_ROOM] Access denied");
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(data);
            if (requestData == null) {
                System.out.println("❌ [ADD_STUDENTS_TO_ROOM] Invalid request data");
                return invalidRequestResponse("Invalid request data");
            }
            
            int roomId = ((Double) requestData.get("roomId")).intValue();
            @SuppressWarnings("unchecked")
            List<Double> studentIdsDouble = (List<Double>) requestData.get("studentIds");
            
            List<Integer> studentIds = studentIdsDouble.stream()
                .map(Double::intValue)
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("👥 [ADD_STUDENTS_TO_ROOM] Adding " + studentIds.size() + 
                             " students to room " + roomId);
            
            ServiceResult<Boolean> result = examRoomService.addStudentsToRoom(roomId, studentIds);
            if (result.isSuccess()) {
                System.out.println("✅ [ADD_STUDENTS_TO_ROOM] Students added successfully");
                return successResponse(result.getMessage());
            } else {
                System.out.println("❌ [ADD_STUDENTS_TO_ROOM] Failed: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ [ADD_STUDENTS_TO_ROOM] Exception: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error adding students to room: " + e.getMessage());
        }
    }
}