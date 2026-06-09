package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

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

        String storedHash = user.getPasswordHash();

        // 1. Thử dùng BCrypt
        try {
            if (BCrypt.checkpw(password, storedHash)) {
                SessionManager.login(user.getId(), user.getUsername(), user.isAdmin());
                return user;
            }
        } catch (IllegalArgumentException e) {
            // Hash không đúng định dạng BCrypt, xử lý tiếp bên dưới
        }

        // 2. Nếu thất bại, thử so sánh với SHA-256 (các bản cũ lưu SHA-256 hex)
        try {
            String sha256 = sha256Hex(password);
            if (sha256.equals(storedHash)) {
                // Nâng cấp mật khẩu lên BCrypt
                String newHash = hashPassword(password);
                DatabaseUtil.updateUserPassword(user.getId(), newHash);
                user.setPasswordHash(newHash);
                SessionManager.login(user.getId(), user.getUsername(), user.isAdmin());
                return user;
            }
        } catch (Exception ex) {
            // Nếu tính SHA-256 lỗi thì bỏ qua và thử so sánh plain text bên dưới
        }

        // 3. Fallback: so sánh plain text (rất cũ, chưa mã hóa)
        if (storedHash.equals(password)) {
            // Nâng cấp mật khẩu lên BCrypt
            String newHash = hashPassword(password);
            DatabaseUtil.updateUserPassword(user.getId(), newHash);
            user.setPasswordHash(newHash);
            SessionManager.login(user.getId(), user.getUsername(), user.isAdmin());
            return user;
        }

        return null;
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute SHA-256", e);
        }
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

    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}