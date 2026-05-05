package com.expensemanager;

import com.expensemanager.entity.*;
import com.expensemanager.database.*;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Test kết nối
        try {
            Connection conn = DatabaseUtil.getConnection();
            System.out.println("Kết nối MySQL thành công!");
            conn.close();
        } catch (Exception e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}