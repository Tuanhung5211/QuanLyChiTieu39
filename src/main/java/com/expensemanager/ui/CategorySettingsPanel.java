package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class CategorySettingsPanel extends JPanel {
    private JList<Category> categoryList;
    private DefaultListModel<Category> listModel;
    private JButton btnAdd, btnDelete;
    private MainFrame mainFrame;

    public CategorySettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        listModel = new DefaultListModel<>();
        categoryList = new JList<>(listModel);
        categoryList.setCellRenderer(new CategoryCellRenderer());
        JScrollPane scrollPane = new JScrollPane(categoryList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAdd = new JButton("Thêm danh mục");
        btnAdd.addActionListener(e -> addCategory());
        btnDelete = new JButton("Xóa danh mục");
        btnDelete.addActionListener(e -> deleteCategory());
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshCategories();
    }

    public void refreshCategories() {
        listModel.clear();
        List<Category> categories = DatabaseUtil.getAllCategories();
        for (Category c : categories) {
            listModel.addElement(c);
        }
    }

    private void addCategory() {
        JTextField txtName = new JTextField();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
        JTextField txtIcon = new JTextField("📌");

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Tên:"));
        panel.add(txtName);
        panel.add(new JLabel("Loại:"));
        panel.add(cmbType);
        panel.add(new JLabel("Icon:"));
        panel.add(txtIcon);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm danh mục mới", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được trống!");
                return;
            }
            TransactionType type = cmbType.getSelectedItem().equals("INCOME") ? TransactionType.INCOME : TransactionType.EXPENSE;
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = new Category(id, name, type);
            DatabaseUtil.insertCategory(category);
            AddTransactionDialog.addCustomEmoji(name, txtIcon.getText().trim());
            refreshCategories();
        }
    }

    private void deleteCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa danh mục \"" + selected.getName() + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseUtil.deleteCategory(selected.getId());
                refreshCategories();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một danh mục để xóa.");
        }
    }

    private class CategoryCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Category) {
                Category c = (Category) value;
                label.setText(c.getName() + " (" + c.getType() + ")");
            }
            return label;
        }
    }
}