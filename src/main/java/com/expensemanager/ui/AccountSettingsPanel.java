package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;
import com.expensemanager.util.InputValidator;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountSettingsPanel extends JPanel {

    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel profileCard;
    private JLabel lblProfileTitle, lblNickname, lblEmail, lblGender;
    private JTextField txtNickname, txtEmail;
    private JComboBox<String> cmbGender;
    private JButton btnUpdateProfile, btnOpenChangePass, btnDeleteAccount;

    public AccountSettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        initComponents();
        updateResponsiveLayout(isVietnamese, 560);
        refreshData();
        applyTheme();
    }

    private void initComponents() {
        profileCard = new JPanel(new BorderLayout(0, 15));
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblProfileTitle = new JLabel();
        lblProfileTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileCard.add(lblProfileTitle, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Nickname
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        lblNickname = new JLabel(); lblNickname.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(lblNickname, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        txtNickname = new JTextField(); styleTextField(txtNickname);
        pForm.add(txtNickname, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.25;
        lblEmail = new JLabel(); lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        txtEmail = new JTextField(); styleTextField(txtEmail);
        pForm.add(txtEmail, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        lblGender = new JLabel(); lblGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(lblGender, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        cmbGender = new JComboBox<>(isVietnamese ? new String[]{"Nam", "Nữ", "Khác"} : new String[]{"Male", "Female", "Other"});
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(cmbGender, gbc);

        // Save
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(15, 8, 5, 8);
        btnUpdateProfile = new JButton(); stylePrimaryButton(btnUpdateProfile);
        btnUpdateProfile.addActionListener(e -> updateProfile());
        pForm.add(btnUpdateProfile, gbc);

        // Change password
        gbc.gridy = 4;
        btnOpenChangePass = new JButton();
        btnOpenChangePass.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnOpenChangePass.setFocusPainted(false);
        btnOpenChangePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenChangePass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnOpenChangePass.addActionListener(e -> openChangePasswordDialog());
        pForm.add(btnOpenChangePass, gbc);

        // Delete account
        gbc.gridy = 5;
        btnDeleteAccount = new JButton();
        btnDeleteAccount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDeleteAccount.setFocusPainted(false);
        btnDeleteAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteAccount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("danger"), 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnDeleteAccount.addActionListener(e -> deleteAccount());
        btnDeleteAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnDeleteAccount.setBackground(ThemeManager.getColor("danger"));
                btnDeleteAccount.setForeground(ThemeManager.getColor("bg"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                applyTheme();
            }
        });
        pForm.add(btnDeleteAccount, gbc);

        profileCard.add(pForm, BorderLayout.CENTER);
        add(profileCard);
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (profileCard != null) {
            profileCard.setBackground(ThemeManager.getColor("surface"));
            profileCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }
        if (lblProfileTitle != null) lblProfileTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblNickname != null) lblNickname.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblEmail != null) lblEmail.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblGender != null) lblGender.setForeground(ThemeManager.getColor("textSecondary"));
        if (txtNickname != null) {
            txtNickname.setBackground(ThemeManager.getColor("input"));
            txtNickname.setForeground(ThemeManager.getColor("textPrimary"));
            txtNickname.setCaretColor(ThemeManager.getColor("accent"));
        }
        if (txtEmail != null) {
            txtEmail.setBackground(ThemeManager.getColor("input"));
            txtEmail.setForeground(ThemeManager.getColor("textPrimary"));
            txtEmail.setCaretColor(ThemeManager.getColor("accent"));
        }
        if (cmbGender != null) {
            cmbGender.setBackground(ThemeManager.getColor("input"));
            cmbGender.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnUpdateProfile != null) {
            btnUpdateProfile.setBackground(ThemeManager.getColor("accent"));
            // Use white text to improve contrast on dark accent
            btnUpdateProfile.setForeground(ThemeManager.getColor("bg"));
        }
        if (btnOpenChangePass != null) {
            btnOpenChangePass.setBackground(ThemeManager.getColor("input"));
            btnOpenChangePass.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setBackground(ThemeManager.getColor("surface"));
            btnDeleteAccount.setForeground(ThemeManager.getColor("danger"));
        }
    }

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username == null) return;

        User user = DatabaseUtil.getUserByUsername(username);
        if (user != null) {
            txtNickname.setText(user.getNickname());
            txtEmail.setText(user.getEmail());
            String g = user.getGender();
            if ("Male".equalsIgnoreCase(g) || "Nam".equalsIgnoreCase(g)) cmbGender.setSelectedIndex(0);
            else if ("Female".equalsIgnoreCase(g) || "Nữ".equalsIgnoreCase(g)) cmbGender.setSelectedIndex(1);
            else cmbGender.setSelectedIndex(2);
        }
    }

    private void updateProfile() {
        String nickname = txtNickname.getText();
        String email = txtEmail.getText();
        String gender = cmbGender.getSelectedIndex() == 0 ? "Male" : (cmbGender.getSelectedIndex() == 1 ? "Female" : "Other");

        try {
            InputValidator.validateNickname(nickname, isVietnamese);
            InputValidator.validateEmail(email, isVietnamese);

            User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
            if (user != null) {
                user.setNickname(nickname.trim());
                user.setEmail(email.trim());
                user.setGender(gender);

                DatabaseUtil.updateUser(user);
                JOptionPane.showMessageDialog(this, isVietnamese ? "Cập nhật thông tin thành công!" : "Profile updated successfully!");
                if (mainFrame != null) mainFrame.refreshAllPanels();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    isVietnamese ? "Lỗi nhập liệu" : "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openChangePasswordDialog() {
        JDialog passDialog = new JDialog(mainFrame, isVietnamese ? "Thay đổi mật khẩu" : "Change Password", true);
        passDialog.setSize(450, 400);
        passDialog.setLocationRelativeTo(this);
        passDialog.getContentPane().setBackground(ThemeManager.getColor("bg"));
        passDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.getColor("bg"));
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lOld = new JLabel(isVietnamese ? "Mật khẩu cũ:" : "Old Password:");
        lOld.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lOld.setForeground(ThemeManager.getColor("textSecondary"));
        formPanel.add(lOld, gbc);
        gbc.gridx = 1;
        JPasswordField txtOldPassword = new JPasswordField();
        stylePasswordField(txtOldPassword);
        formPanel.add(txtOldPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lNew = new JLabel(isVietnamese ? "Mật khẩu mới:" : "New Password:");
        lNew.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lNew.setForeground(ThemeManager.getColor("textSecondary"));
        formPanel.add(lNew, gbc);
        gbc.gridx = 1;
        JPasswordField txtNewPassword = new JPasswordField();
        stylePasswordField(txtNewPassword);
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lConf = new JLabel(isVietnamese ? "Xác nhận MK:" : "Confirm Pass:");
        lConf.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lConf.setForeground(ThemeManager.getColor("textSecondary"));
        formPanel.add(lConf, gbc);
        gbc.gridx = 1;
        JPasswordField txtConfirmPassword = new JPasswordField();
        stylePasswordField(txtConfirmPassword);
        formPanel.add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(10, 8, 5, 8);
        JCheckBox chkShowPasswords = new JCheckBox(isVietnamese ? "Hiển thị mật khẩu" : "Show passwords");
        chkShowPasswords.setForeground(ThemeManager.getColor("textSecondary"));
        chkShowPasswords.setBackground(ThemeManager.getColor("bg"));
        chkShowPasswords.setFocusPainted(false);
        chkShowPasswords.addActionListener(e -> {
            boolean show = chkShowPasswords.isSelected();
            txtOldPassword.setEchoChar(show ? (char) 0 : '•');
            txtNewPassword.setEchoChar(show ? (char) 0 : '•');
            txtConfirmPassword.setEchoChar(show ? (char) 0 : '•');
        });
        formPanel.add(chkShowPasswords, gbc);

        passDialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(ThemeManager.getColor("bg"));

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(ThemeManager.getColor("surface")); btnCancel.setForeground(ThemeManager.getColor("textPrimary"));
        btnCancel.setFocusPainted(false); btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCancel.addActionListener(e -> passDialog.dispose());

        JButton btnConfirm = new JButton(isVietnamese ? "XÁC NHẬN" : "CONFIRM");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(ThemeManager.getColor("accent")); btnConfirm.setForeground(ThemeManager.getColor("bg"));
        btnConfirm.setFocusPainted(false); btnConfirm.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnConfirm.addActionListener(e -> {
            String oldPass = new String(txtOldPassword.getPassword());
            String newPass = new String(txtNewPassword.getPassword());
            String confirmPass = new String(txtConfirmPassword.getPassword());

            try {
                InputValidator.validatePasswordChange(oldPass, newPass, confirmPass, isVietnamese);
                String username = SessionManager.getCurrentUsername();
                User user = UserService.login(username, oldPass);
                if (user == null) {
                    JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Mật khẩu cũ không đúng!" : "Incorrect old password!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    user.setPasswordHash(UserService.hashPassword(newPass));
                    DatabaseUtil.updateUserPassword(user.getId(), user.getPasswordHash());
                    JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Đổi mật khẩu thành công! Hãy đăng nhập lại." : "Password changed! Please re-login.");
                    passDialog.dispose();
                    logout();
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(passDialog, ex.getMessage(), isVietnamese ? "Lỗi cấu trúc" : "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        passDialog.add(btnPanel, BorderLayout.SOUTH);
        passDialog.setVisible(true);
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setBackground(ThemeManager.getColor("input"));
        pf.setForeground(ThemeManager.getColor("textPrimary"));
        pf.setCaretColor(ThemeManager.getColor("accent"));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pf.setPreferredSize(new Dimension(200, 38));
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Bạn có chắc muốn xóa tài khoản? Toàn bộ giao dịch và ngân sách sẽ bị mất và không thể hoàn tác!" : "Are you sure you want to delete your account? All transaction and budget data will be permanently lost!",
                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String userId = SessionManager.getCurrentUserId();
            if (userId != null) {
                DatabaseUtil.deleteTransactionsByUser(userId);
                DatabaseUtil.deleteBudgetsByUser(userId);
                DatabaseUtil.deleteUser(userId);
                logout();
            }
        }
    }

    private void logout() {
        SessionManager.logout();
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        new LoginFrame().setVisible(true);
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        int currentGenderIndex = cmbGender.getSelectedIndex();

        setMaximumSize(new Dimension(fluidWidth, Integer.MAX_VALUE));

        if (profileCard != null) {
            profileCard.setPreferredSize(new Dimension(fluidWidth, 450));
            profileCard.setMaximumSize(new Dimension(fluidWidth, 450));
            profileCard.setMinimumSize(new Dimension(fluidWidth, 450));
        }

        if (isVN) {
            lblProfileTitle.setText("Thông tin cá nhân");
            lblNickname.setText("Tên hiển thị:"); lblEmail.setText("Email:"); lblGender.setText("Giới tính:");
            btnUpdateProfile.setText("Lưu thay đổi"); btnOpenChangePass.setText("Đổi mật khẩu bảo mật");
            btnDeleteAccount.setText("Xóa tài khoản vĩnh viễn");
            cmbGender.setModel(new DefaultComboBoxModel<>(new String[]{"Nam", "Nữ", "Khác"}));
        } else {
            lblProfileTitle.setText("Personal Profile");
            lblNickname.setText("Display Name:"); lblEmail.setText("Email:"); lblGender.setText("Gender:");
            btnUpdateProfile.setText("Save Changes"); btnOpenChangePass.setText("Change Password");
            btnDeleteAccount.setText("Delete Account Permanently");
            cmbGender.setModel(new DefaultComboBoxModel<>(new String[]{"Male", "Female", "Other"}));
        }
        if (currentGenderIndex >= 0 && currentGenderIndex < cmbGender.getItemCount()) {
            cmbGender.setSelectedIndex(currentGenderIndex);
        }
    }
}