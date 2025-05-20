package GUI;


import javax.swing.*;
import java.awt.*;



public class CreateSystemDialog extends JDialog {


    private CreateSystemListener listener;

    public void setCreateSystemListener(CreateSystemListener listener) {
        this.listener = listener;
    }

    private JSlider branchSlider;
    private JSlider truckSlider;
    private JSlider packageSlider;
    private boolean confirmed = false;

    public CreateSystemDialog(JFrame parent) {
        super(parent, "Create post system", true); // modal


        setLayout(new GridLayout(4, 1, 10, 10));
        setSize(600, 400);
        setLocationRelativeTo(parent);

        branchSlider = createSlider(1, 10, 5, "Number of branches");
        truckSlider = createSlider(1, 10, 5, "Number of trucks per branch");
        packageSlider = createSlider(2, 20, 8, "Number of packages");

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            confirmed = true;

            if (listener != null) {
                listener.onSystemCreated(
                        branchSlider.getValue(),
                        truckSlider.getValue(),
                        packageSlider.getValue()
                );
            }

            setVisible(false);
        });

        cancelButton.addActionListener(e -> setVisible(false));

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel);
    }

    private JSlider createSlider(int min, int max, int value, String labelText) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText, JLabel.CENTER);
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        panel.add(label, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        add(panel);
        return slider;
    }

    public int getBranches() { return branchSlider.getValue(); }
    public int getTrucks() { return truckSlider.getValue(); }
    public int getPackages() { return packageSlider.getValue(); }
    public boolean isConfirmed() { return confirmed; }
}