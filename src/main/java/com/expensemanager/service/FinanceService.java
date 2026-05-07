package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.util.JsonUtil;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {
    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "transactions.json";

    public FinanceService() {
        transactionList = new ArrayList<>();
        categoryMap = new HashMap<>();
        loadInitialData();
    }

    private void loadInitialData() {
        List<Category> categories = DatabaseUtil.getAllCategories();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }
        try {
            List<Transaction> savedTransactions = JsonUtil.loadFromJson(JSON_FILE_PATH);
            transactionList = savedTransactions;
        } catch (DataLoadException e) {
            System.err.println("Không thể tải file JSON: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    public void addTransaction(Transaction transaction) {
        DatabaseUtil.insertTransaction(transaction);
        transactionList.add(transaction);
        saveToFile();
    }

    public List<Transaction> getAllTransactions() {
        return transactionList;
    }

    public double getTotalIncome() {
        return transactionList.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalExpense() {
        return transactionList.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public Category getCategoryById(String id) {
        return categoryMap.get(id);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categoryMap.values());
    }

    private void saveToFile() {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi lưu file JSON: " + e.getMessage());
        }
    }
}