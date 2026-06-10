package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.service.PremiumManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese;
    private JTable userTable;
    private DefaultTableModel tableModel;

    private JScrollPane scrollPane;

    private JButton btnGrantPremium;
    private JButton btnRevokePremium;
    private JButton btnDeleteUser;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshTable();
        applyTheme();
    }

    private void initComponents() {
        JLabel title = new JLabel(isVietnamese ? "Quản lý người dùng & Premium" : "User & Premium Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ThemeManager.getColor("accent"));
        add(title, BorderLayout.NORTH);

        String[] columns = isVietnamese ?
                new String[]{"ID", "Tên đăng nhập", "Email", "Premium hạn", "Admin"} :
                new String[]{"ID", "Username", "Email", "Premium Expiry", "Admin"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(28);
        userTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);

        btnGrantPremium = new JButton(isVietnamese ? "🎁 Cấp Premium" : "🎁 Grant Premium");
        btnRevokePremium = new JButton(isVietnamese ? "❌ Hủy Premium" : "❌ Revoke Premium");
        btnDeleteUser = new JButton(isVietnamese ? "🗑️ Xóa người dùng" : "🗑️ Delete User");

        styleButton(btnGrantPremium, ThemeManager.getColor("success"));
        styleButton(btnRevokePremium, ThemeManager.getColor("warning"));
        styleButton(btnDeleteUser, ThemeManager.getColor("danger"));

        btnGrantPremium.addActionListener(e -> grantPremium());
        btnRevokePremium.addActionListener(e -> revokePremium());
        btnDeleteUser.addActionListener(e -> deleteUser());

        buttonPanel.add(btnGrantPremium);
        buttonPanel.add(btnRevokePremium);
        buttonPanel.add(btnDeleteUser);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = DatabaseUtil.getAllUsers();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (User u : users) {
            String expiry = u.getPremiumExpiryDate() != null ?
                    u.getPremiumExpiryDate().format(df) : "---";
            String admin = u.isAdmin() ? "✓" : "";
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    expiry,
                    admin
            });
        }
    }

    private void grantPremium() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Chọn một người dùng trước!" : "Select a user first!");
            return;
        }
        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        boolean isAdmin = ((String) tableModel.getValueAt(selectedRow, 4)).equals("✓");
        if (isAdmin) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Không thể cấp Premium cho tài khoản Admin." : "Cannot grant Premium to Admin account.");
            return;
        }

        String input = JOptionPane.showInputDialog(this,
                isVietnamese ? "Nhập số ngày Premium (ví dụ: 30):" : "Enter number of Premium days (e.g., 30):");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int days = Integer.parseInt(input.trim());
            if (days <= 0) throw new NumberFormatException();
            PremiumManager.activatePremium(userId, days);
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Đã cấp " + days + " ngày Premium cho " + username : "Granted " + days + " days Premium to " + username);
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Số ngày không hợp lệ!" : "Invalid number!");
        }
    }

    private void revokePremium() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Chọn một người dùng trước!" : "Select a user first!");
            return;
        }
        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Xóa Premium của " + username + "?" : "Revoke Premium from " + username + "?",
                isVietnamese ? "Xác nhận" : "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            PremiumManager.deactivatePremium(userId);
            refreshTable();
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Chọn một người dùng trước!" : "Select a user first!");
            return;
        }
        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (username.equals(SessionManager.getCurrentUsername())) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Bạn không thể tự xóa chính mình!" : "You cannot delete yourself!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Xóa vĩnh viễn người dùng " + username + " và toàn bộ dữ liệu của họ (bao gồm giao dịch định kì)?" : "Permanently delete user " + username + " and all their data (including scheduled transactions)?",
                isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseUtil.deleteTransactionsByUser(userId);
            DatabaseUtil.deleteBudgetsByUser(userId);
            DatabaseUtil.deleteRecurringTransactionsByUser(userId); // Thêm dòng này
            DatabaseUtil.deleteUser(userId);
            refreshTable();
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(ThemeManager.getColor("bg"));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (userTable != null) {
            userTable.setBackground(ThemeManager.getColor("surface"));
            userTable.setForeground(ThemeManager.getColor("textPrimary"));
            userTable.getTableHeader().setBackground(ThemeManager.getColor("input"));
            userTable.getTableHeader().setForeground(ThemeManager.getColor("textPrimary"));
            userTable.setGridColor(ThemeManager.getColor("border"));
        }

        if (scrollPane != null) {
            scrollPane.setBackground(ThemeManager.getColor("surface"));
            scrollPane.getViewport().setBackground(ThemeManager.getColor("surface"));
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));
        }
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;

        String[] columns = isVN ?
                new String[]{"ID", "Tên đăng nhập", "Email", "Premium hạn", "Admin"} :
                new String[]{"ID", "Username", "Email", "Premium Expiry", "Admin"};
        tableModel.setColumnIdentifiers(columns);

        if (btnGrantPremium != null) btnGrantPremium.setText(isVN ? "🎁 Cấp Premium" : "🎁 Grant Premium");
        if (btnRevokePremium != null) btnRevokePremium.setText(isVN ? "❌ Hủy Premium" : "❌ Revoke Premium");
        if (btnDeleteUser != null) btnDeleteUser.setText(isVN ? "🗑️ Xóa người dùng" : "🗑️ Delete User");

        refreshTable();
    }
}