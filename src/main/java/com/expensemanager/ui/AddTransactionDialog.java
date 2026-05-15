package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {
    private MainFrame mainFrame;
    private TransactionType selectedType = TransactionType.EXPENSE;
    private Category selectedCategory;
    private JTextField txtAmount, txtNote;
    private JPanel categoryPanel;
    private JButton btnExpense, btnIncome;

    // Khôi phục lại Map để lưu Emoji do người dùng tự định nghĩa
    public static Map<String, String> customEmojiMap = new HashMap<>();
    private static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
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
    }

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);

    public AddTransactionDialog(MainFrame parent) {
        super(parent, "Thêm giao dịch", true);
        this.mainFrame = parent;
        setSize(450, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // --- HEADER: Chọn Thu hoặc Chi ---
        JPanel header = new JPanel(new GridLayout(1, 2, 10, 0));
        header.setBackground(BG_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        btnExpense = createTypeButton("CHI TIÊU", true);
        btnIncome = createTypeButton("THU NHẬP", false);

        btnExpense.addActionListener(e -> switchType(TransactionType.EXPENSE));
        btnIncome.addActionListener(e -> switchType(TransactionType.INCOME));

        header.add(btnExpense);
        header.add(btnIncome);
        add(header, BorderLayout.NORTH);

        // --- CENTER: Form nhập liệu ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        centerPanel.add(createLabel("Số tiền"));
        txtAmount = new JTextField();
        styleTextField(txtAmount, "0");
        centerPanel.add(txtAmount);

        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(createLabel("Danh mục"));

        categoryPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        categoryPanel.setBackground(BG_COLOR);
        JScrollPane scroll = new JScrollPane(categoryPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        centerPanel.add(scroll);

        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(createLabel("Ghi chú"));
        txtNote = new JTextField();
        styleTextField(txtNote, "Nhập ghi chú...");
        centerPanel.add(txtNote);

        add(centerPanel, BorderLayout.CENTER);

        // --- FOOTER: Nút Lưu và Hủy (Khôi phục) ---
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new JButton("HỦY");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setBackground(SURFACE_COLOR);
        btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("LƯU");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setBackground(ACCENT_YELLOW);
        btnSave.setForeground(BG_COLOR);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        btnSave.addActionListener(e -> saveTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        refreshCategories();
    }

    private void switchType(TransactionType type) {
        this.selectedType = type;
        btnExpense.setBackground(type == TransactionType.EXPENSE ? new Color(244, 67, 54) : SURFACE_COLOR);
        btnIncome.setBackground(type == TransactionType.INCOME ? new Color(76, 175, 80) : SURFACE_COLOR);
        btnExpense.setForeground(Color.WHITE);
        btnIncome.setForeground(Color.WHITE);
        refreshCategories();
    }

    private void refreshCategories() {
        categoryPanel.removeAll();
        List<Category> list = DatabaseUtil.getAllCategories();
        for (Category c : list) {
            if (c.getType() == selectedType) {
                categoryPanel.add(createCategoryItem(c));
            }
        }
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(BG_COLOR);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Khôi phục logic ưu tiên lấy Custom Emoji
        String emoji = customEmojiMap.containsKey(c.getName()) ? customEmojiMap.get(c.getName()) : CATEGORY_EMOJI.getOrDefault(c.getName(), "📌");

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(SURFACE_COLOR);
        lblIcon.setPreferredSize(new Dimension(50, 50));

        JLabel lblName = new JLabel(c.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(TEXT_PRIMARY);

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedCategory = c;
                for (Component comp : categoryPanel.getComponents()) {
                    ((JPanel)comp).getComponent(0).setBackground(SURFACE_COLOR);
                }
                lblIcon.setBackground(ACCENT_YELLOW);
            }
        });

        return item;
    }

    // Khôi phục đầy đủ logic Save Transaction từ file gốc
    private void saveTransaction() {
        try {
            String userId = SessionManager.getCurrentUserId();
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "Bạn chưa đăng nhập!");
                return;
            }
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!");
                return;
            }
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục!");
                return;
            }
            String note = txtNote.getText().trim();
            String id = UUID.randomUUID().toString().substring(0, 8);
            Transaction t = new Transaction(id, amount, selectedType, selectedCategory, note);
            DatabaseUtil.insertTransaction(t, userId);

            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            dispose();
            if (mainFrame != null) mainFrame.refreshAllPanels();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!");
        }
    }

    // Khôi phục hàm tĩnh (Static method) bị xóa
    public static void addCustomEmoji(String categoryName, String emoji) {
        customEmojiMap.put(categoryName, emoji);
    }

    private JButton createTypeButton(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBackground(active ? (text.contains("CHI") ? new Color(244, 67, 54) : new Color(76, 175, 80)) : SURFACE_COLOR);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return b;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(150, 150, 150));
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(SURFACE_COLOR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }
}