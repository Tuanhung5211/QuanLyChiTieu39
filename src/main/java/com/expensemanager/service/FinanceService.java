package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // 1. Tải danh mục từ database (dùng chung, không cần userId)
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) {
                        categoryMap.put(c.getId(), c);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể tải danh mục từ database: " + e.getMessage());
        }

        // 2. Tải giao dịch từ file JSON (dữ liệu tạm, sau khi đăng nhập sẽ đồng bộ từ DB)
        try {
            List<Transaction> savedTransactions = JsonUtil.loadFromJson(JSON_FILE_PATH);
            if (savedTransactions != null) {
                transactionList = savedTransactions;
            } else {
                transactionList = new ArrayList<>();
            }
        } catch (DataLoadException e) {
            System.err.println("Không thể tải file JSON, khởi tạo danh sách rỗng: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    // ========== CÁC PHƯƠNG THỨC THAO TÁC VỚI GIAO DỊCH ==========

    public void addTransaction(Transaction transaction) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) {
            System.err.println("Lỗi: Chưa đăng nhập, không thể thêm giao dịch.");
            return;
        }
        try {
            DatabaseUtil.insertTransaction(transaction, userId);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm giao dịch vào database: " + e.getMessage());
        }
        transactionList.add(transaction);
        saveToFile();
    }

    public List<Transaction> getAllTransactions() {
        return transactionList;
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == type)
                .collect(Collectors.toList());
    }

    // ========== CÁC PHƯƠNG THỨC TÍNH TOÁN TỔNG QUAN ==========

    public double getTotalIncome() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    // ========== QUẢN LÝ DANH MỤC (HashMap) ==========

    public Category getCategoryById(String id) {
        return categoryMap.get(id);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categoryMap.values());
    }

    public void refreshCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) {
                        categoryMap.put(c.getId(), c);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể làm mới danh mục: " + e.getMessage());
        }
    }

    // ========== HỖ TRỢ LƯU FILE ==========

    private void saveToFile() {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi khi lưu file JSON: " + e.getMessage());
        }
    }

    public void syncFromDatabase() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;
        List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions(userId);
        this.transactionList = dbTransactions;
        saveToFile();
    }
}