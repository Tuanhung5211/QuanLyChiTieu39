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
    private Map<String, Category> categoryMap; // HashMap quản lý danh mục theo id
    private static final String JSON_FILE_PATH = "transactions.json";

    public FinanceService() {
        transactionList = new ArrayList<>();
        categoryMap = new HashMap<>();
        loadInitialData();
    }

    /**
     * Tải dữ liệu ban đầu: danh mục từ DB, giao dịch từ JSON (nếu có).
     */
    private void loadInitialData() {
        // Tải danh mục từ database và đưa vào HashMap
        List<Category> categories = DatabaseUtil.getAllCategories();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }

        // Tải giao dịch từ file JSON (nếu có)
        try {
            List<Transaction> savedTransactions = JsonUtil.loadFromJson(JSON_FILE_PATH);
            transactionList = savedTransactions;
        } catch (DataLoadException e) {
            System.err.println("Không thể tải file JSON, khởi tạo danh sách rỗng: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    // ========== CÁC PHƯƠNG THỨC THAO TÁC VỚI GIAO DỊCH ==========

    /**
     * Thêm giao dịch mới (vào DB, file JSON và danh sách trong bộ nhớ).
     */
    public void addTransaction(Transaction transaction) {
        DatabaseUtil.insertTransaction(transaction); // Lưu vào DB
        transactionList.add(transaction);            // Thêm vào bộ nhớ
        saveToFile();                                // Lưu ra file
    }

    /**
     * Lấy tất cả giao dịch.
     */
    public List<Transaction> getAllTransactions() {
        return transactionList;
    }

    /**
     * Lọc giao dịch theo loại (INCOME/EXPENSE) - Sử dụng Stream/Lambda.
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionList.stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    // ========== CÁC PHƯƠNG THỨC TÍNH TOÁN TỔNG QUAN ==========

    /**
     * Tính tổng thu nhập - Sử dụng Stream/Lambda.
     */
    public double getTotalIncome() {
        return transactionList.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Tính tổng chi tiêu - Sử dụng Stream/Lambda.
     */
    public double getTotalExpense() {
        return transactionList.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Tính số dư hiện tại.
     */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    // ========== QUẢN LÝ DANH MỤC (HashMap) ==========

    /**
     * Lấy danh mục theo id từ HashMap.
     */
    public Category getCategoryById(String id) {
        return categoryMap.get(id);
    }

    /**
     * Lấy tất cả danh mục từ HashMap.
     */
    public List<Category> getAllCategories() {
        return new ArrayList<>(categoryMap.values());
    }

    /**
     * Làm mới danh mục từ database vào HashMap.
     */
    public void refreshCategories() {
        List<Category> categories = DatabaseUtil.getAllCategories();
        categoryMap.clear();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }
    }

    // ========== HỖ TRỢ LƯU FILE ==========

    /**
     * Lưu danh sách giao dịch hiện tại ra file JSON.
     */
    private void saveToFile() {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi khi lưu file JSON: " + e.getMessage());
        }
    }

    /**
     * Đồng bộ dữ liệu: lấy toàn bộ giao dịch từ DB và ghi đè vào danh sách + file.
     */
    public void syncFromDatabase() {
        List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions();
        this.transactionList = dbTransactions;
        saveToFile();
    }
}