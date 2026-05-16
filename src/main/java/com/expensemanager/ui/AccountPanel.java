package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;

public class AccountPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel lblAvatar, lblId, lblNickname, lblEmail, lblGender;

    public AccountPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(40, 40, 40));
        setPreferredSize(new Dimension(220, 0));

        // Panel thông tin
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(40, 40, 40));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        lblAvatar = new JLabel("?", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        lblId = createInfoLabel("ID: ");
        lblNickname = createInfoLabel("Nickname: ");
        lblEmail = createInfoLabel("Email: ");
        lblGender = createInfoLabel("Gender: ");

        infoPanel.add(lblAvatar);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblId);
        infoPanel.add(lblNickname);
        infoPanel.add(lblEmail);
        infoPanel.add(lblGender);

        add(infoPanel, BorderLayout.NORTH);

        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.addActionListener(e -> signOut());
        JButton btnDeleteAccount = new JButton("Delete Account");
        btnDeleteAccount.setForeground(Color.RED);
        btnDeleteAccount.addActionListener(e -> deleteAccount());

        buttonPanel.add(btnSignOut);
        buttonPanel.add(btnDeleteAccount);

        add(buttonPanel, BorderLayout.SOUTH);

        loadUserInfo();
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.LIGHT_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public void loadUserInfo() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        if (user != null) {
            lblAvatar.setText(user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "?");
            lblId.setText("ID: " + user.getId());
            lblNickname.setText("Nickname: " + (user.getNickname() != null ? user.getNickname() : ""));
            lblEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : ""));
            lblGender.setText("Gender: " + (user.getGender() != null ? user.getGender() : "Other"));
        }
    }

    private void signOut() {
        SessionManager.logout();
        mainFrame.dispose();
        new LoginFrame().setVisible(true);
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa tài khoản? Hành động này không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String userId = SessionManager.getCurrentUserId();
            if (userId != null) {
                // Xóa dữ liệu liên quan (giao dịch, ngân sách) trước khi xóa user
                // Cần thêm các phương thức xóa trong DatabaseUtil (xóa transactions, budgets theo user_id)
                DatabaseUtil.deleteTransactionsByUser(userId);
                DatabaseUtil.deleteBudgetsByUser(userId);
                DatabaseUtil.deleteUser(userId);
                SessionManager.logout();
                mainFrame.dispose();
                new LoginFrame().setVisible(true);
            }
        }
    }
}