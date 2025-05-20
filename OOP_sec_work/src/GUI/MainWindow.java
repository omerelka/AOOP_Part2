package GUI;

import components.MainOffice;
import java.awt.*;
import javax.swing.*;

public class MainWindow extends JFrame {
    private volatile boolean running = false;
    private MainOffice mainOffice;
    private SystemPanel systemPanel = new SystemPanel();

    public MainWindow() {
        setTitle("Post tracking system");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());




        JPanel buttonPanel = new JPanel(new GridLayout(1, 6));



        //////////Create system///////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////
        JButton createButton = new JButton("Create system");
        createButton.addActionListener(e -> {
            CreateSystemDialog dialog = new CreateSystemDialog(this);
            dialog.setCreateSystemListener((branches, trucks, packages) -> {
                mainOffice = new MainOffice(branches, trucks,packages);
                systemPanel.setNumBranches(branches);
                systemPanel.setPackages(mainOffice.getPackages());
                systemPanel.setMainOffice(mainOffice);


                running = true;
                System.out.println("Program running.");

            });
            dialog.setVisible(true);
        });
        buttonPanel.add(createButton);

        //////////////////////////////////////////////////////////////////////////////////////////////////
        //////////Start///////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            if (mainOffice != null) {
                mainOffice.startSimulation();
                Timer repaintTimer = new Timer(500, evt -> systemPanel.repaint());
                repaintTimer.start();
            }
        });

        buttonPanel.add(startButton);


        //////////////////////////////////////////////////////////////////////////////////////////////////
        //////////Stop///////////
        ///////////////////////////////////////////////////////////////////////////////////////////////
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            if (mainOffice != null) {
                mainOffice.pauseSimulation();
            }
        });
        buttonPanel.add(stopButton);


        //////////////////////////////////////////////////////////////////////////////////////////////////
        //////////Resume///////////
        ///////////////////////////////////////////////////////////////////////////////////////////////


        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e ->{
            if(mainOffice != null){
                mainOffice.resumeSimulation();
            }
        });
        buttonPanel.add(resumeButton);

        //////////////////////////////////////////////////////////////////////////////////////////////////
        //////////All packages info///////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        JButton infoButton = new JButton("All packages info");
        infoButton.addActionListener(e -> {
            if (mainOffice != null) {
                new PackageTableWindow(mainOffice.getPackages());
            } else {
                JOptionPane.showMessageDialog(this, "System not created", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(infoButton);

        //////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////Branch info//////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        JButton branchInfoButton = new JButton("Branch info");
        branchInfoButton.addActionListener(e -> {
            if (mainOffice != null) {
                new BranchInfoDialog(this, mainOffice);
            } else {
                JOptionPane.showMessageDialog(this, "System not created", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(branchInfoButton);

        //////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        add(buttonPanel, BorderLayout.SOUTH);
        add(systemPanel, BorderLayout.CENTER);
    }
}
