package com.expensemanager.ui;

import com.expensemanager.entity.Reminder;
import com.expensemanager.service.ReminderService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.ThemeManager;

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

    public ReminderManagerPanel(ReminderService reminderService, boolean isVietnamese) {
        this.reminderService = reminderService;
        this.isVietnamese = isVietnamese;
        setLayout(new BorderLayout());
        setBackground(new Color(30,30,30));
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JLabel title = new JLabel(isVietnamese ? "Quản lý nhắc nhở" : "Reminder Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(255,193,7));
        add(title, BorderLayout.NORTH);

        String[] columns = isVietnamese ?
                new String[]{"ID", "Loại", "Tiêu đề", "Thời gian/Ngày", "Hoạt động"} :
                new String[]{"ID", "Type", "Title", "Time/Date", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        reminderTable = new JTable(tableModel);
        reminderTable.setBackground(new Color(40,40,40));
        reminderTable.setForeground(Color.WHITE);
        reminderTable.getTableHeader().setBackground(new Color(60,60,60));
        reminderTable.getTableHeader().setForeground(Color.WHITE);
        reminderTable.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(reminderTable);
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        JButton btnAdd = new JButton(isVietnamese ? "Thêm nhắc nhở" : "Add Reminder");
        JButton btnEdit = new JButton(isVietnamese ? "Sửa" : "Edit");
        JButton btnDelete = new JButton(isVietnamese ? "Xóa" : "Delete");

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
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(30,30,30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"DAILY", "BILL", "BUDGET"});
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3,20);
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        JComboBox<String> recurringCombo = new JComboBox<>(new String[]{"NONE", "MONTHLY", "YEARLY"});
        JSpinner percentSpinner = new JSpinner(new SpinnerNumberModel(80, 0, 100, 5));
        JCheckBox activeCheck = new JCheckBox(isVietnamese ? "Kích hoạt" : "Active", true);
        activeCheck.setOpaque(false);
        activeCheck.setForeground(Color.WHITE);

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
        saveBtn.setBackground(new Color(255,193,7));
        saveBtn.setForeground(Color.BLACK);
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
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(comp, gbc);
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        refreshTable();
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (reminderTable != null) {
            reminderTable.setBackground(ThemeManager.getColor("input"));
            reminderTable.setForeground(ThemeManager.getColor("textPrimary"));
            reminderTable.getTableHeader().setBackground(ThemeManager.getColor("surface"));
            reminderTable.getTableHeader().setForeground(ThemeManager.getColor("textPrimary"));
        }
        // Các button
        for (Component comp : getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setBackground(ThemeManager.getColor("accent"));
                btn.setForeground(ThemeManager.getColor("bg"));
            }
        }
        refreshTable();
    }
}