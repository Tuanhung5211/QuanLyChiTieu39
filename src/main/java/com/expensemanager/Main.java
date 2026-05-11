package com.expensemanager;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // ===== 1. TEST KẾT NỐI =====
        System.out.println("===== KIỂM TRA KẾT NỐI DATABASE =====");
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("✅ Kết nối MySQL thành công!\n");
        } catch (Exception e) {
            System.out.println("❌ Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
            return; // dừng nếu không kết nối được
        }

        // ===== 2. TEST CRUD CATEGORY =====
        System.out.println("===== TEST CATEGORY CRUD =====");

        // Thêm một danh mục mới
        Category newCat = new Category("CAT999", "Giải trí", TransactionType.EXPENSE);
        DatabaseUtil.insertCategory(newCat);
        System.out.println("✅ Đã thêm danh mục: " + newCat);

        // Đọc tất cả danh mục
        List<Category> categories = DatabaseUtil.getAllCategories();
        System.out.println("Danh sách danh mục:");
        for (Category c : categories) {
            System.out.println("  - " + c.getId() + ": " + c.getName() + " (" + c.getType() + ")");
        }

        // ===== 3. TEST CRUD TRANSACTION =====
        System.out.println("\n===== TEST TRANSACTION CRUD =====");

        // Lấy một danh mục để dùng cho giao dịch
        Category catAnUong = null;
        for (Category c : categories) {
            if (c.getId().equals("CAT001")) {
                catAnUong = c;
                break;
            }
        }

        if (catAnUong != null) {
            // Thêm một giao dịch mới
            Transaction newTxn = new Transaction("TX999", 120000, TransactionType.EXPENSE, catAnUong, "Cà phê với bạn");
            DatabaseUtil.insertTransaction(newTxn);
            System.out.println("✅ Đã thêm giao dịch: " + newTxn);

            // Sửa giao dịch vừa thêm
            newTxn.setAmount(150000);
            newTxn.setNote("Cà phê với bạn (đã update)");
            DatabaseUtil.updateTransaction(newTxn);
            System.out.println("✅ Đã cập nhật giao dịch TX999");
        }

        // Đọc tất cả giao dịch
        List<Transaction> transactions = DatabaseUtil.getAllTransactions();
        System.out.println("Danh sách giao dịch:");
        for (Transaction t : transactions) {
            System.out.println("  - " + t);
        }

        // Xóa giao dịch TX999
        DatabaseUtil.deleteTransaction("TX999");
        System.out.println("✅ Đã xóa giao dịch TX999");

        // ===== 4. TEST CRUD BUDGET =====
        System.out.println("\n===== TEST BUDGET CRUD =====");

        // Thêm ngân sách tháng 6/2026
        Budget newBudget = new Budget("BUD006", 6, 2026, 8000000);
        DatabaseUtil.insertBudget(newBudget);
        System.out.println("✅ Đã thêm ngân sách tháng 6/2026");

        // Đọc ngân sách tháng 5/2026
        Budget budgetMay = DatabaseUtil.getBudget(5, 2026);
        if (budgetMay != null) {
            System.out.println("Ngân sách tháng 5/2026: " + budgetMay);
            // Cập nhật chi tiêu
            budgetMay.addSpent(500000);
            DatabaseUtil.updateBudget(budgetMay);
            System.out.println("✅ Đã cập nhật chi tiêu tháng 5/2026");
        }

        System.out.println("\n===== TẤT CẢ TEST HOÀN TẤT =====");
    }
}