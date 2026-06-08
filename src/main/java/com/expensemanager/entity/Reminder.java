package com.expensemanager.entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reminder {
    public enum ReminderType { DAILY, BILL, BUDGET }
    public enum RecurringType { NONE, MONTHLY, YEARLY }

    private String id;
    private String userId;
    private ReminderType type;
    private String title;
    private String description;
    private LocalTime remindTime;
    private LocalDate dueDate;
    private RecurringType recurring;
    private Integer thresholdPercent;
    private boolean isActive;
    private LocalDate lastTriggered;

    public Reminder() {}

    public Reminder(String id, String userId, ReminderType type, String title, String description,
                    LocalTime remindTime, LocalDate dueDate, RecurringType recurring,
                    Integer thresholdPercent, boolean isActive, LocalDate lastTriggered) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.remindTime = remindTime;
        this.dueDate = dueDate;
        this.recurring = recurring;
        this.thresholdPercent = thresholdPercent;
        this.isActive = isActive;
        this.lastTriggered = lastTriggered;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalTime getRemindTime() { return remindTime; }
    public void setRemindTime(LocalTime remindTime) { this.remindTime = remindTime; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public RecurringType getRecurring() { return recurring; }
    public void setRecurring(RecurringType recurring) { this.recurring = recurring; }
    public Integer getThresholdPercent() { return thresholdPercent; }
    public void setThresholdPercent(Integer thresholdPercent) { this.thresholdPercent = thresholdPercent; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDate getLastTriggered() { return lastTriggered; }
    public void setLastTriggered(LocalDate lastTriggered) { this.lastTriggered = lastTriggered; }
}