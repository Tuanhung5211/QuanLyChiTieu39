package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends JPanel {

    // Giả sử lấy từ Service
    private double totalIncome = 7000000; 
    private double totalExpense = 4000000;

    public StatisticsPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("THỐNG KÊ THU CHI THÁNG NÀY", SwingConstants.CENTER), BorderLayout.NORTH);
    }

    // YÊU CẦU: Override paintComponent để tự vẽ biểu đồ
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Bật chống răng cưa cho nét vẽ mượt hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double total = totalIncome + totalExpense;
        if (total == 0) return; // Không có dữ liệu thì không vẽ

        int incomeAngle = (int) Math.round((totalIncome / total) * 360);
        int expenseAngle = 360 - incomeAngle;

        int x = getWidth() / 2 - 100;
        int y = getHeight() / 2 - 100;
        int width = 200;
        int height = 200;

        // Vẽ phần Thu nhập (Màu xanh)
        g2d.setColor(new Color(0, 153, 0));
        g2d.fillArc(x, y, width, height, 0, incomeAngle);

        // Vẽ phần Chi tiêu (Màu đỏ)
        g2d.setColor(Color.RED);
        g2d.fillArc(x, y, width, height, incomeAngle, expenseAngle);

        // Chú thích (Legend)
        g2d.setColor(new Color(0, 153, 0));
        g2d.fillRect(x + 220, y + 50, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Thu nhập", x + 245, y + 62);

        g2d.setColor(Color.RED);
        g2d.fillRect(x + 220, y + 80, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Chi tiêu", x + 245, y + 92);
    }

    // Hàm gọi khi có dữ liệu mới từ Service
    public void updateChartData(double income, double expense) {
        this.totalIncome = income;
        this.totalExpense = expense;
        repaint(); // Yêu cầu Swing vẽ lại (gọi lại paintComponent)
    }
}