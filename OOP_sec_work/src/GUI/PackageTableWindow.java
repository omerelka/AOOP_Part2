package GUI;

import components.Package;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PackageTableWindow extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public PackageTableWindow(List<Package> packages) {
        setTitle("Package Info");
        setSize(600, 400);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"ID", "Sender", "Destination", "Priority", "Status", "TypeP"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        updateTable(packages);

        // עדכון כל 1 שנייה
        new Timer(1000, e -> updateTable(packages)).start();

        setVisible(true);
    }

    private void updateTable(List<Package> packages) {
        model.setRowCount(0); // מנקה את הטבלה
        for (Package p : packages) {
            model.addRow(new Object[]{
                    p.getPackageID(),
                    p.getSenderAddress().toString(),
                    p.getDestinationAddress().toString(),
                    p.getPriority(),
                    p.getStatus(),
                    p.Type()
            });
        }
    }
}