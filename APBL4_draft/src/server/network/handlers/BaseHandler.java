package server.network.handlers;

import server.service.AuthService;
import utils.Protocol;
import model.User;

public abstract class BaseHandler {
    protected final AuthService authService;
    
    public BaseHandler(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Validate session and check permissions
     */
    protected User validateSession(String sessionToken) {
        return authService.validateSession(sessionToken);
    }
    
    /**
     * Check if user has specific permission
     */
    protected boolean hasPermission(String sessionToken, String permission) {
        return authService.hasPermission(sessionToken, permission);
    }
    
    /**
     * Get current user from session
     */
    protected User getCurrentUser(String sessionToken) {
        return authService.getCurrentUser(sessionToken);
    }
    
    /**
     * Create standard error responses
     */
    protected String sessionExpiredResponse() {
        return Protocol.SESSION_EXPIRED + Protocol.DELIMITER + "Session expired";
    }
    
    protected String accessDeniedResponse() {
        return Protocol.ACCESS_DENIED + Protocol.DELIMITER + "Access denied";
    }
    
    protected String invalidRequestResponse(String message) {
        return Protocol.INVALID_REQUEST + Protocol.DELIMITER + message;
    }
    
    protected String errorResponse(String message) {
        return Protocol.ERROR + Protocol.DELIMITER + message;
    }
    
    protected String successResponse(String data) {
        return Protocol.SUCCESS + Protocol.DELIMITER + data;
    }
}