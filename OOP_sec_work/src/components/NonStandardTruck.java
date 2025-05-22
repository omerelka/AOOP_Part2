package components;

import java.awt.Point;
import java.util.Random;

public class NonStandardTruck extends Truck {

    public NonStandardTruck() {
        super();
        Random r = new Random();
        System.out.println(this);
    }

    public NonStandardTruck(String licensePlate, String truckModel, int length, int width, int height) {
        super(licensePlate, truckModel);
        System.out.println(this);
    }

    @Override
    public void work() {
        synchronized (this) {
            if (!this.isAvailable()) {
                Package p = this.getPackages().get(0);
                this.setTimeLeft(this.getTimeLeft() - 1);
                if (this.getTimeLeft() == 0) {
                    if (p.getStatus() == Status.COLLECTION) {
                        // Truck has arrived at sender, now collect package and go to receiver
                        System.out.println(getName() + " has arrived at sender and collected " + p.getName());
                        
                        // Start delivery immediately - no gap
                        Point destinationPoint = p.getDestinationPoint();
                        if (destinationPoint != null) {
                            int time = Math.abs(p.getDestinationAddress().getStreet() - p.getSenderAddress().getStreet()) % 10 + 1;
                            
                            // Start journey from sender to receiver immediately
                            startJourney(p.getSenderPoint(), destinationPoint, time);
                            // Set color for loaded truck going to destination (dark red)
                            getTruckVisual().autoSetColor("NONSTANDARD", true, false);
                            System.out.println("DEBUG: NonStandardTruck " + getTruckID() + " starting delivery journey from sender " + p.getSenderPoint() + " to receiver " + destinationPoint);
                            
                            this.setTimeLeft(time);
                            p.addRecords(Status.DISTRIBUTION, this);
                            System.out.println(getName() + " is now delivering " + p.getName() + " from sender to receiver, time left: " + this.getTimeLeft());
                        } else {
                            System.out.println("ERROR: " + getName() + " - destinationPoint is null for package " + p.getPackageID());
                        }
                        
                    } else if (p.getStatus() == Status.DISTRIBUTION) {
                        // Truck has arrived at receiver, delivery complete
                        System.out.println(getName() + " has delivered " + p.getName() + " to the destination");
                        this.getPackages().remove(p);
                        p.addRecords(Status.DELIVERED, null);
                        setAvailable(true); // This will hide the truck visual
                        System.out.println("DEBUG: NonStandardTruck " + getTruckID() + " delivery complete - now hidden");
                    }
                }
            }
        }
    }

    @Override
    public void deliverPackage(Package p) {
        // This method is called by Branch.deliverPackage() for packages in DELIVERY status
        // For NonStandardTruck, this is handled in work() method instead
        // Just add the package to delivery queue
        addPackage(p);
        setAvailable(false);
        int time = Math.abs(p.getDestinationAddress().getStreet() - p.getSenderAddress().getStreet()) % 10 + 1;
        this.setTimeLeft(time);
        p.addRecords(Status.DISTRIBUTION, this);
        System.out.println(getName() + " is delivering " + p.getName() + ", time left: " + this.getTimeLeft());
    }

    @Override
    public synchronized void collectPackage(Package p) {
        setAvailable(false);
        Point senderPoint = p.getSenderPoint();
        if (senderPoint != null) {
            int time = p.getSenderAddress().getStreet() % 10 + 1;
            
            // Start journey from hub to sender (bright red - empty truck)
            startJourney(getLocation(), senderPoint, time);
            getTruckVisual().autoSetColor("NONSTANDARD", false, true); // isToSender = true
            System.out.println("DEBUG: NonStandardTruck " + getTruckID() + " starting journey from hub to sender " + senderPoint);
            
            this.setTimeLeft(time);
            this.getPackages().add(p);
            p.setStatus(Status.COLLECTION);
            p.addTracking(new Tracking(MainOffice.getClock(), this, p.getStatus()));
            System.out.println(getName() + " is going to collect package " + p.getPackageID() + " from sender, time to arrive: " + getTimeLeft());
        } else {
            System.out.println("ERROR: " + getName() + " - senderPoint is null for package " + p.getPackageID());
        }
    }

    @Override
    public String toString() {
        return "NonStandardTruck [" + super.toString() + "]";
    }

    @Override
    public void run() {
        work();
    }
}