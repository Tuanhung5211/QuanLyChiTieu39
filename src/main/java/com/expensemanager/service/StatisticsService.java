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

    public double calculateTotal(int month, int year, TransactionType type) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double calculateTotal(int month, int year) {
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount).sum();
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
        return financeService.getAllTransactions().stream()
                .filter(t -> t.getAmount() > threshold)
                .collect(Collectors.toList());
    }
}