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
    private JToggleButton activeCategoryButton;
    private JButton btnExpense, btnIncome;

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

    public AddTransactionDialog(MainFrame mainFrame) {
        super(mainFrame, "Thêm giao dịch", true);
        this.mainFrame = mainFrame;
        setSize(480, 620);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        // Tiêu đề
        JLabel title = new JLabel("Thêm giao dịch", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Tab chọn loại
        JPanel typeTabs = new JPanel(new GridLayout(1, 2, 0, 0));
        typeTabs.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        btnExpense = new JButton("Chi tiêu");
        btnExpense.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExpense.setBackground(new Color(0, 153, 76));
        btnExpense.setForeground(Color.WHITE);
        btnExpense.addActionListener(e -> switchType(TransactionType.EXPENSE));
        btnIncome = new JButton("Thu nhập");
        btnIncome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIncome.setBackground(Color.LIGHT_GRAY);
        btnIncome.setForeground(Color.BLACK);
        btnIncome.addActionListener(e -> switchType(TransactionType.INCOME));
        typeTabs.add(btnExpense);
        typeTabs.add(btnIncome);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(typeTabs, BorderLayout.NORTH);
        centerPanel.add(createCategoryGrid(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createCategoryGrid() {
        categoryPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        categoryPanel.setBackground(new Color(245, 245, 245));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loadCategories(selectedType);
        JScrollPane scrollPane = new JScrollPane(categoryPanel);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private void loadCategories(TransactionType type) {
        categoryPanel.removeAll();
        List<Category> categories = DatabaseUtil.getAllCategories();
        for (Category c : categories) {
            if (c.getType() == type) {
                String emoji = customEmojiMap.getOrDefault(c.getName(),
                        CATEGORY_EMOJI.getOrDefault(c.getName(), "📌"));
                JToggleButton btn = new JToggleButton("<html><center>" + emoji + "<br>" + c.getName() + "</center></html>");
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btn.setHorizontalTextPosition(SwingConstants.CENTER);
                btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn.setPreferredSize(new Dimension(90, 70));
                btn.setBackground(Color.WHITE);
                btn.addActionListener(e -> {
                    if (activeCategoryButton != null) activeCategoryButton.setSelected(false);
                    activeCategoryButton = btn;
                    btn.setSelected(true);
                    selectedCategory = c;
                });
                categoryPanel.add(btn);
            }
        }
        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnAdd.setPreferredSize(new Dimension(90, 70));
        btnAdd.setBackground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            AddCategoryDialog dlg = new AddCategoryDialog(mainFrame, () -> loadCategories(selectedType));
            dlg.setVisible(true);
        });
        categoryPanel.add(btnAdd);
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private void switchType(TransactionType type) {
        selectedType = type;
        if (type == TransactionType.EXPENSE) {
            btnExpense.setBackground(new Color(0, 153, 76));
            btnExpense.setForeground(Color.WHITE);
            btnIncome.setBackground(Color.LIGHT_GRAY);
            btnIncome.setForeground(Color.BLACK);
        } else {
            btnIncome.setBackground(new Color(0, 102, 204));
            btnIncome.setForeground(Color.WHITE);
            btnExpense.setBackground(Color.LIGHT_GRAY);
            btnExpense.setForeground(Color.BLACK);
        }
        loadCategories(type);
        selectedCategory = null;
        if (activeCategoryButton != null) {
            activeCategoryButton.setSelected(false);
            activeCategoryButton = null;
        }
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Số tiền:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        txtAmount = new JTextField();
        panel.add(txtAmount, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        txtNote = new JTextField();
        panel.add(txtNote, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        JButton btnSave = new JButton("Lưu");
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveTransaction());
        panel.add(btnSave, gbc);

        gbc.gridx = 2;
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        panel.add(btnCancel, gbc);

        return panel;
    }

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

    public static void addCustomEmoji(String categoryName, String emoji) {
        customEmojiMap.put(categoryName, emoji);
    }
}