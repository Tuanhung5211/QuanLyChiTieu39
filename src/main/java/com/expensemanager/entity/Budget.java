package com.expensemanager.entity;

import java.time.LocalDate;

public class Budget {
    // --- Các trường dữ liệu cũ ---
    private String userId;
    private int month;
    private int year;
    private double limit;

    // --- Các trường dữ liệu MỚI thêm vào cho chức năng nâng cao ---
    private String id;
    private Category category;
    private LocalDate startDate;
    private LocalDate endDate;
    private int threshold;

    // 🌟 ĐÃ THÊM BIẾN NÀY ĐỂ SỬA LỖI:
    private double spent;

    // 1. CONSTRUCTOR RỖNG (Giúp sửa lỗi "cannot be applied to given types")
    public Budget() {
    }

    // 2. CONSTRUCTOR CŨ (Giữ lại để các file khác trong project không bị báo lỗi)
    public Budget(String userId, int month, int year, double limit) {
        this.userId = userId;
        this.month = month;
        this.year = year;
        this.limit = limit;
    }

    // ================= GETTERS VÀ SETTERS =================

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }

    // 🌟 ĐÃ THÊM GETTER VÀ SETTER CHO SPENT:
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }

    // (Tùy chọn) Thêm hàm hỗ trợ kiểm tra xem có vượt ngân sách không:
    public boolean isOverBudget() {
        return spent > limit;
    }

    public double getRemaining() {
        return Math.max(0, limit - spent);
    }
}