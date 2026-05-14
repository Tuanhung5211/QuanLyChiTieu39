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
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month"; // "week", "month", "year"
    private JButton btnWeek, btnMonth, btnYear;

    private static final Color[] PIE_COLORS = {
            new Color(255, 99, 132), new Color(54, 162, 235), new Color(255, 206, 86),
            new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 159, 64),
            new Color(199, 199, 199), new Color(83, 102, 255), new Color(255, 99, 255),
            new Color(99, 255, 132)
    };

    private JLabel lblTotalExpense;
    private JPanel pieChartPanel;
    private JPanel legendPanel;
    private JPanel lineChartPanel;

    private Map<String, Double> categoryExpenses;
    private double totalExpense;
    private Map<String, Double> timeSeriesData; // dữ liệu cho biểu đồ đường

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("EXPENSES", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Nút chọn chế độ
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        modePanel.setBackground(Color.WHITE);
        btnWeek = createModeButton("Tuần");
        btnMonth = createModeButton("Tháng");
        btnYear = createModeButton("Năm");
        btnMonth.setBackground(new Color(0, 153, 76));
        btnMonth.setForeground(Color.WHITE);
        btnWeek.addActionListener(e -> switchMode("week"));
        btnMonth.addActionListener(e -> switchMode("month"));
        btnYear.addActionListener(e -> switchMode("year"));
        modePanel.add(btnWeek);
        modePanel.add(btnMonth);
        modePanel.add(btnYear);
        add(modePanel, BorderLayout.NORTH);

        // Panel trung tâm (cuộn)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Tổng chi tiêu
        lblTotalExpense = new JLabel("0 VND", SwingConstants.CENTER);
        lblTotalExpense.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalExpense.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTotalExpense);
        centerPanel.add(Box.createVerticalStrut(15));

        // Biểu đồ tròn
        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
        pieChartPanel.setPreferredSize(new Dimension(300, 220));
        pieChartPanel.setMaximumSize(new Dimension(300, 220));
        pieChartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pieChartPanel.setBackground(Color.WHITE);
        centerPanel.add(pieChartPanel);
        centerPanel.add(Box.createVerticalStrut(10));

        // Chú thích
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(legendPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Biểu đồ đường
        lineChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLineChart(g);
            }
        };
        lineChartPanel.setPreferredSize(new Dimension(500, 180));
        lineChartPanel.setMaximumSize(new Dimension(500, 180));
        lineChartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lineChartPanel.setBackground(Color.WHITE);
        centerPanel.add(lineChartPanel);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Nút Làm mới
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        btnWeek.setBackground(Color.LIGHT_GRAY);
        btnWeek.setForeground(Color.BLACK);
        btnMonth.setBackground(Color.LIGHT_GRAY);
        btnMonth.setForeground(Color.BLACK);
        btnYear.setBackground(Color.LIGHT_GRAY);
        btnYear.setForeground(Color.BLACK);
        JButton active = switch (mode) {
            case "week" -> btnWeek;
            case "year" -> btnYear;
            default -> btnMonth;
        };
        active.setBackground(new Color(0, 153, 76));
        active.setForeground(Color.WHITE);
        refreshData();
    }

    public void refreshData() {
        if (financeService == null) return;

        List<Transaction> allTransactions = financeService.getAllTransactions();
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        switch (currentMode) {
            case "week":
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "year":
                start = now.withDayOfYear(1);
                end = now.withDayOfYear(now.lengthOfYear());
                break;
            default: // month
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
        }

        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start))
                .filter(t -> !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        totalExpense = filtered.stream().mapToDouble(Transaction::getAmount).sum();
        lblTotalExpense.setText(String.format("+ %,.0f VND", totalExpense));

        // Dữ liệu biểu đồ tròn
        categoryExpenses = filtered.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                        Collectors.summingDouble(Transaction::getAmount)));

        // Dữ liệu biểu đồ đường (chi tiêu theo đơn vị thời gian)
        timeSeriesData = new LinkedHashMap<>();
        switch (currentMode) {
            case "week":
                // Từ thứ 2 đến chủ nhật
                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                    String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                    timeSeriesData.put(label, 0.0);
                }
                break;
            case "year":
                // Từ tháng 1 đến tháng 12
                for (int m = 1; m <= 12; m++) {
                    timeSeriesData.put("T" + m, 0.0);
                }
                break;
            default: // month
                for (int d = 1; d <= end.getDayOfMonth(); d++) {
                    timeSeriesData.put(String.valueOf(d), 0.0);
                }
                break;
        }

        // Đổ dữ liệu thực tế vào timeSeriesData
        for (Transaction t : filtered) {
            String key;
            LocalDate date = t.getDateTime().toLocalDate();
            switch (currentMode) {
                case "week":
                    key = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                    break;
                case "year":
                    key = "T" + date.getMonthValue();
                    break;
                default:
                    key = String.valueOf(date.getDayOfMonth());
                    break;
            }
            timeSeriesData.merge(key, t.getAmount(), Double::sum);
        }

        pieChartPanel.repaint();
        lineChartPanel.repaint();
        updateLegend();
    }

    private void drawPieChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = pieChartPanel.getWidth(), h = pieChartPanel.getHeight();
        int size = Math.min(w, h) - 20;
        int x = (w - size) / 2, y = (h - size) / 2;

        if (categoryExpenses == null || categoryExpenses.isEmpty() || totalExpense == 0) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillArc(x, y, size, size, 0, 360);
            return;
        }
        double startAngle = 0.0;
        int ci = 0;
        for (Map.Entry<String, Double> e : categoryExpenses.entrySet()) {
            double pct = e.getValue() / totalExpense;
            int arc = (int) Math.round(pct * 360);
            g2d.setColor(PIE_COLORS[ci % PIE_COLORS.length]);
            g2d.fillArc(x, y, size, size, (int) startAngle, arc);
            startAngle += arc;
            ci++;
        }
    }

    private void updateLegend() {
        legendPanel.removeAll();
        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            legendPanel.add(new JLabel("Chưa có dữ liệu"));
        } else {
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryExpenses.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int ci = 0;
            for (Map.Entry<String, Double> e : sorted) {
                double pct = totalExpense > 0 ? (e.getValue() / totalExpense * 100) : 0;
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                left.setBackground(Color.WHITE);
                JPanel box = new JPanel();
                box.setBackground(PIE_COLORS[ci % PIE_COLORS.length]);
                box.setPreferredSize(new Dimension(10, 10));
                left.add(box);
                left.add(new JLabel(e.getKey()));
                row.add(left, BorderLayout.WEST);
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                right.setBackground(Color.WHITE);
                right.add(new JLabel(String.format("%,.0f VND (%.1f%%)", e.getValue(), pct)));
                row.add(right, BorderLayout.EAST);
                legendPanel.add(row);
                ci++;
            }
        }
        legendPanel.revalidate();
        legendPanel.repaint();
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

        int marginLeft = 30, marginRight = 15, marginTop = 15, marginBottom = 30;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        // Vẽ trục
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(marginLeft, marginTop, marginLeft, h - marginBottom);
        g2d.drawLine(marginLeft, h - marginBottom, w - marginRight, h - marginBottom);

        // Vẽ đường và điểm
        if (labels.size() == 1) {
            // Chỉ có 1 điểm
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
            g2d.setStroke(new BasicStroke(2f));
            for (int i = 0; i < xs.length - 1; i++) {
                g2d.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
            }
            for (int i = 0; i < xs.length; i++) {
                g2d.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
            }
        }

        // Vẽ nhãn trục X
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        if (labels.size() <= 15) {
            for (int i = 0; i < labels.size(); i++) {
                int x = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
                String lbl = labels.get(i);
                int sw = g2d.getFontMetrics().stringWidth(lbl);
                g2d.drawString(lbl, x - sw / 2, h - marginBottom + 14);
            }
        } else {
            // Nếu quá nhiều nhãn, chỉ vẽ một vài nhãn
            int step = Math.max(1, labels.size() / 10);
            for (int i = 0; i < labels.size(); i += step) {
                int x = marginLeft + (int)((double)i / (labels.size() - 1) * chartW);
                String lbl = labels.get(i);
                int sw = g2d.getFontMetrics().stringWidth(lbl);
                g2d.drawString(lbl, x - sw / 2, h - marginBottom + 14);
            }
        }
    }
}