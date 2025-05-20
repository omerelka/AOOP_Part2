package GUI;
// SystemPanel.java
// Custom JPanel responsible for drawing the delivery simulation UI
import components.Branch;
import components.MainOffice;
import components.Package;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import GUI.TruckVisual;
import components.Truck;


public class SystemPanel extends JPanel {
    private MainOffice mainOffice;
    private int numBranches = 0;
    private Vector<Package> Packs = new Vector<Package>();
    private Vector<TruckVisual> truckVisuals = new Vector<TruckVisual>();

    public void setMainOffice(MainOffice mo) {
        this.mainOffice = mo;
    }

    public void setNumBranches(int n) {
        this.numBranches = n;
        repaint();
    }

    public void setPackages(Vector<Package> Packs){
        this.Packs = Packs;
    }

    // Assigns screen coordinates (Point) to all branches, packages, and the hub before rendering
    private void setupPoints(int width, int height, int branchWidth, int branchHeight, int spacing) {
        int branchX = 50;

        // Set each branch's visual center point
        for (int i = 0; i < numBranches; i++) {
            int y = spacing * (i + 1);
            int centerX = branchX + branchWidth / 2;
            int centerY = y;
            mainOffice.getBranches().get(i).setLocation(new Point(centerX, centerY));
        }

        // Set sender and destination points for each package (top and bottom of the screen)
        for (int i = 0; i < Packs.size(); i++) {
            int baseX = 150 + i * 70;
            int topY = 65;
            int bottomY = height - 65;
            Package pkg = Packs.get(i);
            pkg.setSenderPoint(new Point(baseX, topY));
            pkg.setDestinationPoint(new Point(baseX, bottomY));
        }

        // Set the center point of the hub rectangle
        if (mainOffice.getHub() != null) {
            int hubX = width - 100;
            int hubY = (height - 200) / 2;
            int hubCenterX = hubX + 40 / 2;
            int hubCenterY = hubY + 200 / 2;
            mainOffice.getHub().setLocation(new Point(hubCenterX, hubCenterY));
        }
       
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (numBranches == 0) return;

        Graphics2D g2 = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Prepare dimensions and layout spacing based on panel size
        int branchWidth = 40;
        int branchHeight = 30;
        int spacing = height / (numBranches + 1);

        int branchX = 50;
        int hubWidth = 40;
        int hubHeight = 200;
        int hubX = width - 100;
        int hubY = (height - hubHeight) / 2;

        // Compute and assign all component locations for this frame
        setupPoints(width, height, branchWidth, branchHeight, spacing);

        // Draw hub
        g2.setColor(Color.decode("#0B7E17"));
        g2.fillRect(hubX, hubY, hubWidth, hubHeight);

        // Draw each branch as a rectangle and a green line to the hub
        for (int i = 0; i < numBranches; i++) {
            Branch branch = mainOffice.getBranches().get(i);
            Point branchCenter = branch.getLocation();

            // Draw branch
            g2.setColor(Color.decode("#1AB4E5"));
            g2.fillRect(branchCenter.x - branchWidth / 2, branchCenter.y - branchHeight / 2, branchWidth, branchHeight);

            // Draw line from branch center to hub center
            Point hubCenter = mainOffice.getHub().getLocation();
            g2.setColor(Color.decode("#0B7E17"));
            g2.drawLine(branchCenter.x, branchCenter.y, hubCenter.x, hubCenter.y);
        }

        // Draw red (sender) and pink (receiver) circles for each package
        // Draw lines representing movement routes:
        // - Red lines for NonStandardPackage (direct to hub and destination)
        // - Blue lines for Standard/SmallPackage (via sender/receiver branches)
        for (int i = 0; i < Packs.size(); i++) {
            Package p = Packs.get(i);
            Point senderPoint = p.getSenderPoint();
            Point destinationPoint = p.getDestinationPoint();
            int radius = 14;

            // עיגול עליון - אדום
            g2.setColor(Color.RED);
            g2.fillOval(senderPoint.x - radius / 2, senderPoint.y - radius / 2, 30, 30);

            // עיגול תחתון - ורוד
            g2.setColor(Color.PINK);
            g2.fillOval(destinationPoint.x - radius / 2, destinationPoint.y - radius / 2, 30, 30);

            if (p instanceof components.NonStandardPackage) {
                Point hubCenter = mainOffice.getHub().getLocation();
                // Red: sender → hub
                g2.setColor(Color.RED);
                g2.drawLine(senderPoint.x, senderPoint.y + 15, hubCenter.x, hubCenter.y);

                // Red: sender → destination
                g2.drawLine(senderPoint.x, senderPoint.y + 15, destinationPoint.x, destinationPoint.y);
            } else {
                Branch senderBranch = mainOffice.getBranches().get(p.getSenderAddress().getZip());
                Branch receiverBranch = mainOffice.getBranches().get(p.getDestinationAddress().getZip());
                Point senderBranchPoint = senderBranch.getLocation();
                Point receiverBranchPoint = receiverBranch.getLocation();

                g2.setColor(Color.BLUE);
                g2.drawLine(senderPoint.x, senderPoint.y + 15, senderBranchPoint.x, senderBranchPoint.y);
                g2.drawLine(receiverBranchPoint.x, receiverBranchPoint.y, destinationPoint.x, destinationPoint.y);
            }

        }

        // Assign initial truck positions based on their branch or hub location
        for (Branch b : mainOffice.getBranches()) {
            for (components.Truck t : b.getListTrucks()) {
                t.setLocation(b.getLocation()); // initial truck position at branch
            }
        }
        for (components.Truck t : mainOffice.getHub().getListTrucks()) {
            t.setLocation(mainOffice.getHub().getLocation());
        }

    }
}