package components;
import java.awt.*;


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
public synchronized void collectPackage(Package p) {
    setAvailable(false);
    int time = p.getSenderAddress().getStreet() % 10 + 1;
    this.setTimeLeft(time);
    this.setTotalTime(time); // Store total time
    
    // Set movement: Branch → Customer (sender)
    this.setStartPoint(p.getSenderBranch().getLocation());
    this.setEndPoint(p.getSenderPoint());
    
    this.getPackages().add(p);
    p.addTracking(new Tracking(MainOffice.getClock(), this, Status.COLLECTION));
    System.out.println(getName() + " is going to collect package " + p.getPackageID() + ", time to arrive: " + getTimeLeft());
}

@Override
public void deliverPackage(Package p) {
    addPackage(p);
    setAvailable(false);
    int time = p.getDestinationAddress().getStreet() % 10 + 1;
    this.setTimeLeft(time);
    this.setTotalTime(time); // Store total time
    
    // Set movement: Branch → Customer (destination)
    this.setStartPoint(p.getDestBranch().getLocation());
    this.setEndPoint(p.getDestinationPoint());
    
    p.addRecords(Status.DISTRIBUTION, this);
    System.out.println(getName() + " is delivering " + p.getName() + ", time left: " + getTimeLeft());
}
    
    @Override
public void work() {
    if (!isAvailable()) {
        setTimeLeft(getTimeLeft() - 1);
        if (this.getTimeLeft() == 0) {
            for (Package p : this.getPackages()) {
                if (p.getStatus() == Status.CREATION) {
                    // ואן אסף את החבילה, עכשיו חזור לסניף
                    p.addRecords(Status.BRANCH_STORAGE, p.getSenderBranch());
                    
                    // תתחיל נסיעת חזרה לסניף
                    int returnTime = p.getSenderAddress().getStreet() % 10 + 1;
                    this.setTimeLeft(returnTime);
                    this.setTotalTime(returnTime);
                    this.setStartPoint(p.getSenderPoint());
                    this.setEndPoint(p.getSenderBranch().getLocation());
                    
                    System.out.println(getName() + " has collected " + p.getName() + " and is returning to " + p.getSenderBranch().getName());
                    return; // צא מהלולאה - עדיין עובד
                    
                } else if (p.getStatus() == Status.DELIVERY) {
                    // ואן מסר את החבילה ללקוח
                    p.addRecords(Status.DELIVERED, null);
                    p.getDestBranch().removePackage(p);
                    System.out.println(getName() + " has delivered " + p.getName() + " to the destination");
                    if (p instanceof SmallPackage && ((SmallPackage) p).isAcknowledge()) {
                        System.out.println("Acknowledge sent for " + p.getName());
                    }
                } else if (p.getStatus() == Status.BRANCH_STORAGE) {
                    // ✅ ואן הגיע חזרה לסניף - הוסף את החבילה לסניף
                    p.getSenderBranch().addPackage(p);
                    System.out.println(getName() + " returned to " + p.getSenderBranch().getName() + " and left " + p.getName() + " for standard truck pickup");
                }
            }
            
            // רק אחרי שסיים הכל - נעשה זמין
            this.getPackages().removeAll(getPackages());
            this.setAvailable(true);
            this.setStartPoint(null);
            this.setEndPoint(null);
        }
    }
}

    @Override
    public void run() {
        work();
    }

    @Override
    public void draw(Graphics2D g2, Point position) {
    // Draw truck body (16x16 square, dark blue)
    g2.setColor(Color.BLUE.darker());
    g2.fillRect(position.x - 8, position.y - 8, 16, 16);
    
    // Draw 4 black wheels (10x10 circles at corners)
    g2.setColor(Color.BLACK);
    g2.fillOval(position.x - 13, position.y - 13, 10, 10); // Top-left
    g2.fillOval(position.x + 3, position.y - 13, 10, 10);  // Top-right
    g2.fillOval(position.x - 13, position.y + 3, 10, 10);  // Bottom-left
    g2.fillOval(position.x + 3, position.y + 3, 10, 10);   // Bottom-right
}
}