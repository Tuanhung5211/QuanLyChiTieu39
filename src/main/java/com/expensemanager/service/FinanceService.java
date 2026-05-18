package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Subject;
import com.expensemanager.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceService extends Subject {
    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "transactions.json";

    public FinanceService() {
        transactionList = new ArrayList<>();
        categoryMap = new HashMap<>();
        loadInitialData();
    }

    private void loadInitialData() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) categoryMap.put(c.getId(), c);
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể tải danh mục: " + e.getMessage());
        }

        try {
            List<Transaction> saved = JsonUtil.loadFromJson(JSON_FILE_PATH);
            transactionList = saved != null ? saved : new ArrayList<>();
        } catch (DataLoadException e) {
            System.err.println("Không thể tải file JSON: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    public void syncFromDatabase() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;
        List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions(userId);
        if (dbTransactions != null) {
            this.transactionList = dbTransactions;
            saveToFile();
            notifyObservers(EventType.DATA_LOADED, null);
        }
    }

    public void addTransaction(Transaction transaction) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) {
            System.err.println("Chưa đăng nhập, không thể thêm giao dịch");
            return;
        }
        try {
            DatabaseUtil.insertTransaction(transaction, userId);
            transactionList.add(transaction);
            saveToFile();
            notifyObservers(EventType.TRANSACTION_ADDED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm giao dịch: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction transaction) {
        try {
            DatabaseUtil.updateTransaction(transaction);
            notifyObservers(EventType.TRANSACTION_UPDATED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật giao dịch: " + e.getMessage());
        }
    }

    public void deleteTransaction(String transactionId) {
        try {
            DatabaseUtil.deleteTransaction(transactionId);
            transactionList.removeIf(t -> t.getId().equals(transactionId));
            saveToFile();
            notifyObservers(EventType.TRANSACTION_DELETED, transactionId);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa giao dịch: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionList;
    }

    public double getTotalIncome() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalExpense() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public Category getCategoryById(String id) { return categoryMap.get(id); }
    public List<Category> getAllCategories() { return new ArrayList<>(categoryMap.values()); }

    public void refreshCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) for (Category c : categories) categoryMap.put(c.getId(), c);
        } catch (Exception e) {
            System.err.println("Lỗi làm mới danh mục: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try { JsonUtil.saveToJson(transactionList, JSON_FILE_PATH); }
        catch (DataLoadException e) { System.err.println("Lỗi lưu JSON: " + e.getMessage()); }
    }
    // ====================================================================
    // 🌟 THÊM MỚI: LẤY MỐC NGÀY XA NHẤT TRONG QUÁ KHỨ CÓ GIAO DỊCH
    // ====================================================================
    public java.time.LocalDate getEarliestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .min(java.time.LocalDate::compareTo)
                .orElse(null);
    }

    // ====================================================================
    // 🌟 THÊM MỚI: LẤY MỐC NGÀY MỚI NHẤT CÓ GIAO DỊCH TRONG HỆ THỐNG
    // ====================================================================
    public java.time.LocalDate getLatestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .max(java.time.LocalDate::compareTo)
                .orElse(null);
    }
}