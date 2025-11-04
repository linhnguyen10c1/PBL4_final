package server.service;

/**
 * Service Result wrapper for consistent service layer responses
 * 
 * @author linhnguyen10c1
 * @since 2025-09-14 13:35:08 UTC
 */
public class ServiceResult<T> {
    private final boolean success;
    private final String message;
    private final T data;
    
    public ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    public static <T> ServiceResult<T> success(String message, T data) {
        return new ServiceResult<>(true, message, data);
    }
    
    public static <T> ServiceResult<T> success(String message) {
        return new ServiceResult<>(true, message, null);
    }
    
    public static <T> ServiceResult<T> error(String message) {
        return new ServiceResult<>(false, message, null);
    }
    @Override
    public String toString() {
        return "ServiceResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}