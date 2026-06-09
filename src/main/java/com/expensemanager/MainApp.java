package com.expensemanager;

import com.expensemanager.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 🌟 FIX LỖI GIAO DIỆN TRẮNG: Sử dụng CrossPlatform thay vì SystemLook
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}