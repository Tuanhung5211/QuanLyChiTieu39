package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.UserService;
import com.expensemanager.util.InputValidator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

public class LoginFrame extends JFrame {
    private JPanel cards;
    private CardLayout cardLayout;
    private JPanel loginPanel, registerPanel;

    // --- Components cho Đăng nhập ---
    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    // --- Components cho Đăng ký ---
    private JTextField txtRegUsername, txtRegNickname, txtRegEmail;
    private JPasswordField txtRegPassword;
    private JComboBox<String> comboGender;

    private boolean isVietnamese = true;

    // --- Hệ màu sắc phẳng (Flat Dark Mode) ---
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

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 5, 0); pForm.add(lblAvatar, gbc);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ACCENT_YELLOW);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 25, 0); pForm.add(lblTitle, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 2; pForm.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridy = 3; txtLoginUsername = new JTextField(); styleTextField(txtLoginUsername); pForm.add(txtLoginUsername, gbc);

        gbc.gridy = 4; pForm.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridy = 5; txtLoginPassword = new JPasswordField(); styleTextField(txtLoginPassword); pForm.add(txtLoginPassword, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(25, 0, 15, 0);
        JButton btnLogin = new JButton("ĐĂNG NHẬP"); stylePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> login());
        pForm.add(btnLogin, gbc);

        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink("Chưa có tài khoản? Đăng ký ngay", "register"), gbc);

        loginPanel.add(pForm);
    }

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

        gbc.gridy = 2; pForm.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridy = 3; txtRegUsername = new JTextField(); styleTextField(txtRegUsername); pForm.add(txtRegUsername, gbc);

        gbc.gridy = 4; pForm.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridy = 5; txtRegPassword = new JPasswordField(); styleTextField(txtRegPassword); pForm.add(txtRegPassword, gbc);

        gbc.gridy = 6; pForm.add(createLabel("Tên hiển thị:"), gbc);
        gbc.gridy = 7; txtRegNickname = new JTextField(); styleTextField(txtRegNickname); pForm.add(txtRegNickname, gbc);

        gbc.gridy = 8; pForm.add(createLabel("Email:"), gbc);
        gbc.gridy = 9; txtRegEmail = new JTextField(); styleTextField(txtRegEmail); pForm.add(txtRegEmail, gbc);

        gbc.gridy = 10; pForm.add(createLabel("Giới tính:"), gbc);
        gbc.gridy = 11;
        comboGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        styleComboBoxUI(comboGender);
        pForm.add(comboGender, gbc);

        gbc.gridy = 12; gbc.insets = new Insets(20, 0, 15, 0);
        JButton btnRegister = new JButton("ĐĂNG KÝ NGAY"); stylePrimaryButton(btnRegister);
        btnRegister.addActionListener(e -> register());
        pForm.add(btnRegister, gbc);

        gbc.gridy = 13; gbc.insets = new Insets(0, 0, 0, 0);
        pForm.add(createLink("Quay lại Đăng nhập", "login"), gbc);

        registerPanel.add(pForm);
    }

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
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
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

        combo.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW);
        btn.setForeground(new Color(18, 18, 18));
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setBorder(new LineBorder(ACCENT_YELLOW, 1, true));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 170, 0)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT_YELLOW); }
        });
    }

    // 🌟 KHẮC PHỤC TRIỆT ĐỂ LUỒNG ĐĂNG NHẬP CHỐNG ĐỂ TRỐNG DỮ LIỆU
    private void login() {
        String username = txtLoginUsername.getText();
        String password = new String(txtLoginPassword.getPassword());

        try {
            InputValidator.validateLogin(username, password, isVietnamese);

            User user = UserService.login(username.trim(), password.trim());
            if (user != null) {
                new MainFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi đăng nhập: Tên người dùng hoặc mật khẩu không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 🌟 KHẮC PHỤC TRIỆT ĐỂ LUỒNG ĐĂNG KÝ HỘI VIÊN TOÀN DIỆN
    private void register() {
        String username = txtRegUsername.getText();
        String password = new String(txtRegPassword.getPassword());
        String nickname = txtRegNickname.getText();
        String email = txtRegEmail.getText();
        String gender = (String) comboGender.getSelectedItem();

        try {
            // Do form UI gộp chung, ta truyền password vào cả 2 tham số để bỏ qua kiểm tra khớp pass, tập trung quét format
            InputValidator.validateRegister(username, password, password, email, nickname, isVietnamese);

            User user = UserService.register(username.trim(), password.trim(), nickname.trim(), email.trim(), gender);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Lỗi đăng ký: Tên đăng nhập đã tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký" + nickname.trim() + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                txtLoginUsername.setText(username.trim());
                txtLoginPassword.setText("");
                cardLayout.show(cards, "login");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}