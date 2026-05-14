package com.expensemanager.ui;

import com.expensemanager.service.UserService;
import com.expensemanager.entity.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtNickname;      // dùng khi đăng ký
    private JTextField txtEmail;         // <-- Thêm email
    private JComboBox<String> cmbGender; // <-- Thêm gender
    private JTabbedPane tabbedPane;

    public LoginFrame() {
        setTitle("Đăng nhập - Quản lý chi tiêu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // ---------- Panel Đăng nhập ----------
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        txtUsername = new JTextField(15);
        loginPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtPassword = new JPasswordField(15);
        loginPanel.add(txtPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.addActionListener(e -> login());
        loginPanel.add(btnLogin, gbc);

        tabbedPane.addTab("Đăng nhập", loginPanel);

        // ---------- Panel Đăng ký ----------
        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Tên đăng nhập
        gbc.gridx = 0; gbc.gridy = 0;
        registerPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1;
        JTextField txtRegUsername = new JTextField(15);
        registerPanel.add(txtRegUsername, gbc);

        // Mật khẩu
        gbc.gridx = 0; gbc.gridy = 1;
        registerPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtRegPassword = new JPasswordField(15);
        registerPanel.add(txtRegPassword, gbc);

        // Nickname
        gbc.gridx = 0; gbc.gridy = 2;
        registerPanel.add(new JLabel("Nickname:"), gbc);
        gbc.gridx = 1;
        txtNickname = new JTextField(15);
        registerPanel.add(txtNickname, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        registerPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        registerPanel.add(txtEmail, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy = 4;
        registerPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        registerPanel.add(cmbGender, gbc);

        // Nút Đăng ký
        gbc.gridx = 1; gbc.gridy = 5;
        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.addActionListener(e -> {
            String username = txtRegUsername.getText().trim();
            String password = new String(txtRegPassword.getPassword());
            String nickname = txtNickname.getText().trim();
            String email = txtEmail.getText().trim();
            String gender = (String) cmbGender.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            // Gọi register với 5 tham số
            User user = UserService.register(username, password, nickname, email, gender);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!");
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công! Hãy đăng nhập.");
                tabbedPane.setSelectedIndex(0);
                txtUsername.setText(username);
            }
        });
        registerPanel.add(btnRegister, gbc);

        tabbedPane.addTab("Đăng ký", registerPanel);

        add(tabbedPane);
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }
        User user = UserService.login(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!");
        } else {
            // Mở MainFrame và đóng LoginFrame
            new MainFrame().setVisible(true);
            dispose();
        }
    }
}