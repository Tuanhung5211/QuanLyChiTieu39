package com.expensemanager.service;

public class SessionManager {
    private static String currentUserId;
    private static String currentUsername;
    private static String language = "vi"; // mặc định Tiếng Việt

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

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String lang) {
        language = lang;
    }
}