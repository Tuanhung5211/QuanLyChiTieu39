package com.expensemanager.util;

import java.util.regex.Pattern;

public class InputValidator {

    // Regex kiểm tra cấu trúc định dạng Email quốc tế
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    // Giới hạn số tiền tối đa tránh lỗi tràn số dữ liệu (99 Tỷ VND)
    private static final double MAX_AMOUNT = 99999999999.0;

    /**
     * 1. Bắt lỗi Số tiền (Giao dịch & Ngân sách)
     */
    public static double validateAmount(String amountStr, boolean isVietnamese) throws IllegalArgumentException {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền không được để trống!" : "Amount field cannot be empty!");
        }

        String cleanStr = amountStr.trim().replace(",", "").replace(".", "");
        double amount;
        try {
            amount = Double.parseDouble(cleanStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền nhập vào phải là ký số hợp lệ!" : "Amount must be a valid number!");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền phải lớn hơn 0 đ!" : "Amount must be greater than 0!");
        }

        if (amount > MAX_AMOUNT) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền vượt quá hạn mức tối đa (99 Tỷ đ)!" : "Amount exceeds maximum limit (99 Billion)!");
        }

        return amount;
    }

    /**
     * 2. Bắt lỗi Tên danh mục chi tiêu mới
     */
    public static String validateCategoryName(String name, boolean isVietnamese) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên danh mục không được để trống!" : "Category name cannot be empty!");
        }

        String cleanName = name.trim();
        if (cleanName.length() < 1 || cleanName.length() > 30) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên danh mục phải nằm trong khoảng từ 1 đến 30 ký tự!" : "Category name must be between 1 and 30 characters!");
        }

        if (cleanName.contains("'") || cleanName.contains("\"") || cleanName.contains(";")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên danh mục không chứa ký tự nguy hiểm (', \", ;)" : "Category name cannot contain special characters (', \", ;)");
        }

        return cleanName;
    }

    /**
     * 3. Bắt lỗi Ghi chú giao dịch
     */
    public static String validateTransactionNote(String note, boolean isVietnamese) throws IllegalArgumentException {
        if (note == null) return "";
        String cleanNote = note.trim();
        if (cleanNote.length() > 200) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Nội dung ghi chú không được vượt quá 200 ký tự!" : "Transaction note cannot exceed 200 characters!");
        }
        return cleanNote;
    }

    /**
     * 4. MỚI: Bắt lỗi Luồng Đăng Nhập (Login Validation)
     */
    public static void validateLogin(String username, String password, boolean isVietnamese) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên đăng nhập không được để trống!" : "Username cannot be empty!");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được để trống!" : "Password cannot be empty!");
        }
    }

    /**
     * 5. MỚI: Bắt lỗi Luồng Đăng Ký (Registration Validation)
     */
    public static void validateRegister(String username, String password, String confirmPassword, String email, String nickname, boolean isVietnamese) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên đăng nhập không được để trống!" : "Username cannot be empty!");
        }

        String cleanUser = username.trim();
        // Giới hạn tài khoản từ 4-20 ký tự, chỉ cho phép chữ, số, dấu gạch dưới chống SQL Injection
        if (!cleanUser.matches("^[a-zA-Z0-9_]{4,20}$")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên đăng nhập từ 4-20 ký tự (chỉ dùng chữ, số và dấu gạch dưới)!" :
                    "Username must be 4-20 characters, containing only letters, numbers, and underscores!");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được để trống!" : "Password cannot be empty!");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu bảo mật phải nằm trong khoảng từ 6 đến 32 ký tự!" : "Password must be between 6 and 32 characters!");
        }
        if (password.contains(" ")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được phép chứa khoảng trắng!" : "Password cannot contain spaces!");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu nhập lại xác nhận không trùng khớp!" : "Confirm password does not match!");
        }

        validateEmail(email, isVietnamese);
        validateNickname(nickname, isVietnamese);
    }

    /**
     * 6. MỚI: Bắt lỗi Đổi mật khẩu / Thay đổi thông tin cá nhân (Settings Validation)
     */
    public static void validatePasswordChange(String oldPassword, String newPassword, String confirmNewPassword, boolean isVietnamese) throws IllegalArgumentException {
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu cũ không được để trống!" : "Old password cannot be empty!");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới không được để trống!" : "New password cannot be empty!");
        }
        if (newPassword.length() < 6 || newPassword.length() > 32) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới phải từ 6 đến 32 ký tự!" : "New password must be between 6 and 32 characters!");
        }
        if (newPassword.contains(" ")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới không được chứa khoảng trắng!" : "New password cannot contain spaces!");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Xác nhận mật khẩu mới nhập lại không khớp!" : "Confirm new password does not match!");
        }
    }

    /**
     * Helper: Kiểm tra cấu trúc Email
     */
    public static String validateEmail(String email, boolean isVietnamese) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Địa chỉ Email không được để trống!" : "Email address cannot be empty!");
        }
        String cleanEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Định dạng Email không hợp lệ (Ví dụ: abc@gmail.com)!" : "Invalid email format!");
        }
        if (cleanEmail.length() > 50) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Độ dài Email không được vượt quá 50 ký tự!" : "Email address cannot exceed 50 characters!");
        }
        return cleanEmail;
    }

    /**
     * Helper: Kiểm tra cấu trúc Nickname hiển thị ở Sidebar
     */
    public static String validateNickname(String nickname, boolean isVietnamese) throws IllegalArgumentException {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên hiển thị không được để trống!" : "Nickname cannot be empty!");
        }
        String cleanNickname = nickname.trim();
        if (cleanNickname.length() < 2 || cleanNickname.length() > 20) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên hiển thị phải nằm trong khoảng từ 2 đến 20 ký tự!" : "Nickname must be between 2 and 20 characters!");
        }
        return cleanNickname;
    }
}