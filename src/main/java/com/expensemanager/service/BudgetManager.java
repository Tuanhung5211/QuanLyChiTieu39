<<<<<<< HEAD
package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.exception.InvalidAmountException;

public class BudgetManager {
    private FinanceService financeService;

    public BudgetManager(FinanceService financeService) {
        this.financeService = financeService;
    }

    // Thiết lập ngân sách cho tháng này
    public void setBudget(int month, int year, double limit) throws InvalidAmountException {
        if (limit <= 0) {
            throw new InvalidAmountException("Hạn mức ngân sách phải lớn hơn 0!");
        }
        String id = "BUD" + String.format("%02d%04d", month, year);
        Budget budget = new Budget(id, month, year, limit);
        DatabaseUtil.insertBudget(budget);
    }

    // Kiểm tra ngân sách tháng này, trả về thông báo cảnh báo nếu vượt
    public String checkBudget() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        Budget budget = DatabaseUtil.getBudget(month, year);
        if (budget == null) {
            return "Chưa thiết lập ngân sách cho tháng này.";
        }
        // Cập nhật số đã chi
        budget.setSpent(financeService.getTotalExpense());
        DatabaseUtil.updateBudget(budget);

        if (budget.isOverBudget()) {
            return String.format("⚠️ CẢNH BÁO: Đã vượt ngân sách! Hạn mức: %,.0f VND, đã chi: %,.0f VND",
                    budget.getLimit(), budget.getSpent());
        } else {
            return String.format("✅ Còn %,.0f VND trong tổng hạn mức %,.0f VND",
                    budget.getRemaining(), budget.getLimit());
        }
    }
=======
package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.exception.InvalidAmountException;

public class BudgetManager {
    private FinanceService financeService;

    public BudgetManager(FinanceService financeService) {
        this.financeService = financeService;
    }

    // Thiết lập ngân sách cho tháng này
    public void setBudget(int month, int year, double limit) throws InvalidAmountException {
        if (limit <= 0) {
            throw new InvalidAmountException("Hạn mức ngân sách phải lớn hơn 0!");
        }
        String id = "BUD" + String.format("%02d%04d", month, year);
        Budget budget = new Budget(id, month, year, limit);
        DatabaseUtil.insertBudget(budget);
    }

    // Kiểm tra ngân sách tháng này, trả về thông báo cảnh báo nếu vượt
    public String checkBudget() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        Budget budget = DatabaseUtil.getBudget(month, year);
        if (budget == null) {
            return "Chưa thiết lập ngân sách cho tháng này.";
        }
        // Cập nhật số đã chi
        budget.setSpent(financeService.getTotalExpense());
        DatabaseUtil.updateBudget(budget);

        if (budget.isOverBudget()) {
            return String.format("⚠️ CẢNH BÁO: Đã vượt ngân sách! Hạn mức: %,.0f VND, đã chi: %,.0f VND",
                    budget.getLimit(), budget.getSpent());
        } else {
            return String.format("✅ Còn %,.0f VND trong tổng hạn mức %,.0f VND",
                    budget.getRemaining(), budget.getLimit());
        }
    }
>>>>>>> eca639ab7522996ea3bb57cfede15bde0dc01d01
}