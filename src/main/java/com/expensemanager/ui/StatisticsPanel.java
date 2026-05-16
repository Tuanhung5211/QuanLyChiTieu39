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
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month";
    private int currentOffset = 0;
    private String currentChartType = "pie";

    private static final Color[] PIE_COLORS = {
            new Color(0x34, 0x98, 0xDB), new Color(0xE7, 0x4C, 0x3C), new Color(0xF1, 0xC4, 0x0F),
            new Color(0x2E, 0xCC, 0x71), new Color(0x9B, 0x59, 0xB6), new Color(0xE6, 0x7E, 0x22),
            new Color(0x1A, 0xBC, 0x9C), new Color(0x34, 0x49, 0x5E), new Color(0xC0, 0x39, 0x2B),
            new Color(0x7F, 0x8C, 0x8D)
    };

    private JLabel lblTotalExpense;
    private JPanel pieChartPanel;
    private JPanel lineChartPanel;
    private JPanel legendPanel;
    private JLabel lblTimeRange;
    private JButton btnPrevTime, btnNextTime, btnPrevChart, btnNextChart;
    private JButton btnWeek, btnMonth, btnYear;

    private Map<String, Double> categoryExpenses;
    private double totalExpense;
    private Map<String, Double> timeSeriesData;
    private JPanel chartSwitcher;
    private CardLayout chartCardLayout;

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(18, 18, 18));

        JLabel title = new JLabel("Thống kê chi tiêu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JPanel chartTogglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        chartTogglePanel.setBackground(new Color(18, 18, 18));
        btnPrevChart = createArrowButton("<"); btnNextChart = createArrowButton(">");
        btnPrevChart.addActionListener(e -> switchChartType("pie"));
        btnNextChart.addActionListener(e -> switchChartType("line"));
        chartTogglePanel.add(btnPrevChart); chartTogglePanel.add(btnNextChart);
        headerPanel.add(chartTogglePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Control Panel
        JPanel timeControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        timeControlPanel.setBackground(new Color(18, 18, 18));

        btnWeek = createModeButton("Tuần");
        btnMonth = createModeButton("Tháng");
        btnYear = createModeButton("Năm");
        updateModeButtonStyle(btnMonth, true);
        btnWeek.addActionListener(e -> switchMode("week"));
        btnMonth.addActionListener(e -> switchMode("month"));
        btnYear.addActionListener(e -> switchMode("year"));

        btnPrevTime = createArrowButton("◀");
        btnNextTime = createArrowButton("▶");
        btnPrevTime.addActionListener(e -> { currentOffset--; refreshData(); });
        btnNextTime.addActionListener(e -> { currentOffset++; refreshData(); });

        lblTimeRange = new JLabel();
        lblTimeRange.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTimeRange.setForeground(Color.LIGHT_GRAY);

        timeControlPanel.add(btnWeek); timeControlPanel.add(btnMonth); timeControlPanel.add(btnYear);
        timeControlPanel.add(Box.createHorizontalStrut(30));
        timeControlPanel.add(btnPrevTime); timeControlPanel.add(lblTimeRange); timeControlPanel.add(btnNextTime);
        add(timeControlPanel, BorderLayout.CENTER);

        // Main Chart Area
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(new Color(18, 18, 18));

        chartCardLayout = new CardLayout();
        chartSwitcher = new JPanel(chartCardLayout);
        chartSwitcher.setBackground(new Color(18, 18, 18));

        // Pie (Donut)
        pieChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); drawDonutChart(g); }
        };
        pieChartPanel.setPreferredSize(new Dimension(400, 400));
        pieChartPanel.setBackground(new Color(18, 18, 18));

        lblTotalExpense = new JLabel("", SwingConstants.CENTER);
        lblTotalExpense.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalExpense.setForeground(Color.WHITE);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(400, 400));
        pieChartPanel.setBounds(0, 0, 400, 400);
        lblTotalExpense.setBounds(0, 0, 400, 400);
        layeredPane.add(pieChartPanel, Integer.valueOf(0));
        layeredPane.add(lblTotalExpense, Integer.valueOf(1));

        // Line Chart
        lineChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); drawLineChart(g); }
        };
        lineChartPanel.setBackground(new Color(18, 18, 18));

        chartSwitcher.add(layeredPane, "pie");
        chartSwitcher.add(lineChartPanel, "line");

        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(new Color(18, 18, 18));
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(null);
        legendScroll.setPreferredSize(new Dimension(300, 0));

        mainPanel.add(chartSwitcher, BorderLayout.CENTER);
        mainPanel.add(legendScroll, BorderLayout.EAST);
        add(mainPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.GRAY);
        btn.setBackground(new Color(30, 30, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateModeButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
        } else {
            btn.setBackground(new Color(30, 30, 30));
            btn.setForeground(Color.GRAY);
        }
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 30, 30));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void switchMode(String mode) {
        this.currentMode = mode; this.currentOffset = 0;
        updateModeButtonStyle(btnWeek, mode.equals("week"));
        updateModeButtonStyle(btnMonth, mode.equals("month"));
        updateModeButtonStyle(btnYear, mode.equals("year"));
        refreshData();
    }

    private void switchChartType(String type) {
        this.currentChartType = type;
        chartCardLayout.show(chartSwitcher, type);
    }

    public void refreshData() {
        if (financeService == null) return;
        List<Transaction> allTransactions = financeService.getAllTransactions();
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        String timeLabel = "";

        switch (currentMode) {
            case "week":
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(currentOffset);
                end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                timeLabel = String.format("Tuần %s - %s", start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")), end.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
                break;
            case "year":
                start = now.withDayOfYear(1).plusYears(currentOffset);
                end = start.withDayOfYear(start.lengthOfYear());
                timeLabel = String.format("Năm %d", start.getYear());
                break;
            default:
                YearMonth ym = YearMonth.of(now.getYear(), now.getMonthValue()).plusMonths(currentOffset);
                start = ym.atDay(1); end = ym.atEndOfMonth();
                timeLabel = String.format("Tháng %d/%d", ym.getMonthValue(), ym.getYear());
                break;
        }

        lblTimeRange.setText(timeLabel);

        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start) && !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        totalExpense = filtered.stream().mapToDouble(Transaction::getAmount).sum();
        lblTotalExpense.setText(String.format("<html><center><span style='font-size:14px; color:#AAA;'>Tổng chi</span><br><span style='font-size:22px; font-weight:bold; color:#FFF;'>%,.0f đ</span></center></html>", totalExpense));

        categoryExpenses = filtered.stream().collect(Collectors.groupingBy(
                t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                Collectors.summingDouble(Transaction::getAmount)));

        timeSeriesData = new LinkedHashMap<>();
        if (currentMode.equals("week")) {
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) timeSeriesData.put(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), 0.0);
        } else if (currentMode.equals("year")) {
            for (int m = 1; m <= 12; m++) timeSeriesData.put("T" + m, 0.0);
        } else {
            for (int d = 1; d <= end.getDayOfMonth(); d++) timeSeriesData.put(String.valueOf(d), 0.0);
        }

        for (Transaction t : filtered) {
            String key; LocalDate date = t.getDateTime().toLocalDate();
            if (currentMode.equals("week")) key = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            else if (currentMode.equals("year")) key = "T" + date.getMonthValue();
            else key = String.valueOf(date.getDayOfMonth());
            timeSeriesData.merge(key, t.getAmount(), Double::sum);
        }

        pieChartPanel.repaint(); lineChartPanel.repaint(); updateLegend();
    }

    private void drawDonutChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = pieChartPanel.getWidth(), h = pieChartPanel.getHeight();
        int size = Math.min(w, h) - 40;
        int x = (w - size) / 2, y = (h - size) / 2;

        if (categoryExpenses == null || categoryExpenses.isEmpty() || totalExpense == 0) {
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillArc(x, y, size, size, 0, 360);
        } else {
            double startAngle = 90.0; int ci = 0;
            for (Map.Entry<String, Double> e : categoryExpenses.entrySet()) {
                double pct = e.getValue() / totalExpense;
                int arc = (int) Math.round(pct * 360);
                g2d.setColor(PIE_COLORS[ci % PIE_COLORS.length]);
                g2d.fillArc(x, y, size, size, (int) startAngle, -arc);
                startAngle -= arc; ci++;
            }
        }
        // Vẽ vòng tròn Đen ở giữa để tạo Donut
        int innerSize = size - 100; // Độ dày vành
        int ix = (w - innerSize) / 2, iy = (h - innerSize) / 2;
        g2d.setColor(new Color(18, 18, 18));
        g2d.fillOval(ix, iy, innerSize, innerSize);
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

        int marginLeft = 40, marginRight = 20, marginTop = 20, marginBottom = 40;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        g2d.setColor(new Color(40, 40, 40));
        for (int i = 0; i < 5; i++) {
            int y = marginTop + (chartH * i / 4);
            g2d.drawLine(marginLeft, y, w - marginRight, y);
        }

        int[] xs = new int[labels.size()], ys = new int[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            xs[i] = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
            ys[i] = marginTop + (int)((1 - values.get(i) / maxVal) * chartH);
        }

        g2d.setColor(new Color(255, 193, 7)); // Đường Line màu Vàng
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawPolyline(xs, ys, labels.size());

        g2d.setStroke(new BasicStroke(1f));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i < labels.size(); i++) {
            g2d.setColor(new Color(18, 18, 18));
            g2d.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
            g2d.setColor(new Color(255, 193, 7));
            g2d.drawOval(xs[i] - 5, ys[i] - 5, 10, 10);

            if (labels.size() <= 15 || i % (labels.size()/10) == 0) {
                g2d.setColor(Color.LIGHT_GRAY);
                int sw = g2d.getFontMetrics().stringWidth(labels.get(i));
                g2d.drawString(labels.get(i), xs[i] - sw / 2, h - 15);
            }
        }
    }

    private void updateLegend() {
        legendPanel.removeAll();
        if (categoryExpenses == null || categoryExpenses.isEmpty()) return;
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryExpenses.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        int ci = 0;
        for (Map.Entry<String, Double> e : sorted) {
            double pct = totalExpense > 0 ? (e.getValue() / totalExpense * 100) : 0;
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(new Color(18, 18, 18));
            row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setBackground(new Color(18, 18, 18));
            JPanel colorBox = new JPanel();
            colorBox.setBackground(PIE_COLORS[ci % PIE_COLORS.length]);
            colorBox.setPreferredSize(new Dimension(15, 15));
            left.add(colorBox);
            JLabel lblName = new JLabel(e.getKey());
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblName.setForeground(Color.WHITE);
            left.add(lblName);
            row.add(left, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            right.setBackground(new Color(18, 18, 18));
            JLabel lblPct = new JLabel(String.format("%.1f%%", pct));
            lblPct.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblPct.setForeground(PIE_COLORS[ci % PIE_COLORS.length]);
            right.add(lblPct);
            JLabel lblAmount = new JLabel(String.format("%,.0f", e.getValue()));
            lblAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblAmount.setForeground(Color.LIGHT_GRAY);
            right.add(lblAmount);
            row.add(right, BorderLayout.EAST);

            legendPanel.add(row); ci++;
        }
        legendPanel.revalidate(); legendPanel.repaint();
    }
}