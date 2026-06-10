package com.expensemanager.util;

import java.util.regex.Pattern;

public class InputValidator {

    // Regex cho username: 4-20 ký tự, chỉ gồm chữ cái, số, gạch dưới
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    // Email: bắt buộc có ít nhất 1 chữ số ngay trước @ và đuôi @gmail.com
    private static final Pattern GMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]*[0-9]@gmail\\.com$",
            Pattern.CASE_INSENSITIVE
    );

    // Giới hạn số tiền tối đa (99 tỷ VND)
    private static final double MAX_AMOUNT = 99_999_999_999.0;

    /**
     * Kiểm tra và chuyển đổi chuỗi số tiền nhập vào.
     * Hỗ trợ các định dạng:
     * - "1000" (không dấu)
     * - "1,000" (dấu phẩy phân cách hàng nghìn)
     * - "1000.5" (dấu chấm thập phân)
     * - "1.000,5" (dấu chấm phân cách hàng nghìn, dấu phẩy thập phân)
     * - "1,000.5" (dấu phẩy phân cách hàng nghìn, dấu chấm thập phân)
     */
    public static double validateAmount(String amountStr, boolean isVietnamese) throws IllegalArgumentException {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền không được để trống!" : "Amount field cannot be empty!");
        }

        String clean = amountStr.trim().replaceAll("\\s+", "");

        // Loại bỏ ký tự không phải số, dấu chấm, dấu phẩy
        if (!clean.matches("[\\d.,]+")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền chỉ được nhập chữ số, dấu chấm hoặc dấu phẩy!" : "Amount must contain only digits, dots or commas!");
        }

        int dots = countChar(clean, '.');
        int commas = countChar(clean, ',');

        String normalized;
        if (dots > 0 && commas > 0) {
            int lastDot = clean.lastIndexOf('.');
            int lastComma = clean.lastIndexOf(',');
            if (lastDot > lastComma) {
                normalized = clean.replace(",", "");
            } else {
                normalized = clean.replace(".", "").replace(',', '.');
            }
        } else if (dots > 0 && commas == 0) {
            if (dots > 1) {
                normalized = clean.replace(".", "");
            } else {
                int dotIndex = clean.indexOf('.');
                String after = clean.substring(dotIndex + 1);
                if (after.length() <= 2 && after.matches("\\d+")) {
                    normalized = clean;
                } else {
                    normalized = clean.replace(".", "");
                }
            }
        } else if (dots == 0 && commas > 0) {
            if (commas > 1) {
                normalized = clean.replace(",", "");
            } else {
                int commaIndex = clean.indexOf(',');
                String after = clean.substring(commaIndex + 1);
                if (after.length() <= 2 && after.matches("\\d+")) {
                    normalized = clean.replace(',', '.');
                } else {
                    normalized = clean.replace(",", "");
                }
            }
        } else {
            normalized = clean;
        }

        if (!normalized.matches("\\d+(\\.\\d{1,2})?") && !normalized.matches("\\d+")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Định dạng số tiền không hợp lệ! Ví dụ: 1000, 1.000,5 hoặc 1000.5" :
                    "Invalid amount format! Examples: 1000, 1,000.5 or 1000.5");
        }

        double amount;
        try {
            amount = Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền nhập vào không phải là số hợp lệ!" : "Amount must be a valid number!");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền phải lớn hơn 0!" : "Amount must be greater than 0!");
        }

        if (amount > MAX_AMOUNT) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Số tiền vượt quá hạn mức tối đa (99 tỷ VND)!" : "Amount exceeds maximum limit (99 billion)!");
        }

        return amount;
    }

    private static int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }

    /**
     * Kiểm tra tên danh mục (không rỗng, độ dài 1-30, không chứa ký tự nguy hiểm)
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
     * Kiểm tra ghi chú giao dịch (tối đa 200 ký tự)
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
     * Kiểm tra thông tin đăng nhập
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
     * Kiểm tra thông tin đăng ký
     */
    public static void validateRegister(String username, String password, String confirmPassword, String email, String nickname, boolean isVietnamese) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên đăng nhập không được để trống!" : "Username cannot be empty!");
        }
        String cleanUser = username.trim();
        if (!USERNAME_PATTERN.matcher(cleanUser).matches()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên đăng nhập từ 4-20 ký tự (chỉ dùng chữ, số và dấu gạch dưới)!" :
                    "Username must be 4-20 characters, containing only letters, numbers, and underscores!");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được để trống!" : "Password cannot be empty!");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu phải có ít nhất 8 ký tự!" : "Password must be at least 8 characters!");
        }
        if (password.length() > 32) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được vượt quá 32 ký tự!" : "Password cannot exceed 32 characters!");
        }
        if (password.contains(" ")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu không được chứa khoảng trắng!" : "Password cannot contain spaces!");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số!" :
                    "Password must contain at least one uppercase letter, one lowercase letter and one digit!");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu nhập lại xác nhận không trùng khớp!" : "Confirm password does not match!");
        }

        validateEmail(email, isVietnamese);
        validateNickname(nickname, isVietnamese);
    }

    /**
     * Kiểm tra thay đổi mật khẩu
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
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới phải có ít nhất 8 ký tự!" : "New password must be at least 8 characters!");
        }
        if (newPassword.length() > 32) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới không được vượt quá 32 ký tự!" : "New password cannot exceed 32 characters!");
        }
        if (newPassword.contains(" ")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới không được chứa khoảng trắng!" : "New password cannot contain spaces!");
        }
        if (!newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Mật khẩu mới phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số!" :
                    "New password must contain at least one uppercase letter, one lowercase letter and one digit!");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Xác nhận mật khẩu mới nhập lại không khớp!" : "Confirm new password does not match!");
        }
    }

    /**
     * Kiểm tra định dạng email: phải có ít nhất 1 chữ số ngay trước @ và đuôi @gmail.com
     */
    public static String validateEmail(String email, boolean isVietnamese) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Địa chỉ Email không được để trống!" : "Email address cannot be empty!");
        }
        String cleanEmail = email.trim();
        if (!GMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Email phải có đuôi @gmail.com và phải có ít nhất 1 chữ số ngay trước @ (ví dụ: ten123@gmail.com)!" :
                    "Email must end with @gmail.com and have at least one digit right before @ (e.g., name123@gmail.com)!");
        }
        if (cleanEmail.length() > 50) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Độ dài Email không được vượt quá 50 ký tự!" : "Email address cannot exceed 50 characters!");
        }
        return cleanEmail;
    }

    /**
     * Kiểm tra tên hiển thị (nickname): 2-20 ký tự, không rỗng
     */
    public static String validateNickname(String nickname, boolean isVietnamese) throws IllegalArgumentException {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên hiển thị không được để trống!" : "Nickname cannot be empty!");
        }
        String cleanNickname = nickname.trim();
        if (cleanNickname.length() < 2 || cleanNickname.length() > 20) {
            throw new IllegalArgumentException(isVietnamese ?
                    "Tên hiển thị phải từ 2 đến 20 ký tự!" : "Nickname must be between 2 and 20 characters!");
        }
        return cleanNickname;
    }
}