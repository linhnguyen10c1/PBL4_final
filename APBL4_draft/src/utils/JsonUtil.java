// utils/JsonUtil.java - Version đơn giản và ổn định
package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    
    private static final Gson gson = new GsonBuilder()
            .setLenient()  // More flexible parsing
            .create();
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            if (object == null) {
                return "{}";
            }
            return gson.toJson(object);
        } catch (Exception e) {
            System.err.println("Error converting to JSON: " + e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return gson.fromJson(json, Map.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing JSON to map: " + e.getMessage());
            return null;
        }
    }

    // ✅ SIMPLIFIED VERSION - Không có complex debugging
    public static <T> List<T> fromJsontoList(String json, Class<T> clazz) {
        System.out.println("🔍 Parsing JSON to List<" + clazz.getSimpleName() + ">");
        System.out.println("📋 JSON input: " + (json != null ? json.substring(0, Math.min(200, json.length())) : "null"));
        
        if (json == null || json.trim().isEmpty()) {
            System.err.println("❌ JSON is null or empty");
            return new ArrayList<>();
        }
        
        try {
            // ✅ Use TypeToken for List<T>
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            List<T> result = gson.fromJson(json, listType);
            
            if (result != null) {
                System.out.println("✅ Successfully parsed " + result.size() + " items");
                return result;
            } else {
                System.err.println("❌ Gson returned null");
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            System.err.println("❌ JSON parsing failed: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ FALLBACK: Try manual approach
            return parseManually(json, clazz);
        }
    }
    
    // ✅ FALLBACK: Manual parsing method
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseManually(String json, Class<T> clazz) {
        System.out.println("🔄 Trying manual parsing fallback...");
        
        try {
            // Parse to List of Maps first
            Type listOfMapsType = TypeToken.getParameterized(List.class, Map.class).getType();
            List<Map<String, Object>> listOfMaps = gson.fromJson(json, listOfMapsType);
            
            if (listOfMaps == null) {
                System.err.println("❌ Could not parse to List<Map>");
                return new ArrayList<>();
            }
            
            System.out.println("✅ Parsed to " + listOfMaps.size() + " maps");
            
            // Convert each Map to target object
            List<T> result = new ArrayList<>();
            for (int i = 0; i < listOfMaps.size(); i++) {
                Map<String, Object> map = listOfMaps.get(i);
                System.out.println("🔍 Converting map " + i + ": " + map.keySet());
                
                try {
                    // Convert map back to JSON, then to object
                    String objectJson = gson.toJson(map);
                    T object = gson.fromJson(objectJson, clazz);
                    
                    if (object != null) {
                        result.add(object);
                        System.out.println("✅ Converted object " + i + ": " + object.toString());
                    } else {
                        System.err.println("❌ Failed to convert map " + i + " to " + clazz.getSimpleName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error converting map " + i + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ Manual parsing completed: " + result.size() + " objects");
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Manual parsing also failed: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}