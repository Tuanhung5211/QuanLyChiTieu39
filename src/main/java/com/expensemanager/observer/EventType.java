package com.expensemanager.observer;

public enum EventType {
    TRANSACTION_ADDED,      // Thêm giao dịch mới
    TRANSACTION_UPDATED,    // Sửa giao dịch
    TRANSACTION_DELETED,    // Xóa giao dịch
    BUDGET_CHANGED,         // Thay đổi ngân sách
    CATEGORY_ADDED,         // Thêm danh mục mới
    CATEGORY_DELETED,       // Xóa danh mục
    DATA_LOADED,            // Load dữ liệu xong
    DATA_CLEARED            // Xóa toàn bộ dữ liệu
}