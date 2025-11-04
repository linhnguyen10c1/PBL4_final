package server.network.handlers;

import server.service.AuthService;
import server.service.UserService;
import server.service.ServiceResult;
import model.User;
import utils.JsonUtil;
import utils.Protocol;

import java.util.List;
import java.util.Map;

public class UserHandler extends BaseHandler {
    private final UserService userService;
    
    public UserHandler(AuthService authService) {
        super(authService);
        this.userService = new UserService();
    }
    
    public String handleCreateUser(String data, String sessionToken) {
        if (!hasPermission(sessionToken, "MANAGE_USERS")) {
            return accessDeniedResponse();
        }
        
        try {
            User user = JsonUtil.fromJson(data, User.class);
            if (user == null) {
                return invalidRequestResponse("Invalid user data");
            }
            
            ServiceResult<User> result = userService.createUser(user);
            if (result.isSuccess()) {
                String userJson = JsonUtil.toJson(result.getData());
                return successResponse(result.getMessage() + Protocol.DELIMITER + userJson);
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            return errorResponse("Error creating user: " + e.getMessage());
        }
    }
    
    public String handleUpdateUser(String data, String sessionToken) {
        if (!hasPermission(sessionToken, "MANAGE_USERS")) {
            return accessDeniedResponse();
        }
        
        try {
            User user = JsonUtil.fromJson(data, User.class);
            if (user == null) {
                return invalidRequestResponse("Invalid user data");
            }
            
            ServiceResult<User> result = userService.updateUser(user);
            if (result.isSuccess()) {
                String userJson = JsonUtil.toJson(result.getData());
                return successResponse(result.getMessage() + Protocol.DELIMITER + userJson);
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            return errorResponse("Error updating user: " + e.getMessage());
        }
    }
    
    public String handleDeleteUser(String data, String sessionToken) {
        if (!hasPermission(sessionToken, "MANAGE_USERS")) {
            return accessDeniedResponse();
        }
        
        try {
            Map<String, Object> deleteData = JsonUtil.fromJsonToMap(data);
            if (deleteData == null || !deleteData.containsKey("userId")) {
                return invalidRequestResponse("User ID is required");
            }
            
            int userId = ((Double) deleteData.get("userId")).intValue();
            ServiceResult<Boolean> result = userService.deleteUser(userId);
            
            if (result.isSuccess()) {
                return successResponse(result.getMessage());
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            return errorResponse("Error deleting user: " + e.getMessage());
        }
    }
    
    public String handleGetUsers(String sessionToken) {
        if (!hasPermission(sessionToken, "MANAGE_USERS")) {
            return accessDeniedResponse();
        }
        
        try {
            ServiceResult<List<User>> result = userService.getAllUsers();
            if (result.isSuccess()) {
                String usersJson = JsonUtil.toJson(result.getData());
                System.out.println("📤 Sending users JSON: " + usersJson);
                return successResponse(usersJson);
            } else {
                System.err.println("❌ UserService error: " + result.getMessage());
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Exception in handleGetUsers: " + e.getMessage());
            e.printStackTrace();
            return errorResponse("Error retrieving users: " + e.getMessage());
        }
    }
    
    public String handleSearchUsers(String data, String sessionToken) {
        if (!hasPermission(sessionToken, "MANAGE_USERS")) {
            return accessDeniedResponse();
        }

        try {
            Map<String, Object> searchData = JsonUtil.fromJsonToMap(data);
            String keyword = (searchData != null && searchData.get("keyword") != null)
                    ? searchData.get("keyword").toString()
                    : "";

            ServiceResult<List<User>> result = userService.searchUsers(keyword);
            if (result.isSuccess()) {
                String usersJson = JsonUtil.toJson(result.getData());
                return successResponse(usersJson);
            } else {
                return errorResponse(result.getMessage());
            }
        } catch (Exception e) {
            return errorResponse("Error searching users: " + e.getMessage());
        }
    }
}