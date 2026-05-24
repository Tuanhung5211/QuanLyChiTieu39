package com.expensemanager.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ValidationUI {
    private static Border defaultBorder;
    private static final Color ERROR_COLOR = Color.RED;
    private static final int ERROR_THICKNESS = 2;

    // Khởi tạo border mặc định từ một JTextField đã được style
    public static void initDefaultBorder(JTextField sample) {
        if (sample != null && sample.getBorder() != null) {
            defaultBorder = sample.getBorder();
        }
    }

    // Lấy border mặc định (nếu chưa có thì tạo một border giả định)
    private static Border getDefaultBorder() {
        if (defaultBorder != null) return defaultBorder;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    // Tạo border lỗi (giữ nguyên phần inside border của mặc định)
    private static Border createErrorBorder() {
        Border base = getDefaultBorder();
        if (base instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder cb = (javax.swing.border.CompoundBorder) base;
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ERROR_COLOR, ERROR_THICKNESS),
                    cb.getInsideBorder()
            );
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR_COLOR, ERROR_THICKNESS),
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
        if (field != null && defaultBorder != null) {
            field.setBorder(defaultBorder);
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