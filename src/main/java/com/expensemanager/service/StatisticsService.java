package com.expensemanager.service;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.entity.Category;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsService {

    // =====================================================================
    // 1. KHAI BÁO BIẾN LOGIC
    // =====================================================================
    private FinanceService financeService;

    public StatisticsService(FinanceService financeService) {
        this.financeService = financeService;
    }

    // =====================================================================
    // 2. NGHIỆP VỤ TÍNH TỔNG QUÁT THEO THỜI GIAN VÀ LOẠI
    // =====================================================================
    public double calculateTotal(int month, int year, TransactionType type) {
        if (SessionManager.getCurrentUserId() == null) return 0;
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotal(int month, int year) {
        if (SessionManager.getCurrentUserId() == null) return 0;
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // =====================================================================
    // 3. NGHIỆP VỤ TÍNH TOÁN THEO DANH MỤC CỤ THỂ
    // =====================================================================
    public double calculateByCategory(Category category) {
        if (SessionManager.getCurrentUserId() == null) return 0;
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateByCategory(Category category, int month, int year) {
        if (SessionManager.getCurrentUserId() == null) return 0;
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // =====================================================================
    // 4. NGHIỆP VỤ TỔNG HỢP NHANH THÁNG HIỆN TẠI
    // =====================================================================
    public double getTotalIncomeThisMonth() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.INCOME);
    }

    public double getTotalExpenseThisMonth() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.EXPENSE);
    }

    public double getBalanceThisMonth() {
        return getTotalIncomeThisMonth() - getTotalExpenseThisMonth();
    }

    // =====================================================================
    // 5. TRUY XUẤT NÂNG CAO VÀ GETTER
    // =====================================================================
    public List<Transaction> getHighValueTransactions(double threshold) {
        if (SessionManager.getCurrentUserId() == null) return List.of();
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getAmount() > threshold)
                .collect(Collectors.toList());
    }

    public FinanceService getFinanceService() {
        return financeService;
    }
}