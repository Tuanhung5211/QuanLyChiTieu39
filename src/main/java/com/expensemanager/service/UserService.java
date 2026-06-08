package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UserService {

    public static User register(String username, String password, String nickname, String email, String gender) {
        if (DatabaseUtil.getUserByUsername(username) != null) { //nếu user tồn tại
            boolean isVN = "vi".equalsIgnoreCase(SessionManager.getLanguage());
            throw new IllegalArgumentException(isVN ?
                    "Tên đăng nhập đã tồn tại trên hệ thống!" : "Username already exists!");
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = hashPassword(password);

        User user = new User(id, username, passwordHash, nickname, email, gender);
        DatabaseUtil.insertUser(user);
        return user;
    }//Xử lý toàn bộ logic đăng ký, từ kiểm tra trùng username, tạo ID, mã hóa mật khẩu, đến lưu database.
    // Nếu có lỗi, ném ngoại lệ rõ ràng để LoginFrame bắt và hiển thị cho người dùng.

    public static User login(String username, String password) {
        User user = DatabaseUtil.getUserByUsername(username);
        if (user == null) return null;
        String passwordHash = hashPassword(password);
        if (passwordHash.equals(user.getPasswordHash())) {
            SessionManager.login(user.getId(), user.getUsername());
            return user;
        }
        return null;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes()); //băm mật khẩu
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi hệ thống mã hóa bảo mật SHA-256", e);
        }
    }
}