package utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Timezone Utility - Đảm bảo tất cả thời gian đều theo Việt Nam
 */
public class TimezoneUtil {
    
    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Lấy thời gian hiện tại theo Việt Nam
     */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }
    
    /**
     * Chuyển LocalDateTime thành Timestamp theo Việt Nam
     */
    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        
        ZonedDateTime vietnamTime = localDateTime.atZone(VIETNAM_ZONE);
        return Timestamp.from(vietnamTime.toInstant());
    }
    
    /**
     * Chuyển Timestamp thành LocalDateTime theo Việt Nam
     */
    public static LocalDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        
        return timestamp.toInstant().atZone(VIETNAM_ZONE).toLocalDateTime();
    }
    
    /**
     * Parse string thành LocalDateTime theo Việt Nam
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("❌ Error parsing datetime: " + dateTimeString);
            return null;
        }
    }
    
    /**
     * Format LocalDateTime thành string hiển thị
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Format Timestamp thành string hiển thị
     */
    public static String formatForDisplay(Timestamp timestamp) {
        if (timestamp == null) return "";
        return formatForDisplay(fromTimestamp(timestamp));
    }
    
    /**
     * Kiểm tra thời gian có trong khoảng không (theo Việt Nam)
     */
    public static boolean isTimeInRange(LocalDateTime checkTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (checkTime == null) return false;
        if (startTime == null && endTime == null) return true;
        if (startTime == null) return checkTime.isBefore(endTime);
        if (endTime == null) return checkTime.isAfter(startTime);
        
        return checkTime.isAfter(startTime) && checkTime.isBefore(endTime);
    }
    
    /**
     * Lấy thời gian hiện tại dưới dạng Timestamp (Việt Nam)
     */
    public static Timestamp getCurrentTimestamp() {
        return toTimestamp(nowVietnam());
    }
    
    /**
     * So sánh 2 thời gian theo Việt Nam
     */
    public static boolean isAfter(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null || time2 == null) return false;
        return time1.isAfter(time2);
    }
    
    public static boolean isBefore(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null || time2 == null) return false;
        return time1.isBefore(time2);
    }
}