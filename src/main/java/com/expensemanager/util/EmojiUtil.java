package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

public class EmojiUtil {
    public static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        // --- Danh mục mặc định cũ ---
        CATEGORY_EMOJI.put("Mua sắm", "🛍️");
        CATEGORY_EMOJI.put("Ăn uống", "🍔");
        CATEGORY_EMOJI.put("Điện thoại", "📱");
        CATEGORY_EMOJI.put("Giải trí", "🎮");
        CATEGORY_EMOJI.put("Giáo dục", "📚");
        CATEGORY_EMOJI.put("Làm đẹp", "💄");
        CATEGORY_EMOJI.put("Thể thao", "⚽");
        CATEGORY_EMOJI.put("Xã hội", "👥");
        CATEGORY_EMOJI.put("Di chuyển", "🚗");
        CATEGORY_EMOJI.put("Quần áo", "👗");
        CATEGORY_EMOJI.put("Xe cộ", "🏍️");
        CATEGORY_EMOJI.put("Điện tử", "💻");
        CATEGORY_EMOJI.put("Du lịch", "✈️");
        CATEGORY_EMOJI.put("Sức khỏe", "🏥");
        CATEGORY_EMOJI.put("Sửa chữa", "🔧");
        CATEGORY_EMOJI.put("Nhà cửa", "🏠");
        CATEGORY_EMOJI.put("Quà tặng", "🎁");
        CATEGORY_EMOJI.put("Từ thiện", "💖");
        CATEGORY_EMOJI.put("Ăn vặt", "🍿");
        CATEGORY_EMOJI.put("Trái cây", "🍎");
        CATEGORY_EMOJI.put("Lương", "💰");
        CATEGORY_EMOJI.put("Học bổng", "🎓");
        CATEGORY_EMOJI.put("Tiền được cho", "💵");

        // --- 🌟 BỔ SUNG: Loạt danh mục thực tế phong phú mới 🌟 ---
        CATEGORY_EMOJI.put("Xăng dầu", "⛽");
        CATEGORY_EMOJI.put("Thú cưng", "🐱");
        CATEGORY_EMOJI.put("Thuốc men", "💊");
        CATEGORY_EMOJI.put("Điện nước", "⚡");
        CATEGORY_EMOJI.put("Internet", "🌐");
        CATEGORY_EMOJI.put("Thuê nhà", "🏢");
        CATEGORY_EMOJI.put("Bảo hiểm", "🛡️");
        CATEGORY_EMOJI.put("Đầu tư", "📈");
        CATEGORY_EMOJI.put("Làm thêm", "💼");
        CATEGORY_EMOJI.put("Tiết kiệm", "🐷");
    }
}