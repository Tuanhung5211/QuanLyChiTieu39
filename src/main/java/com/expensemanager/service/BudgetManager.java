package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.InvalidAmountException;

public class BudgetManager {
    private FinanceService financeService;

    public BudgetManager(FinanceService financeService) {
        this.financeService = financeService;
    }

    public void setBudget(int month, int year, double limit) throws InvalidAmountException {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) throw new InvalidAmountException("Chưa đăng nhập");
        if (limit <= 0) throw new InvalidAmountException("Hạn mức ngân sách phải lớn hơn 0!");

        // 🌟 ĐÃ KHẮC PHỤC: Thêm userId vào đầu khóa chính để phân tách vùng dữ liệu đa tài khoản
        String id = userId + "_BUD_" + String.format("%02d%04d", month, year);
        Budget budget = new Budget(id, month, year, limit);
        DatabaseUtil.insertBudget(budget, userId);
    }

    public String checkBudget() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return "Chưa đăng nhập";

        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        Budget budget = DatabaseUtil.getBudget(month, year, userId);
        if (budget == null) return "Chưa thiết lập ngân sách.";

        double monthlyExpense = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();

        budget.setSpent(monthlyExpense);
        DatabaseUtil.updateBudget(budget);

        boolean isVN = "vi".equalsIgnoreCase(SessionManager.getLanguage());
        if (budget.isOverBudget()) {
            return String.format(isVN ? "⚠️ Vượt ngân sách! Hạn mức: %,.0f VND, đã chi: %,.0f VND"
                            : "⚠️ Over Budget! Limit: %,.0f VND, Spent: %,.0f VND",
                    budget.getLimit(), budget.getSpent());
        } else {
            return String.format(isVN ? "✅ Còn lại: %,.0f VND" : "✅ Remaining: %,.0f VND", budget.getRemaining());
        }
    }
}