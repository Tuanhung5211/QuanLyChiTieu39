package com.expensemanager;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.ui.LoginFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Set Look and Feel cho đẹp (optional)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Khởi tạo database tables trước khi chạy ứng dụng
        try {
            DatabaseUtil.initializeDatabase();
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            // Vẫn chạy ứng dụng nhưng có thể bị lỗi sau
        }

        // Chạy ứng dụng
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}