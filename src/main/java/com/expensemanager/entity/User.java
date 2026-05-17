package com.expensemanager.entity;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String gender;

    // 🌟 ĐÃ THÊM: Thuộc tính avatar để lưu trữ icon/ảnh đại diện của người dùng
    private String avatar;

    // Constructor 6 tham số (Dùng cho Đăng ký mới)
    public User(String id, String username, String passwordHash, String nickname, String email, String gender) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.avatar = "👤"; // Giá trị mặc định khi mới tạo tài khoản
    }

    // Constructor 4 tham số cũ (Giữ lại để tương thích với luồng DB cũ)
    public User(String id, String username, String passwordHash, String nickname) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = "";
        this.gender = "Other";
        this.avatar = "👤";
    }

    // --- Các hàm Getters và Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // 🌟 ĐÃ THÊM: Getter và Setter cho Avatar
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}