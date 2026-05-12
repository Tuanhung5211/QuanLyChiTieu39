package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.exception.InvalidAmountException; // Cần tạo class này
import com.expensemanager.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceService {
    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "src/main/resources/data/transactions.json";

    public FinanceService() {
        this.transactionList = new ArrayList<>();
        this.categoryMap = new HashMap<>();
        // Việc nạp dữ liệu ban đầu thường được gọi từ MainApp hoặc MainFrame
        // để có thể bắt lỗi và hiển thị Dialog thông báo nếu cần.
    }

    /**
     * Nạp dữ liệu ban đầu từ Database và JSON.
     * @throws DataLoadException nếu có lỗi nghiêm trọng khi đọc dữ liệu.
     */
    public void loadInitialData() throws DataLoadException {
        // 1. Tải danh mục từ database
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) categoryMap.put(c.getId(), c);
                }
            }
        } catch (Exception e) {
            // Log lỗi nhưng không chặn tiến trình nếu đây là lỗi DB không nghiêm trọng
            System.err.println("Cảnh báo: Không thể tải danh mục từ database.");
        }

        // 2. Tải giao dịch từ file JSON
        try {
            List<Transaction> savedTransactions = JsonUtil.loadFromJson(JSON_FILE_PATH);
            this.transactionList = (savedTransactions != null) ? savedTransactions : new ArrayList<>();
        } catch (DataLoadException e) {
            // Ném lại ngoại lệ để UI xử lý (ví dụ: yêu cầu người dùng kiểm tra file)
            throw new DataLoadException("Lỗi nạp lịch sử giao dịch: " + e.getMessage());
        }
    }

    // ========== CÁC PHƯƠNG THỨC THAO TÁC GIAO DỊCH ==========

    /**
     * Thêm giao dịch với kiểm tra logic dữ liệu (Validation).
     */
    public void addTransaction(Transaction transaction) throws InvalidAmountException, DataLoadException {
        // Kiểm tra dữ liệu đầu vào (Xử lý dữ liệu)
        if (transaction == null) {
            throw new IllegalArgumentException("Giao dịch không được để trống.");
        }

        if (transaction.getAmount() <= 0) {
            throw new InvalidAmountException("Số tiền phải lớn hơn 0. Giá trị nhập: " + transaction.getAmount());
        }

        // Thao tác Database (I/O)
        try {
            DatabaseUtil.insertTransaction(transaction);
        } catch (Exception e) {
            // Nếu lỗi DB, ta có thể chọn ghi log và vẫn cho phép lưu vào JSON để tránh mất dữ liệu tạm thời
            System.err.println("Lỗi Database: " + e.getMessage());
        }

        transactionList.add(transaction);

        // Thao tác File (I/O)
        saveToFile();
    }

    // ========== CÁC PHƯƠNG THỨC TÍNH TOÁN (Stream API) ==========

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
        // Sử dụng LaTeX cho công thức tính toán đơn giản
        // $$Balance = \sum Income - \sum Expense$$
        return getTotalIncome() - getTotalExpense();
    }

    // ========== HỖ TRỢ LƯU TRỮ VÀ ĐỒNG BỘ ==========

    /**
     * Lưu danh sách vào file JSON.
     * Bọc ngoại lệ I/O để đảm bảo tính bền vững của dữ liệu.
     */
    private void saveToFile() throws DataLoadException {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            throw new DataLoadException("Không thể sao lưu dữ liệu vào JSON: " + e.getMessage());
        }
    }

    public void syncFromDatabase() throws DataLoadException {
        try {
            List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions();
            if (dbTransactions != null) {
                this.transactionList = new ArrayList<>(dbTransactions);
                saveToFile();
            }
        } catch (Exception e) {
            throw new DataLoadException("Lỗi đồng bộ dữ liệu từ Database: " + e.getMessage());
        }
    }

    // Các getter khác...
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactionList);
    }
}