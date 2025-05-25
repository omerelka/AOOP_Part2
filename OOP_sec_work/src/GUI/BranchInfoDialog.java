package GUI;

import components.MainOffice;
import components.Package;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BranchInfoDialog extends JDialog {
    private JComboBox<String> branchComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private MainOffice mainOffice;
    private JFrame branchTableWindow;

    public BranchInfoDialog(JFrame parent, MainOffice mainOffice) {
        super(parent, "Choose branch", true);
        this.mainOffice = mainOffice;
        setLayout(new BorderLayout());

        // Combo box for branches
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
            int selectedIndex = branchComboBox.getSelectedIndex();
            String selectedBranchName = (String) branchComboBox.getSelectedItem();
            
            // Create the real-time updating window
            createBranchTableWindow(selectedIndex, selectedBranchName);
            
            // Close this dialog
            dispose();
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

    private void createBranchTableWindow(int branchIndex, String branchName) {
        branchTableWindow = new JFrame("Branch Info - " + branchName);
        branchTableWindow.setSize(600, 400);
        branchTableWindow.setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Package ID", "Sender", "Destination", "Priority", "Status"}, 0);
        JTable table = new JTable(model);
        branchTableWindow.add(new JScrollPane(table), BorderLayout.CENTER);

        // Method to update table data
        Runnable updateTable = () -> {
            model.setRowCount(0); // Clear the table
            
            Vector<Package> packages;
            if (branchIndex == 0) {
                // Hub packages (Sorting center)
                packages = mainOffice.getHub().getPackages();
            } else {
                // Branch packages (convert to 0-based index)
                packages = mainOffice.getBranches().get(branchIndex - 1).getPackages();
            }
            
            for (Package p : packages) {
                model.addRow(new Object[]{
                        p.getPackageID(),
                        p.getSenderAddress().toString(),
                        p.getDestinationAddress().toString(),
                        p.getPriority(),
                        p.getStatus()
                });
            }
        };

        // Initial table update
        updateTable.run();

        // Update every 1 second
        Timer updateTimer = new Timer(1000, e -> updateTable.run());
        updateTimer.start();

        branchTableWindow.setVisible(true);
    }
}