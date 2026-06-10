package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.util.CategoryTranslator;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel implements Observer {

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private String currentMode = "month";
    private int currentOffset = 0;
    private String currentChartType = "pie";

    private JLabel lblMainTitle, lblTimeRange;
    private JPanel chartDrawPanel, legendPanel;
    private JButton btnPrevTime, btnNextTime;
    private JButton btnWeekTab, btnMonthTab, btnYearTab;
    private JButton btnPieChartToggle, btnLineChartToggle;

    private boolean isVietnamese = true;

    // Dữ liệu chung cho legend và biểu đồ (đã lọc và đã gom nhóm top 9 + Khác)
    private ChartData currentChartData = null;

    // =====================================================================
    // 2. CONSTRUCTOR - KHỞI TẠO BỐ CỤC
    // =====================================================================
    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        if (statsService != null) this.financeService = statsService.getFinanceService();

        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        initComponents();
        applyTheme();
    }

    private void initComponents() {
        // --- HEADER ---
        JPanel topHeaderPanel = new JPanel(new BorderLayout());
        topHeaderPanel.setOpaque(false);

        lblMainTitle = new JLabel("Phân tích thống kê chi tiêu");
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
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

        // --- KHUNG TRUNG TÂM ---
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // CỘT TRÁI: ĐỒ THỊ
        JPanel leftChartCard = new JPanel(new BorderLayout(0, 10));
        leftChartCard.setName("surfacePanel");
        leftChartCard.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        leftChartCard.setPreferredSize(new Dimension(0, 0));

        JPanel timeNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        timeNavPanel.setOpaque(false);
        btnPrevTime = createInnerArrowButton("<");
        btnNextTime = createInnerArrowButton(">");
        lblTimeRange = new JLabel("---");
        lblTimeRange.setFont(new Font("Segoe UI", Font.BOLD, 15));

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
        leftChartCard.add(chartDrawPanel, BorderLayout.CENTER);

        mainGbc.gridx = 0;
        mainGbc.weightx = 0.65;
        mainGrid.add(leftChartCard, mainGbc);

        // CỘT PHẢI: CHÚ THÍCH
        JPanel rightLegendCard = new JPanel(new BorderLayout(0, 15));
        rightLegendCard.setName("surfacePanel");
        rightLegendCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        rightLegendCard.setPreferredSize(new Dimension(0, 0));

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
        rightLegendCard.add(rightTabsRow, BorderLayout.NORTH);

        legendPanel = new JPanel(new GridBagLayout());

        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(null);
        legendScroll.setOpaque(false);
        legendScroll.getViewport().setOpaque(false);
        legendScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        legendScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        legendScroll.getVerticalScrollBar().setUnitIncrement(32);

        rightLegendCard.add(legendScroll, BorderLayout.CENTER);

        mainGbc.gridx = 1;
        mainGbc.weightx = 0.35;
        mainGbc.insets = new Insets(0, 20, 0, 0);
        mainGrid.add(rightLegendCard, mainGbc);

        add(mainGrid, BorderLayout.CENTER);
    }

    // =====================================================================
    // 3. XỬ LÝ DỮ LIỆU
    // =====================================================================
    public void refreshData() {
        if (statsService == null || financeService == null) return;

        LocalDate targetDate = calculateTargetDate();
        updateNavigationButtonsStatus();

        // Lọc giao dịch theo chế độ
        List<Transaction> allTx = financeService.getAllTransactions();
        List<Transaction> filtered = filterTransactions(allTx, targetDate);

        // Xây dựng dữ liệu top 9 + Khác (giữ originalName để tra cứu emoji)
        currentChartData = buildChartData(filtered);

        // Cập nhật legend
        renderLegend(currentChartData);

        // Vẽ lại biểu đồ
        chartDrawPanel.repaint();
    }

    private List<Transaction> filterTransactions(List<Transaction> transactions, LocalDate targetDate) {
        List<Transaction> result = new ArrayList<>();
        if (transactions == null) return result;

        if ("week".equals(currentMode)) {
            LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
            LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);
            for (Transaction t : transactions) {
                if (t == null) continue;
                LocalDate tDate = t.getDateTime().toLocalDate();
                if (!tDate.isBefore(monday) && !tDate.isAfter(sunday)) {
                    result.add(t);
                }
            }
        } else if ("month".equals(currentMode)) {
            int month = targetDate.getMonthValue();
            int year = targetDate.getYear();
            for (Transaction t : transactions) {
                if (t == null) continue;
                LocalDate tDate = t.getDateTime().toLocalDate();
                if (tDate.getYear() == year && tDate.getMonthValue() == month) {
                    result.add(t);
                }
            }
        } else if ("year".equals(currentMode)) {
            int year = targetDate.getYear();
            for (Transaction t : transactions) {
                if (t == null) continue;
                if (t.getDateTime().getYear() == year) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    private ChartData buildChartData(List<Transaction> transactions) {
        // Gom nhóm theo tên gốc (tiếng Việt) để giữ nguyên key cho emoji
        Map<String, Double> catMap = new LinkedHashMap<>();
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                String originalName = t.getCategory().getName();
                double amt = t.getAmount();
                catMap.put(originalName, catMap.getOrDefault(originalName, 0.0) + amt);
                total += amt;
            }
        }

        // Sắp xếp giảm dần theo số tiền
        List<Map.Entry<String, Double>> sorted = catMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Top 9 + Khác, đồng thời dịch tên hiển thị
        List<ChartItem> items = new ArrayList<>();
        double others = 0;
        int count = 0;
        for (Map.Entry<String, Double> entry : sorted) {
            String originalName = entry.getKey();
            double amt = entry.getValue();
            if (count < 9) {
                double pct = total > 0 ? (amt / total) * 100 : 0;
                String displayName = CategoryTranslator.translate(originalName, isVietnamese);
                items.add(new ChartItem(originalName, displayName, amt, pct));
                count++;
            } else {
                others += amt;
            }
        }
        if (others > 0) {
            double pctOthers = total > 0 ? (others / total) * 100 : 0;
            String othersKey = isVietnamese ? "Khác" : "Others";
            items.add(new ChartItem(othersKey, othersKey, others, pctOthers)); // originalName = displayName cho mục Khác
        }

        return new ChartData(items, total);
    }

    private void renderLegend(ChartData data) {
        legendPanel.removeAll();
        if (data == null || data.items.isEmpty()) {
            legendPanel.revalidate();
            legendPanel.repaint();
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.insets = new Insets(0, 0, 2, 0);

        int ci = 0;
        for (ChartItem item : data.items) {
            Color color = (item.displayName.equals("Khác") || item.displayName.equals("Others"))
                    ? ThemeManager.getColor("textSecondary")
                    : ThemeManager.getColor("chart" + (ci % 9));

            JPanel itemContainer = new JPanel(new BorderLayout(0, 6));
            itemContainer.setOpaque(false);
            itemContainer.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

            JPanel textRow = new JPanel(new BorderLayout());
            textRow.setOpaque(false);

            JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            leftGroup.setOpaque(false);

            JPanel indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(12, 12));
            indicator.setBackground(color);
            leftGroup.add(indicator);

            // Emoji: dùng originalName để tra cứu, fallback cho mục Khác
            String emoji;
            if (item.displayName.equals("Khác") || item.displayName.equals("Others")) {
                emoji = "📦";
            } else {
                emoji = EmojiUtil.CATEGORY_EMOJI.getOrDefault(item.originalName, "\uD83D\uDCCD");
            }
            JLabel lblEmoji = new JLabel(emoji);
            lblEmoji.setFont(EmojiUtil.getEmojiFont(15));
            leftGroup.add(lblEmoji);

            JLabel lblCatText = new JLabel(item.displayName + " " + String.format("%.1f%%", item.percent));
            lblCatText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblCatText.setForeground(ThemeManager.getColor("textPrimary"));
            leftGroup.add(lblCatText);
            textRow.add(leftGroup, BorderLayout.WEST);

            JLabel lblAmount = new JLabel(isVietnamese ? String.format("%,.0f đ", item.amount) : String.format("%,.0f VND", item.amount));
            lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblAmount.setForeground(ThemeManager.getColor("textPrimary"));
            textRow.add(lblAmount, BorderLayout.EAST);

            itemContainer.add(textRow, BorderLayout.NORTH);

            JPanel progressLine = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(ThemeManager.getColor("progressTrack"));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2d.setColor(color);
                    int fillW = (int) (getWidth() * (item.percent / 100.0));
                    g2d.fillRoundRect(0, 0, fillW, getHeight(), 4, 4);
                }
            };
            progressLine.setPreferredSize(new Dimension(0, 5));
            progressLine.setOpaque(false);
            itemContainer.add(progressLine, BorderLayout.CENTER);

            JPanel borderWrapper = new JPanel(new BorderLayout());
            borderWrapper.setOpaque(false);
            borderWrapper.add(itemContainer, BorderLayout.CENTER);

            JSeparator separator = new JSeparator();
            separator.setForeground(ThemeManager.getColor("border"));
            borderWrapper.add(separator, BorderLayout.SOUTH);

            borderWrapper.setPreferredSize(new Dimension(100, 60));
            gbc.gridy = ci;
            legendPanel.add(borderWrapper, gbc);
            ci++;
        }

        GridBagConstraints pushGbc = new GridBagConstraints();
        pushGbc.gridx = 0; pushGbc.gridy = ci; pushGbc.weightx = 1.0; pushGbc.weighty = 1.0; pushGbc.fill = GridBagConstraints.BOTH;
        JPanel verticalFiller = new JPanel();
        verticalFiller.setOpaque(false);
        legendPanel.add(verticalFiller, pushGbc);

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // ============== VẼ BIỂU ĐỒ ==============
    private void paintCustomChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = chartDrawPanel.getWidth();
        int h = chartDrawPanel.getHeight();
        if (w <= 0 || h <= 0) return;

        if ("pie".equals(currentChartType)) {
            paintPieChart(g2, w, h);
        } else {
            paintLineChart(g2, w, h);
        }
    }

    private void paintPieChart(Graphics2D g2, int w, int h) {
        if (currentChartData == null || currentChartData.items.isEmpty() || currentChartData.total <= 0) {
            g2.setColor(ThemeManager.getColor("textSecondary"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            String msg = isVietnamese ? "Không có dữ liệu trong kỳ" : "No data available in this period";
            g2.drawString(msg, w / 2 - 80, h / 2);
            return;
        }

        int size = Math.min(w, h) - 80;
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        int startAngle = 90;
        int ci = 0;

        for (ChartItem item : currentChartData.items) {
            int arcAngle = (int) Math.round((item.amount / currentChartData.total) * 360);
            if (item.displayName.equals("Khác") || item.displayName.equals("Others")) {
                g2.setColor(ThemeManager.getColor("textSecondary"));
            } else {
                g2.setColor(ThemeManager.getColor("chart" + (ci % 9)));
            }
            g2.fillArc(x, y, size, size, startAngle, arcAngle);
            startAngle += arcAngle;
            ci++;
        }

        // Lỗ tròn trung tâm
        int innerSize = (int) (size * 0.55);
        int innerX = x + (size - innerSize) / 2;
        int innerY = y + (size - innerSize) / 2;
        g2.setColor(ThemeManager.getColor("surface"));
        g2.fillOval(innerX, innerY, innerSize, innerSize);

        // Chữ tổng chi tiêu
        g2.setColor(ThemeManager.getColor("textSecondary"));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        String centerTitle = isVietnamese ? "Tổng chi tiêu" : "Total Expenses";
        g2.drawString(centerTitle, w / 2 - g2.getFontMetrics().stringWidth(centerTitle) / 2, h / 2 - 12);

        g2.setColor(ThemeManager.getColor("danger"));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        String totalStr = isVietnamese ? String.format("-%,.0f đ", currentChartData.total) : String.format("-%,.0f VND", currentChartData.total);
        g2.drawString(totalStr, w / 2 - g2.getFontMetrics().stringWidth(totalStr) / 2, h / 2 + 15);
    }

    private void paintLineChart(Graphics2D g2, int w, int h) {
        LocalDate targetDate = calculateTargetDate();
        List<Transaction> transactions = filterTransactions(financeService.getAllTransactions(), targetDate);

        int numPoints = 7;
        String[] xLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] timeValues = new double[numPoints];

        if ("week".equals(currentMode)) {
            LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
            LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);
            for (Transaction t : transactions) {
                if (t.getType() == TransactionType.EXPENSE) {
                    LocalDate tDate = t.getDateTime().toLocalDate();
                    if (!tDate.isBefore(monday) && !tDate.isAfter(sunday)) {
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
            int month = targetDate.getMonthValue();
            int year = targetDate.getYear();
            for (Transaction t : transactions) {
                if (t.getType() == TransactionType.EXPENSE) {
                    LocalDate tDate = t.getDateTime().toLocalDate();
                    if (tDate.getYear() == year && tDate.getMonthValue() == month) {
                        int dayIndex = tDate.getDayOfMonth() - 1;
                        timeValues[dayIndex] += t.getAmount();
                    }
                }
            }
        } else { // year
            numPoints = 12;
            xLabels = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            timeValues = new double[numPoints];
            int year = targetDate.getYear();
            for (Transaction t : transactions) {
                if (t.getType() == TransactionType.EXPENSE) {
                    if (t.getDateTime().getYear() == year) {
                        int monthIndex = t.getDateTime().getMonthValue() - 1;
                        timeValues[monthIndex] += t.getAmount();
                    }
                }
            }
        }

        double maxValue = Arrays.stream(timeValues).max().orElse(0);
        double step = Math.ceil((maxValue / 2.0) / 50000.0) * 50000.0;
        if (step == 0) step = 50000.0;
        double ceilMaxValue = step * 2;

        int paddingLeft = 75; int paddingRight = 25; int paddingTop = 35; int paddingBottom = 40;
        int chartW = w - paddingLeft - paddingRight; int chartH = h - paddingTop - paddingBottom;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= 2; i++) {
            int yGrid = paddingTop + (i * chartH / 2);
            g2.setStroke(new BasicStroke(1f));
            Color border = ThemeManager.getColor("border");
            g2.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), 100));
            g2.drawLine(paddingLeft, yGrid, w - paddingRight, yGrid);

            double currentTickValue = ceilMaxValue - (i * step);
            String tickLabel = isVietnamese ? String.format("%,.0f đ", currentTickValue) : String.format("%,.0f", currentTickValue);
            g2.setColor(ThemeManager.getColor("textSecondary"));
            int labelW = g2.getFontMetrics().stringWidth(tickLabel);
            g2.drawString(tickLabel, paddingLeft - labelW - 8, yGrid + 4);
        }

        int[] pointsX = new int[numPoints]; int[] pointsY = new int[numPoints];
        int stepX = numPoints > 1 ? chartW / (numPoints - 1) : chartW;

        for (int i = 0; i < numPoints; i++) {
            pointsX[i] = paddingLeft + (i * stepX);
            pointsY[i] = (h - paddingBottom) - (int) ((timeValues[i] / ceilMaxValue) * chartH);
        }

        Color accent = ThemeManager.getColor("accent");
        for (int i = 0; i < numPoints - 1; i++) {
            int[] polyX = {pointsX[i], pointsX[i + 1], pointsX[i + 1], pointsX[i]};
            int[] polyY = {pointsY[i], pointsY[i + 1], h - paddingBottom, h - paddingBottom};
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
            g2.fillPolygon(polyX, polyY, 4);
        }

        g2.setColor(accent);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < numPoints - 1; i++) {
            g2.drawLine(pointsX[i], pointsY[i], pointsX[i + 1], pointsY[i + 1]);
        }

        for (int i = 0; i < numPoints; i++) {
            g2.setColor(accent); g2.fillOval(pointsX[i] - 5, pointsY[i] - 5, 10, 10);
            g2.setColor(ThemeManager.getColor("surface")); g2.fillOval(pointsX[i] - 2, pointsY[i] - 2, 4, 4);
        }

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g2.setColor(ThemeManager.getColor("textSecondary"));
        for (int i = 0; i < numPoints; i++) {
            if ("month".equals(currentMode) && numPoints > 15 && i % 5 != 0 && i != numPoints - 1) continue;
            String label = xLabels[i];
            int labelW = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, pointsX[i] - labelW / 2, h - paddingBottom + 20);
        }
    }

    // =====================================================================
    // 4. CÁC HÀM TIỆN ÍCH VÀ ĐIỀU HƯỚNG
    // =====================================================================
    private LocalDate calculateTargetDate() {
        LocalDate targetDate = LocalDate.now();
        if ("week".equals(currentMode)) {
            targetDate = targetDate.plusWeeks(currentOffset);
            LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
            LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekNumber = targetDate.get(weekFields.weekOfWeekBasedYear());
            lblTimeRange.setText(isVietnamese ?
                    "Tuần " + weekNumber + " (" + monday.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")" :
                    "Week " + weekNumber + " (" + monday.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        } else if ("month".equals(currentMode)) {
            targetDate = targetDate.plusMonths(currentOffset);
            if (isVietnamese) {
                lblTimeRange.setText("Tháng " + targetDate.getMonthValue() + "/" + targetDate.getYear());
            } else {
                String monthName = targetDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
                lblTimeRange.setText(monthName + " " + targetDate.getYear());
            }
        } else {
            targetDate = targetDate.plusYears(currentOffset);
            lblTimeRange.setText(isVietnamese ? "Năm " + targetDate.getYear() : "Year " + targetDate.getYear());
        }
        return targetDate;
    }

    private void updateNavigationButtonsStatus() {
        LocalDate earliestTxDate = financeService.getEarliestTransactionDate();
        LocalDate latestTxDate = financeService.getLatestTransactionDate();
        LocalDate realTimeNow = LocalDate.now();

        if (earliestTxDate == null || latestTxDate == null) {
            if (btnPrevTime != null) btnPrevTime.setEnabled(false);
            if (btnNextTime != null) btnNextTime.setEnabled(false);
        } else {
            LocalDate checkPrev = LocalDate.now();
            if ("week".equals(currentMode)) checkPrev = realTimeNow.plusWeeks(currentOffset - 1);
            else if ("month".equals(currentMode)) checkPrev = realTimeNow.plusMonths(currentOffset - 1);
            else checkPrev = realTimeNow.plusYears(currentOffset - 1);

            if ("week".equals(currentMode)) {
                btnPrevTime.setEnabled(!checkPrev.with(DayOfWeek.SUNDAY).isBefore(earliestTxDate.with(DayOfWeek.MONDAY)));
            } else if ("month".equals(currentMode)) {
                btnPrevTime.setEnabled(!checkPrev.withDayOfMonth(1).isBefore(earliestTxDate.withDayOfMonth(1)));
            } else {
                btnPrevTime.setEnabled(checkPrev.getYear() >= earliestTxDate.getYear());
            }

            LocalDate checkNext = LocalDate.now();
            if ("week".equals(currentMode)) checkNext = realTimeNow.plusWeeks(currentOffset + 1);
            else if ("month".equals(currentMode)) checkNext = realTimeNow.plusMonths(currentOffset + 1);
            else checkNext = realTimeNow.plusYears(currentOffset + 1);

            boolean isNextInFuture = false;
            boolean isNextPastDataBounds = false;

            if ("week".equals(currentMode)) {
                isNextInFuture = checkNext.with(DayOfWeek.MONDAY).isAfter(realTimeNow.with(DayOfWeek.SUNDAY));
                isNextPastDataBounds = checkNext.with(DayOfWeek.MONDAY).isAfter(latestTxDate);
            } else if ("month".equals(currentMode)) {
                isNextInFuture = checkNext.withDayOfMonth(1).isAfter(realTimeNow.withDayOfMonth(realTimeNow.lengthOfMonth()));
                isNextPastDataBounds = checkNext.withDayOfMonth(1).isAfter(latestTxDate.withDayOfMonth(latestTxDate.lengthOfMonth()));
            } else {
                isNextInFuture = checkNext.getYear() > realTimeNow.getYear();
                isNextPastDataBounds = checkNext.getYear() > latestTxDate.getYear();
            }
            if (btnNextTime != null) {
                btnNextTime.setEnabled(!isNextInFuture && !isNextPastDataBounds);
            }
        }
    }

    // ============== CÁC HÀM TẠO NÚT ==============
    private JButton createStyleNavButton(String text, boolean active) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void toggleChartTypeButtons(boolean isPieActive) {
        if (btnPieChartToggle != null) {
            btnPieChartToggle.setBackground(isPieActive ? ThemeManager.getColor("input") : ThemeManager.getColor("surface"));
            btnPieChartToggle.setForeground(isPieActive ? ThemeManager.getColor("accent") : ThemeManager.getColor("textPrimary"));
            btnPieChartToggle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        }
        if (btnLineChartToggle != null) {
            btnLineChartToggle.setBackground(!isPieActive ? ThemeManager.getColor("input") : ThemeManager.getColor("surface"));
            btnLineChartToggle.setForeground(!isPieActive ? ThemeManager.getColor("accent") : ThemeManager.getColor("textPrimary"));
            btnLineChartToggle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        }
    }

    private JButton createIntervalTabButton(String text, boolean active) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectIntervalTab(JButton target) {
        JButton[] tabs = {btnWeekTab, btnMonthTab, btnYearTab};
        for(JButton btn : tabs) {
            if (btn != null) {
                btn.setBackground(ThemeManager.getColor("input"));
                btn.setForeground(ThemeManager.getColor("textPrimary"));
            }
        }
        if (target != null) {
            target.setBackground(ThemeManager.getColor("accent"));
            target.setForeground(ThemeManager.getColor("bg"));
        }
    }

    private JButton createInnerArrowButton(String text) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(32, 26)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ============== LANGUAGE & THEME ==============
    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;
        if (lblMainTitle != null) lblMainTitle.setText(isVN ? "Phân tích thống kê chi tiêu" : "Expense Statistical Analysis");
        if (btnPieChartToggle != null) btnPieChartToggle.setText(isVN ? "< Biểu đồ tròn" : "< Donut Chart");
        if (btnLineChartToggle != null) btnLineChartToggle.setText(isVN ? "Biểu đồ đường >" : "Line Chart >");
        if (btnWeekTab != null) btnWeekTab.setText(isVN ? "Tuần" : "Week");
        if (btnMonthTab != null) btnMonthTab.setText(isVN ? "Tháng" : "Month");
        if (btnYearTab != null) btnYearTab.setText(isVN ? "Năm" : "Year");
        refreshData();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED || eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }

    public void applyTheme() {
        ThemeManager.applyThemeRecursively(this);

        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                for (Component innerComp : ((JPanel) comp).getComponents()) {
                    if (innerComp instanceof JPanel && "surfacePanel".equals(innerComp.getName())) {
                        innerComp.setBackground(ThemeManager.getColor("surface"));
                        if (((JPanel) innerComp).getBorder() instanceof javax.swing.border.CompoundBorder) {
                            ((JPanel) innerComp).setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                            ));
                        } else {
                            ((JPanel) innerComp).setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
                        }
                    }
                }
            }
        }

        if (lblMainTitle != null) lblMainTitle.setForeground(ThemeManager.getColor("textPrimary"));
        if (lblTimeRange != null) lblTimeRange.setForeground(ThemeManager.getColor("accent"));

        if (chartDrawPanel != null) chartDrawPanel.setBackground(ThemeManager.getColor("surface"));
        if (legendPanel != null) legendPanel.setBackground(ThemeManager.getColor("surface"));

        if (btnPrevTime != null) {
            btnPrevTime.setBackground(ThemeManager.getColor("input"));
            btnPrevTime.setForeground(ThemeManager.getColor("textPrimary"));
            btnPrevTime.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        }
        if (btnNextTime != null) {
            btnNextTime.setBackground(ThemeManager.getColor("input"));
            btnNextTime.setForeground(ThemeManager.getColor("textPrimary"));
            btnNextTime.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        }

        toggleChartTypeButtons("pie".equals(currentChartType));

        if ("week".equals(currentMode)) selectIntervalTab(btnWeekTab);
        else if ("month".equals(currentMode)) selectIntervalTab(btnMonthTab);
        else selectIntervalTab(btnYearTab);

        refreshData();
    }

    // ============== LỚP NỘI BỘ ==============
    private static class ChartItem {
        String originalName;  // tên gốc (tiếng Việt) để tra cứu emoji
        String displayName;   // tên hiển thị (đã dịch)
        double amount;
        double percent;

        ChartItem(String originalName, String displayName, double amount, double percent) {
            this.originalName = originalName;
            this.displayName = displayName;
            this.amount = amount;
            this.percent = percent;
        }
    }

    private static class ChartData {
        List<ChartItem> items;
        double total;
        ChartData(List<ChartItem> items, double total) {
            this.items = items;
            this.total = total;
        }
    }
}