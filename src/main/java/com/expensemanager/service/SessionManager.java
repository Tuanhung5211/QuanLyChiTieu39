package com.expensemanager.service;

public class SessionManager {
    private static String currentUserId;
    private static String currentUsername;

    public static void login(String userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }
}