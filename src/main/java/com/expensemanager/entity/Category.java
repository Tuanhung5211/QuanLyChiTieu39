package com.expensemanager.entity;

public class Category {
    private String id;
    private String name;
    private TransactionType type;

    // 🌟 1. THÊM CONSTRUCTOR RỖNG ĐỂ SỬA LỖI BIÊN DỊCH
    public Category() {
    }

    // 2. CONSTRUCTOR CŨ (Giữ lại để không lỗi các phần khác)
    public Category(String id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // ================= GETTERS VÀ SETTERS =================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    // 🌟 3. THÊM HÀM NÀY ĐỂ COMBOBOX HIỂN THỊ TÊN DANH MỤC CỰC KỲ ĐẸP
    @Override
    public String toString() {
        return name;
    }
}