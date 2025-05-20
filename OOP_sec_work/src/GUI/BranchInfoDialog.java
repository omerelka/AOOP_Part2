package GUI;

import components.MainOffice;
import components.Branch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class BranchInfoDialog extends JDialog {
    private JComboBox<String> branchComboBox;
    private JButton okButton;
    private JButton cancelButton;

    public BranchInfoDialog(JFrame parent, MainOffice mainOffice) {
        super(parent, "Choose branch", true);
        setLayout(new BorderLayout());

        // רשימת סניפים
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
            String info;

            if (index == 0) {
                info = mainOffice.getHub().toString();
            } else {
                Branch branch = mainOffice.getBranches().get(index - 1);
                info = branch.toString();
            }

            JOptionPane.showMessageDialog(this, info, "Branch info", JOptionPane.INFORMATION_MESSAGE);
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