package com.expensemanager.util;

import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ValidationUI {
    // Tăng độ dày nếu muốn rõ nét hơn, chuẩn Material thường để 1px bo góc nhẹ
    private static final int ERROR_THICKNESS = 1;

    // Viền mặc định: Rộng rãi, thoáng đãng, bo tròn (true)
    public static Border getDefaultBorder() {
        Color borderColor = ThemeManager.getColor("border");
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16) // Padding Google: rộng và phẳng
        );
    }

    // Thiết lập viền khởi tạo (Lấy từ màu Theme)
    public static void initDefaultBorder(JTextField field) {
        if (field != null) {
            field.setBorder(getDefaultBorder());
        }
    }

    // Viền báo lỗi: Màu đỏ danger của hệ thống
    public static Border createErrorBorder() {
        Color errorColor = ThemeManager.getColor("danger");
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(errorColor, ERROR_THICKNESS, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    public static Border createErrorBorder(JComboBox<?> cb) {
        Color errorColor = ThemeManager.getColor("danger");
        if (cb.getBorder() instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder current = (javax.swing.border.CompoundBorder) cb.getBorder();
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(errorColor, ERROR_THICKNESS, true),
                    current.getInsideBorder()
            );
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(errorColor, ERROR_THICKNESS, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    public static void setErrorBorder(JTextField field) {
        if (field != null) {
            field.setBorder(createErrorBorder());
        }
    }

    public static void resetBorder(JTextField field) {
        if (field != null) {
            field.setBorder(getDefaultBorder());
        }
    }

    // Tự động xóa lỗi khi người dùng gõ hoặc click vào ô
    public static void addAutoReset(JTextField field) {
        if (field == null) return;
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                resetBorder(field);
                field.setBackground(ThemeManager.getColor("bg")); // Hiệu ứng sáng nền khi focus
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBackground(ThemeManager.getColor("inputBg")); // Trả về màu nền input
            }
        });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
        });
    }
}