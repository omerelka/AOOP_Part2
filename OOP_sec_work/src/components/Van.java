package components;

import java.awt.Point;

public class Van extends Truck {
    
    public Van() {
        super();
        System.out.println(this);
    }
    
    public Van(String licensePlate, String truckModel) {
        super(licensePlate, truckModel);
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Van [" + super.toString() + "]";
    }
    
    @Override
    public void deliverPackage(Package p) {
        addPackage(p);
        setAvailable(false);
        int time = p.getDestinationAddress().getStreet() % 10 + 1;
        
        // Start journey from current location to destination
        Point destinationPoint = p.getDestinationPoint();
        if (destinationPoint != null) {
            startJourney(getLocation(), destinationPoint, time);
        }
        
        this.setTimeLeft(time);
        p.addRecords(Status.DISTRIBUTION, this);
        System.out.println(getName() + " is delivering " + p.getName() + ", time left: " + getTimeLeft());
    }
    
    @Override
    public synchronized void collectPackage(Package p) {
        setAvailable(false);
        int time = p.getSenderAddress().getStreet() % 10 + 1;
        
        // Start journey to sender address
        Point senderPoint = p.getSenderPoint();
        if (senderPoint != null) {
            startJourney(getLocation(), senderPoint, time);
            // Override color setting for Van
            getTruckVisual().autoSetColor("VAN", false, true); // Van going to sender
            System.out.println("DEBUG: Van " + getTruckID() + " collecting, color should be dark blue");
        }
        
        this.getPackages().add(p);
        p.setStatus(Status.COLLECTION);
        p.addTracking(new Tracking(MainOffice.getClock(), this, p.getStatus()));
        System.out.println(getName() + " is collecting package " + p.getPackageID() + ", time to arrive: " + getTimeLeft());
    }
    
    @Override
    public void work() {
        if (!isAvailable()) {
            setTimeLeft(getTimeLeft() - 1);
            if (this.getTimeLeft() == 0) {
                for (Package p : this.getPackages()) {
                    if (p.getStatus() == Status.COLLECTION) {
                        p.addRecords(Status.BRANCH_STORAGE, p.getSenderBranch());
                        System.out.println(getName() + " has collected " + p.getName() + " and arrived back to " + p.getSenderBranch().getName());
                        
                        // Journey back to branch completed
                        Point branchLocation = p.getDestBranch().getLocation();
                        if (branchLocation != null) {
                            // Don't start a journey, just mark as arrived and hide
                            System.out.println("DEBUG: Van " + getTruckID() + " returned to branch - now hidden");
                        }
                    } else {
                        p.addRecords(Status.DELIVERED, null);
                        p.getDestBranch().removePackage(p);
                        System.out.println(getName() + " has delivered " + p.getName() + " to the destination");
                        if (p instanceof SmallPackage && ((SmallPackage) p).isAcknowledge()) {
                            System.out.println("Acknowledge sent for " + p.getName());
                        }
                    }
                }
                this.getPackages().removeAll(getPackages());
                this.setAvailable(true); // This will hide the truck visual
            }
        }
    }

    @Override
    public void run() {
        work();
    }
}