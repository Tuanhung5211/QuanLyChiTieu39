package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private JTextField txtNickname, txtEmail;
    private JComboBox<String> cmbGender;
    private JPasswordField txtNewPassword, txtConfirmPassword;
    private JButton btnSave;
    private User currentUser;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nickname:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        txtNickname = new JTextField(15);
        formPanel.add(txtNickname, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtEmail = new JTextField(15);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        txtNewPassword = new JPasswordField(15);
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Xác nhận MK:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        txtConfirmPassword = new JPasswordField(15);
        formPanel.add(txtConfirmPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveProfile());
        formPanel.add(btnSave, gbc);

        add(formPanel, BorderLayout.NORTH);
        loadUserData();
    }

    public void loadUserData() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;
        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        if (user != null) {
            this.currentUser = user;
            txtNickname.setText(user.getNickname() != null ? user.getNickname() : "");
            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            cmbGender.setSelectedItem(user.getGender() != null ? user.getGender() : "Other");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nickname không được để trống!");
            return;
        }

        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }
            currentUser.setPasswordHash(UserService.hashPassword(newPassword));
        }

        currentUser.setNickname(nickname);
        currentUser.setEmail(email);
        currentUser.setGender(gender);
        DatabaseUtil.updateUser(currentUser);

        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainFrame) {
            ((MainFrame) window).refreshAllPanels();
        }
    }
}