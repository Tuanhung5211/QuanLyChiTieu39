package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;
import com.expensemanager.util.InputValidator;

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

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);

    private int getResponsiveWidth() {
        if (mainFrame == null) return 560;
        int frameWidth = mainFrame.getWidth();
        if (frameWidth >= 1600) return 850;
        if (frameWidth >= 1400) return 700;
        return 560;
    }

    public AccountSettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        profileCard = new JPanel(new BorderLayout(0, 15));
        profileCard.setBackground(SURFACE_COLOR);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblProfileTitle = new JLabel();
        lblProfileTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblProfileTitle.setForeground(ACCENT_YELLOW);
        profileCard.add(lblProfileTitle, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        lblNickname = new JLabel(); lblNickname.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblNickname.setForeground(TEXT_SECONDARY);
        pForm.add(lblNickname, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        txtNickname = new JTextField(); styleTextField(txtNickname);
        pForm.add(txtNickname, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.25;
        lblEmail = new JLabel(); lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblEmail.setForeground(TEXT_SECONDARY);
        pForm.add(lblEmail, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        txtEmail = new JTextField(); styleTextField(txtEmail);
        pForm.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        lblGender = new JLabel(); lblGender.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblGender.setForeground(TEXT_SECONDARY);
        pForm.add(lblGender, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;

        cmbGender = new JComboBox<>(isVietnamese ? new String[]{"Nam", "Nữ", "Khác"} : new String[]{"Male", "Female", "Other"});
        cmbGender.setBackground(INPUT_BG); cmbGender.setForeground(TEXT_PRIMARY); cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(15, 8, 5, 8);
        btnUpdateProfile = new JButton(); stylePrimaryButton(btnUpdateProfile);
        btnUpdateProfile.addActionListener(e -> updateProfile());
        pForm.add(btnUpdateProfile, gbc);

        gbc.gridy = 4;
        btnOpenChangePass = new JButton();
        btnOpenChangePass.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnOpenChangePass.setBackground(INPUT_BG);
        btnOpenChangePass.setForeground(TEXT_PRIMARY);
        btnOpenChangePass.setFocusPainted(false);
        btnOpenChangePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenChangePass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 65, 65), 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnOpenChangePass.addActionListener(e -> openChangePasswordDialog());
        pForm.add(btnOpenChangePass, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(15, 8, 5, 8);
        btnDeleteAccount = new JButton();
        btnDeleteAccount.setBackground(SURFACE_COLOR);
        btnDeleteAccount.setForeground(DANGER_RED);
        btnDeleteAccount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDeleteAccount.setFocusPainted(false);
        btnDeleteAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteAccount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER_RED, 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnDeleteAccount.addActionListener(e -> deleteAccount());
        btnDeleteAccount.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnDeleteAccount.setBackground(DANGER_RED); btnDeleteAccount.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnDeleteAccount.setBackground(SURFACE_COLOR); btnDeleteAccount.setForeground(DANGER_RED); }
        });
        pForm.add(btnDeleteAccount, gbc);

        profileCard.add(pForm, BorderLayout.CENTER);
        add(profileCard);

        updateResponsiveLayout(isVietnamese, 560);
        refreshData();
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
            btnUpdateProfile.setText("Lưu thay đổi"); btnOpenChangePass.setText("Đổi mật khẩu");
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

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username != null) {
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
    }

    // 🌟 TÍCH HỢP BẮT LỖI LUỒNG THAY ĐỔI THÔNG TIN CÁ NHÂN TRONG CONFIG SETTINGS
    private void updateProfile() {
        String nickname = txtNickname.getText();
        String email = txtEmail.getText();
        String gender = cmbGender.getSelectedIndex() == 0 ? "Male" : (cmbGender.getSelectedIndex() == 1 ? "Female" : "Other");

        try {
            InputValidator.validateNickname(nickname, isVietnamese);
            InputValidator.validateEmail(email, isVietnamese);

            User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
            if (user != null) {
                user.setNickname(nickname.trim()); user.setEmail(email.trim()); user.setGender(gender);
                DatabaseUtil.updateUser(user);
                JOptionPane.showMessageDialog(this, isVietnamese ? "Cập nhật thông tin thành công!" : "Profile updated successfully!");
                if (mainFrame != null) mainFrame.refreshAllPanels();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    isVietnamese ? "Lỗi nhập liệu" : "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 🌟 TÍCH HỢP BẮT LỖI LUỒNG ĐỔI MẬT KHẨU BẢO MẬT JDIALOG
    private void openChangePasswordDialog() {
        JDialog passDialog = new JDialog(mainFrame, isVietnamese ? "Thay đổi mật khẩu" : "Change Password", true);
        passDialog.setSize(400, 320);
        passDialog.setLocationRelativeTo(this);
        passDialog.getContentPane().setBackground(new Color(18, 18, 18));
        passDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBackground(new Color(18, 18, 18));
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 15, 20));

        JLabel lOld = new JLabel(isVietnamese ? "Mật khẩu cũ:" : "Old Password:");
        lOld.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lOld.setForeground(TEXT_SECONDARY);
        JPasswordField txtOldPassword = new JPasswordField(); styleTextField(txtOldPassword);

        JLabel lNew = new JLabel(isVietnamese ? "Mật khẩu mới:" : "New Password:");
        lNew.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lNew.setForeground(TEXT_SECONDARY);
        JPasswordField txtNewPassword = new JPasswordField(); styleTextField(txtNewPassword);

        JLabel lConf = new JLabel(isVietnamese ? "Xác nhận MK:" : "Confirm Pass:");
        lConf.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lConf.setForeground(TEXT_SECONDARY);
        JPasswordField txtConfirmPassword = new JPasswordField(); styleTextField(txtConfirmPassword);

        formPanel.add(lOld); formPanel.add(txtOldPassword);
        formPanel.add(lNew); formPanel.add(txtNewPassword);
        formPanel.add(lConf); formPanel.add(txtConfirmPassword);
        passDialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(new Color(18, 18, 18));

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(SURFACE_COLOR); btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setFocusPainted(false); btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCancel.addActionListener(e -> passDialog.dispose());

        JButton btnConfirm = new JButton(isVietnamese ? "XÁC NHẬN" : "CONFIRM");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(ACCENT_YELLOW); btnConfirm.setForeground(new Color(18, 18, 18));
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
                    DatabaseUtil.updateUser(user);
                    JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Đổi mật khẩu thành công! Hãy đăng nhập lại." : "Password changed! Please re-login.");
                    passDialog.dispose();
                    logout();
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(passDialog, ex.getMessage(),
                        isVietnamese ? "Lỗi cấu trúc" : "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        passDialog.add(btnPanel, BorderLayout.SOUTH);
        passDialog.setVisible(true);
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
        tf.setBackground(INPUT_BG); tf.setForeground(TEXT_PRIMARY); tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW); btn.setForeground(SURFACE_COLOR); btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }
}