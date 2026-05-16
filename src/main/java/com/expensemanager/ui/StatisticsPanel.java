package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel implements Observer {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month";
    private int currentOffset = 0;
    private String currentChartType = "pie";

    private static final Color[] PIE_COLORS = {
            new Color(46, 204, 113),  // Xanh lá neon
            new Color(52, 152, 219),  // Xanh dương dịu
            new Color(230, 126, 34),  // Cam rực
            new Color(241, 196, 15),  // Vàng hổ phách
            new Color(155, 89, 182),  // Tím ánh quang
            new Color(231, 76, 60),   // Đỏ phản quang
            new Color(26, 188, 156),  // Xanh ngọc
            new Color(0, 153, 76),    // Xanh lá đậm
            new Color(255, 159, 243), // Hồng dạ quang
            new Color(149, 165, 166)  // Xám khói
    };

    private JPanel pieChartPanel;
    private JPanel lineChartPanel;
    private JPanel legendPanel;
    private JLabel lblTimeRange;
    private JButton btnPrevTime, btnNextTime;
    private JButton btnPrevChart, btnNextChart;
    private JButton btnWeek, btnMonth, btnYear;

    private Map<String, Double> categoryExpenses;
    private double totalExpense;
    private Map<String, Double> timeSeriesData;

    private JPanel chartSwitcher;
    private CardLayout chartCardLayout;

    // Hệ màu sắc phẳng Flat Dark Mode đồng bộ hệ thống chính
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_MUTED = new Color(150, 150, 150);
    private final Color BORDER_COLOR = new Color(55, 55, 55);

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // --- KHU VỰC TIÊU ĐỀ TRÊN CÙNG ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));

        JLabel title = new JLabel("Phân tích thống kê chi tiêu", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JPanel chartTogglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chartTogglePanel.setBackground(BG_COLOR);
        btnPrevChart = createArrowButton("<  Biểu đồ tròn");
        btnNextChart = createArrowButton("Biểu đồ đường  >");
        btnPrevChart.addActionListener(e -> switchChartType("pie"));
        btnNextChart.addActionListener(e -> switchChartType("line"));
        chartTogglePanel.add(btnPrevChart);
        chartTogglePanel.add(btnNextChart);
        headerPanel.add(chartTogglePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- KHU VỰC KHỐI NỘI DUNG CHÍNH (Đã mở rộng tối đa không gian biểu đồ) ---
        JPanel mainPanel = new JPanel(new BorderLayout(25, 0));
        mainPanel.setOpaque(false);

        // 🌟 KHỐI TRÁI UNIFIED CARD: Gom Header thời gian nằm TRÙNG khít khung biểu đồ
        JPanel leftCard = new JPanel(new BorderLayout());
        leftCard.setBackground(SURFACE_COLOR);
        leftCard.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        JPanel leftHeaderBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8)); // Căn GIỮA tuyệt đối
        leftHeaderBox.setBackground(SURFACE_COLOR);
        leftHeaderBox.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        btnPrevTime = createArrowButton("◀");
        btnNextTime = createArrowButton("▶");
        btnPrevTime.addActionListener(e -> { currentOffset--; refreshData(); });
        btnNextTime.addActionListener(e -> { currentOffset++; refreshData(); });

        lblTimeRange = new JLabel("", SwingConstants.CENTER);
        lblTimeRange.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTimeRange.setForeground(ACCENT_YELLOW);
        lblTimeRange.setPreferredSize(new Dimension(280, 30));

        leftHeaderBox.add(btnPrevTime);
        leftHeaderBox.add(lblTimeRange);
        leftHeaderBox.add(btnNextTime);
        leftCard.add(leftHeaderBox, BorderLayout.NORTH);

        // CardLayout chuyển đổi biểu đồ lồng bên trong khối trái
        chartCardLayout = new CardLayout();
        chartSwitcher = new JPanel(chartCardLayout);
        chartSwitcher.setOpaque(false);

        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDonutChart(g);
            }
        };
        pieChartPanel.setBackground(SURFACE_COLOR);

        lineChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLineChart(g);
            }
        };
        lineChartPanel.setBackground(SURFACE_COLOR);

        chartSwitcher.add(pieChartPanel, "pie");
        chartSwitcher.add(lineChartPanel, "line");
        leftCard.add(chartSwitcher, BorderLayout.CENTER);

        // 🌟 KHỐI PHẢI UNIFIED CARD: Gom Header Tuần/Tháng/Năm TRÙNG khít khung danh mục
        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setBackground(SURFACE_COLOR);
        rightCard.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        rightCard.setPreferredSize(new Dimension(340, 0)); // Khống chế chiều rộng vừa vặn mẫu

        JPanel rightHeaderBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8)); // Căn GIỮA tuyệt đối
        rightHeaderBox.setBackground(SURFACE_COLOR);
        rightHeaderBox.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        btnWeek = createModeButton("Tuần");
        btnMonth = createModeButton("Tháng");
        btnYear = createModeButton("Năm");
        updateModeButtonStyle(btnMonth, true);

        btnWeek.addActionListener(e -> switchMode("week"));
        btnMonth.addActionListener(e -> switchMode("month"));
        btnYear.addActionListener(e -> switchMode("year"));

        rightHeaderBox.add(btnWeek);
        rightHeaderBox.add(btnMonth);
        rightHeaderBox.add(btnYear);
        rightCard.add(rightHeaderBox, BorderLayout.NORTH);

        // Danh sách cuốn dọc danh mục lồng bên trong khối phải
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(SURFACE_COLOR);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(null); // Gỡ viền trùng lặp
        legendScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        legendScroll.getViewport().setBackground(SURFACE_COLOR);
        rightCard.add(legendScroll, BorderLayout.CENTER);

        // Đẩy vào luồng phân bổ (Khối trái bung rộng ở CENTER, Khối phải thu gọn ở EAST)
        mainPanel.add(leftCard, BorderLayout.CENTER);
        mainPanel.add(rightCard, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        refreshData();
    }

    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(INPUT_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateModeButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(ACCENT_YELLOW);
            btn.setForeground(BG_COLOR);
            btn.setBorder(BorderFactory.createEmptyBorder(7, 19, 7, 19));
        } else {
            btn.setBackground(INPUT_BG);
            btn.setForeground(TEXT_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                    BorderFactory.createEmptyBorder(6, 18, 6, 18)));
        }
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SURFACE_COLOR);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 55), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent evt) { btn.setBackground(INPUT_BG); btn.setForeground(ACCENT_YELLOW); }
            @Override public void mouseExited(MouseEvent evt) { btn.setBackground(SURFACE_COLOR); btn.setForeground(TEXT_PRIMARY); }
        });
        return btn;
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        this.currentOffset = 0;
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
            case "week": {
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(currentOffset);
                end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                int weekNumber = start.get(WeekFields.of(Locale.getDefault()).weekOfYear());
                timeLabel = String.format("Tuần %d (%s - %s)", weekNumber,
                        start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                        end.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                break;
            }
            case "year": {
                start = now.withDayOfYear(1).plusYears(currentOffset);
                end = start.withDayOfYear(start.lengthOfYear());
                timeLabel = String.format("Năm %d", start.getYear());
                break;
            }
            default: {
                YearMonth ym = YearMonth.of(now.getYear(), now.getMonthValue()).plusMonths(currentOffset);
                start = ym.atDay(1);
                end = ym.atEndOfMonth();
                timeLabel = String.format("Tháng %d/%d", ym.getMonthValue(), ym.getYear());
                break;
            }
        }

        lblTimeRange.setText(timeLabel);

        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start))
                .filter(t -> !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        totalExpense = filtered.stream().mapToDouble(Transaction::getAmount).sum();

        categoryExpenses = filtered.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                        Collectors.summingDouble(Transaction::getAmount)));

        timeSeriesData = new LinkedHashMap<>();
        switch (currentMode) {
            case "week":
                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1))
                    timeSeriesData.put(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), 0.0);
                break;
            case "year":
                for (int m = 1; m <= 12; m++) timeSeriesData.put("T" + m, 0.0);
                break;
            default:
                for (int d = 1; d <= end.getDayOfMonth(); d++) timeSeriesData.put(String.valueOf(d), 0.0);
        }

        for (Transaction t : filtered) {
            String key;
            LocalDate date = t.getDateTime().toLocalDate();
            if (currentMode.equals("week")) key = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            else if (currentMode.equals("year")) key = "T" + date.getMonthValue();
            else key = String.valueOf(date.getDayOfMonth());
            timeSeriesData.merge(key, t.getAmount(), Double::sum);
        }

        pieChartPanel.repaint();
        lineChartPanel.repaint();
        updateLegend();
    }

    private void drawDonutChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = pieChartPanel.getWidth(), h = pieChartPanel.getHeight();
        int size = Math.min(w, h) - 50;
        int x = (w - size) / 2, y = (h - size) / 2;

        if (categoryExpenses == null || categoryExpenses.isEmpty() || totalExpense == 0) {
            g2d.setColor(BORDER_COLOR);
            g2d.fillArc(x, y, size, size, 0, 360);
            drawDonutHoleAndText(g2d, x, y, size, 0);
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
        drawDonutHoleAndText(g2d, x, y, size, totalExpense);
    }

    private void drawDonutHoleAndText(Graphics2D g2d, int x, int y, int size, double amount) {
        int holeSize = (int) (size * 0.58);
        int hx = x + (size - holeSize) / 2;
        int hy = y + (size - holeSize) / 2;
        g2d.setColor(SURFACE_COLOR);
        g2d.fillOval(hx, hy, holeSize, holeSize);

        g2d.setColor(TEXT_MUTED);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String titleStr = "Tổng chi tiêu";
        int titleW = g2d.getFontMetrics().stringWidth(titleStr);
        g2d.drawString(titleStr, x + size / 2 - titleW / 2, y + size / 2 - 5);

        g2d.setColor(new Color(255, 82, 82));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String amountStr = String.format("-%,.0f đ", amount);
        int amountW = g2d.getFontMetrics().stringWidth(amountStr);
        g2d.drawString(amountStr, x + size / 2 - amountW / 2, y + size / 2 + 20);
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

        int marginLeft = 65, marginRight = 25, marginTop = 40, marginBottom = 50;
        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString(String.format("Tổng: %,.0f đ", totalExpense), marginLeft, marginTop - 15);
        double avg = labels.isEmpty() ? 0 : totalExpense / labels.size();
        g2d.setColor(TEXT_MUTED);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString(String.format("Trung bình: %,.1f đ", avg), marginLeft + 180, marginTop - 15);

        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f}, 0.0f));
        for (int i = 0; i <= 5; i++) {
            int y = marginTop + (chartH * i / 5);
            g2d.drawLine(marginLeft, y, w - marginRight, y);

            double valY = maxVal - (maxVal * i / 5);
            String valStr = String.format("%,.0f", valY);
            g2d.setColor(TEXT_MUTED);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            int strW = g2d.getFontMetrics().stringWidth(valStr);
            g2d.drawString(valStr, marginLeft - strW - 8, y + 4);
            g2d.setColor(new Color(60, 60, 60));
        }

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(BORDER_COLOR);
        g2d.drawLine(marginLeft, h - marginBottom, w - marginRight, h - marginBottom);

        g2d.setColor(TEXT_MUTED);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        int step = (labels.size() > 15) ? Math.max(1, labels.size() / 12) : 1;
        for (int i = 0; i < labels.size(); i += step) {
            int x = marginLeft + (int) ((double) i / (labels.size() - 1) * chartW);
            String lbl = labels.get(i);
            int sw = g2d.getFontMetrics().stringWidth(lbl);

            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.translate(x - sw / 2, h - marginBottom + 16);
            g2dRotated.rotate(Math.toRadians(-30));
            g2dRotated.drawString(lbl, 0, 0);
            g2dRotated.dispose();
        }

        if (labels.size() > 1) {
            int[] xs = new int[labels.size()];
            int[] ys = new int[labels.size()];
            for (int i = 0; i < labels.size(); i++) {
                xs[i] = marginLeft + (int) ((double) i / (labels.size() - 1) * chartW);
                ys[i] = marginTop + (int) ((1 - values.get(i) / maxVal) * chartH);
            }

            g2d.setColor(ACCENT_YELLOW);
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawPolyline(xs, ys, labels.size());

            for (int i = 0; i < xs.length; i++) {
                g2d.setColor(SURFACE_COLOR);
                g2d.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2d.setColor(ACCENT_YELLOW);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
            }
        }
    }

    private void updateLegend() {
        legendPanel.removeAll();
        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu chi tiêu");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            empty.setForeground(TEXT_MUTED);
            legendPanel.add(empty);
        } else {
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryExpenses.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int ci = 0;
            for (Map.Entry<String, Double> e : sorted) {
                double pct = totalExpense > 0 ? (e.getValue() / totalExpense * 100) : 0;

                // Khối bao bọc một hàng thông tin danh mục
                JPanel rowWrapper = new JPanel();
                rowWrapper.setLayout(new BoxLayout(rowWrapper, BoxLayout.Y_AXIS));
                rowWrapper.setOpaque(false);
                rowWrapper.setBorder(BorderFactory.createEmptyBorder(6, 5, 6, 5));
                // 🌟 QUAN TRỌNG: Khống chế cứng chiều cao tối đa của hàng tránh lỗi giãn cách
                rowWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                // Hàng trên cùng (Tên + % nằm trái, Số tiền nằm phải)
                JPanel topRow = new JPanel(new BorderLayout());
                topRow.setOpaque(false);

                JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                leftBox.setOpaque(false);

                JPanel colorBox = new JPanel();
                colorBox.setBackground(PIE_COLORS[ci % PIE_COLORS.length]);
                colorBox.setPreferredSize(new Dimension(14, 14));
                leftBox.add(colorBox);

                JLabel lblName = new JLabel(String.format("%s  %.1f%%", e.getKey(), pct));
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
                lblName.setForeground(TEXT_PRIMARY);
                leftBox.add(lblName);
                topRow.add(leftBox, BorderLayout.WEST);

                JLabel lblAmount = new JLabel(String.format("%,.0f đ", e.getValue()));
                lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
                lblAmount.setForeground(TEXT_PRIMARY);
                topRow.add(lblAmount, BorderLayout.EAST);

                rowWrapper.add(topRow);
                rowWrapper.add(Box.createVerticalStrut(6)); // Tạo khoảng đệm nhỏ tinh tế giữa chữ và thanh progress

                // Thanh tiến trình màu vàng ôm khít ngay dưới dòng chữ
                final double percentValue = pct;
                JPanel barPanel = new JPanel(null) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(50, 50, 50));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        g2.setColor(ACCENT_YELLOW);
                        int fillWidth = (int) (getWidth() * (percentValue / 100.0));
                        g2.fillRoundRect(0, 0, fillWidth, getHeight(), 6, 6);
                    }
                };
                barPanel.setPreferredSize(new Dimension(0, 6));
                barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
                barPanel.setOpaque(false);
                rowWrapper.add(barPanel);

                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(45, 45, 45));
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

                legendPanel.add(rowWrapper);
                legendPanel.add(separator);
                ci++;
            }
            // 🌟 LÒ XO ĐỆM: Hút toàn bộ khoảng trống thừa dồn xuống đáy panel, giữ danh sách luôn khít ở top trên
            legendPanel.add(Box.createVerticalGlue());
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED ||
                eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED ||
                eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(() -> refreshData());
        }
    }
}