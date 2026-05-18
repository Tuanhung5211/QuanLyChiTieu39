package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Subject;
import com.expensemanager.util.JsonUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceService extends Subject {

    // =====================================================================
    // 1. KHAI BÁO BIẾN LOGIC VÀ LƯU TRỮ TRÊN RAM
    // =====================================================================
    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "transactions.json";

    // =====================================================================
    // 2. CONSTRUCTOR & KHỞI TẠO DỮ LIỆU BAN ĐẦU
    // =====================================================================
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

    // =====================================================================
    // 3. QUẢN LÝ NGHIỆP VỤ DANH MỤC (CATEGORIES)
    // =====================================================================
    public void addCategory(Category category) {
        try {
            DatabaseUtil.insertCategory(category);
            categoryMap.put(category.getId(), category);
            notifyObservers(EventType.CATEGORY_ADDED, category);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ thêm danh mục: " + e.getMessage());
        }
    }

    public void deleteCategory(String categoryId) {
        try {
            DatabaseUtil.deleteCategory(categoryId);
            categoryMap.remove(categoryId);
            notifyObservers(EventType.CATEGORY_DELETED, categoryId);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ xóa danh mục: " + e.getMessage());
        }
    }

    public void refreshCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) {
                for (Category c : categories) categoryMap.put(c.getId(), c);
            }
        } catch (Exception e) {
            System.err.println("Lỗi làm mới danh mục: " + e.getMessage());
        }
    }

    // =====================================================================
    // 4. QUẢN LÝ NGHIỆP VỤ GIAO DỊCH (TRANSACTIONS)
    // =====================================================================
    public void addTransaction(Transaction transaction) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

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
            for (int i = 0; i < transactionList.size(); i++) {
                if (transactionList.get(i).getId().equals(transaction.getId())) {
                    transactionList.set(i, transaction);
                    break;
                }
            }
            saveToFile();
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

    // =====================================================================
    // 5. TRUY XUẤT THÔNG TIN VÀ XỬ LÝ I/O FILE
    // =====================================================================
    private void saveToFile() {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi lưu JSON: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() { return transactionList; }
    public Category getCategoryById(String id) { return categoryMap.get(id); }
    public List<Category> getAllCategories() { return new ArrayList<>(categoryMap.values()); }

    public LocalDate getEarliestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    public LocalDate getLatestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);
    }
}