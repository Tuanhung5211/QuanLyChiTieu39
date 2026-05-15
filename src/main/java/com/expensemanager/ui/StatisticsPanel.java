package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
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

    private String currentMode = "month";
    private int currentOffset = 0;
    private String currentChartType = "line";

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color LINE_COLOR = new Color(255, 193, 7);

    private static final Color[] PIE_COLORS = {
            new Color(0x34, 0x98, 0xDB), new Color(0xE7, 0x4C, 0x3C), new Color(0xF1, 0xC4, 0x0F),
            new Color(0x2E, 0xCC, 0x71), new Color(0x9B, 0x59, 0xB6), new Color(0x1A, 0xBC, 0x9C),
            new Color(0xE6, 0x7E, 0x22), new Color(0x9E, 0x9E, 0x9E)
    };

    private JLabel lblTotalExpense;
    private JPanel pieChartPanel, lineChartPanel, legendPanel;
    private JLabel lblTimeRange;
    private JButton btnWeek, btnMonth, btnYear;
    private Map<String, Double> categoryExpenses;
    private double totalExpense;
    private Map<String, Double> timeSeriesData;
    private CardLayout chartCardLayout;
    private JPanel chartSwitcher;

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // --- 1. TOP CONTAINER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel title = new JLabel("Báo cáo chi tiêu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        topHeader.add(title, BorderLayout.WEST);

        JPanel toggleGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toggleGroup.setOpaque(false);
        JButton btnPie = createIconButton("🥧");
        JButton btnLine = createIconButton("📈");
        btnPie.addActionListener(e -> switchChartType("pie"));
        btnLine.addActionListener(e -> switchChartType("line"));
        toggleGroup.add(btnPie);
        toggleGroup.add(btnLine);
        topHeader.add(toggleGroup, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setOpaque(false);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modePanel.setOpaque(false);
        btnWeek = createCompactBtn("Tuần");
        btnMonth = createCompactBtn("Tháng");
        btnYear = createCompactBtn("Năm");
        updateBtnStyle(btnMonth, true);
        btnWeek.addActionListener(e -> switchMode("week"));
        btnMonth.addActionListener(e -> switchMode("month"));
        btnYear.addActionListener(e -> switchMode("year"));
        modePanel.add(btnWeek);
        modePanel.add(Box.createHorizontalStrut(10));
        modePanel.add(btnMonth);
        modePanel.add(Box.createHorizontalStrut(10));
        modePanel.add(btnYear);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        navPanel.setOpaque(false);
        lblTimeRange = new JLabel();
        lblTimeRange.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTimeRange.setForeground(TEXT_SECONDARY);
        lblTimeRange.setHorizontalAlignment(SwingConstants.CENTER);
        lblTimeRange.setPreferredSize(new Dimension(160, 30));

        JButton btnPrev = createIconButton("❮");
        JButton btnNext = createIconButton("❯");
        btnPrev.addActionListener(e -> { currentOffset--; refreshData(); });
        btnNext.addActionListener(e -> { currentOffset++; refreshData(); });

        navPanel.add(btnPrev);
        navPanel.add(lblTimeRange);
        navPanel.add(btnNext);

        filterBar.add(modePanel, BorderLayout.WEST);
        filterBar.add(navPanel, BorderLayout.EAST);

        topContainer.add(topHeader);
        topContainer.add(Box.createVerticalStrut(20));
        topContainer.add(filterBar);
        topContainer.add(Box.createVerticalStrut(30));

        add(topContainer, BorderLayout.NORTH);

        // --- 2. MAIN CENTER ---
        chartCardLayout = new CardLayout();
        chartSwitcher = new JPanel(chartCardLayout);
        chartSwitcher.setOpaque(false);

        pieChartPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); drawDonutChart(g);
            }
        };
        pieChartPanel.setOpaque(false);
        lblTotalExpense = new JLabel("", SwingConstants.CENTER);
        lblTotalExpense.setForeground(TEXT_PRIMARY);
        pieChartPanel.add(lblTotalExpense);

        lineChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); drawLineChart(g);
            }
        };
        lineChartPanel.setOpaque(false);

        chartSwitcher.add(pieChartPanel, "pie");
        chartSwitcher.add(lineChartPanel, "line");
        chartCardLayout.show(chartSwitcher, "pie"); // Đổi hiển thị mặc định sang biểu đồ tròn để dễ test

        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(legendPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(280, 0));
        applyModernScrollBar(scroll);

        JPanel mainCenter = new JPanel(new BorderLayout());
        mainCenter.setOpaque(false);
        mainCenter.add(chartSwitcher, BorderLayout.CENTER);
        mainCenter.add(scroll, BorderLayout.EAST);

        add(mainCenter, BorderLayout.CENTER);

        refreshData();
    }

    private JButton createIconButton(String icon) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        b.setForeground(TEXT_PRIMARY);
        b.setBackground(SURFACE_COLOR);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createCompactBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(TEXT_SECONDARY);
        b.setBackground(SURFACE_COLOR);
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void updateBtnStyle(JButton b, boolean active) {
        b.setBackground(active ? ACCENT_YELLOW : SURFACE_COLOR);
        b.setForeground(active ? BG_COLOR : TEXT_SECONDARY);
    }

    private void switchMode(String mode) {
        currentMode = mode; currentOffset = 0;
        updateBtnStyle(btnWeek, mode.equals("week"));
        updateBtnStyle(btnMonth, mode.equals("month"));
        updateBtnStyle(btnYear, mode.equals("year"));
        refreshData();
    }

    private void switchChartType(String type) {
        currentChartType = type;
        chartCardLayout.show(chartSwitcher, type);
    }

    public void refreshData() {
        if (financeService == null) return;
        financeService.syncFromDatabase();
        List<Transaction> transactions = financeService.getAllTransactions();
        LocalDate now = LocalDate.now();
        LocalDate start, end;

        if (currentMode.equals("week")) {
            start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(currentOffset);
            end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            lblTimeRange.setText(start.getDayOfMonth() + " - " + end.getDayOfMonth() + " thg " + start.getMonthValue());
        } else if (currentMode.equals("year")) {
            start = now.withDayOfYear(1).plusYears(currentOffset);
            end = start.withDayOfYear(start.lengthOfYear());
            lblTimeRange.setText("Năm " + start.getYear());
        } else {
            YearMonth ym = YearMonth.now().plusMonths(currentOffset);
            start = ym.atDay(1); end = ym.atEndOfMonth();
            lblTimeRange.setText("Tháng " + ym.getMonthValue() + "/" + ym.getYear());
        }

        List<Transaction> filtered = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start) && !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        totalExpense = filtered.stream().mapToDouble(Transaction::getAmount).sum();
        lblTotalExpense.setText(String.format("<html><center><span style='color:#A0A0A0; font-size:12px;'>TỔNG CHI</span><br><b style='font-size:24px;'>%,.0f đ</b></center></html>", totalExpense));

        categoryExpenses = filtered.stream().collect(Collectors.groupingBy(
                t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                Collectors.summingDouble(Transaction::getAmount)));

        timeSeriesData = new LinkedHashMap<>();
        if (currentMode.equals("week")) {
            for (int i=0; i<7; i++) timeSeriesData.put(start.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), 0.0);
        } else if (currentMode.equals("year")) {
            for (int i=1; i<=12; i++) timeSeriesData.put("T"+i, 0.0);
        } else {
            for (int i=1; i<=end.getDayOfMonth(); i++) timeSeriesData.put(String.valueOf(i), 0.0);
        }

        for (Transaction t : filtered) {
            String key; LocalDate d = t.getDateTime().toLocalDate();
            if (currentMode.equals("week")) key = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            else if (currentMode.equals("year")) key = "T"+d.getMonthValue();
            else key = String.valueOf(d.getDayOfMonth());
            timeSeriesData.merge(key, t.getAmount(), Double::sum);
        }

        lineChartPanel.repaint(); pieChartPanel.repaint(); updateLegend();
    }

    private void drawLineChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = lineChartPanel.getWidth(), h = lineChartPanel.getHeight();
        if (timeSeriesData == null || timeSeriesData.isEmpty()) return;

        List<Double> values = new ArrayList<>(timeSeriesData.values());
        double max = values.stream().mapToDouble(d -> d).max().orElse(1);
        if (max == 0) max = 1;

        int padLeft = 45, padRight = 10, padTop = 10, padBottom = 30;
        int graphW = w - padLeft - padRight;
        int graphH = h - padTop - padBottom;

        g2.setColor(new Color(50, 50, 50));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i=0; i<=4; i++) {
            int y = h - padBottom - (graphH * i / 4);
            g2.drawLine(padLeft, y, w - padRight, y);

            String lblY = formatValue(max * i / 4);
            int lblW = g2.getFontMetrics().stringWidth(lblY);
            g2.setColor(TEXT_SECONDARY);
            g2.drawString(lblY, padLeft - lblW - 8, y + 4);
            g2.setColor(new Color(50, 50, 50));
        }

        int n = values.size();
        int stepX = graphW / (n > 1 ? n - 1 : 1);

        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i=0; i<n; i++) {
            xs[i] = padLeft + i * stepX;
            ys[i] = h - padBottom - (int)(values.get(i) / max * graphH);
        }

        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(2.5f));
        for (int i=0; i<n-1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        }

        List<String> labels = new ArrayList<>(timeSeriesData.keySet());
        for (int i=0; i<n; i++) {
            g2.setColor(BG_COLOR);
            g2.fillOval(xs[i]-5, ys[i]-5, 10, 10);
            g2.setColor(LINE_COLOR);
            g2.fillOval(xs[i]-3, ys[i]-3, 6, 6);
            g2.drawOval(xs[i]-5, ys[i]-5, 10, 10);

            boolean shouldDraw = currentMode.equals("week") || currentMode.equals("year")
                    || (currentMode.equals("month") && (i == 0 || i == n-1 || (i+1)%5 == 0));
            if (shouldDraw) {
                g2.setColor(TEXT_SECONDARY);
                int lblW = g2.getFontMetrics().stringWidth(labels.get(i));
                g2.drawString(labels.get(i), xs[i] - lblW/2, h - 10);
            }
        }
    }

    private String formatValue(double value) {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000);
        if (value >= 1_000) return String.format("%.1fK", value / 1_000);
        return String.format("%.0f", value);
    }

    // --- CẬP NHẬT: LÀM DÀY VIỀN BIỂU ĐỒ TRÒN ---
    private void drawDonutChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = pieChartPanel.getWidth(), h = pieChartPanel.getHeight();
        int size = Math.min(w, h) - 20;
        int x = (w - size) / 2, y = (h - size) / 2;

        if (totalExpense == 0) {
            g2.setColor(SURFACE_COLOR);
            g2.fillOval(x, y, size, size);
        } else {
            double start = 90; int i = 0;
            for (double val : categoryExpenses.values()) {
                int angle = (int)Math.round(val / totalExpense * 360);
                g2.setColor(PIE_COLORS[i++ % PIE_COLORS.length]);
                g2.fillArc(x, y, size, size, (int)start, -angle);
                start -= angle;
            }
        }

        // Thu nhỏ kích thước lỗ hổng (từ size - 120 thành size - 160) để viền trở nên dày dặn và mập mạp hơn
        int holeSize = Math.max(0, size - 160);
        g2.setColor(BG_COLOR);
        g2.fillOval(x + (size - holeSize) / 2, y + (size - holeSize) / 2, holeSize, holeSize);
    }

    // --- CẬP NHẬT: GÓI GỌN CHÚ THÍCH (LEGEND) VÀ CHẤM MÀU TRÒN ---
    private void updateLegend() {
        legendPanel.removeAll();
        if (categoryExpenses == null) return;
        List<Map.Entry<String, Double>> list = new ArrayList<>(categoryExpenses.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int i = 0;
        for (Map.Entry<String, Double> e : list) {
            JPanel row = new JPanel();
            // Sử dụng BoxLayout theo chiều ngang để khóa kích thước
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
            // KHÓA CỨNG CHUẨN: Ngăn không cho row giãn ra tận mép phải màn hình
            row.setMaximumSize(new Dimension(280, 35));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Vẽ chấm màu hình tròn hoàn hảo thay vì vuông
            final Color dotColor = PIE_COLORS[i++ % PIE_COLORS.length];
            JPanel colorDot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(dotColor);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            colorDot.setOpaque(false);
            colorDot.setMinimumSize(new Dimension(14, 14));
            colorDot.setPreferredSize(new Dimension(14, 14));
            colorDot.setMaximumSize(new Dimension(14, 14));

            JLabel name = new JLabel(e.getKey());
            name.setForeground(TEXT_PRIMARY);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            JLabel val = new JLabel(String.format("%,.0f đ", e.getValue()));
            val.setForeground(TEXT_SECONDARY);
            val.setFont(new Font("Segoe UI", Font.BOLD, 15));

            row.add(colorDot);
            row.add(Box.createRigidArea(new Dimension(12, 0))); // Khoảng cách giữa chấm màu và tên
            row.add(name);
            row.add(Box.createHorizontalGlue()); // Đẩy số tiền sang sát bên phải, nhưng vẫn nằm trong giới hạn 280px
            row.add(val);

            legendPanel.add(row);
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private void applyModernScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 70, 70);
                this.trackColor = BG_COLOR;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
    }
}