package com.expensemanager.service;

import com.expensemanager.entity.Reminder;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.util.ReminderStore;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ReminderService {
    private FinanceService financeService;
    private Timer timer;
    private String userId;

    public ReminderService(FinanceService financeService) {
        this.financeService = financeService;
        this.userId = SessionManager.getCurrentUserId();
        startBackgroundChecker();
    }

    public void reloadUserId() {
        this.userId = SessionManager.getCurrentUserId();
    }

    private List<Reminder> getReminders() {
        if (userId == null) return List.of();
        return ReminderStore.loadReminders(userId);
    }

    private void saveReminders(List<Reminder> list) {
        if (userId != null) ReminderStore.saveReminders(userId, list);
    }

    public void addReminder(Reminder reminder) {
        List<Reminder> list = getReminders();
        reminder.setId(UUID.randomUUID().toString().substring(0, 8));
        list.add(reminder);
        saveReminders(list);
    }

    public void updateReminder(Reminder reminder) {
        List<Reminder> list = getReminders();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(reminder.getId())) {
                list.set(i, reminder);
                break;
            }
        }
        saveReminders(list);
    }

    public void deleteReminder(String id) {
        List<Reminder> list = getReminders();
        list.removeIf(r -> r.getId().equals(id));
        saveReminders(list);
    }

    public List<Reminder> getUserReminders() {
        return getReminders();
    }

    private void startBackgroundChecker() {
        timer = new Timer(60000, e -> checkAndNotify());
        timer.start();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    private void checkAndNotify() {
        if (userId == null || !userId.equals(SessionManager.getCurrentUserId())) {
            userId = SessionManager.getCurrentUserId();
            if (userId == null) return;
        }

        List<Reminder> reminders = getReminders();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        boolean changed = false;

        for (Reminder r : reminders) {
            if (!r.isActive()) continue;
            if (r.getLastTriggered() != null && r.getLastTriggered().equals(today)) continue;

            boolean shouldNotify = false;

            switch (r.getType()) {
                case DAILY:
                    if (r.getRemindTime() != null && r.getRemindTime().equals(now)) {
                        shouldNotify = true;
                    }
                    break;
                case BILL:
                    if (r.getDueDate() != null && r.getDueDate().equals(today)) {
                        shouldNotify = true;
                        if (r.getRecurring() != Reminder.RecurringType.NONE) {
                            LocalDate next = r.getDueDate();
                            if (r.getRecurring() == Reminder.RecurringType.MONTHLY)
                                next = next.plusMonths(1);
                            else if (r.getRecurring() == Reminder.RecurringType.YEARLY)
                                next = next.plusYears(1);
                            r.setDueDate(next);
                            changed = true;
                        }
                    }
                    break;
                case BUDGET:
                    shouldNotify = checkBudgetAlert(r);
                    break;
            }

            if (shouldNotify) {
                showNotification(r);
                r.setLastTriggered(today);
                changed = true;
            }
        }

        if (changed) saveReminders(reminders);
    }

    private boolean checkBudgetAlert(Reminder r) {
        LocalDate now = LocalDate.now();
        double totalExpense = financeService.getAllTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().getYear() == now.getYear()
                        && t.getDateTime().getMonthValue() == now.getMonthValue())
                .mapToDouble(t -> t.getAmount())
                .sum();

        String userId = SessionManager.getCurrentUserId();
        var budget = com.expensemanager.database.DatabaseUtil.getBudget(now.getMonthValue(), now.getYear(), userId);
        if (budget == null || budget.getLimit() <= 0) return false;

        int percent = (int) ((totalExpense / budget.getLimit()) * 100);
        Integer threshold = r.getThresholdPercent();
        if (threshold != null && percent >= threshold) {
            LocalDate last = r.getLastTriggered();
            if (last == null || last.getMonth() != now.getMonth() || last.getYear() != now.getYear()) {
                r.setDescription(String.format("Đã chi %d%% ngân sách tháng (%.0f/%.0f VND)", percent, totalExpense, budget.getLimit()));
                return true;
            }
        }
        return false;
    }

    private void showNotification(Reminder r) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "Money Tracker");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(r.getTitle(), r.getDescription(), TrayIcon.MessageType.INFO);
                tray.remove(trayIcon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        r.getTitle() + "\n" + r.getDescription(),
                        "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    public void autoCreateBudgetAlerts() {
        if (userId == null) return;
        LocalDate now = LocalDate.now();
        double totalExpense = financeService.getAllTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().getYear() == now.getYear()
                        && t.getDateTime().getMonthValue() == now.getMonthValue())
                .mapToDouble(t -> t.getAmount())
                .sum();

        var budget = com.expensemanager.database.DatabaseUtil.getBudget(now.getMonthValue(), now.getYear(), userId);
        if (budget == null || budget.getLimit() <= 0) return;

        int percent = (int) ((totalExpense / budget.getLimit()) * 100);
        int[] thresholds = {50, 80, 100};
        List<Reminder> existing = getReminders();
        for (int th : thresholds) {
            if (percent >= th) {
                boolean alreadyExists = existing.stream()
                        .anyMatch(r -> r.getType() == Reminder.ReminderType.BUDGET
                                && r.getThresholdPercent() != null && r.getThresholdPercent() == th
                                && r.getLastTriggered() != null
                                && r.getLastTriggered().getMonth() == now.getMonth()
                                && r.getLastTriggered().getYear() == now.getYear());
                if (!alreadyExists) {
                    Reminder r = new Reminder(null, userId, Reminder.ReminderType.BUDGET,
                            "Cảnh báo ngân sách",
                            String.format("Đã chi %d%% ngân sách tháng (%.0f/%.0f VND)", percent, totalExpense, budget.getLimit()),
                            null, null, Reminder.RecurringType.NONE, th, true, null);
                    addReminder(r);
                }
            }
        }
    }
}