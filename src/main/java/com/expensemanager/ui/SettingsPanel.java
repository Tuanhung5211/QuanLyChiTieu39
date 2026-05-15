package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField txtNickname;
    private JTextField txtEmail;
    private JComboBox<String> cmbGender;
    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Cài đặt tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(SURFACE_COLOR);
        btnLogout.setForeground(DANGER_RED);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        headerPanel.add(btnLogout, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- BODY CHỨA 2 FORM ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // 1. Form Cập nhật thông tin
        bodyPanel.add(createProfileSection());
        bodyPanel.add(Box.createVerticalStrut(25));

        // 2. Form Đổi mật khẩu
        bodyPanel.add(createSecuritySection());
        bodyPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createProfileSection() {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ACCENT_YELLOW);
        section.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SURFACE_COLOR);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(createLabel("Tên hiển thị:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNickname = new JTextField();
        styleTextField(txtNickname);
        form.add(txtNickname, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtEmail = new JTextField();
        styleTextField(txtEmail);
        form.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createLabel("Giới tính:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbGender.setBackground(INPUT_BG);
        cmbGender.setForeground(TEXT_PRIMARY);
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        form.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 0, 10);
        JButton btnUpdate = new JButton("Lưu thay đổi");
        stylePrimaryButton(btnUpdate);
        btnUpdate.addActionListener(e -> updateProfile());
        form.add(btnUpdate, gbc);

        section.add(form, BorderLayout.CENTER);
        return section;
    }

    private JPanel createSecuritySection() {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảo mật");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ACCENT_YELLOW);
        section.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SURFACE_COLOR);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(createLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtOldPassword = new JPasswordField();
        styleTextField(txtOldPassword);
        form.add(txtOldPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNewPassword = new JPasswordField();
        styleTextField(txtNewPassword);
        form.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createLabel("Xác nhận MK:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtConfirmPassword = new JPasswordField();
        styleTextField(txtConfirmPassword);
        form.add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 0, 10);
        JButton btnChangePass = new JButton("Đổi mật khẩu");
        stylePrimaryButton(btnChangePass);
        btnChangePass.addActionListener(e -> changePassword());
        form.add(btnChangePass, gbc);

        section.add(form, BorderLayout.CENTER);
        return section;
    }

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username != null) {
            User user = DatabaseUtil.getUserByUsername(username);
            if (user != null) {
                txtNickname.setText(user.getNickname());
                txtEmail.setText(user.getEmail());
                cmbGender.setSelectedItem(user.getGender());
            }
        }
        txtOldPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    private void updateProfile() {
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();

        String username = SessionManager.getCurrentUsername();
        if (username == null) return;

        User user = DatabaseUtil.getUserByUsername(username);
        if (user != null) {
            user.setNickname(nickname);
            user.setEmail(email);
            user.setGender(gender);
            DatabaseUtil.updateUser(user);
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");

            // F5 lại toàn bộ giao diện để Avatar và Tên trên Sidebar đổi theo
            mainFrame.dispose();
            new MainFrame().setVisible(true);
        }
    }

    private void changePassword() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường mật khẩu!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = SessionManager.getCurrentUsername();
        User user = UserService.login(username, oldPass);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Mật khẩu cũ không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            user.setPasswordHash(newPass);
            DatabaseUtil.updateUser(user);
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            logout();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Đã gọi chuẩn xác hàm của TheTays
            SessionManager.logout();

            mainFrame.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW);
        btn.setForeground(BG_COLOR);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(255, 205, 50)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT_YELLOW); }
        });
    }
}