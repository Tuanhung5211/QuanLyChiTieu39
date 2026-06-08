package com.expensemanager.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ValidationUI {
    private static Border defaultBorder;
    private static final int ERROR_THICKNESS = 2;

    // Khởi tạo border mặc định từ một JTextField đã được style
    public static void initDefaultBorder(JTextField sample) {
        if (sample != null && sample.getBorder() != null) {
            defaultBorder = sample.getBorder();
        }
    }

    // Lấy border mặc định đồng bộ theo ThemeManager
    private static Border getDefaultBorder() {
        if (defaultBorder != null) return defaultBorder;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    // Tạo border lỗi đồng bộ theo màu "danger" của ThemeManager
    private static Border createErrorBorder() {
        Color errorColor = ThemeManager.getColor("danger");

        if (defaultBorder != null && defaultBorder instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder cb = (javax.swing.border.CompoundBorder) defaultBorder;
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(errorColor, ERROR_THICKNESS),
                    cb.getInsideBorder()
            );
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(errorColor, ERROR_THICKNESS),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        );
    }

    // Set border lỗi cho field
    public static void setErrorBorder(JTextField field) {
        if (field != null) {
            field.setBorder(createErrorBorder());
        }
    }

    // Reset border về mặc định
    public static void resetBorder(JTextField field) {
        if (field != null) {
            field.setBorder(getDefaultBorder());
        }
    }

    // Tự động xóa lỗi khi người dùng gõ hoặc focus vào field
    public static void addAutoReset(JTextField field) {
        if (field == null) return;
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                resetBorder(field);
            }
        });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { resetBorder(field); }
        });
    }
}