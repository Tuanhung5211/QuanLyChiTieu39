package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month";   // "week", "month", "year"
    private int currentOffset = 0;          // 0 = hiện tại, -1 = trước...
    private String currentChartType = "pie"; // "pie" hoặc "line"

    private static final Color[] PIE_COLORS = {
            new Color(0x4A, 0x90, 0xD9), new Color(0xF3, 0x9C, 0x12), new Color(0xE7, 0x4C, 0x3C),
            new Color(0x27, 0xAE, 0x60), new Color(0x8E, 0x44, 0xAD), new Color(0x2C, 0x3E, 0x50),
            new Color(0xD3, 0x54, 0x00), new Color(0x16, 0xA0, 0x85), new Color(0xC0, 0x39, 0x2B),
            new Color(0x7F, 0x8C, 0x8D)
    };

    // Các thành phần giao diện
    private JLabel lblTotalExpense;
    private JPanel pieChartPanel;
    private JPanel lineChartPanel;
    private JPanel legendPanel;
    private JLabel lblTimeRange;
    private JButton btnPrevTime, btnNextTime;
    private JButton btnPrevChart, btnNextChart;

    private Map<String, Double> categoryExpenses;
    private double totalExpense;
    private Map<String, Double> timeSeriesData;

    private JPanel chartSwitcher;
    private CardLayout chartCardLayout;

    private JButton btnWeek, btnMonth, btnYear;

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== Header: tiêu đề + nút chuyển đổi biểu đồ =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

        JLabel title = new JLabel("Chi tiêu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(50, 50, 50));
        headerPanel.add(title, BorderLayout.WEST);

        // Hai nút mũi tên chuyển đổi loại biểu đồ
        JPanel chartTogglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        chartTogglePanel.setBackground(Color.WHITE);
        btnPrevChart = createArrowButton("<");
        btnNextChart = createArrowButton(">");
        btnPrevChart.addActionListener(e -> switchChartType("pie"));
        btnNextChart.addActionListener(e -> switchChartType("line"));
        chartTogglePanel.add(btnPrevChart);
        chartTogglePanel.add(btnNextChart);
        headerPanel.add(chartTogglePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ===== Dòng chọn chế độ thời gian + điều hướng =====
        JPanel timeControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        timeControlPanel.setBackground(Color.WHITE);
        timeControlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        btnWeek = createModeButton("Tuần");
        btnMonth = createModeButton("Tháng");
        btnYear = createModeButton("Năm");
        updateModeButtonStyle(btnMonth, true); // mặc định Tháng
        btnWeek.addActionListener(e -> switchMode("week"));
        btnMonth.addActionListener(e -> switchMode("month"));
        btnYear.addActionListener(e -> switchMode("year"));

        btnPrevTime = createArrowButton("◀");
        btnNextTime = createArrowButton("▶");
        btnPrevTime.addActionListener(e -> { currentOffset--; refreshData(); });
        btnNextTime.addActionListener(e -> { currentOffset++; refreshData(); });

        lblTimeRange = new JLabel();
        lblTimeRange.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTimeRange.setForeground(new Color(80, 80, 80));

        timeControlPanel.add(btnWeek);
        timeControlPanel.add(btnMonth);
        timeControlPanel.add(btnYear);
        timeControlPanel.add(Box.createHorizontalStrut(20));
        timeControlPanel.add(btnPrevTime);
        timeControlPanel.add(lblTimeRange);
        timeControlPanel.add(btnNextTime);
        add(timeControlPanel, BorderLayout.CENTER);

        // ===== Khu vực chính: biểu đồ (trái) + chú thích (phải) =====
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(Color.WHITE);

        // --- Panel biểu đồ (CardLayout) ---
        chartCardLayout = new CardLayout();
        chartSwitcher = new JPanel(chartCardLayout);
        chartSwitcher.setBackground(Color.WHITE);

        // Biểu đồ tròn
        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
        pieChartPanel.setPreferredSize(new Dimension(350, 300));
        pieChartPanel.setBackground(Color.WHITE);

        // Tổng chi tiêu hiển thị giữa biểu đồ tròn
        lblTotalExpense = new JLabel("", SwingConstants.CENTER);
        lblTotalExpense.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalExpense.setForeground(new Color(60, 60, 60));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(350, 300));
        pieChartPanel.setBounds(0, 0, 350, 300);
        lblTotalExpense.setBounds(0, 0, 350, 300);
        layeredPane.add(pieChartPanel, Integer.valueOf(0));
        layeredPane.add(lblTotalExpense, Integer.valueOf(1));

        // Biểu đồ đường
        lineChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLineChart(g);
            }
        };
        lineChartPanel.setPreferredSize(new Dimension(500, 300));
        lineChartPanel.setBackground(Color.WHITE);

        chartSwitcher.add(layeredPane, "pie");
        chartSwitcher.add(lineChartPanel, "line");
        chartCardLayout.show(chartSwitcher, "pie");

        // --- Chú thích (luôn hiển thị bên phải) ---
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(BorderFactory.createEmptyBorder());
        legendScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        legendScroll.setPreferredSize(new Dimension(220, 0));

        mainPanel.add(chartSwitcher, BorderLayout.CENTER);
        mainPanel.add(legendScroll, BorderLayout.EAST);
        add(mainPanel, BorderLayout.SOUTH);

        refreshData();
    }

    // ==================== Helper tạo UI ====================
    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(100, 100, 100));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateModeButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(0, 153, 76));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 100, 100));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        }
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(80, 80, 80));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        this.currentOffset = 0; // reset offset khi đổi chế độ
        updateModeButtonStyle(btnWeek, mode.equals("week"));
        updateModeButtonStyle(btnMonth, mode.equals("month"));
        updateModeButtonStyle(btnYear, mode.equals("year"));
        refreshData();
    }

    private void switchChartType(String type) {
        this.currentChartType = type;
        chartCardLayout.show(chartSwitcher, type);
    }

    // ==================== Dữ liệu ====================
    public void refreshData() {
        if (financeService == null) return;

        List<Transaction> allTransactions = financeService.getAllTransactions();
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        String timeLabel = "";

        switch (currentMode) {
            case "week": {
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(currentOffset);
                end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                timeLabel = String.format("%s - %s",
                        start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                        end.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
            }
            case "year": {
                start = now.withDayOfYear(1).plusYears(currentOffset);
                end = start.withDayOfYear(start.lengthOfYear());
                timeLabel = String.valueOf(start.getYear());
                break;
            }
            default: { // month
                YearMonth ym = YearMonth.of(now.getYear(), now.getMonthValue()).plusMonths(currentOffset);
                start = ym.atDay(1);
                end = ym.atEndOfMonth();
                timeLabel = String.format("Tháng %d/%d", ym.getMonthValue(), ym.getYear());
                break;
            }
        }

        lblTimeRange.setText(timeLabel);

        // Lọc giao dịch
        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start))
                .filter(t -> !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        totalExpense = filtered.stream().mapToDouble(Transaction::getAmount).sum();
        lblTotalExpense.setText(String.format("<html><center><span style='font-size:12px; color:#888;'>Tổng chi</span><br><span style='font-size:18px; font-weight:bold; color:#333;'>+%,.0f đ</span></center></html>", totalExpense));

        // Dữ liệu cho biểu đồ tròn
        categoryExpenses = filtered.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                        Collectors.summingDouble(Transaction::getAmount)));

        // Dữ liệu cho biểu đồ đường
        timeSeriesData = new LinkedHashMap<>();
        if (currentMode.equals("week")) {
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                timeSeriesData.put(label, 0.0);
            }
        } else if (currentMode.equals("year")) {
            for (int m = 1; m <= 12; m++) {
                timeSeriesData.put("T" + m, 0.0);
            }
        } else { // month
            for (int d = 1; d <= end.getDayOfMonth(); d++) {
                timeSeriesData.put(String.valueOf(d), 0.0);
            }
        }

        for (Transaction t : filtered) {
            String key;
            LocalDate date = t.getDateTime().toLocalDate();
            if (currentMode.equals("week")) {
                key = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            } else if (currentMode.equals("year")) {
                key = "T" + date.getMonthValue();
            } else {
                key = String.valueOf(date.getDayOfMonth());
            }
            timeSeriesData.merge(key, t.getAmount(), Double::sum);
        }

        pieChartPanel.repaint();
        lineChartPanel.repaint();
        updateLegend();
    }

    // ==================== Vẽ biểu đồ ====================
    private void drawPieChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = pieChartPanel.getWidth(), h = pieChartPanel.getHeight();
        int size = Math.min(w, h) - 30;
        int x = (w - size) / 2, y = (h - size) / 2;

        if (categoryExpenses == null || categoryExpenses.isEmpty() || totalExpense == 0) {
            g2d.setColor(new Color(220, 220, 220));
            g2d.fillArc(x, y, size, size, 0, 360);
            return;
        }
        double startAngle = 90.0;
        int ci = 0;
        for (Map.Entry<String, Double> e : categoryExpenses.entrySet()) {
            double pct = e.getValue() / totalExpense;
            int arc = (int) Math.round(pct * 360);
            g2d.setColor(PIE_COLORS[ci % PIE_COLORS.length]);
            g2d.fillArc(x, y, size, size, (int) startAngle, -arc);
            startAngle -= arc;
            ci++;
        }
    }

    private void drawLineChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = lineChartPanel.getWidth(), h = lineChartPanel.getHeight();
        if (timeSeriesData == null || timeSeriesData.isEmpty()) return;

        List<String> labels = new ArrayList<>(timeSeriesData.keySet());
        List<Double> values = new ArrayList<>(timeSeriesData.values());
        double maxVal = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        int marginLeft = 40, marginRight = 20, marginTop = 20, marginBottom = 35;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        // Lưới ngang
        g2d.setColor(new Color(240, 240, 240));
        for (int i = 0; i < 5; i++) {
            int y = marginTop + (chartH * i / 4);
            g2d.drawLine(marginLeft, y, w - marginRight, y);
        }

        // Trục
        g2d.setColor(new Color(180, 180, 180));
        g2d.drawLine(marginLeft, marginTop, marginLeft, h - marginBottom);
        g2d.drawLine(marginLeft, h - marginBottom, w - marginRight, h - marginBottom);

        // Đường và điểm
        if (labels.size() == 1) {
            int x = marginLeft + chartW / 2;
            int y = marginTop + (int)((1 - values.get(0) / maxVal) * chartH);
            g2d.setColor(new Color(0, 153, 76));
            g2d.fillOval(x - 4, y - 4, 8, 8);
            g2d.drawString(String.format("%,.0f", values.get(0)), x - 15, y - 8);
        } else {
            int[] xs = new int[labels.size()];
            int[] ys = new int[labels.size()];
            for (int i = 0; i < labels.size(); i++) {
                xs[i] = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
                ys[i] = marginTop + (int)((1 - values.get(i) / maxVal) * chartH);
            }
            g2d.setColor(new Color(0, 153, 76));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawPolyline(xs, ys, labels.size());
            // Điểm
            g2d.setStroke(new BasicStroke(1f));
            for (int i = 0; i < xs.length; i++) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2d.setColor(new Color(0, 153, 76));
                g2d.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
            }
        }

        // Nhãn trục X
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        if (labels.size() <= 15) {
            for (int i = 0; i < labels.size(); i++) {
                int x = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
                String lbl = labels.get(i);
                int sw = g2d.getFontMetrics().stringWidth(lbl);
                g2d.drawString(lbl, x - sw / 2, h - marginBottom + 15);
            }
        } else {
            int step = Math.max(1, labels.size() / 10);
            for (int i = 0; i < labels.size(); i += step) {
                int x = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
                String lbl = labels.get(i);
                int sw = g2d.getFontMetrics().stringWidth(lbl);
                g2d.drawString(lbl, x - sw / 2, h - marginBottom + 15);
            }
        }
    }

    // ==================== Legend ====================
    private void updateLegend() {
        legendPanel.removeAll();
        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Color.GRAY);
            legendPanel.add(empty);
        } else {
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryExpenses.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int ci = 0;
            for (Map.Entry<String, Double> e : sorted) {
                double pct = totalExpense > 0 ? (e.getValue() / totalExpense * 100) : 0;
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                left.setBackground(Color.WHITE);
                JPanel colorBox = new JPanel();
                colorBox.setBackground(PIE_COLORS[ci % PIE_COLORS.length]);
                colorBox.setPreferredSize(new Dimension(12, 12));
                left.add(colorBox);
                JLabel lblName = new JLabel(e.getKey());
                lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                left.add(lblName);
                row.add(left, BorderLayout.WEST);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                right.setBackground(Color.WHITE);
                JLabel lblPct = new JLabel(String.format("%.1f%%", pct));
                lblPct.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblPct.setForeground(PIE_COLORS[ci % PIE_COLORS.length]);
                right.add(lblPct);
                JLabel lblAmount = new JLabel(String.format("%,.0f đ", e.getValue()));
                lblAmount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblAmount.setForeground(new Color(100, 100, 100));
                right.add(lblAmount);
                row.add(right, BorderLayout.EAST);

                legendPanel.add(row);
                ci++;
            }
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }
}