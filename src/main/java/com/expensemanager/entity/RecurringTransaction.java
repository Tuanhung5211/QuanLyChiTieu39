package com.expensemanager.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho một giao dịch lặp lại (Recurring Transaction).
 * Mỗi RecurringTransaction tạo ra các Transaction thực tế theo một mô hình lặp lại.
 */
public class RecurringTransaction {
    public enum RecurrenceType {
        DAILY,      // Hàng ngày
        WEEKLY,     // Hàng tuần
        MONTHLY,    // Hàng tháng
        YEARLY,     // Hàng năm
        CUSTOM      // Tùy chỉnh (sau N ngày)
    }

    private String id;
    private String userId;
    private double amount;
    private TransactionType type;
    private Category category;
    private String note;
    private RecurrenceType recurrenceType;
    private int customIntervalDays;  // Được sử dụng khi recurrenceType = CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;        // NULL = không có hạn chế
    private LocalDateTime createdAt;
    private boolean isActive;
    private LocalDate lastGeneratedDate;  // Ngày tạo giao dịch cuối cùng

    // Constructor
    public RecurringTransaction() {
    }

    public RecurringTransaction(String id, String userId, double amount, TransactionType type,
                                Category category, String note, RecurrenceType recurrenceType,
                                int customIntervalDays, LocalDate startDate, LocalDate endDate,
                                LocalDateTime createdAt, boolean isActive, LocalDate lastGeneratedDate) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.recurrenceType = recurrenceType;
        this.customIntervalDays = customIntervalDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.lastGeneratedDate = lastGeneratedDate;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public int getCustomIntervalDays() { return customIntervalDays; }
    public void setCustomIntervalDays(int customIntervalDays) { this.customIntervalDays = customIntervalDays; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDate getLastGeneratedDate() { return lastGeneratedDate; }
    public void setLastGeneratedDate(LocalDate lastGeneratedDate) { this.lastGeneratedDate = lastGeneratedDate; }

    /**
     * Tính toán ngày tiếp theo sẽ tạo giao dịch dựa trên loại lặp lại
     */
    public LocalDate getNextOccurrenceDate() {
        if (!isActive || lastGeneratedDate == null) {
            return startDate;
        }

        LocalDate next = lastGeneratedDate;
        switch (recurrenceType) {
            case DAILY:
                next = next.plusDays(1);
                break;
            case WEEKLY:
                next = next.plusWeeks(1);
                break;
            case MONTHLY:
                next = next.plusMonths(1);
                break;
            case YEARLY:
                next = next.plusYears(1);
                break;
            case CUSTOM:
                next = next.plusDays(customIntervalDays);
                break;
        }
        return next;
    }

    /**
     * Kiểm tra xem giao dịch này có nên được tạo ngày hôm nay không
     */
    public boolean shouldGenerateToday() {
        if (!isActive) return false;

        LocalDate today = LocalDate.now();

        // Kiểm tra nếu chưa bắt đầu
        if (today.isBefore(startDate)) return false;

        // Kiểm tra nếu đã kết thúc
        if (endDate != null && today.isAfter(endDate)) return false;

        // Lần đầu tiên tạo
        if (lastGeneratedDate == null) {
            return today.equals(startDate) || today.isAfter(startDate);
        }

        // Tính ngày tiếp theo cần tạo
        LocalDate nextDate = getNextOccurrenceDate();
        return !today.isBefore(nextDate);
    }

    @Override
    public String toString() {
        return String.format("[LẶP] %s: %,.0f VND (%s) - %s",
                type, amount, recurrenceType, note);
    }
}

