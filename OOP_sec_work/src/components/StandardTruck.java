package components;

import java.awt.Point;
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
            p.addRecords(Status.DELIVERY, destination);
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
                        setAvailable(true); // This will hide the truck visual
                        destination = null;
                    } else {
                        load(destination, MainOffice.getHub(), Status.HUB_TRANSPORT);
                        int travelTime = (new Random()).nextInt(6) + 1;
                        
                        // Start journey back to hub
                        Point hubLocation = MainOffice.getHub().getLocation();
                        if (hubLocation != null) {
                            startJourney(destination.getLocation(), hubLocation, travelTime);
                            // Set color for loaded truck
                            getTruckVisual().autoSetColor("STANDARD", true, false);
                            System.out.println("DEBUG: StandardTruck " + getTruckID() + " starting journey back to hub with " + getPackages().size() + " packages");
                        }
                        
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
        
        Point branchLocation = targetBranch.getLocation();
        if (branchLocation != null) {
            startJourney(getLocation(), branchLocation, travelTime);
            // Set color for empty truck going to branch
            getTruckVisual().autoSetColor("STANDARD", false, false);
            System.out.println("DEBUG: StandardTruck " + getTruckID() + " starting journey to " + targetBranch.getName());
        }
        
        setTimeLeft(travelTime);
    }

    @Override
    public void run() {
        work();
    }
}