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
        this.transactionList = new ArrayList<>();
        this.categoryMap = new HashMap<>();
        loadInitialData();
    }

    private void loadInitialData() {
        // 1. Nạp danh mục: Sử dụng Stream để Map ID với đối tượng Category
        List<Category> categoriesFromDb = DatabaseUtil.getAllCategories();
        if (categoriesFromDb != null) {
            this.categoryMap = categoriesFromDb.stream()
                    .collect(Collectors.toMap(
                            Category::getId,
                            c -> c,
                            (existing, replacement) -> existing
                    ));
        }

        // 2. Nạp giao dịch: Đảm bảo danh sách có thể thay đổi (Mutable) để add được sau này
        try {
            List<Transaction> saved = JsonUtil.loadFromJson(JSON_FILE_PATH);
            // Ép về ArrayList để tránh lỗi UnsupportedOperationException khi thêm mới
            this.transactionList = (saved != null) ? new ArrayList<>(saved) : new ArrayList<>();
        } catch (DataLoadException e) {
            System.err.println("Không thể tải dữ liệu: " + e.getMessage());
            this.transactionList = new ArrayList<>();
        }
    }

    // --- TÍNH TOÁN (ĐÃ SỬ DỤNG STREAM API) ---

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

    // --- CÁC PHƯƠNG THỨC QUẢN LÝ ---

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            DatabaseUtil.insertTransaction(transaction);
            this.transactionList.add(transaction);
            saveToFile();
        }
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(this.transactionList);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(this.categoryMap.values());
    }

    /**
     * SỬA LỖI: Truy xuất ID thông qua object Category của bạn A
     */
    public List<Transaction> filterByCategory(String categoryId) {
        if (categoryId == null) return new ArrayList<>();
        return transactionList.stream()
                .filter(t -> t.getCategory() != null && categoryId.equals(t.getCategory().getId()))
                .collect(Collectors.toList());
    }

    private void saveToFile() {
        try {
            JsonUtil.saveToJson(this.transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi lưu file: " + e.getMessage());
        }
    }
}