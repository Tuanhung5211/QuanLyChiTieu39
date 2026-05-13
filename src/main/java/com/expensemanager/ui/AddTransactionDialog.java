package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {
    private JTextField txtA, txtN, txtD;
    private JComboBox<Category> cmbC;
    private JRadioButton rbI, rbE;
    private MainFrame mainFrame;
    private Transaction editT;

    public AddTransactionDialog(MainFrame mf) { this(mf, null); }
    public AddTransactionDialog(MainFrame mf, Transaction t) {
        super(mf, t == null ? "Thêm giao dịch mới" : "Sửa giao dịch", true);
        this.mainFrame = mf; this.editT = t;
        setSize(450, 550); setLocationRelativeTo(mf);
        getContentPane().setBackground(new Color(30, 30, 30)); setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(10, 1, 5, 2)); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 45, 10, 45));

        txtD = f(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtA = f(""); txtN = f(""); cmbC = new JComboBox<>();

        rbI = new JRadioButton("Thu nhập"); rbE = new JRadioButton("Chi tiêu", true);
        rbI.setForeground(Color.WHITE); rbE.setForeground(Color.WHITE);
        rbI.setOpaque(false); rbE.setOpaque(false);
        ButtonGroup g = new ButtonGroup(); g.add(rbI); g.add(rbE);
        
        rbI.addActionListener(e -> loadFilteredCategories());
        rbE.addActionListener(e -> loadFilteredCategories());

        JPanel tp = new JPanel(new FlowLayout(FlowLayout.LEFT)); tp.setOpaque(false); tp.add(rbI); tp.add(rbE);

        loadFilteredCategories();

        if (editT != null) {
            txtD.setText(editT.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtA.setText(String.format("%.0f", editT.getAmount()));
            txtN.setText(editT.getNote());
            if (editT.getType() == TransactionType.INCOME) { rbI.setSelected(true); loadFilteredCategories(); }
            for (int i=0; i<cmbC.getItemCount(); i++) if (cmbC.getItemAt(i).getName().equals(editT.getCategory().getName())) cmbC.setSelectedIndex(i);
        }

        p.add(l("Ngày (dd/MM/yyyy):")); p.add(txtD);
        p.add(l("Số tiền (VND):")); p.add(txtA);
        p.add(l("Loại giao dịch:")); p.add(tp);
        p.add(l("Danh mục:")); p.add(cmbC);
        p.add(l("Ghi chú:")); p.add(txtN);
        add(p, BorderLayout.CENTER);

        JButton btn = new JButton(editT == null ? "LƯU GIAO DỊCH" : "CẬP NHẬT");
        btn.setBackground(new Color(46, 204, 113));
        btn.addActionListener(e -> save());
        add(btn, BorderLayout.SOUTH);
    }

    private void loadFilteredCategories() {
        cmbC.removeAllItems();
        TransactionType type = rbI.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
        for (Category c : DatabaseUtil.getAllCategories()) {
            if (c.getType() == type) cmbC.addItem(c);
        }
    }

    private void save() {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
            LocalDate d = LocalDate.parse(txtD.getText().trim(), dtf);
            double a = Double.parseDouble(txtA.getText().trim());
            Category cat = (Category) cmbC.getSelectedItem();
            TransactionType type = rbI.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;

            if (editT == null) {
                Transaction tr = new Transaction(UUID.randomUUID().toString().substring(0,8), a, type, cat, txtN.getText());
                tr.setDateTime(d.atStartOfDay());
                mainFrame.getFinanceService().addTransaction(tr);
                JOptionPane.showMessageDialog(this, "Thêm giao dịch thành công!");
            } else {
                editT.setAmount(a); editT.setNote(txtN.getText()); editT.setDateTime(d.atStartOfDay());
                DatabaseUtil.updateTransaction(editT);
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            }
            mainFrame.refreshAllPanels();
            dispose();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi dữ liệu!"); }
    }
    private JTextField f(String t) { JTextField f = new JTextField(t); f.setBackground(new Color(50, 50, 50)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE); return f; }
    private JLabel l(String t) { JLabel l = new JLabel(t); l.setForeground(Color.WHITE); return l; }
}