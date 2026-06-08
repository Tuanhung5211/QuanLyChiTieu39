package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.ThemeManager;

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

    public AccountPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setPreferredSize(new Dimension(240, 0));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getColor("border")));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(30, 20, 20, 20));

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("A", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAvatar.setOpaque(true);
        lblAvatar.setPreferredSize(new Dimension(55, 55));
        lblAvatar.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        avatarRow.add(lblAvatar);

        lblNickname = new JLabel("User");
        lblNickname.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNickname.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        avatarRow.add(lblNickname);
        topContainer.add(avatarRow);

        topContainer.add(Box.createVerticalStrut(15));

        lblIdLabel = new JLabel("ID:"); lblIdValue = new JLabel("---");
        lblEmailLabel = new JLabel("Email:"); lblEmailValue = new JLabel("---");
        lblGenderLabel = new JLabel("Giới tính:"); lblGenderValue = new JLabel("---");

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(createProfileRow(lblIdLabel, lblIdValue));
        infoPanel.add(createProfileRow(lblEmailLabel, lblEmailValue));
        infoPanel.add(createProfileRow(lblGenderLabel, lblGenderValue));

        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, infoPanel.getPreferredSize().height));
        topContainer.add(infoPanel);
        topContainer.add(Box.createVerticalGlue());
        add(topContainer, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(15, 15, 20, 15));

        btnLogout = new JButton();
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        btnLogout.addActionListener(e -> logout());
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(ThemeManager.getColor("danger"));
                btnLogout.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                applyTheme();
            }
        });
        bottomContainer.add(btnLogout, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        updateLanguageText();
        refreshData();
        applyTheme();
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("surface"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getColor("border")));
        if (lblAvatar != null) {
            lblAvatar.setBackground(ThemeManager.getColor("input"));
            lblAvatar.setForeground(ThemeManager.getColor("accent"));
        }
        if (lblNickname != null) lblNickname.setForeground(ThemeManager.getColor("textPrimary"));
        if (lblIdLabel != null) lblIdLabel.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblEmailLabel != null) lblEmailLabel.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblGenderLabel != null) lblGenderLabel.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblIdValue != null) lblIdValue.setForeground(ThemeManager.getColor("textPrimary"));
        if (lblEmailValue != null) lblEmailValue.setForeground(ThemeManager.getColor("textPrimary"));
        if (lblGenderValue != null) lblGenderValue.setForeground(ThemeManager.getColor("textPrimary"));
        if (btnLogout != null) {
            btnLogout.setBackground(ThemeManager.getColor("input"));
            btnLogout.setForeground(ThemeManager.getColor("textPrimary"));
        }
    }

    private JPanel createProfileRow(JLabel lblLabel, JLabel lblValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setPreferredSize(new Dimension(75, 22));
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    public void updateLanguageText() {
        // Already handled in MainFrame, but set text if needed
    }

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username == null) return;
        User user = DatabaseUtil.getUserByUsername(username);
        if (user == null) return;

        String nickname = user.getNickname();
        if (nickname != null && nickname.length() > 14) nickname = nickname.substring(0, 12) + "...";
        lblNickname.setText(nickname != null ? nickname : "User");
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

    private void logout() {
        SessionManager.logout();
        if (mainFrame != null) mainFrame.dispose();
        new LoginFrame().setVisible(true);
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }
}