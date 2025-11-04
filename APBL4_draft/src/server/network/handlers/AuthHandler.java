package server.network.handlers;

import server.service.AuthService;
import server.service.AuthService.AuthResult;
import utils.Protocol;
import utils.JsonUtil;
import java.util.Map;

public class AuthHandler extends BaseHandler {
    
    public AuthHandler(AuthService authService) {
        super(authService);
    }
    
    /**
     * Handle login request
     */
    public String handleLogin(String data) {
        System.out.println("[LOGIN] Received raw data: " + data);
        try {
            Map<String, Object> loginData = JsonUtil.fromJsonToMap(data);
            System.out.println("[LOGIN] Parsed JSON");
            if (loginData == null) {
                return Protocol.LOGIN_FAILED + Protocol.DELIMITER + "Invalid login data";
            }
            String username = (String) loginData.get("username");
            String password = (String) loginData.get("password");
            System.out.println("[LOGIN] username=" + username);

            AuthResult result = authService.login(username, password);
            System.out.println("[LOGIN] AuthResult success=" + result.isSuccess());

            if (result.isSuccess()) {
                String userJson = JsonUtil.toJson(result.getUser());
                return Protocol.LOGIN_SUCCESS + Protocol.DELIMITER + result.getSessionToken() + Protocol.DELIMITER + userJson;
            } else {
                return Protocol.LOGIN_FAILED + Protocol.DELIMITER + result.getMessage();
            }
        } catch (Throwable t) {
            System.err.println("[LOGIN] FATAL: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace();
            return Protocol.ERROR + Protocol.DELIMITER + "Fatal login error";
        }
    }
    
    /**
     * Handle logout request
     */
    public String handleLogout(String sessionToken) {
        try {
            authService.logout(sessionToken);
            return successResponse("Logged out successfully");
        } catch (Exception e) {
            return errorResponse("Logout error");
        }
    }
}