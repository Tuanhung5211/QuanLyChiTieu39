package com.expensemanager.service;

public class SessionManager {
    private static String currentUserId;
    private static String currentUsername;
    private static String language = "vi";
    private static boolean isAdmin = false;

    public static void login(String userId, String username, boolean isAdminFlag) {
        currentUserId = userId;
        currentUsername = username;
        isAdmin = isAdminFlag;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
        isAdmin = false;
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

    public static boolean isAdmin() {
        return isAdmin;
    }
}