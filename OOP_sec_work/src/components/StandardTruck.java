package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StandardTruck extends Truck {
    private int maxWeight;
    private Branch destination;

    public StandardTruck() {
        super();
        maxWeight = ((new Random()).nextInt(2) + 2) * 100;
        System.out.println(this);
    }

    public StandardTruck(String licensePlate, String truckModel, int maxWeight) {
        super(licensePlate, truckModel);
        this.maxWeight = maxWeight;
        System.out.println(this);
    }

    public Branch getDestination() {
        return destination;
    }

    public void setDestination(Branch destination) {
        this.destination = destination;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    @Override
    public String toString() {
        return "StandardTruck [" + super.toString() + ",maxWeight=" + maxWeight + "]";
    }

    public void unload() {
        for (Package p : getPackages()) {
            deliverPackage(p);
        }
        getPackages().removeAll(getPackages());
        System.out.println(getName() + " unloaded packages at " + destination.getName());
    }

    @Override
public void deliverPackage(Package p) {
    if (destination == MainOffice.getHub())
        p.addRecords(Status.HUB_STORAGE, destination);
    else
        p.addRecords(Status.DELIVERY, destination); // ✅ שנה ל-DELIVERY במקום DISTRIBUTION
    destination.addPackage(p);
}

    public void load(Branch sender, Branch dest, Status status) {
        double totalWeight = 0;
        List<Package> packagesToLoad = new ArrayList<>();

        // First pass: identify packages to load (read-only)
        synchronized (sender.getPackages()) {
            for (Package p : sender.getPackages()) {
                if (p.getStatus() == Status.BRANCH_STORAGE ||
                        (p.getStatus() == Status.HUB_STORAGE && p.getDestBranch() == dest)) {

                    double packageWeight = (p instanceof SmallPackage) ? 1 : ((StandardPackage) p).getWeight();

                    if (totalWeight + packageWeight <= maxWeight) {
                        packagesToLoad.add(p);
                        totalWeight += packageWeight;
                    }
                }
            }
        }

        // Second pass: actually move the packages
        for (Package p : packagesToLoad) {
            getPackages().add(p);
            sender.removePackage(p);
            p.addRecords(status, this);
        }

        System.out.println(this.getName() + " loaded " + packagesToLoad.size() + " packages at " + sender.getName());
    }

    @Override
    public void work() {
        synchronized (this) {
            if (!isAvailable()) {
                setTimeLeft(getTimeLeft() - 1);
                if (getTimeLeft() == 0) {
                    System.out.println("StandardTruck " + getTruckID() + " arrived to " + destination.getName());
                    unload();
                    if (destination == MainOffice.getHub()) {
                        setAvailable(true);
                        destination = null;
                    } else {
                        load(destination, MainOffice.getHub(), Status.HUB_TRANSPORT);
                        int travelTime = (new Random()).nextInt(6) + 1;
                        setTimeLeft(travelTime);
                        destination = MainOffice.getHub();
                        System.out.println(this.getName() + " is on it's way to the HUB, time to arrive: " + getTimeLeft());
                    }
                }
            }
        }
    }

    // Method to start journey to a branch (called by Hub)
    public void startJourneyToBranch(Branch targetBranch, int travelTime) {
    setDestination(targetBranch);
    setAvailable(false);
    setTimeLeft(travelTime);
    setTotalTime(travelTime); // Store total time
    
    // Set movement: Hub → Branch
    Point startPoint = MainOffice.getHub().getLocation();
    Point endPoint = targetBranch.getLocation();
    
    this.setStartPoint(startPoint);
    this.setEndPoint(endPoint);
    
    System.out.println("DEBUG: " + getName() + " starting journey from " + startPoint + " to " + endPoint + ", timeLeft: " + travelTime);
}

    @Override
    public void run() {
        work();
    }

   @Override
public void draw(Graphics2D g2, Point position) {
    // Choose color based on whether truck has packages
    if (getPackages().isEmpty()) {
        g2.setColor(Color.GREEN.brighter()); // Light green when empty
    } else {
        g2.setColor(Color.GREEN.darker());   // Dark green when loaded
    }
    
    // Draw truck body (16x16 square)
    g2.fillRect(position.x - 8, position.y - 8, 16, 16);
    
    // Draw 4 black wheels (10x10 circles at corners)
    g2.setColor(Color.BLACK);
    g2.fillOval(position.x - 13, position.y - 13, 10, 10); // Top-left
    g2.fillOval(position.x + 3, position.y - 13, 10, 10);  // Top-right
    g2.fillOval(position.x - 13, position.y + 3, 10, 10);  // Bottom-left
    g2.fillOval(position.x + 3, position.y + 3, 10, 10);   // Bottom-right
    
    // Draw package count above the truck if it has packages
    if (!getPackages().isEmpty()) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String packageCount = String.valueOf(getPackages().size());
        
        // Get text metrics to center the text above the truck
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(packageCount);
        
        // Draw the number centered above the truck (20 pixels above)
        g2.drawString(packageCount, position.x - textWidth/2, position.y - 20);
    }
}
}