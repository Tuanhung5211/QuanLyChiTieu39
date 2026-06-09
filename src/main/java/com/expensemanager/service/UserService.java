package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.util.EmailService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UserService {

    public static User register(String username, String password, String nickname, String email, String gender) {
        if (DatabaseUtil.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists!");
        }
        String id = java.util.UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = hashPassword(password);
        User user = new User(id, username, passwordHash, nickname, email, gender);
        DatabaseUtil.insertUser(user);
        return user;
    }

    public static User login(String username, String password) {
        User user = DatabaseUtil.getUserByUsername(username);
        if (user == null) return null;
        String passwordHash = hashPassword(password);
        if (passwordHash.equals(user.getPasswordHash())) {
            // ✅ Sửa: truyền thêm tham số isAdmin
            SessionManager.login(user.getId(), user.getUsername(), user.isAdmin());
            return user;
        }
        return null;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi mã hóa", e);
        }
    }

    // ========== PHƯƠNG THỨC CHO QUÊN MẬT KHẨU ==========
    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean resetPasswordByEmail(String email) {
        User user = DatabaseUtil.getUserByEmail(email);
        if (user == null) return false;
        String newPlainPassword = generateRandomPassword(8);
        String newHashed = hashPassword(newPlainPassword);
        boolean updated = DatabaseUtil.updatePasswordByEmail(email, newHashed);
        if (updated) {
            EmailService.sendNewPassword(email, newPlainPassword);
            return true;
        }
        return false;
    }
}