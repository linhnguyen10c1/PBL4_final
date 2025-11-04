// Test.java - Chạy test này trước
package utils;

import java.util.List;

import model.User;

public class Test {
    public static void main(String[] args) {
        String testJson = "[{\"userId\":9,\"username\":\"admisds\",\"password\":\"\",\"fullName\":\"ddddd\",\"role\":\"STUDENT\",\"isActive\":true,\"createdAt\":\"2025-10-06 23:13:39\",\"updatedAt\":\"2025-10-06 23:13:39\"}]";
        
        System.out.println("Testing JSON parsing...");
        List<User> users = JsonUtil.fromJsontoList(testJson, User.class);
        
        System.out.println("Result: " + (users != null ? users.size() + " users" : "null"));
        if (users != null && !users.isEmpty()) {
            User firstUser = users.get(0);
            System.out.println("First user: " + firstUser.toString());
            System.out.println("User ID: " + firstUser.getUserId());
            System.out.println("Username: " + firstUser.getUsername());
            System.out.println("Is Active: " + firstUser.isActive());
        }
    }
}