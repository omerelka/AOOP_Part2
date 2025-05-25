package GUI;

import components.Branch;
import components.MainOffice;
import components.Package;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.*;

public class BranchInfoDialog extends JDialog {
    private JComboBox<String> branchComboBox;
    private JButton okButton;
    private JButton cancelButton;

    public BranchInfoDialog(JFrame parent, MainOffice mainOffice) {
        super(parent, "Choose branch", true);
        setLayout(new BorderLayout());

        // קומבו של סניפים
        branchComboBox = new JComboBox<>();
        branchComboBox.addItem("Sorting center");
        for (int i = 0; i < mainOffice.getBranches().size(); i++) {
            branchComboBox.addItem("Branch " + (i + 1));
        }

        JPanel centerPanel = new JPanel();
        centerPanel.add(branchComboBox);
        add(centerPanel, BorderLayout.CENTER);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener((ActionEvent e) -> {
            int index = branchComboBox.getSelectedIndex();

            // קבלת החבילות מהאב או מסניף
            Vector<Package> packages;
            if (index == 0) {
                packages = mainOffice.getHub().getPackages();
            } else {
                Branch branch = mainOffice.getBranches().get(index - 1);
                packages = branch.getPackages();
            }

            // יצירת טבלה
            String[] columnNames = { "Package ID", "Sender", "Destination", "Priority", "Status" };
            String[][] data = new String[packages.size()][5];

            for (int i = 0; i < packages.size(); i++) {
                Package p = packages.get(i);
                data[i][0] = String.valueOf(p.getPackageID());
                data[i][1] = p.getSenderAddress().toString();
                data[i][2] = p.getDestinationAddress().toString();
                data[i][3] = p.getPriority().toString();
                data[i][4] = p.getStatus().toString();
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 200));

            JOptionPane.showMessageDialog(this, scrollPane, "Package list", JOptionPane.PLAIN_MESSAGE);
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}