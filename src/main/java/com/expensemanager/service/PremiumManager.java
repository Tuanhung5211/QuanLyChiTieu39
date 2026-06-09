package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import java.time.LocalDate;

public class PremiumManager {

    public static boolean isPremium(String userId) {
        if (userId == null) return false;
        // Lấy user hiện tại từ username (vì userId có thể không phải là username)
        String username = SessionManager.getCurrentUsername();
        if (username == null) return false;
        User user = DatabaseUtil.getUserByUsername(username);
        if (user == null) return false;
        LocalDate expiry = user.getPremiumExpiryDate();
        return expiry != null && !expiry.isBefore(LocalDate.now());
    }

    public static void activatePremium(String userId, int days) {
        LocalDate newExpiry = LocalDate.now().plusDays(days);
        DatabaseUtil.updateUserPremium(userId, newExpiry);
    }

    public static void deactivatePremium(String userId) {
        DatabaseUtil.updateUserPremium(userId, null);
    }

    public static LocalDate getPremiumExpiryDate(String userId) {
        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        return user != null ? user.getPremiumExpiryDate() : null;
    }
}