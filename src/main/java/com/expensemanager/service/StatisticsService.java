package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
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

    public double calculateTotal(int month, int year, TransactionType type) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return 0;
        List<Transaction> transactions = DatabaseUtil.getAllTransactions(userId);
        return transactions.stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotal(int month, int year) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return 0;
        List<Transaction> transactions = DatabaseUtil.getAllTransactions(userId);
        return transactions.stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateByCategory(Category category) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return 0;
        List<Transaction> transactions = DatabaseUtil.getAllTransactions(userId);
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateByCategory(Category category, int month, int year) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return 0;
        List<Transaction> transactions = DatabaseUtil.getAllTransactions(userId);
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalIncomeThisMonth() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.INCOME);
    }

    public double getTotalExpenseThisMonth() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        return calculateTotal(month, year, TransactionType.EXPENSE);
    }

    public double getBalanceThisMonth() {
        return getTotalIncomeThisMonth() - getTotalExpenseThisMonth();
    }

    public List<Transaction> getHighValueTransactions(double threshold) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return List.of();
        List<Transaction> transactions = DatabaseUtil.getAllTransactions(userId);
        return transactions.stream()
                .filter(t -> t.getAmount() > threshold)
                .collect(Collectors.toList());
    }
    public FinanceService getFinanceService() {
        return financeService;
    }
}