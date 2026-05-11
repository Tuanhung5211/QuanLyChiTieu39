package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final StatisticsService statsService;
    private JLabel lblIncome, lblExpense, lblRemaining, lblBudget, lblExpTotal, lblMonth;
    private double percentRemaining = 0;

    public DashboardPanel(MainFrame mainFrame) {
        this.statsService = new StatisticsService(new com.expensemanager.service.FinanceService());

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        JLabel title = new JLabel("Báo cáo tháng", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        content.add(createCard("Thống kê giao dịch", true));
        content.add(Box.createVerticalStrut(25));
        content.add(createCard("Tình hình ngân sách", false));

        add(content, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createCard(String title, boolean isStats) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setMaximumSize(new Dimension(1200, isStats ? 140 : 250));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(180, 180, 180));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(lblTitle, BorderLayout.NORTH);

        if (isStats) {
            JPanel info = new JPanel(new GridLayout(1, 3));
            info.setOpaque(false);
            lblMonth = new JLabel("Tháng 05"); 
            lblMonth.setForeground(Color.WHITE);
            lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 22));
            
            lblExpense = createValueBox("Tổng chi");
            lblIncome = createValueBox("Tổng thu");
            
            info.add(lblMonth); info.add(lblExpense); info.add(lblIncome);
            card.add(info, BorderLayout.CENTER);
        } else {
            JPanel body = new JPanel(new BorderLayout(40, 0));
            body.setOpaque(false);

            JPanel circle = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = 110, x = (getWidth()-size)/2, y = (getHeight()-size)/2;
                    g2.setColor(new Color(60, 60, 60));
                    g2.setStroke(new BasicStroke(10));
                    g2.drawOval(x, y, size, size);
                    g2.setColor(new Color(255, 215, 64));
                    g2.drawArc(x, y, size, size, 90, (int)-(percentRemaining * 3.6));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.format("%.1f%%", percentRemaining), x+35, y+60);
                }
            };
            circle.setPreferredSize(new Dimension(150, 150));
            circle.setOpaque(false);
            body.add(circle, BorderLayout.WEST);

            JPanel details = new JPanel(new GridLayout(3, 2, 0, 15));
            details.setOpaque(false);
            lblRemaining = addRow(details, "Còn lại:");
            lblBudget = addRow(details, "Ngân sách:");
            lblExpTotal = addRow(details, "Đã chi:");
            body.add(details, BorderLayout.CENTER);
            
            card.add(body, BorderLayout.CENTER);
        }
        return card;
    }

    private JLabel createValueBox(String label) {
        JLabel l = new JLabel("0");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JLabel addRow(JPanel p, String text) {
        JLabel l1 = new JLabel(text); l1.setForeground(new Color(170, 170, 170));
        JLabel l2 = new JLabel("0"); l2.setForeground(Color.WHITE);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l2.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l1); p.add(l2);
        return l2;
    }

    public void refreshData() {
        double inc = statsService.getTotalIncomeThisMonth();
        double exp = statsService.getTotalExpenseThisMonth();
        double limit = 5000000;
        double rem = limit - exp;
        percentRemaining = (limit > 0) ? (rem / limit) * 100 : 0;
        if (percentRemaining < 0) percentRemaining = 0;

        lblMonth.setText("Tháng " + LocalDate.now().getMonthValue());
        lblIncome.setText(String.format("%,.0f", inc));
        lblExpense.setText(String.format("%,.0f", exp));
        lblRemaining.setText(String.format("%,.0f VNĐ", rem));
        lblBudget.setText(String.format("%,.0f VNĐ", limit));
        lblExpTotal.setText(String.format("%,.0f VNĐ", exp));
        repaint();
    }
}