package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

public class EmojiUtil {
    public static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        // 🌟 ĐÃ ĐỒNG BỘ 100% UNICODE TÊN GỌI VỚI HỆ THỐNG DANH MỤC THỰC TẾ TRÊN DB
        CATEGORY_EMOJI.put("Ăn uống", "\uD83C\uDF54");    // 🍔
        CATEGORY_EMOJI.put("Đi chợ", "\uD83D\uDED2");     // 🛒
        CATEGORY_EMOJI.put("Ăn vặt", "\uD83C\uDF7F");     // 🍿
        CATEGORY_EMOJI.put("Trái cây", "\uD83C\uDF4E");    // 🍎
        CATEGORY_EMOJI.put("Mua sắm", "\uD83D\uDECD");     // 🛍️
        CATEGORY_EMOJI.put("Quần áo", "\uD83D\uDC57");     // 👗
        CATEGORY_EMOJI.put("Điện tử", "\uD83D\uDCBB");     // 💻
        CATEGORY_EMOJI.put("Xăng dầu", "\u26FD");         // ⛽
        CATEGORY_EMOJI.put("Xe cộ", "\uD83C\uDFCD");      // 🏍️
        CATEGORY_EMOJI.put("Di chuyển", "\uD83D\uDE97");   // 🚗
        CATEGORY_EMOJI.put("Điện nước", "\u26A1");         // ⚡
        CATEGORY_EMOJI.put("Internet", "\uD83C\uDF10");    // 🌐
        CATEGORY_EMOJI.put("Thuê nhà", "\uD83C\uDFE2");    // 🏢
        CATEGORY_EMOJI.put("Điện thoại", "\uD83D\uDCF1");   // 📱
        CATEGORY_EMOJI.put("Giải trí", "\uD83C\uDFAE");    // 🎮
        CATEGORY_EMOJI.put("Phim ảnh", "\uD83C\uDFAC");    // 🎬
        CATEGORY_EMOJI.put("Du lịch", "\u2708");           // ✈️
        CATEGORY_EMOJI.put("Học tập", "\uD83D\uDCDA");     // 📚
        CATEGORY_EMOJI.put("Sức khỏe", "\uD83C\uDFE5");    // 🏥
        CATEGORY_EMOJI.put("Thuốc men", "\uD83D\uDC8A");    // 💊
        CATEGORY_EMOJI.put("Làm đẹp", "\uD83D\uDC84");     // 💄
        CATEGORY_EMOJI.put("Thể thao", "\u26BD");         // ⚽
        CATEGORY_EMOJI.put("Thú cưng", "\uD83D\uDC31");    // 🐱
        CATEGORY_EMOJI.put("Chi khác", "\uD83D\uDCCD");    // 📌

        // DANH MỤC THU
        CATEGORY_EMOJI.put("Lương", "\uD83D\uDCB0");       // 💰
        CATEGORY_EMOJI.put("Thưởng", "\uD83D\uDCB5");      // 💵
        CATEGORY_EMOJI.put("Học bổng", "\uD83C\uDF93");    // 🎓
        CATEGORY_EMOJI.put("Được cho", "\u2709\uFE0F");    // ✉️
        CATEGORY_EMOJI.put("Làm thêm", "\uD83D\uDCBC");    // 💼
        CATEGORY_EMOJI.put("Đầu tư", "\uD83D\uDCC8");      // 📈
        CATEGORY_EMOJI.put("Tiết kiệm", "\uD83D\uDC37");   // 🐷
        CATEGORY_EMOJI.put("Thu khác", "\uD83E\uDE99");    // 🪙
    }
}