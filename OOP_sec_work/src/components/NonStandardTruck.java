package components;
import java.awt.*;
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
public synchronized void collectPackage(Package p) {
    setAvailable(false);
    int time = p.getSenderAddress().getStreet() % 10 + 1;
    this.setTimeLeft(time);
    this.setTotalTime(time); // Store total time
    
    // Set movement: Hub → Customer (sender)
    this.setStartPoint(MainOffice.getHub().getLocation());
    this.setEndPoint(p.getSenderPoint());
    
    this.getPackages().add(p);
    p.setStatus(Status.COLLECTION);
    p.addTracking(new Tracking(MainOffice.getClock(), this, p.getStatus()));
    System.out.println(getName() + " is going to collect package " + p.getPackageID() + " from sender, time to arrive: " + getTimeLeft());
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
                    
                    int time = Math.abs(p.getDestinationAddress().getStreet() - p.getSenderAddress().getStreet()) % 10 + 1;
                    this.setTimeLeft(time);
                    this.setTotalTime(time); // Store total time
                    
                    // Set movement: Customer (sender) → Customer (destination)
                    this.setStartPoint(p.getSenderPoint());
                    this.setEndPoint(p.getDestinationPoint());
                    
                    p.addRecords(Status.DISTRIBUTION, this);
                    System.out.println(getName() + " is now delivering " + p.getName() + " from sender to receiver, time left: " + this.getTimeLeft());
                    
                } else if (p.getStatus() == Status.DISTRIBUTION) {
                    // Truck has arrived at receiver, delivery complete
                    System.out.println(getName() + " has delivered " + p.getName() + " to the destination");
                    this.getPackages().remove(p);
                    p.addRecords(Status.DELIVERED, null);
                    // Remove package from hub since NonStandard packages start from hub
                    MainOffice.getHub().removePackage(p);
                    setAvailable(true);
                    
                    // Clear movement points
                    this.setStartPoint(null);
                    this.setEndPoint(null);
                }
            }
        }
    }
}

    @Override
    public void deliverPackage(Package p) {
        // This method is called by Branch.deliverPackage() for packages in DELIVERY status
        // For NonStandardTruck, this is handled in work() method instead
        addPackage(p);
        setAvailable(false);
        int time = Math.abs(p.getDestinationAddress().getStreet() - p.getSenderAddress().getStreet()) % 10 + 1;
        this.setTimeLeft(time);
        p.addRecords(Status.DISTRIBUTION, this);
        System.out.println(getName() + " is delivering " + p.getName() + ", time left: " + this.getTimeLeft());
    }



    @Override
    public String toString() {
        return "NonStandardTruck [" + super.toString() + "]";
    }

    @Override
    public void run() {
        work();
    }

    @Override
    public void draw(Graphics2D g2, Point position) {
    // Choose color based on whether truck has packages
    if (getPackages().isEmpty()) {
        g2.setColor(Color.PINK); // Light red when empty
    } else {
        g2.setColor(Color.RED.darker());   // Dark red when loaded
    }
    
    // Draw truck body (16x16 square)
    g2.fillRect(position.x - 8, position.y - 8, 16, 16);
    
    // Draw 4 black wheels (10x10 circles at corners)
    g2.setColor(Color.BLACK);
    g2.fillOval(position.x - 13, position.y - 13, 10, 10); // Top-left
    g2.fillOval(position.x + 3, position.y - 13, 10, 10);  // Top-right
    g2.fillOval(position.x - 13, position.y + 3, 10, 10);  // Bottom-left
    g2.fillOval(position.x + 3, position.y + 3, 10, 10);   // Bottom-right
}
}