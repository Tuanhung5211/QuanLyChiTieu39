package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.util.EmojiUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel implements Observer {

    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month";
    private int currentOffset = 0;
    private String currentChartType = "pie";

    private JLabel lblMainTitle, lblTimeRange;
    private JPanel chartDrawPanel;
    private JButton btnPrevTime, btnNextTime;
    private JButton btnWeekTab, btnMonthTab, btnYearTab;
    private JButton btnPieChartToggle, btnLineChartToggle;

    private JTable rankingTable;
    private DefaultTableModel rankingModel;

    private boolean isVietnamese = true;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_MUTED = new Color(150, 150, 150);
    private final Color DANGER_RED = new Color(244, 67, 54);

    private static final Color[] CHART_COLORS = {
            new Color(46, 204, 113), new Color(52, 152, 219), new Color(155, 89, 182),
            new Color(230, 126, 34), new Color(241, 196, 15), new Color(231, 76, 60),
            new Color(26, 188, 156), new Color(149, 165, 166), new Color(243, 156, 18)
    };

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        if (statsService != null) this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Header
        JPanel topHeaderPanel = new JPanel(new BorderLayout());
        topHeaderPanel.setOpaque(false);

        lblMainTitle = new JLabel("Phân tích thống kê chi tiêu");
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMainTitle.setForeground(TEXT_PRIMARY);
        topHeaderPanel.add(lblMainTitle, BorderLayout.WEST);

        JPanel chartToggleWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chartToggleWrapper.setOpaque(false);
        btnPieChartToggle = createStyleNavButton("< Biểu đồ tròn", true);
        btnLineChartToggle = createStyleNavButton("Biểu đồ đường >", false);

        btnPieChartToggle.addActionListener(e -> { currentChartType = "pie"; toggleChartTypeButtons(true); chartDrawPanel.repaint(); });
        btnLineChartToggle.addActionListener(e -> { currentChartType = "line"; toggleChartTypeButtons(false); chartDrawPanel.repaint(); });

        chartToggleWrapper.add(btnPieChartToggle);
        chartToggleWrapper.add(btnLineChartToggle);
        topHeaderPanel.add(chartToggleWrapper, BorderLayout.EAST);
        add(topHeaderPanel, BorderLayout.NORTH);

        // Main grid
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // Left: Chart
        JPanel leftChartCard = new JPanel(new BorderLayout(0, 10));
        leftChartCard.setBackground(SURFACE_COLOR);
        leftChartCard.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true));
        leftChartCard.setPreferredSize(new Dimension(0, 0));

        JPanel timeNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        timeNavPanel.setOpaque(false);
        btnPrevTime = createInnerArrowButton("<");
        btnNextTime = createInnerArrowButton(">");
        lblTimeRange = new JLabel("---");
        lblTimeRange.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTimeRange.setForeground(ACCENT_YELLOW);

        btnPrevTime.addActionListener(e -> { currentOffset--; refreshData(); });
        btnNextTime.addActionListener(e -> { currentOffset++; refreshData(); });

        timeNavPanel.add(btnPrevTime);
        timeNavPanel.add(lblTimeRange);
        timeNavPanel.add(btnNextTime);
        leftChartCard.add(timeNavPanel, BorderLayout.NORTH);

        chartDrawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintCustomChart(g);
            }
        };
        chartDrawPanel.setBackground(SURFACE_COLOR);
        leftChartCard.add(chartDrawPanel, BorderLayout.CENTER);

        mainGbc.gridx = 0;
        mainGbc.weightx = 0.6;
        mainGrid.add(leftChartCard, mainGbc);

        // Right: Category Ranking Table
        JPanel rightCard = new JPanel(new BorderLayout(0, 10));
        rightCard.setBackground(SURFACE_COLOR);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        rightCard.setPreferredSize(new Dimension(0, 0));

        JPanel rightTabsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightTabsRow.setOpaque(false);
        btnWeekTab = createIntervalTabButton("Tuần", false);
        btnMonthTab = createIntervalTabButton("Tháng", true);
        btnYearTab = createIntervalTabButton("Năm", false);

        btnWeekTab.addActionListener(e -> { currentMode = "week"; currentOffset = 0; selectIntervalTab(btnWeekTab); refreshData(); });
        btnMonthTab.addActionListener(e -> { currentMode = "month"; currentOffset = 0; selectIntervalTab(btnMonthTab); refreshData(); });
        btnYearTab.addActionListener(e -> { currentMode = "year"; currentOffset = 0; selectIntervalTab(btnYearTab); refreshData(); });

        rightTabsRow.add(btnWeekTab);
        rightTabsRow.add(btnMonthTab);
        rightTabsRow.add(btnYearTab);
        rightCard.add(rightTabsRow, BorderLayout.NORTH);

        String[] columns = isVietnamese ? new String[]{"Danh mục", "Số tiền (VND)", "Tỷ lệ"} : new String[]{"Category", "Amount (VND)", "Percent"};
        rankingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        rankingTable = new JTable(rankingModel);
        rankingTable.setBackground(SURFACE_COLOR);
        rankingTable.setForeground(TEXT_PRIMARY);
        rankingTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rankingTable.getTableHeader().setBackground(INPUT_BG);
        rankingTable.getTableHeader().setForeground(TEXT_PRIMARY);
        rankingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        rankingTable.setRowHeight(28);
        rankingTable.setShowGrid(true);
        rankingTable.setGridColor(new Color(60,60,60));

        JScrollPane rankingScroll = new JScrollPane(rankingTable);
        rankingScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_YELLOW),
                isVietnamese ? "Xếp hạng danh mục chi tiêu" : "Category Ranking",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), ACCENT_YELLOW
        ));
        rightCard.add(rankingScroll, BorderLayout.CENTER);

        mainGbc.gridx = 1;
        mainGbc.weightx = 0.4;
        mainGbc.insets = new Insets(0, 20, 0, 0);
        mainGrid.add(rightCard, mainGbc);

        add(mainGrid, BorderLayout.CENTER);
    }

    public void refreshData() {
        if (statsService == null || financeService == null) return;

        LocalDate targetDate = calculateTargetDate();
        updateNavigationButtonsStatus();

        int month = targetDate.getMonthValue();
        int year = targetDate.getYear();

        List<Transaction> transactions = financeService.getAllTransactions();
        Map<String, Double> dataMap = new java.util.HashMap<>();
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if (t != null && t.getType() == TransactionType.EXPENSE && t.getDateTime().getYear() == year) {
                if ("month".equals(currentMode) && t.getDateTime().getMonthValue() != month) continue;
                String catName = t.getCategory().getName();
                dataMap.put(catName, dataMap.getOrDefault(catName, 0.0) + t.getAmount());
                totalExpense += t.getAmount();
            }
        }

        // Cập nhật bảng xếp hạng
        rankingModel.setRowCount(0);
        List<Map.Entry<String, Double>> sorted = dataMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        for (Map.Entry<String, Double> entry : sorted) {
            double amt = entry.getValue();
            double percent = totalExpense > 0 ? (amt / totalExpense) * 100 : 0;
            rankingModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format("%,.0f", amt),
                    String.format("%.2f%%", percent)
            });
        }

        // Vẽ lại biểu đồ
        chartDrawPanel.repaint();
    }

    // Các phương thức vẽ biểu đồ (giữ nguyên như cũ)
    private void paintCustomChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = chartDrawPanel.getWidth();
        int h = chartDrawPanel.getHeight();
        if (w <= 0 || h <= 0) return;

        LocalDate targetDate = calculateTargetDate();
        List<Transaction> transactions = financeService.getAllTransactions();

        if ("pie".equals(currentChartType)) {
            paintPieChart(g2, w, h, targetDate, transactions);
        } else {
            paintLineChart(g2, w, h, targetDate, transactions);
        }
    }

    private void paintPieChart(Graphics2D g2, int w, int h, LocalDate targetDate, List<Transaction> transactions) {
        Map<String, Double> dataMap = new java.util.HashMap<>();
        double total = 0;
        for (Transaction t : transactions) {
            if (t != null && t.getType() == TransactionType.EXPENSE && t.getDateTime().getYear() == targetDate.getYear()) {
                if ("month".equals(currentMode) && t.getDateTime().getMonthValue() != targetDate.getMonthValue()) continue;
                dataMap.put(t.getCategory().getName(), dataMap.getOrDefault(t.getCategory().getName(), 0.0) + t.getAmount());
                total += t.getAmount();
            }
        }
        if (total == 0) {
            g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2.drawString(isVietnamese ? "Không có dữ liệu trong kỳ" : "No data available in this period", w / 2 - 80, h / 2);
            return;
        }
        int size = Math.min(w, h) - 80;
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        int startAngle = 90;
        int ci = 0;
        List<Map.Entry<String, Double>> sortedPieEntries = dataMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        for (Map.Entry<String, Double> entry : sortedPieEntries) {
            int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
            g2.setColor(CHART_COLORS[ci % CHART_COLORS.length]);
            g2.fillArc(x, y, size, size, startAngle, arcAngle);
            startAngle += arcAngle;
            ci++;
        }
        int innerSize = (int) (size * 0.55);
        int innerX = x + (size - innerSize) / 2;
        int innerY = y + (size - innerSize) / 2;
        g2.setColor(SURFACE_COLOR);
        g2.fillOval(innerX, innerY, innerSize, innerSize);
        g2.setColor(TEXT_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        String centerTitle = isVietnamese ? "Tổng chi tiêu" : "Total Expenses";
        g2.drawString(centerTitle, w / 2 - g2.getFontMetrics().stringWidth(centerTitle) / 2, h / 2 - 12);
        g2.setColor(DANGER_RED);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        String totalStr = isVietnamese ? String.format("-%,.0f đ", total) : String.format("-%,.0f VND", total);
        g2.drawString(totalStr, w / 2 - g2.getFontMetrics().stringWidth(totalStr) / 2, h / 2 + 15);
    }

    private void paintLineChart(Graphics2D g2, int w, int h, LocalDate targetDate, List<Transaction> transactions) {
        int numPoints = 7;
        String[] xLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] timeValues = new double[numPoints];
        if ("week".equals(currentMode)) {
            LocalDate monday = targetDate.with(java.time.DayOfWeek.MONDAY);
            for (Transaction t : transactions) {
                if (t != null && t.getType() == TransactionType.EXPENSE) {
                    LocalDate tDate = t.getDateTime().toLocalDate();
                    if (!tDate.isBefore(monday) && tDate.isBefore(monday.plusDays(7))) {
                        int dayIndex = t.getDateTime().getDayOfWeek().getValue() - 1;
                        timeValues[dayIndex] += t.getAmount();
                    }
                }
            }
        } else if ("month".equals(currentMode)) {
            numPoints = targetDate.lengthOfMonth();
            xLabels = new String[numPoints];
            timeValues = new double[numPoints];
            for (int i = 0; i < numPoints; i++) xLabels[i] = String.valueOf(i + 1);
            for (Transaction t : transactions) {
                if (t != null && t.getType() == TransactionType.EXPENSE && t.getDateTime().getYear() == targetDate.getYear() && t.getDateTime().getMonthValue() == targetDate.getMonthValue()) {
                    int dayIndex = t.getDateTime().getDayOfMonth() - 1;
                    timeValues[dayIndex] += t.getAmount();
                }
            }
        } else {
            numPoints = 12;
            xLabels = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            timeValues = new double[numPoints];
            for (Transaction t : transactions) {
                if (t != null && t.getType() == TransactionType.EXPENSE && t.getDateTime().getYear() == targetDate.getYear()) {
                    int monthIndex = t.getDateTime().getMonthValue() - 1;
                    timeValues[monthIndex] += t.getAmount();
                }
            }
        }
        double maxValue = 0;
        for (double v : timeValues) if (v > maxValue) maxValue = v;
        double step = Math.ceil((maxValue / 2.0) / 50000.0) * 50000.0;
        if (step == 0) step = 50000.0;
        double ceilMaxValue = step * 2;
        int paddingLeft = 75, paddingRight = 25, paddingTop = 35, paddingBottom = 40;
        int chartW = w - paddingLeft - paddingRight, chartH = h - paddingTop - paddingBottom;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= 2; i++) {
            int yGrid = paddingTop + (i * chartH / 2);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 35));
            g2.drawLine(paddingLeft, yGrid, w - paddingRight, yGrid);
            double currentTickValue = ceilMaxValue - (i * step);
            String tickLabel = isVietnamese ? String.format("%,.0f đ", currentTickValue) : String.format("%,.0f", currentTickValue);
            g2.setColor(TEXT_MUTED);
            int labelW = g2.getFontMetrics().stringWidth(tickLabel);
            g2.drawString(tickLabel, paddingLeft - labelW - 8, yGrid + 4);
        }
        int[] pointsX = new int[numPoints], pointsY = new int[numPoints];
        int stepX = numPoints > 1 ? chartW / (numPoints - 1) : chartW;
        for (int i = 0; i < numPoints; i++) {
            pointsX[i] = paddingLeft + (i * stepX);
            pointsY[i] = (h - paddingBottom) - (int) ((timeValues[i] / ceilMaxValue) * chartH);
        }
        for (int i = 0; i < numPoints - 1; i++) {
            int[] polyX = {pointsX[i], pointsX[i + 1], pointsX[i + 1], pointsX[i]};
            int[] polyY = {pointsY[i], pointsY[i + 1], h - paddingBottom, h - paddingBottom};
            g2.setColor(new Color(255, 193, 7, 25));
            g2.fillPolygon(polyX, polyY, 4);
        }
        g2.setColor(ACCENT_YELLOW);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < numPoints - 1; i++) g2.drawLine(pointsX[i], pointsY[i], pointsX[i + 1], pointsY[i + 1]);
        for (int i = 0; i < numPoints; i++) {
            g2.setColor(ACCENT_YELLOW); g2.fillOval(pointsX[i] - 5, pointsY[i] - 5, 10, 10);
            g2.setColor(SURFACE_COLOR); g2.fillOval(pointsX[i] - 2, pointsY[i] - 2, 4, 4);
        }
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g2.setColor(TEXT_MUTED);
        for (int i = 0; i < numPoints; i++) {
            if ("month".equals(currentMode) && numPoints > 15 && i % 5 != 0 && i != numPoints - 1) continue;
            String label = xLabels[i];
            int labelW = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, pointsX[i] - labelW / 2, h - paddingBottom + 20);
        }
    }

    private LocalDate calculateTargetDate() {
        LocalDate targetDate = LocalDate.now();
        if ("week".equals(currentMode)) {
            targetDate = targetDate.plusWeeks(currentOffset);
            LocalDate monday = targetDate.with(java.time.DayOfWeek.MONDAY);
            LocalDate sunday = targetDate.with(java.time.DayOfWeek.SUNDAY);
            int weekNumber = targetDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            lblTimeRange.setText(isVietnamese ?
                    "Tuần " + weekNumber + " (" + monday.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")" :
                    "Week " + weekNumber + " (" + monday.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        } else if ("month".equals(currentMode)) {
            targetDate = targetDate.plusMonths(currentOffset);
            if (isVietnamese) lblTimeRange.setText("Tháng " + targetDate.getMonthValue() + "/" + targetDate.getYear());
            else lblTimeRange.setText(targetDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + targetDate.getYear());
        } else {
            targetDate = targetDate.plusYears(currentOffset);
            lblTimeRange.setText(isVietnamese ? "Năm " + targetDate.getYear() : "Year " + targetDate.getYear());
        }
        return targetDate;
    }

    private void updateNavigationButtonsStatus() {
        LocalDate earliest = financeService.getEarliestTransactionDate();
        LocalDate latest = financeService.getLatestTransactionDate();
        if (earliest == null || latest == null) {
            if (btnPrevTime != null) btnPrevTime.setEnabled(false);
            if (btnNextTime != null) btnNextTime.setEnabled(false);
        } else {
            LocalDate checkPrev = LocalDate.now();
            if ("week".equals(currentMode)) checkPrev = checkPrev.plusWeeks(currentOffset - 1);
            else if ("month".equals(currentMode)) checkPrev = checkPrev.plusMonths(currentOffset - 1);
            else checkPrev = checkPrev.plusYears(currentOffset - 1);
            btnPrevTime.setEnabled(!checkPrev.isBefore(earliest));
            LocalDate checkNext = LocalDate.now();
            if ("week".equals(currentMode)) checkNext = checkNext.plusWeeks(currentOffset + 1);
            else if ("month".equals(currentMode)) checkNext = checkNext.plusMonths(currentOffset + 1);
            else checkNext = checkNext.plusYears(currentOffset + 1);
            boolean isNextFuture = checkNext.isAfter(LocalDate.now());
            boolean isNextPastData = checkNext.isAfter(latest);
            btnNextTime.setEnabled(!isNextFuture && !isNextPastData);
        }
    }

    private JButton createStyleNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBackground(active ? INPUT_BG : SURFACE_COLOR);
        btn.setForeground(active ? ACCENT_YELLOW : TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void toggleChartTypeButtons(boolean isPieActive) {
        btnPieChartToggle.setBackground(isPieActive ? INPUT_BG : SURFACE_COLOR);
        btnPieChartToggle.setForeground(isPieActive ? ACCENT_YELLOW : TEXT_PRIMARY);
        btnLineChartToggle.setBackground(!isPieActive ? INPUT_BG : SURFACE_COLOR);
        btnLineChartToggle.setForeground(!isPieActive ? ACCENT_YELLOW : TEXT_PRIMARY);
    }

    private JButton createIntervalTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBackground(active ? ACCENT_YELLOW : INPUT_BG);
        btn.setForeground(active ? BG_COLOR : TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectIntervalTab(JButton target) {
        btnWeekTab.setBackground(INPUT_BG); btnWeekTab.setForeground(TEXT_PRIMARY);
        btnMonthTab.setBackground(INPUT_BG); btnMonthTab.setForeground(TEXT_PRIMARY);
        btnYearTab.setBackground(INPUT_BG); btnYearTab.setForeground(TEXT_PRIMARY);
        target.setBackground(ACCENT_YELLOW); target.setForeground(BG_COLOR);
    }

    private JButton createInnerArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(INPUT_BG);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        btn.setPreferredSize(new Dimension(32, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;
        if (lblMainTitle != null) lblMainTitle.setText(isVN ? "Phân tích thống kê chi tiêu" : "Expense Statistical Analysis");
        if (btnPieChartToggle != null) btnPieChartToggle.setText(isVN ? "< Biểu đồ tròn" : "< Donut Chart");
        if (btnLineChartToggle != null) btnLineChartToggle.setText(isVN ? "Biểu đồ đường >" : "Line Chart >");
        if (btnWeekTab != null) btnWeekTab.setText(isVN ? "Tuần" : "Week");
        if (btnMonthTab != null) btnMonthTab.setText(isVN ? "Tháng" : "Month");
        if (btnYearTab != null) btnYearTab.setText(isVN ? "Năm" : "Year");

        // Cập nhật tiêu đề bảng
        if (rankingTable != null) {
            String[] columns = isVN ? new String[]{"Danh mục", "Số tiền (VND)", "Tỷ lệ"} : new String[]{"Category", "Amount (VND)", "Percent"};
            rankingModel.setColumnIdentifiers(columns);
        }
        refreshData();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }
}