package com.expensemanager;

import com.expensemanager.entity.*;
import com.expensemanager.service.FinanceService;
import com.expensemanager.ui.MainFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Khởi tạo FinanceService (sẽ load dữ liệu từ DB và JSON)
        FinanceService financeService = new FinanceService();

        // In ra để kiểm tra
        System.out.println("Tổng thu: " + financeService.getTotalIncome());
        System.out.println("Tổng chi: " + financeService.getTotalExpense());
        System.out.println("Số dư: " + financeService.getBalance());

        // Chạy giao diện
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}