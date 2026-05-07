package com.expensemanager.entity;

public class Category {
    private String id;
    private String name;
    private TransactionType type;   // INCOME hoặc EXPENSE

    public Category(String id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }
    public TransactionType getType() { return type; }

    // Setter (chỉ cho phép sửa tên)
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return this.name;   // để hiển thị trên giao diện
    }
}