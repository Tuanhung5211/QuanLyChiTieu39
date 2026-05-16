package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class AddCategoryDialog extends JDialog {
    private JTextField txtName, txtIcon;
    private JRadioButton rbExpense, rbIncome;
    private Runnable onCategoryAdded;

    private static final String[] EMOJI_LIST = {
            "🍔", "🍕", "🍎", "🍿", "🎮", "📚", "💄", "⚽", "👥", "🚗",
            "👗", "🏍️", "💻", "✈️", "🏥", "🔧", "🏠", "🎁", "💖", "🛍️",
            "📱", "🐶", "🎵", "💊", "🎓", "💰", "💵", "💎", "🛒", "🎬"
    };

    public AddCategoryDialog(JFrame parent, Runnable onCategoryAdded) {
        super(parent, "Thêm danh mục mới", true);
        this.onCategoryAdded = onCategoryAdded;
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Tên danh mục:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        txtName = new JTextField();
        inputPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Icon:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtIcon = new JTextField("📌", 3);
        txtIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        inputPanel.add(txtIcon, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        JButton btnChooseIcon = new JButton("Chọn");
        btnChooseIcon.addActionListener(e -> showEmojiPicker());
        inputPanel.add(btnChooseIcon, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Loại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        rbExpense = new JRadioButton("Chi tiêu");
        rbIncome = new JRadioButton("Thu nhập");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbExpense);
        bg.add(rbIncome);
        rbExpense.setSelected(true);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(rbExpense);
        typePanel.add(rbIncome);
        inputPanel.add(typePanel, gbc);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> saveCategory());
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showEmojiPicker() {
        JDialog emojiDialog = new JDialog(this, "Chọn icon", true);
        emojiDialog.setSize(300, 200);
        emojiDialog.setLocationRelativeTo(this);
        JPanel emojiPanel = new JPanel(new GridLayout(0, 6, 5, 5));
        emojiPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String emoji : EMOJI_LIST) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            btn.addActionListener(e -> {
                txtIcon.setText(emoji);
                emojiDialog.dispose();
            });
            emojiPanel.add(btn);
        }
        emojiDialog.add(new JScrollPane(emojiPanel));
        emojiDialog.setVisible(true);
    }

    private void saveCategory() {
        String name = txtName.getText().trim();
        String icon = txtIcon.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên danh mục!");
            return;
        }
        TransactionType type = rbIncome.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
        String id = UUID.randomUUID().toString().substring(0, 8);
        Category category = new Category(id, name, type);
        DatabaseUtil.insertCategory(category);
        AddTransactionDialog.addCustomEmoji(name, icon);
        JOptionPane.showMessageDialog(this, "Đã thêm danh mục!");
        dispose();
        if (onCategoryAdded != null) onCategoryAdded.run();
    }
}