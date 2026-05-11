package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {
    private JTextField txtAmount, txtNote, txtDate;
    private JComboBox<Category> cmbCategory;
    private JRadioButton rbIncome, rbExpense;
    private MainFrame mainFrame;
    private Transaction editTransaction;

    public AddTransactionDialog(MainFrame mainFrame) {
        this(mainFrame, null);
    }

    public AddTransactionDialog(MainFrame mainFrame, Transaction t) {
        super(mainFrame, t == null ? "Thêm giao dịch mới" : "Sửa giao dịch", true);
        this.mainFrame = mainFrame;
        this.editTransaction = t;
        setSize(420, 520);
        setLocationRelativeTo(mainFrame);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(10, 1, 5, 2));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 45, 10, 45));

        txtDate = createField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtAmount = createField("");
        txtNote = createField("");
        
        cmbCategory = new JComboBox<>();
        loadCategories();

        rbIncome = new JRadioButton("Thu nhập");
        rbExpense = new JRadioButton("Chi tiêu", true);
        rbIncome.setForeground(Color.WHITE);
        rbExpense.setForeground(Color.WHITE);
        rbIncome.setOpaque(false);
        rbExpense.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        g.add(rbIncome);
        g.add(rbExpense);
        JPanel tp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tp.setOpaque(false);
        tp.add(rbIncome);
        tp.add(rbExpense);

        if (editTransaction != null) {
            txtDate.setText(editTransaction.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtAmount.setText(String.format("%.0f", editTransaction.getAmount()));
            txtNote.setText(editTransaction.getNote());
            if (editTransaction.getType() == TransactionType.INCOME) rbIncome.setSelected(true);
            
            for (int i = 0; i < cmbCategory.getItemCount(); i++) {
                if (cmbCategory.getItemAt(i).getId().equals(editTransaction.getCategory().getId())) {
                    cmbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }

        p.add(label("Ngày (dd/MM/yyyy):"));
        p.add(txtDate);
        p.add(label("Số tiền (VND):"));
        p.add(txtAmount);
        p.add(label("Loại giao dịch:"));
        p.add(tp);
        p.add(label("Danh mục:"));
        p.add(cmbCategory);
        p.add(label("Ghi chú:"));
        p.add(txtNote);
        add(p, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        bp.setOpaque(false);
        JButton btn = new JButton(editTransaction == null ? "LƯU GIAO DỊCH" : "CẬP NHẬT GIAO DỊCH");
        btn.setPreferredSize(new Dimension(250, 42));
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> save());
        bp.add(btn);
        add(bp, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        List<Category> categories = DatabaseUtil.getAllCategories();
        cmbCategory.removeAllItems();
        for (Category c : categories) {
            cmbCategory.addItem(c);
        }
    }

    private JTextField createField(String t) {
        JTextField f = new JTextField(t);
        f.setBackground(new Color(50, 50, 50));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        return f;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private void save() {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
            LocalDate d = LocalDate.parse(txtDate.getText().trim(), dtf);
            double a = Double.parseDouble(txtAmount.getText().trim());
            TransactionType type = rbIncome.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
            Category cat = (Category) cmbCategory.getSelectedItem();

            if (cat == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng thêm danh mục trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editTransaction == null) {
                Transaction t = new Transaction(UUID.randomUUID().toString().substring(0, 8), a, type, cat, txtNote.getText());
                t.setDateTime(d.atStartOfDay());
                DatabaseUtil.insertTransaction(t);
            } else {
                editTransaction.setAmount(a);
                editTransaction.setNote(txtNote.getText());
                editTransaction.setDateTime(d.atStartOfDay());
                // Logic cập nhật đối tượng đã tồn tại trong Database
            }

            if (mainFrame != null) {
                mainFrame.refreshAllPanels();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: Kiểm tra lại định dạng ngày hoặc số tiền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}