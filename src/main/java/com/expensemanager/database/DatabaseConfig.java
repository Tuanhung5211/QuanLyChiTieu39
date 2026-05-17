package com.expensemanager.database;

public class DatabaseConfig {
    // 🌟 ĐÃ CẬP NHẬT: Thêm tham số useUnicode, characterEncoding và connectionCollation vào đuôi URL
    public static final String DB_URL = "jdbc:mysql://localhost:3306/expense_manager?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";

    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "lenhan142";
}