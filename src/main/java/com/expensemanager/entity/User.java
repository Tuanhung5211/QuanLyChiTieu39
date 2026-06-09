package com.expensemanager.entity;

import java.time.LocalDate;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String gender;
    private String avatar;
    private LocalDate premiumExpiryDate;
    private boolean isAdmin;

    // Constructor 6 tham số (dùng cho đăng ký mới)
    public User(String id, String username, String passwordHash, String nickname, String email, String gender) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.avatar = "👤";
        this.premiumExpiryDate = null;
        this.isAdmin = false;
    }

    // Constructor 4 tham số cũ (giữ lại tương thích)
    public User(String id, String username, String passwordHash, String nickname) {
        this(id, username, passwordHash, nickname, "", "Other");
    }

    // Getters & Setters
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

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDate getPremiumExpiryDate() { return premiumExpiryDate; }
    public void setPremiumExpiryDate(LocalDate premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}