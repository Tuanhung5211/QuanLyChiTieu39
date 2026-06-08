package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.UserService;
import com.expensemanager.util.InputValidator;
import com.expensemanager.util.ValidationUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JPanel cards;
    private CardLayout cardLayout;
    private JPanel loginPanel, registerPanel;

    // Đăng nhập
    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;
    private JCheckBox chkShowLoginPwd;
    private JLabel lblForgotPassword;

    // Đăng ký
    private JTextField txtRegUsername, txtRegNickname, txtRegEmail;
    private JPasswordField txtRegPassword;
    private JCheckBox chkShowRegPwd;
    private JComboBox<String> comboGender;

    private boolean isVietnamese = true;

    // Màu sắc (giữ nguyên từ code gốc)
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color BORDER_COLOR = new Color(50, 50, 50);

    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);

    public LoginFrame() {
        setTitle("Money Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 750);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(BG_COLOR);

        createLoginPanel();
        createRegisterPanel();

        cards.add(loginPanel, "login");
        cards.add(registerPanel, "register");

        add(cards);
        cardLayout.show(cards, "login");
    }

    // ==================== MÀN HÌNH ĐĂNG NHẬP ====================
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(BG_COLOR);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBackground(SURFACE_COLOR);
        pForm.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(35, 40, 35, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Avatar
        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 5, 0); pForm.add(lblAvatar, gbc);

        // Title
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ACCENT_YELLOW);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 25, 0); pForm.add(lblTitle, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        // Username
        gbc.gridy = 2; pForm.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridy = 3;
        txtLoginUsername = new JTextField();
        styleTextField(txtLoginUsername);
        ValidationUI.initDefaultBorder(txtLoginUsername);
        ValidationUI.addAutoReset(txtLoginUsername);
        pForm.add(txtLoginUsername, gbc);

        // Password
        gbc.gridy = 4; pForm.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridy = 5;
        txtLoginPassword = new JPasswordField();
        styleTextField(txtLoginPassword);
        ValidationUI.addAutoReset(txtLoginPassword);
        pForm.add(txtLoginPassword, gbc);

        // Checkbox hiện mật khẩu
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 10, 0);
        chkShowLoginPwd = new JCheckBox(isVietnamese ? "Hiển thị mật khẩu" : "Show password");
        chkShowLoginPwd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowLoginPwd.setForeground(TEXT_SECONDARY);
        chkShowLoginPwd.setBackground(SURFACE_COLOR);
        chkShowLoginPwd.setFocusPainted(false);
        chkShowLoginPwd.addActionListener(e -> {
            if (chkShowLoginPwd.isSelected()) txtLoginPassword.setEchoChar((char) 0);
            else txtLoginPassword.setEchoChar('•');
        });
        pForm.add(chkShowLoginPwd, gbc);

        // Quên mật khẩu link
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 15, 0);
        lblForgotPassword = new JLabel(isVietnamese ? "Quên mật khẩu?" : "Forgot password?");
        lblForgotPassword.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblForgotPassword.setForeground(ACCENT_YELLOW);
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });
        pForm.add(lblForgotPassword, gbc);

        // Nút đăng nhập
        gbc.gridy = 8; gbc.insets = new Insets(5, 0, 15, 0);
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        stylePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> login());

        // Thêm key listener để Enter đăng nhập
        txtLoginUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnLogin.doClick();
            }
        });
        txtLoginPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnLogin.doClick();
            }
        });
        pForm.add(btnLogin, gbc);
        getRootPane().setDefaultButton(btnLogin);

        // Link chuyển sang đăng ký
        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink("Chưa có tài khoản? Đăng ký ngay", "register"), gbc);

        loginPanel.add(pForm);
    }

    // ==================== MÀN HÌNH ĐĂNG KÝ ====================
    private void createRegisterPanel() {
        registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBackground(BG_COLOR);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBackground(SURFACE_COLOR);
        pForm.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(25, 40, 25, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 0, 0); pForm.add(lblAvatar, gbc);

        JLabel lblTitle = new JLabel("ĐĂNG KÝ", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ACCENT_YELLOW);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 15, 0); pForm.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 0, 4, 0);

        // Username
        gbc.gridy = 2; pForm.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridy = 3;
        txtRegUsername = new JTextField();
        styleTextField(txtRegUsername);
        ValidationUI.initDefaultBorder(txtRegUsername);
        ValidationUI.addAutoReset(txtRegUsername);
        pForm.add(txtRegUsername, gbc);

        // Password
        gbc.gridy = 4; pForm.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridy = 5;
        txtRegPassword = new JPasswordField();
        styleTextField(txtRegPassword);
        ValidationUI.addAutoReset(txtRegPassword);
        pForm.add(txtRegPassword, gbc);

        // Checkbox hiện mật khẩu
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 10, 0);
        chkShowRegPwd = new JCheckBox(isVietnamese ? "Hiển thị mật khẩu" : "Show password");
        chkShowRegPwd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowRegPwd.setForeground(TEXT_SECONDARY);
        chkShowRegPwd.setBackground(SURFACE_COLOR);
        chkShowRegPwd.setFocusPainted(false);
        chkShowRegPwd.addActionListener(e -> {
            if (chkShowRegPwd.isSelected()) txtRegPassword.setEchoChar((char) 0);
            else txtRegPassword.setEchoChar('•');
        });
        pForm.add(chkShowRegPwd, gbc);

        // Nickname
        gbc.gridy = 7; gbc.insets = new Insets(4, 0, 4, 0);
        pForm.add(createLabel("Tên hiển thị:"), gbc);
        gbc.gridy = 8;
        txtRegNickname = new JTextField();
        styleTextField(txtRegNickname);
        ValidationUI.addAutoReset(txtRegNickname);
        pForm.add(txtRegNickname, gbc);

        // Email
        gbc.gridy = 9; pForm.add(createLabel("Email:"), gbc);
        gbc.gridy = 10;
        txtRegEmail = new JTextField();
        styleTextField(txtRegEmail);
        ValidationUI.addAutoReset(txtRegEmail);
        pForm.add(txtRegEmail, gbc);

        // Giới tính
        gbc.gridy = 11; pForm.add(createLabel("Giới tính:"), gbc);
        gbc.gridy = 12;
        comboGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        styleComboBoxUI(comboGender);
        pForm.add(comboGender, gbc);

        // Nút đăng ký
        gbc.gridy = 13; gbc.insets = new Insets(20, 0, 15, 0);
        JButton btnRegister = new JButton("ĐĂNG KÝ NGAY");
        stylePrimaryButton(btnRegister);
        btnRegister.addActionListener(e -> register());

        // Thêm key listener cho Enter đăng ký
        txtRegUsername.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnRegister.doClick();
            }
        });
        txtRegPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnRegister.doClick();
            }
        });
        pForm.add(btnRegister, gbc);
        getRootPane().setDefaultButton(btnRegister);

        // Link quay lại đăng nhập
        gbc.gridy = 14; gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink("Quay lại Đăng nhập", "login"), gbc);

        registerPanel.add(pForm);
    }

    // ==================== CÁC PHƯƠNG THỨC HELPER ====================
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private JLabel createLink(String text, String targetTab) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { cardLayout.show(cards, targetTab); }
            @Override public void mouseEntered(MouseEvent e) { lbl.setForeground(ACCENT_YELLOW); }
            @Override public void mouseExited(MouseEvent e) { lbl.setForeground(TEXT_PRIMARY); }
        });
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(FONT_INPUT);
        tf.setPreferredSize(new Dimension(300, 42));
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(0, 12, 0, 12)));
    }

    private void styleComboBoxUI(JComboBox<?> combo) {
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_INPUT);
        combo.setPreferredSize(new Dimension(300, 42));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBackground(index < 0 ? INPUT_BG : (isSelected ? ACCENT_YELLOW : SURFACE_COLOR));
                l.setForeground(isSelected ? BG_COLOR : TEXT_PRIMARY);
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        combo.setUI(new BasicComboBoxUI() {
            @Override protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox);
                popup.setBorder(new LineBorder(BORDER_COLOR, 1));
                return popup;
            }
            @Override protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setBackground(INPUT_BG);
                btn.setBorder(new EmptyBorder(0, 5, 0, 5));
                btn.setForeground(TEXT_PRIMARY);
                return btn;
            }
        });
        combo.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(0, 0, 0, 0)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW);
        btn.setForeground(BG_COLOR);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setBorder(new LineBorder(ACCENT_YELLOW, 1, true));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 170, 0)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT_YELLOW); }
        });
    }

    // ==================== LOGIC XỬ LÝ ====================
    private void login() {
        ValidationUI.resetBorder(txtLoginUsername);
        ValidationUI.resetBorder(txtLoginPassword);

        String username = txtLoginUsername.getText();
        String password = new String(txtLoginPassword.getPassword());

        try {
            InputValidator.validateLogin(username, password, isVietnamese);
            User user = UserService.login(username.trim(), password.trim());
            if (user != null) {
                new MainFrame().setVisible(true);
                dispose();
            } else {
                ValidationUI.setErrorBorder(txtLoginUsername);
                ValidationUI.setErrorBorder(txtLoginPassword);
                JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            if (username.trim().isEmpty()) ValidationUI.setErrorBorder(txtLoginUsername);
            if (password.isEmpty()) ValidationUI.setErrorBorder(txtLoginPassword);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(this, msg, "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==================== QUÊN MẬT KHẨU ====================
    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, isVietnamese ? "Khôi phục mật khẩu" : "Forgot Password", true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblEmail = new JLabel(isVietnamese ? "Nhập email đã đăng ký:" : "Enter your registered email:");
        lblEmail.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblEmail, gbc);

        JTextField txtEmail = new JTextField(20);
        txtEmail.setEditable(true);
        txtEmail.setEnabled(true);
        txtEmail.setBackground(INPUT_BG);
        txtEmail.setForeground(TEXT_PRIMARY);
        txtEmail.setCaretColor(ACCENT_YELLOW);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        // Border mặc định
        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        txtEmail.setBorder(defaultBorder);
        txtEmail.setPreferredSize(new Dimension(250, 38));
        gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(txtEmail, gbc);

        // Tự động xóa viền đỏ khi gõ hoặc focus
        txtEmail.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void reset() { txtEmail.setBorder(defaultBorder); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reset(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reset(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reset(); }
        });
        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtEmail.setBorder(defaultBorder);
            }
        });

        // Nút gửi
        JButton btnSend = new JButton(isVietnamese ? "Gửi mật khẩu mới" : "Send new password");
        btnSend.setBackground(ACCENT_YELLOW);
        btnSend.setForeground(BG_COLOR);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnSend, gbc);

        // Thêm key listener để nhấn Enter gửi (phải đặt sau khi tạo btnSend)
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSend.doClick(); // Kích hoạt nút gửi
                }
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> txtEmail.requestFocusInWindow());

        // Xử lý sự kiện nút gửi
        btnSend.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
                JOptionPane.showMessageDialog(dialog,
                        isVietnamese ? "Vui lòng nhập email!" : "Please enter email!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                txtEmail.requestFocus();
                return;
            }
            // Reset border
            txtEmail.setBorder(defaultBorder);

            // TODO: Gọi service gửi email (thay bằng logic thật khi có EmailService)
            // boolean sent = UserService.resetPasswordByEmail(email);
            boolean sent = UserService.resetPasswordByEmail(email);

            if (sent) {
                JOptionPane.showMessageDialog(dialog,
                        isVietnamese ? "Mật khẩu mới đã được gửi vào email của bạn!" : "New password sent!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
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