package com.expensemanager.Service;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.entity.Category;
import java.util.List;
import java.util.stream.Collectors;

public class Statistics {
    private List<Transaction> transactions;

    public Statistics(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // THỐNG KÊ THEO THÁNG (OVERLOAD)
    // Tính tổng tiền theo loại giao dịch (Thu hoặc Chi) trong một tháng cụ thể
    public double calculateTotal(int month, int year, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }


    // Tính tổng tất cả chi tiêu/thu nhập (không phân biệt loại) trong một tháng
    public double calculateTotal(int month, int year) {
        return transactions.stream()
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    //  THỐNG KÊ THEO DANH MỤC (OVERLOAD)
    // Tính tổng tiền của một danh mục cụ thể (Ví dụ: Ăn uống, Lương...)
    public double calculateByCategory(Category category) {
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }


     // Tính tổng tiền của một danh mục trong một khoảng thời gian nhất định (tháng/năm)
    public double calculateByCategory(Category category, int month, int year) {
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .filter(t -> t.getDateTime().getMonthValue() == month)
                .filter(t -> t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // phương thức hỗ trợ khác.
    public List<Transaction> getHighValueTransactions(double threshold) {
        return transactions.stream()
                .filter(t -> t.getAmount() > threshold)
                .collect(Collectors.toList());
    }
}