package com.expensemanager.ui;

import com.expensemanager.service.BudgetManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

public class BudgetPanel extends JPanel {
    private MainFrame mainFrame;
    private BudgetManager budgetManager;
    private JTextField txtLimit;

    // Bộ màu đồng bộ
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);

    public BudgetPanel(MainFrame mainFrame, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.budgetManager = budgetManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Ngân sách chi tiêu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- BODY ---
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        // Form thiết lập
        body.add(createSetupCard());
        body.add(Box.createVerticalGlue());

        add(body, BorderLayout.CENTER);
    }

    private JPanel createSetupCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SURFACE_COLOR);
        card.setMaximumSize(new Dimension(600, 300));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        LocalDate now = LocalDate.now();
        JLabel lblInfo = new JLabel("Thiết lập hạn mức cho Tháng " + now.getMonthValue() + " năm " + now.getYear());
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblInfo.setForeground(ACCENT_YELLOW);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lblInfo, gbc);

        JLabel lblHint = new JLabel("Hệ thống sẽ cảnh báo nếu chi tiêu vượt quá số tiền này.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHint.setForeground(TEXT_SECONDARY);
        gbc.gridy = 1;
        card.add(lblHint, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1; gbc.insets = new Insets(30, 10, 10, 10);
        JLabel lblAmount = new JLabel("Số tiền (VND):");
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmount.setForeground(TEXT_PRIMARY);
        card.add(lblAmount, gbc);

        txtLimit = new JTextField();
        txtLimit.setBackground(INPUT_BG);
        txtLimit.setForeground(Color.WHITE);
        txtLimit.setCaretColor(ACCENT_YELLOW);
        txtLimit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtLimit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridx = 1;
        card.add(txtLimit, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 0, 10);
        JButton btnSave = new JButton("Cập nhật ngân sách");
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> handleSave());
        card.add(btnSave, gbc);

        return card;
    }

    private void handleSave() {
        try {
            String input = txtLimit.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền!");
                return;
            }

            double limit = Double.parseDouble(input);
            LocalDate now = LocalDate.now();

            // Gọi hàm của team TheTays
            budgetManager.setBudget(now.getMonthValue(), now.getYear(), limit);

            JOptionPane.showMessageDialog(this, "Đã cập nhật ngân sách thành công!");
            mainFrame.refreshAllPanels(); // Cập nhật lại Dashboard ngay lập tức

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (com.expensemanager.exception.InvalidAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    public void refreshData() {
        // Có thể lấy ngân sách hiện tại từ DB để hiển thị lên txtLimit nếu muốn
        txtLimit.setText("");
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW);
        btn.setForeground(BG_COLOR);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(255, 205, 50)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT_YELLOW); }
        });
    }
}