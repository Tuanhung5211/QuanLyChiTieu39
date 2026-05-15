package com.expensemanager.ui;

import com.expensemanager.service.UserService;
import com.expensemanager.entity.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JTextField txtNickname;
    private JTextField txtEmail;
    private JComboBox<String> cmbGender;

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton btnTabLogin;
    private JButton btnTabRegister;

    // Bộ màu đồng bộ với MainFrame
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    public LoginFrame() {
        setTitle("Money Tracker - TheTays Team");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // --- HEADER (Logo & Toggle Tabs) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        JLabel lblLogo = new JLabel("💸 Money Tracker", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
        lblLogo.setForeground(ACCENT_YELLOW);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblLogo);
        headerPanel.add(Box.createVerticalStrut(25));

        // Nút chuyển Tab Đăng nhập / Đăng ký
        JPanel tabTogglePanel = new JPanel(new GridLayout(1, 2));
        tabTogglePanel.setBackground(BG_COLOR);
        tabTogglePanel.setMaximumSize(new Dimension(350, 40));

        btnTabLogin = createTabButton("Đăng nhập", true);
        btnTabRegister = createTabButton("Đăng ký", false);

        btnTabLogin.addActionListener(e -> switchTab("login"));
        btnTabRegister.addActionListener(e -> switchTab("register"));

        tabTogglePanel.add(btnTabLogin);
        tabTogglePanel.add(btnTabRegister);
        headerPanel.add(tabTogglePanel);

        add(headerPanel, BorderLayout.NORTH);

        // --- BODY (CardLayout chứa 2 form) ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_COLOR);

        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createRegisterPanel(), "register");

        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Tên đăng nhập:"), gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField();
        styleTextField(txtUsername);
        panel.add(txtUsername, gbc);

        gbc.gridy = 2;
        panel.add(createLabel("Mật khẩu:"), gbc);

        gbc.gridy = 3;
        txtPassword = new JPasswordField();
        styleTextField(txtPassword);
        panel.add(txtPassword, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(40, 30, 10, 30);
        JButton btnLogin = new JButton("Đăng nhập");
        stylePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin, gbc);

        // Đẩy các component lên trên cùng
        gbc.gridy = 5; gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);
        gbc.weightx = 1.0;

        // Cột 1: Labels | Cột 2: Inputs
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(createLabel("Tài khoản:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtRegUsername = new JTextField();
        styleTextField(txtRegUsername);
        panel.add(txtRegUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtRegPassword = new JPasswordField();
        styleTextField(txtRegPassword);
        panel.add(txtRegPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createLabel("Nickname:"), gbc);
        gbc.gridx = 1;
        txtNickname = new JTextField();
        styleTextField(txtNickname);
        panel.add(txtNickname, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField();
        styleTextField(txtEmail);
        panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(createLabel("Giới tính:"), gbc);
        gbc.gridx = 1;
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbGender.setBackground(INPUT_BG);
        cmbGender.setForeground(TEXT_PRIMARY);
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cmbGender.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        panel.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 30, 10, 30);
        JButton btnRegister = new JButton("Tạo tài khoản");
        stylePrimaryButton(btnRegister);
        btnRegister.addActionListener(e -> register());
        panel.add(btnRegister, gbc);

        gbc.gridy = 6; gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    // ================== LOGIC XỬ LÝ ==================

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User user = UserService.login(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            new MainFrame().setVisible(true);
            dispose();
        }
    }

    private void register() {
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword());
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = UserService.register(username, password, nickname, email, gender);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtUsername.setText(username);
            txtPassword.setText("");
            switchTab("login");
        }
    }

    private void switchTab(String tabName) {
        cardLayout.show(cardPanel, tabName);
        if (tabName.equals("login")) {
            updateTabStyle(btnTabLogin, true);
            updateTabStyle(btnTabRegister, false);
        } else {
            updateTabStyle(btnTabLogin, false);
            updateTabStyle(btnTabRegister, true);
        }
    }

    // ================== HELPER TẠO GIAO DIỆN ==================

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(150, 150, 150));
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(255, 205, 50)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT_YELLOW); }
        });
    }

    private JButton createTabButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateTabStyle(btn, isActive);
        return btn;
    }

    private void updateTabStyle(JButton btn, boolean isActive) {
        if (isActive) {
            btn.setForeground(ACCENT_YELLOW);
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT_YELLOW));
        } else {
            btn.setForeground(new Color(120, 120, 120));
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
        }
    }
}