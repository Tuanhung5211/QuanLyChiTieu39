package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.InvalidAmountException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetManager {
    private FinanceService financeService;
    private ReminderService reminderService;
    private List<Budget> budgets;

    public BudgetManager(FinanceService financeService, ReminderService reminderService) {
        this.financeService = financeService;
        this.reminderService = reminderService;
        this.budgets = new ArrayList<>();
        loadBudgetsFromDatabase();
    }

    private void loadBudgetsFromDatabase() {
        String userId = SessionManager.getCurrentUserId();
        if (userId != null) {
            this.budgets = DatabaseUtil.getAllBudgets(userId);
        }
    }

    public void addBudget(Budget budget) throws InvalidAmountException {
        if (budget.getLimit() <= 0) {
            throw new InvalidAmountException("Hạn mức ngân sách phải lớn hơn 0!");
        }
        if (budget.getId() == null) {
            budget.setId("B_" + System.currentTimeMillis());
        }

        budgets.add(budget);
        DatabaseUtil.insertBudget(budget);

        if (reminderService != null) reminderService.autoCreateBudgetAlerts();
    }

    public List<Budget> getAllBudgets() {
        updateAllBudgetsSpent();
        return budgets;
    }

    public void deleteBudget(String id) {
        budgets.removeIf(b -> b.getId().equals(id));
        DatabaseUtil.deleteBudget(id);
    }

    private void updateAllBudgetsSpent() {
        if (financeService == null) return;
        List<Transaction> allTransactions = financeService.getAllTransactions();

        for (Budget budget : budgets) {
            double spent = allTransactions.stream()
                    .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                    .filter(t -> !t.getDateTime().toLocalDate().isBefore(budget.getStartDate()) &&
                            !t.getDateTime().toLocalDate().isAfter(budget.getEndDate()))
                    .filter(t -> budget.getCategory() == null ||
                            (t.getCategory() != null && t.getCategory().getName().equals(budget.getCategory().getName())))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            budget.setSpent(spent);
        }
    }

    public String checkBudget() {
        if (budgets.isEmpty()) return "Chưa thiết lập ngân sách.";
        updateAllBudgetsSpent();
        boolean isVN = "vi".equalsIgnoreCase(SessionManager.getLanguage());
        long exceededCount = budgets.stream().filter(b -> b.getSpent() > b.getLimit()).count();
        if (exceededCount > 0) {
            return isVN ? "⚠️ CẢNH BÁO: Có " + exceededCount + " ngân sách vượt mức!"
                    : "⚠️ ALERT: " + exceededCount + " budgets exceeded!";
        }
        return isVN ? "✅ Các ngân sách đang trong mức an toàn." : "✅ All budgets are safe.";
    }

    // Hàm cũ giữ lại tương thích
    public void setBudget(int month, int year, double limit) throws InvalidAmountException {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) throw new InvalidAmountException("Chưa đăng nhập");

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Budget b = new Budget();
        b.setLimit(limit);
        b.setStartDate(start);
        b.setEndDate(end);
        b.setUserId(userId);
        b.setThreshold(80);

        addBudget(b);
    }
}