package com.expensemanager;

import com.expensemanager.ui.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf; // Import FlatLaf
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // 1. Kích hoạt FlatLaf Dark Mode - Đây là dòng "ma thuật" giải quyết mọi lỗi màu sắc
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            // 2. Mở giao diện
            new LoginFrame().setVisible(true);
        });
    }
}