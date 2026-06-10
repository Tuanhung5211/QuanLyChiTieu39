package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Dịch tên danh mục mặc định giữa tiếng Việt và tiếng Anh.
 * Các danh mục mặc định được lưu trong database bằng tiếng Việt.
 */
public class CategoryTranslator {

    private static final Map<String, String> VI_TO_EN = new HashMap<>();
    private static final Map<String, String> EN_TO_VI = new HashMap<>();

    static {
        add("Ăn uống", "Food & Dining");
        add("Đi chợ", "Grocery");
        add("Ăn vặt", "Snacks");
        add("Trái cây", "Fruits");
        add("Mua sắm", "Shopping");
        add("Quần áo", "Clothing");
        add("Điện tử", "Electronics");
        add("Xăng dầu", "Gasoline");
        add("Xe cộ", "Vehicles");
        add("Di chuyển", "Transport");
        add("Điện nước", "Utilities");
        add("Internet", "Internet");
        add("Thuê nhà", "Rent");
        add("Điện thoại", "Phone");
        add("Giải trí", "Entertainment");
        add("Phim ảnh", "Movies");
        add("Du lịch", "Travel");
        add("Học tập", "Education");
        add("Sức khỏe", "Health");
        add("Thuốc men", "Medicine");
        add("Làm đẹp", "Beauty");
        add("Thể thao", "Sports");
        add("Thú cưng", "Pets");
        add("Chi khác", "Other Expense");
        add("Lương", "Salary");
        add("Thưởng", "Bonus");
        add("Học bổng", "Scholarship");
        add("Được cho", "Gift");
        add("Làm thêm", "Part-time Job");
        add("Đầu tư", "Investment");
        add("Tiết kiệm", "Savings");
        add("Thu khác", "Other Income");
    }

    private static void add(String vi, String en) {
        VI_TO_EN.put(vi, en);
        EN_TO_VI.put(en, vi);
    }

    /**
     * Trả về tên hiển thị của danh mục phù hợp với ngôn ngữ.
     *
     * @param originalName tên gốc trong database (thường là tiếng Việt)
     * @param isVietnamese true nếu giao diện đang là tiếng Việt, false nếu tiếng Anh
     * @return tên đã dịch (nếu có trong từ điển), ngược lại trả về tên gốc
     */
    public static String translate(String originalName, boolean isVietnamese) {
        if (originalName == null) return "";
        if (isVietnamese) {
            // Nếu giao diện tiếng Việt, trả về tiếng Việt (có thể đã là tiếng Việt)
            // Nếu tên gốc là tiếng Anh (do người dùng tự tạo), thử dịch ngược lại tiếng Việt
            String vi = EN_TO_VI.get(originalName);
            return vi != null ? vi : originalName;
        } else {
            // Giao diện tiếng Anh, dịch từ tiếng Việt sang tiếng Anh
            String en = VI_TO_EN.get(originalName);
            return en != null ? en : originalName;
        }
    }
}