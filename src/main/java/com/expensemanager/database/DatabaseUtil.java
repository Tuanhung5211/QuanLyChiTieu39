package com.expensemanager.database;

import com.expensemanager.entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }

    // ========== CATEGORIES ==========
    public static void insertCategory(Category category) {
        String sql = "INSERT INTO categories (id, name, type) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getType().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                TransactionType type = TransactionType.valueOf(rs.getString("type"));
                list.add(new Category(id, name, type));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========== TRANSACTIONS ==========
    public static void insertTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (id, amount, type, category_id, date_time, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getType().name());
            stmt.setString(4, transaction.getCategory().getId());
            stmt.setTimestamp(5, Timestamp.valueOf(transaction.getDateTime()));
            stmt.setString(6, transaction.getNote());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name, c.type as category_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                double amount = rs.getDouble("amount");
                TransactionType type = TransactionType.valueOf(rs.getString("type"));
                String categoryId = rs.getString("category_id");
                String categoryName = rs.getString("category_name");
                TransactionType categoryType = TransactionType.valueOf(rs.getString("category_type"));
                Category category = new Category(categoryId, categoryName, categoryType);
                LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
                String note = rs.getString("note");

                Transaction transaction = new Transaction(id, amount, type, category, note);
                list.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET amount=?, type=?, category_id=?, note=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getType().name());
            stmt.setString(3, transaction.getCategory().getId());
            stmt.setString(4, transaction.getNote());
            stmt.setString(5, transaction.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTransaction(String id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== BUDGETS ==========
    public static void insertBudget(Budget budget) {
        String sql = "INSERT INTO budgets (id, month, year, budget_limit, spent) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, budget.getId());
            stmt.setInt(2, budget.getMonth());
            stmt.setInt(3, budget.getYear());
            stmt.setDouble(4, budget.getLimit());
            stmt.setDouble(5, budget.getSpent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBudget(Budget budget) {
        String sql = "UPDATE budgets SET spent=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, budget.getSpent());
            stmt.setString(2, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Budget getBudget(int month, int year) {
        String sql = "SELECT * FROM budgets WHERE month=? AND year=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                double limit = rs.getDouble("budget_limit");
                double spent = rs.getDouble("spent");
                Budget budget = new Budget(id, month, year, limit);
                budget.setSpent(spent);
                return budget;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}