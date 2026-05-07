package com.expensemanager.service;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.entity.Category;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsService {
    private FinanceService financeService;

    public StatisticsService(FinanceService financeService) {
        this.financeService = financeService;
    }

    // Overload 1: Tính tổng theo loại giao dịch trong một tháng
    public double calculateTotal(int month, int year, TransactionType type) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Overload 2: Tính tổng tất cả giao dịch trong tháng
    public double calculateTotal(int month, int year) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Overload 3: Tính tổng theo danh mục
    public double calculateByCategory(Category category) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Overload 4: Tính tổng theo danh mục + tháng/năm
    public double calculateByCategory(Category category, int month, int year) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Tổng thu tháng này
    public double getTotalIncomeThisMonth() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.INCOME);
    }

    // Tổng chi tháng này
    public double getTotalExpenseThisMonth() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.EXPENSE);
    }

    // Số dư tháng này
    public double getBalanceThisMonth() {
        return getTotalIncomeThisMonth() - getTotalExpenseThisMonth();
    }

    // Danh sách giao dịch giá trị cao
    public List<Transaction> getHighValueTransactions(double threshold) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getAmount() > threshold)
                .collect(Collectors.toList());
    }
}