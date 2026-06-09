package com.expensemanager.ui;

import com.expensemanager.entity.Reminder;
import com.expensemanager.service.ReminderService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReminderManagerPanel extends JPanel {
    private ReminderService reminderService;
    private boolean isVietnamese;
    private JTable reminderTable;
    private DefaultTableModel tableModel;

    // 👉 Các nút chuyển thành biến instance
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public ReminderManagerPanel(ReminderService reminderService, boolean isVietnamese) {
        this.reminderService = reminderService;
        this.isVietnamese = isVietnamese;
        setLayout(new BorderLayout());
        initComponents();
        refreshTable();
        applyTheme();
    }

    private void initComponents() {
        JLabel title = new JLabel(isVietnamese ? "Quản lý nhắc nhở" : "Reminder Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeManager.getColor("accent"));
        add(title, BorderLayout.NORTH);

        String[] columns = isVietnamese ?
                new String[]{"ID", "Loại", "Tiêu đề", "Thời gian/Ngày", "Hoạt động"} :
                new String[]{"ID", "Type", "Title", "Time/Date", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        reminderTable = new JTable(tableModel);
        reminderTable.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(reminderTable);
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);

        // 👉 Gán trực tiếp vào biến instance (không khai báo lại)
        btnAdd = new JButton(isVietnamese ? "Thêm nhắc nhở" : "Add Reminder");
        btnEdit = new JButton(isVietnamese ? "Sửa" : "Edit");
        btnDelete = new JButton(isVietnamese ? "Xóa" : "Delete");

        btnAdd.addActionListener(e -> showReminderDialog(null));
        btnEdit.addActionListener(e -> {
            int row = reminderTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) tableModel.getValueAt(row, 0);
                List<Reminder> reminders = reminderService.getUserReminders();
                Reminder selected = reminders.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
                if (selected != null) showReminderDialog(selected);
            } else {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Chọn một dòng để sửa" : "Select a row to edit");
            }
        });
        btnDelete.addActionListener(e -> {
            int row = reminderTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, isVietnamese ? "Xóa nhắc nhở này?" : "Delete this reminder?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    reminderService.deleteReminder(id);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Chọn một dòng để xóa" : "Select a row to delete");
            }
        });

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (reminderTable != null) {
            reminderTable.setBackground(ThemeManager.getColor("input"));
            reminderTable.setForeground(ThemeManager.getColor("textPrimary"));
            reminderTable.getTableHeader().setBackground(ThemeManager.getColor("surface"));
            reminderTable.getTableHeader().setForeground(ThemeManager.getColor("textPrimary"));
            reminderTable.setGridColor(ThemeManager.getColor("border"));
        }
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(ThemeManager.getColor("accent"));
            } else if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0) {
                JPanel panel = (JPanel) comp;
                for (Component btnComp : panel.getComponents()) {
                    if (btnComp instanceof JButton) {
                        JButton btn = (JButton) btnComp;
                        btn.setBackground(ThemeManager.getColor("accent"));
                        btn.setForeground(ThemeManager.getColor("bg"));
                    }
                }
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Reminder> reminders = reminderService.getUserReminders();
        for (Reminder r : reminders) {
            String timeOrDate = "";
            if (r.getType() == Reminder.ReminderType.DAILY && r.getRemindTime() != null)
                timeOrDate = r.getRemindTime().toString();
            else if (r.getType() == Reminder.ReminderType.BILL && r.getDueDate() != null)
                timeOrDate = r.getDueDate().toString();
            else if (r.getType() == Reminder.ReminderType.BUDGET && r.getThresholdPercent() != null)
                timeOrDate = r.getThresholdPercent() + "%";
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getType().toString(),
                    r.getTitle(),
                    timeOrDate,
                    r.isActive() ? "✓" : "✗"
            });
        }
    }

    private void showReminderDialog(Reminder existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isVietnamese ? "Nhắc nhở" : "Reminder", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getColor("bg"));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ThemeManager.getColor("bg"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"DAILY", "BILL", "BUDGET"});
        styleCombo(typeCombo);
        JTextField titleField = new JTextField(20); styleTextField(titleField);
        JTextArea descArea = new JTextArea(3,20); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        styleTextArea(descArea);
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        JComboBox<String> recurringCombo = new JComboBox<>(new String[]{"NONE", "MONTHLY", "YEARLY"});
        styleCombo(recurringCombo);
        JSpinner percentSpinner = new JSpinner(new SpinnerNumberModel(80, 0, 100, 5));
        JCheckBox activeCheck = new JCheckBox(isVietnamese ? "Kích hoạt" : "Active", true);
        activeCheck.setOpaque(false);
        activeCheck.setForeground(ThemeManager.getColor("textPrimary"));
        activeCheck.setBackground(ThemeManager.getColor("bg"));

        if (existing != null) {
            typeCombo.setSelectedItem(existing.getType().name());
            titleField.setText(existing.getTitle());
            descArea.setText(existing.getDescription());
            if (existing.getRemindTime() != null) {
                java.util.Date d = java.util.Date.from(existing.getRemindTime().atDate(LocalDate.now()).atZone(java.time.ZoneId.systemDefault()).toInstant());
                timeSpinner.setValue(d);
            }
            if (existing.getDueDate() != null) {
                java.util.Date d = java.util.Date.from(existing.getDueDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                dateSpinner.setValue(d);
            }
            if (existing.getRecurring() != null)
                recurringCombo.setSelectedItem(existing.getRecurring().name());
            if (existing.getThresholdPercent() != null)
                percentSpinner.setValue(existing.getThresholdPercent());
            activeCheck.setSelected(existing.isActive());
        }

        int y = 0;
        addRow(form, gbc, isVietnamese ? "Loại:" : "Type:", typeCombo, y++);
        addRow(form, gbc, isVietnamese ? "Tiêu đề:" : "Title:", titleField, y++);
        addRow(form, gbc, isVietnamese ? "Mô tả:" : "Description:", new JScrollPane(descArea), y++);
        addRow(form, gbc, isVietnamese ? "Giờ nhắc (DAILY):" : "Remind time (DAILY):", timeSpinner, y++);
        addRow(form, gbc, isVietnamese ? "Ngày đáo hạn (BILL):" : "Due date (BILL):", dateSpinner, y++);
        addRow(form, gbc, isVietnamese ? "Lặp lại:" : "Recurring:", recurringCombo, y++);
        addRow(form, gbc, isVietnamese ? "Ngưỡng % (BUDGET):" : "Threshold % (BUDGET):", percentSpinner, y++);
        addRow(form, gbc, "", activeCheck, y++);

        JButton saveBtn = new JButton(isVietnamese ? "Lưu" : "Save");
        saveBtn.setBackground(ThemeManager.getColor("accent"));
        saveBtn.setForeground(ThemeManager.getColor("bg"));
        saveBtn.addActionListener(e -> {
            try {
                Reminder.ReminderType type = Reminder.ReminderType.valueOf((String) typeCombo.getSelectedItem());
                String title = titleField.getText().trim();
                if (title.isEmpty()) throw new IllegalArgumentException(isVietnamese ? "Tiêu đề không được trống" : "Title required");
                String desc = descArea.getText().trim();
                LocalTime time = null;
                LocalDate due = null;
                Reminder.RecurringType recurring = Reminder.RecurringType.valueOf((String) recurringCombo.getSelectedItem());
                Integer threshold = (Integer) percentSpinner.getValue();
                boolean active = activeCheck.isSelected();

                if (type == Reminder.ReminderType.DAILY) {
                    java.util.Date d = (java.util.Date) timeSpinner.getValue();
                    time = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
                } else if (type == Reminder.ReminderType.BILL) {
                    java.util.Date d = (java.util.Date) dateSpinner.getValue();
                    due = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                }

                if (existing == null) {
                    Reminder r = new Reminder(null, SessionManager.getCurrentUserId(), type, title, desc, time, due, recurring, threshold, active, null);
                    reminderService.addReminder(r);
                } else {
                    existing.setType(type);
                    existing.setTitle(title);
                    existing.setDescription(desc);
                    existing.setRemindTime(time);
                    existing.setDueDate(due);
                    existing.setRecurring(recurring);
                    existing.setThresholdPercent(threshold);
                    existing.setActive(active);
                    reminderService.updateReminder(existing);
                }
                refreshTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = y; gbc.gridwidth = 2;
        form.add(saveBtn, gbc);
        dialog.add(form);
        dialog.setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, Component comp, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(ThemeManager.getColor("textPrimary"));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(comp, gbc);
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(5,8,5,8)
        ));
    }

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(ThemeManager.getColor("input"));
        ta.setForeground(ThemeManager.getColor("textPrimary"));
        ta.setCaretColor(ThemeManager.getColor("accent"));
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(5,8,5,8)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(ThemeManager.getColor("input"));
        combo.setForeground(ThemeManager.getColor("textPrimary"));
    }

    // 👉 PHƯƠNG THỨC CẬP NHẬT NGÔN NGỮ ĐẦY ĐỦ
    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;

        // Cập nhật tiêu đề cột
        String[] columns = isVN ?
                new String[]{"ID", "Loại", "Tiêu đề", "Thời gian/Ngày", "Hoạt động"} :
                new String[]{"ID", "Type", "Title", "Time/Date", "Active"};
        tableModel.setColumnIdentifiers(columns);

        // Cập nhật văn bản nút
        if (btnAdd != null) btnAdd.setText(isVN ? "Thêm nhắc nhở" : "Add Reminder");
        if (btnEdit != null) btnEdit.setText(isVN ? "Sửa" : "Edit");
        if (btnDelete != null) btnDelete.setText(isVN ? "Xóa" : "Delete");

        refreshTable();
    }
}