package GUI;

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

    public void setMainOffice(MainOffice mo) {
        this.mainOffice = mo;
    }

    public void setNumBranches(int n) {
        this.numBranches = n;
        repaint();
    }

    public void setPackages(Vector<Package> Packs) {
        this.Packs = Packs;
    }

    // Collects all truck visuals from branches and hub
    private Vector<TruckVisual> getAllTruckVisuals() {
        Vector<TruckVisual> allVisuals = new Vector<>();
        
        if (mainOffice == null) return allVisuals;
        
        // Get truck visuals from all branches
        for (Branch branch : mainOffice.getBranches()) {
            for (Truck truck : branch.getListTrucks()) {
                if (truck.getTruckVisual() != null) {
                    allVisuals.add(truck.getTruckVisual());
                }
            }
        }
        
        // Get truck visuals from hub
        if (mainOffice.getHub() != null) {
            for (Truck truck : mainOffice.getHub().getListTrucks()) {
                if (truck.getTruckVisual() != null) {
                    allVisuals.add(truck.getTruckVisual());
                }
            }
        }
        
        return allVisuals;
    }

    // Assigns screen coordinates to all branches, packages, and the hub
    private void setupPoints(int width, int height, int branchWidth, int branchHeight, int spacing) {
        int branchX = 50;

        // Set each branch's visual center point
        for (int i = 0; i < numBranches; i++) {
            int y = spacing * (i + 1);
            int centerX = branchX + branchWidth / 2;
            int centerY = y;
            mainOffice.getBranches().get(i).setLocation(new Point(centerX, centerY));
        }

        // Set sender and destination points for each package
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

        if (numBranches == 0 || mainOffice == null) return;

        Graphics2D g2 = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Prepare dimensions and layout spacing
        int branchWidth = 40;
        int branchHeight = 30;
        int spacing = height / (numBranches + 1);

        int branchX = 50;
        int hubWidth = 40;
        int hubHeight = 200;
        int hubX = width - 100;
        int hubY = (height - hubHeight) / 2;

        // Compute and assign all component locations
        setupPoints(width, height, branchWidth, branchHeight, spacing);

        // Draw hub
        g2.setColor(Color.decode("#0B7E17"));
        g2.fillRect(hubX, hubY, hubWidth, hubHeight);

        // Draw branches and connection lines
        for (int i = 0; i < numBranches; i++) {
            Branch branch = mainOffice.getBranches().get(i);
            Point branchCenter = branch.getLocation();

            // Determine branch color based on packages to deliver
            boolean hasPackagesToDeliver = false;
            synchronized (branch.getPackages()) {
                for (Package p : branch.getPackages()) {
                    if (p.getStatus() == components.Status.DELIVERY) {
                        hasPackagesToDeliver = true;
                        break;
                    }
                }
            }

            // Draw branch with appropriate color
            if (hasPackagesToDeliver) {
                g2.setColor(Color.decode("#1A5490")); // Dark blue
            } else {
                g2.setColor(Color.decode("#1AB4E5")); // Light blue
            }
            g2.fillRect(branchCenter.x - branchWidth / 2, branchCenter.y - branchHeight / 2, branchWidth, branchHeight);

            // Draw line from branch center to hub center
            Point hubCenter = mainOffice.getHub().getLocation();
            g2.setColor(Color.decode("#0B7E17"));
            g2.drawLine(branchCenter.x, branchCenter.y, hubCenter.x, hubCenter.y);
        }

        // Draw package circles and routes
        for (int i = 0; i < Packs.size(); i++) {
            Package p = Packs.get(i);
            Point senderPoint = p.getSenderPoint();
            Point destinationPoint = p.getDestinationPoint();
            int radius = 14;

            // Determine colors based on package status and location
            Color senderColor = Color.RED;
            Color destinationColor = Color.PINK;
            
            // Make colors darker if package is at that location
            if (p.getStatus() == components.Status.CREATION || p.getStatus() == components.Status.COLLECTION) {
                senderColor = Color.RED.darker();
            }
            if (p.getStatus() == components.Status.DELIVERED) {
                destinationColor = Color.RED.darker();
            }

            // Draw sender circle
            g2.setColor(senderColor);
            g2.fillOval(senderPoint.x - radius / 2, senderPoint.y - radius / 2, 30, 30);

            // Draw destination circle
            g2.setColor(destinationColor);
            g2.fillOval(destinationPoint.x - radius / 2, destinationPoint.y - radius / 2, 30, 30);

            // Draw routes based on package type
            if (p instanceof components.NonStandardPackage) {
                Point hubCenter = mainOffice.getHub().getLocation();
                g2.setColor(Color.RED);
                // Red: sender → hub
                g2.drawLine(senderPoint.x, senderPoint.y + 15, hubCenter.x, hubCenter.y);
                // Red: sender → destination
                g2.drawLine(senderPoint.x, senderPoint.y + 15, destinationPoint.x, destinationPoint.y);
            } else {
                // Standard and Small packages
                Branch senderBranch = mainOffice.getBranches().get(p.getSenderAddress().getZip());
                Branch receiverBranch = mainOffice.getBranches().get(p.getDestinationAddress().getZip());
                Point senderBranchPoint = senderBranch.getLocation();
                Point receiverBranchPoint = receiverBranch.getLocation();

                g2.setColor(Color.BLUE);
                g2.drawLine(senderPoint.x, senderPoint.y + 15, senderBranchPoint.x, senderBranchPoint.y);
                g2.drawLine(receiverBranchPoint.x, receiverBranchPoint.y, destinationPoint.x, destinationPoint.y);
            }
        }

        // Set initial truck locations but don't show them (they should only be visible when moving)
        for (Branch b : mainOffice.getBranches()) {
            for (Truck t : b.getListTrucks()) {
                t.setLocation(b.getLocation()); // Set location but truck visual remains hidden
            }
        }
        if (mainOffice.getHub() != null) {
            for (Truck t : mainOffice.getHub().getListTrucks()) {
                t.setLocation(mainOffice.getHub().getLocation()); // Set location but truck visual remains hidden
            }
        }

        // Draw all truck visuals (only visible ones will actually draw)
        Vector<TruckVisual> truckVisuals = getAllTruckVisuals();
        for (TruckVisual visual : truckVisuals) {
            if (visual != null) {
                visual.draw(g2); // TruckVisual.draw() already checks visibility internally
            }
        }
    }
}