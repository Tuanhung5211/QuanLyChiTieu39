package com.expensemanager.ui;

import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;
import com.expensemanager.util.ConfigLocalStorage;
import com.expensemanager.util.InputValidator;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.util.ValidationUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JPanel cards;
    private CardLayout cardLayout;
    private JPanel loginPanel, registerPanel;

    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;
    private JCheckBox chkShowLoginPwd;
    private JLabel lblForgotPassword;

    private JTextField txtRegUsername, txtRegNickname, txtRegEmail;
    private JPasswordField txtRegPassword;
    private JCheckBox chkShowRegPwd;
    private JComboBox<String> comboGender;

    private boolean isVietnamese;
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);

    public LoginFrame() {
        // 👉 Load ngôn ngữ từ cấu hình đã lưu (mặc định là tiếng Việt)
        this.isVietnamese = ConfigLocalStorage.loadLanguage();

        setTitle("Money Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 750);
        setResizable(false);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        createLoginPanel();
        createRegisterPanel();

        cards.add(loginPanel, "login");
        cards.add(registerPanel, "register");

        add(cards);
        cardLayout.show(cards, "login");
        applyTheme();
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBorder(new CompoundBorder(
                new LineBorder(ThemeManager.getColor("border"), 1, true),
                new EmptyBorder(35, 40, 35, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
        lblAvatar.setForeground(ThemeManager.getColor("textPrimary"));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 5, 0); pForm.add(lblAvatar, gbc);

        JLabel lblTitle = new JLabel(isVietnamese ? "ĐĂNG NHẬP" : "LOGIN", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ThemeManager.getColor("accent"));
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 25, 0); pForm.add(lblTitle, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 2; pForm.add(createLabel(isVietnamese ? "Tên đăng nhập:" : "Username:"), gbc);
        gbc.gridy = 3;
        txtLoginUsername = new JTextField();
        styleTextField(txtLoginUsername);
        ValidationUI.initDefaultBorder(txtLoginUsername);
        ValidationUI.addAutoReset(txtLoginUsername);
        pForm.add(txtLoginUsername, gbc);

        gbc.gridy = 4;
        pForm.add(createLabel(isVietnamese ? "Mật khẩu:" : "Password:"), gbc);
        gbc.gridy = 5;
        txtLoginPassword = new JPasswordField();
        styleTextField(txtLoginPassword);
        ValidationUI.addAutoReset(txtLoginPassword);
        pForm.add(txtLoginPassword, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 10, 0);
        chkShowLoginPwd = new JCheckBox(isVietnamese ? "Hiển thị mật khẩu" : "Show password");
        chkShowLoginPwd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowLoginPwd.setForeground(ThemeManager.getColor("textSecondary"));
        chkShowLoginPwd.setFocusPainted(false);
        chkShowLoginPwd.setOpaque(false); // Sửa lỗi lốm đốm màu nền
        chkShowLoginPwd.addActionListener(e -> {
            if (chkShowLoginPwd.isSelected()) txtLoginPassword.setEchoChar((char) 0);
            else txtLoginPassword.setEchoChar('•');
        });
        pForm.add(chkShowLoginPwd, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 15, 0);
        lblForgotPassword = new JLabel(isVietnamese ? "Quên mật khẩu?" : "Forgot password?");
        lblForgotPassword.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblForgotPassword.setForeground(ThemeManager.getColor("accent"));
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });
        pForm.add(lblForgotPassword, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 15, 0);
        JButton btnLogin = new JButton(isVietnamese ? "ĐĂNG NHẬP" : "LOGIN");
        stylePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> login());
        pForm.add(btnLogin, gbc);
        getRootPane().setDefaultButton(btnLogin);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink(isVietnamese ? "Chưa có tài khoản? Đăng ký ngay" : "Don't have an account? Register now", "register"), gbc);

        loginPanel.add(pForm);
    }

    private void createRegisterPanel() {
        registerPanel = new JPanel(new GridBagLayout());

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBorder(new CompoundBorder(
                new LineBorder(ThemeManager.getColor("border"), 1, true),
                new EmptyBorder(25, 40, 25, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblAvatar.setForeground(ThemeManager.getColor("textPrimary"));
        gbc.gridy = 0; pForm.add(lblAvatar, gbc);

        JLabel lblTitle = new JLabel(isVietnamese ? "ĐĂNG KÝ" : "REGISTER", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ThemeManager.getColor("accent"));
        gbc.gridy = 1; pForm.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridy = 2; pForm.add(createLabel(isVietnamese ? "Tên đăng nhập:" : "Username:"), gbc);
        gbc.gridy = 3;
        txtRegUsername = new JTextField();
        styleTextField(txtRegUsername);
        ValidationUI.addAutoReset(txtRegUsername);
        pForm.add(txtRegUsername, gbc);

        gbc.gridy = 4; pForm.add(createLabel(isVietnamese ? "Mật khẩu:" : "Password:"), gbc);
        gbc.gridy = 5;
        txtRegPassword = new JPasswordField();
        styleTextField(txtRegPassword);
        ValidationUI.addAutoReset(txtRegPassword);
        pForm.add(txtRegPassword, gbc);

        gbc.gridy = 6;
        chkShowRegPwd = new JCheckBox(isVietnamese ? "Hiển thị mật khẩu" : "Show password");
        chkShowRegPwd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowRegPwd.setForeground(ThemeManager.getColor("textSecondary"));
        chkShowRegPwd.setFocusPainted(false);
        chkShowRegPwd.setOpaque(false); // Sửa lỗi lốm đốm màu nền
        chkShowRegPwd.addActionListener(e -> {
            if (chkShowRegPwd.isSelected()) txtRegPassword.setEchoChar((char) 0);
            else txtRegPassword.setEchoChar('•');
        });
        pForm.add(chkShowRegPwd, gbc);

        gbc.gridy = 7; pForm.add(createLabel(isVietnamese ? "Tên hiển thị:" : "Nickname:"), gbc);
        gbc.gridy = 8;
        txtRegNickname = new JTextField();
        styleTextField(txtRegNickname);
        ValidationUI.addAutoReset(txtRegNickname);
        pForm.add(txtRegNickname, gbc);

        gbc.gridy = 9; pForm.add(createLabel("Email:"), gbc);
        gbc.gridy = 10;
        txtRegEmail = new JTextField();
        styleTextField(txtRegEmail);
        ValidationUI.addAutoReset(txtRegEmail);
        pForm.add(txtRegEmail, gbc);

        gbc.gridy = 11; pForm.add(createLabel(isVietnamese ? "Giới tính:" : "Gender:"), gbc);
        gbc.gridy = 12;
        comboGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        styleComboBox(comboGender);
        pForm.add(comboGender, gbc);

        gbc.gridy = 13;
        gbc.insets = new Insets(20, 0, 15, 0);
        JButton btnRegister = new JButton(isVietnamese ? "ĐĂNG KÝ NGAY" : "REGISTER NOW");
        stylePrimaryButton(btnRegister);
        btnRegister.addActionListener(e -> register());
        pForm.add(btnRegister, gbc);

        gbc.gridy = 14;
        gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink(isVietnamese ? "Quay lại Đăng nhập" : "Back to Login", "login"), gbc);

        registerPanel.add(pForm);
    }

    public void applyTheme() {
        cards.setBackground(ThemeManager.getColor("bg"));
        applyThemeToPanel(loginPanel);
        applyThemeToPanel(registerPanel);
    }

    private void applyThemeToPanel(JPanel panel) {
        if (panel == null) return;
        panel.setBackground(ThemeManager.getColor("bg"));
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel pForm = (JPanel) comp;
                pForm.setBackground(ThemeManager.getColor("surface"));
                pForm.setBorder(new CompoundBorder(
                        new LineBorder(ThemeManager.getColor("border"), 1, true),
                        ((CompoundBorder) pForm.getBorder()).getInsideBorder()
                ));
                for (Component inner : pForm.getComponents()) {
                    if (inner instanceof JLabel) {
                        JLabel lbl = (JLabel) inner;
                        if (lbl.getText().equals("Quên mật khẩu?") || lbl.getText().equals("Forgot password?")) {
                            lbl.setForeground(ThemeManager.getColor("accent"));
                        } else {
                            lbl.setForeground(ThemeManager.getColor("textSecondary"));
                        }
                    } else if (inner instanceof JTextField || inner instanceof JPasswordField) {
                        JTextField tf = (JTextField) inner;
                        tf.setBackground(ThemeManager.getColor("input"));
                        tf.setForeground(ThemeManager.getColor("textPrimary"));
                        tf.setCaretColor(ThemeManager.getColor("accent"));
                        // Cập nhật lại viền để đồng bộ màu border mới
                        tf.setBorder(new CompoundBorder(new LineBorder(ThemeManager.getColor("border"), 1, true), new EmptyBorder(0, 12, 0, 12)));
                    } else if (inner instanceof JCheckBox) {
                        JCheckBox cb = (JCheckBox) inner;
                        cb.setForeground(ThemeManager.getColor("textSecondary"));
                        cb.setBackground(ThemeManager.getColor("surface"));
                        cb.setOpaque(false); // Sửa lỗi viền trắng/đen
                    } else if (inner instanceof JButton) {
                        JButton btn = (JButton) inner;
                        if (btn.getText().contains("ĐĂNG") || btn.getText().contains("LOGIN") || btn.getText().contains("REGISTER")) {
                            btn.setBackground(ThemeManager.getColor("accent"));
                            btn.setForeground(ThemeManager.getColor("bg"));
                            btn.setBorder(new LineBorder(ThemeManager.getColor("accent"), 1, true)); // Cập nhật viền nút
                        }
                    } else if (inner instanceof JComboBox) {
                        JComboBox<?> combo = (JComboBox<?>) inner;
                        combo.setBackground(ThemeManager.getColor("input"));
                        combo.setForeground(ThemeManager.getColor("textPrimary"));
                        combo.setBorder(new CompoundBorder(new LineBorder(ThemeManager.getColor("border"), 1, true), new EmptyBorder(0, 0, 0, 0)));
                    }
                }
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ThemeManager.getColor("textSecondary"));
        return lbl;
    }

    private JLabel createLink(String text, String targetTab) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ThemeManager.getColor("textPrimary"));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cards, targetTab);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lbl.setForeground(ThemeManager.getColor("accent"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lbl.setForeground(ThemeManager.getColor("textPrimary"));
            }
        });
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(FONT_INPUT);
        tf.setPreferredSize(new Dimension(300, 42));
        tf.setBorder(new CompoundBorder(new LineBorder(ThemeManager.getColor("border"), 1, true), new EmptyBorder(0, 12, 0, 12)));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(ThemeManager.getColor("input"));
        combo.setForeground(ThemeManager.getColor("textPrimary"));
        combo.setFont(FONT_INPUT);
        combo.setPreferredSize(new Dimension(300, 42));
        combo.setBorder(new CompoundBorder(new LineBorder(ThemeManager.getColor("border"), 1, true), new EmptyBorder(0, 0, 0, 0)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ThemeManager.getColor("accent"));
        btn.setForeground(ThemeManager.getColor("bg"));
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setBorder(new LineBorder(ThemeManager.getColor("accent"), 1, true));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeManager.getColor("accent").darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ThemeManager.getColor("accent"));
            }
        });
    }

    private void login() {
        ValidationUI.resetBorder(txtLoginUsername);
        ValidationUI.resetBorder(txtLoginPassword);
        String username = txtLoginUsername.getText();
        String password = new String(txtLoginPassword.getPassword());

        try {
            InputValidator.validateLogin(username, password, isVietnamese);
            User user = UserService.login(username.trim(), password.trim());
            if (user != null) {
                SessionManager.login(user.getId(), user.getUsername(), user.isAdmin());
                new MainFrame().setVisible(true);
                dispose();
            } else {
                ValidationUI.setErrorBorder(txtLoginUsername);
                ValidationUI.setErrorBorder(txtLoginPassword);
                JOptionPane.showMessageDialog(this, isVietnamese ? "Sai tên đăng nhập hoặc mật khẩu!" : "Incorrect username or password!", isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            if (username.trim().isEmpty()) ValidationUI.setErrorBorder(txtLoginUsername);
            if (password.isEmpty()) ValidationUI.setErrorBorder(txtLoginPassword);
            JOptionPane.showMessageDialog(this, ex.getMessage(), isVietnamese ? "Lỗi" : "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void register() {
        ValidationUI.resetBorder(txtRegUsername);
        ValidationUI.resetBorder(txtRegPassword);
        ValidationUI.resetBorder(txtRegNickname);
        ValidationUI.resetBorder(txtRegEmail);

        String username = txtRegUsername.getText();
        String password = new String(txtRegPassword.getPassword());
        String nickname = txtRegNickname.getText();
        String email = txtRegEmail.getText();
        String gender = (String) comboGender.getSelectedItem();

        try {
            InputValidator.validateRegister(username, password, password, email, nickname, isVietnamese);
            User user = UserService.register(username.trim(), password.trim(), nickname.trim(), email.trim(), gender);

            if (user == null) {
                ValidationUI.setErrorBorder(txtRegUsername);
                JOptionPane.showMessageDialog(this, isVietnamese ? "Tên đăng nhập đã tồn tại!" : "Username already exists!", isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Đăng ký thành công!" : "Registration successful!", isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
                txtLoginUsername.setText(username.trim());
                txtLoginPassword.setText("");
                if (chkShowLoginPwd != null) chkShowLoginPwd.setSelected(false);
                cardLayout.show(cards, "login");
            }
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.contains("Tên đăng nhập") || msg.contains("Username")) ValidationUI.setErrorBorder(txtRegUsername);
            if (msg.contains("Mật khẩu") || msg.contains("Password")) ValidationUI.setErrorBorder(txtRegPassword);
            if (msg.contains("Tên hiển thị") || msg.contains("Nickname")) ValidationUI.setErrorBorder(txtRegNickname);
            if (msg.contains("Email")) ValidationUI.setErrorBorder(txtRegEmail);
            JOptionPane.showMessageDialog(this, msg, isVietnamese ? "Lỗi nhập liệu" : "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, isVietnamese ? "Khôi phục mật khẩu" : "Forgot Password", true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ThemeManager.getColor("bg"));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.getColor("bg"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblEmail = new JLabel(isVietnamese ? "Nhập email đã đăng ký:" : "Enter your registered email:");
        lblEmail.setForeground(ThemeManager.getColor("textPrimary"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblEmail, gbc);

        JTextField txtEmail = new JTextField(20);
        txtEmail.setBackground(ThemeManager.getColor("input"));
        txtEmail.setForeground(ThemeManager.getColor("textPrimary"));
        txtEmail.setCaretColor(ThemeManager.getColor("accent"));
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        txtEmail.setBorder(defaultBorder);
        txtEmail.setPreferredSize(new Dimension(250, 38));
        gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(txtEmail, gbc);

        txtEmail.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void reset() { txtEmail.setBorder(defaultBorder); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reset(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reset(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reset(); }
        });
        txtEmail.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { txtEmail.setBorder(defaultBorder); }
        });

        JButton btnSend = new JButton(isVietnamese ? "Gửi mật khẩu mới" : "Send new password");
        btnSend.setBackground(ThemeManager.getColor("accent"));
        btnSend.setForeground(ThemeManager.getColor("bg"));
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnSend, gbc);

        txtEmail.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnSend.doClick();
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> txtEmail.requestFocusInWindow());

        btnSend.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            javax.swing.border.Border errorBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getColor("danger"), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            );

            if (email.isEmpty()) {
                txtEmail.setBorder(errorBorder);
                JOptionPane.showMessageDialog(dialog,
                        isVietnamese ? "Vui lòng nhập email!" : "Please enter email!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return;
            }

            txtEmail.setBorder(defaultBorder);

            boolean sent = UserService.resetPasswordByEmail(email);
            if (sent) {
                JOptionPane.showMessageDialog(dialog,
                        isVietnamese ? "Mật khẩu mới đã được gửi vào email của bạn!" : "New password sent!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                txtEmail.setBorder(errorBorder);
                JOptionPane.showMessageDialog(dialog,
                        isVietnamese ? "Email không tồn tại hoặc lỗi gửi!" : "Email not found or send error!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtEmail.requestFocus();
            }
        });

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}