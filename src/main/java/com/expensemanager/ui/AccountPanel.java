package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountPanel extends JPanel implements Observer {
    private MainFrame mainFrame;
    private boolean isVietnamese = true;

    private JLabel lblAvatar, lblNickname;
    private JLabel lblIdLabel, lblIdValue;
    private JLabel lblEmailLabel, lblEmailValue;
    private JLabel lblGenderLabel, lblGenderValue;
    private JButton btnLogout;

    private final Color SIDEBAR_BG = new Color(30, 30, 30);
    private final Color AVATAR_BG = new Color(45, 45, 45);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);

    public AccountPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setPreferredSize(new Dimension(240, 0));
        setBackground(SIDEBAR_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(30, 20, 20, 20));

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(AVATAR_BG);
        lblAvatar.setPreferredSize(new Dimension(55, 55));
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true));
        avatarRow.add(lblAvatar);

        lblNickname = new JLabel("User");
        lblNickname.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNickname.setForeground(TEXT_PRIMARY);
        lblNickname.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        avatarRow.add(lblNickname);
        topContainer.add(avatarRow);

        topContainer.add(Box.createVerticalStrut(20));

        lblIdLabel = new JLabel("ID:"); lblIdValue = new JLabel("---");
        lblEmailLabel = new JLabel("Email:"); lblEmailValue = new JLabel("---");
        lblGenderLabel = new JLabel("Giới tính:"); lblGenderValue = new JLabel("---");

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(createProfileRow(lblIdLabel, lblIdValue));
        infoPanel.add(createProfileRow(lblEmailLabel, lblEmailValue));
        infoPanel.add(createProfileRow(lblGenderLabel, lblGenderValue));
        topContainer.add(infoPanel);

        topContainer.add(Box.createVerticalGlue());
        add(topContainer, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(15, 15, 20, 15));

        btnLogout = new JButton();
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(new Color(45, 45, 45));
        btnLogout.setForeground(TEXT_PRIMARY);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        btnLogout.addActionListener(e -> logout());
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(DANGER_RED); btnLogout.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(45, 45, 45)); btnLogout.setForeground(TEXT_PRIMARY); }
        });
        bottomContainer.add(btnLogout, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        updateLanguageText();
        refreshData();
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        updateLanguageText();
    }

    private void updateLanguageText() {
        if (isVietnamese) {
            lblIdLabel.setText("ID:"); lblEmailLabel.setText("Email:");
            lblGenderLabel.setText("Giới tính:"); btnLogout.setText("Đăng xuất");
        } else {
            lblIdLabel.setText("ID:"); lblEmailLabel.setText("Email:");
            lblGenderLabel.setText("Gender:"); btnLogout.setText("Logout");
        }
        refreshData();
    }

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username != null) {
            User user = DatabaseUtil.getUserByUsername(username);
            if (user != null) {
                lblNickname.setText(user.getNickname());
                // 🌟 ĐÃ SỬA: Thay thế chuỗi mã sinh viên chạy thử thành chuỗi an toàn hệ thống "N/A"
                lblIdValue.setText(user.getId() != null ? user.getId() : "N/A");

                String email = user.getEmail();
                if (email != null && email.length() > 18) email = email.substring(0, 16) + "...";
                lblEmailValue.setText(email != null ? email : "---");

                String gender = user.getGender();
                if (isVietnamese) {
                    if ("Male".equalsIgnoreCase(gender) || "Nam".equalsIgnoreCase(gender)) lblGenderValue.setText("Nam");
                    else if ("Female".equalsIgnoreCase(gender) || "Nữ".equalsIgnoreCase(gender)) lblGenderValue.setText("Nữ");
                    else lblGenderValue.setText("Khác");
                } else {
                    if ("Male".equalsIgnoreCase(gender) || "Nam".equalsIgnoreCase(gender)) lblGenderValue.setText("Male");
                    else if ("Female".equalsIgnoreCase(gender) || "Nữ".equalsIgnoreCase(gender)) lblGenderValue.setText("Female");
                    else lblGenderValue.setText("Other");
                }

                if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                    lblAvatar.setText(user.getNickname().substring(0, 1).toUpperCase());
                }
            }
        }
    }

    private JPanel createProfileRow(JLabel lblLabel, JLabel lblValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setForeground(TEXT_SECONDARY);
        lblLabel.setPreferredSize(new Dimension(70, 22));
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(TEXT_PRIMARY);
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    private void logout() {
        SessionManager.logout();
        if (mainFrame != null) mainFrame.dispose();
        new LoginFrame().setVisible(true);
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(() -> refreshData());
        }
    }
}