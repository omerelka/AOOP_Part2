package components;

import java.util.Vector;
import java.util.Random;
import java.awt.*;
import GUI.TruckVisual;

public abstract class Truck implements Node, Runnable , TruckVisual{
    private static int countID = 2000;
    final private int truckID;
    private String licensePlate;
    private String truckModel;
    private boolean available = true;
    private boolean isEmpty;
    private int timeLeft = 0;
    private Vector<Package> packages = new Vector<Package>();
    private Point location;

    private Point startPoint;
    private Point endPoint;
    private int totalTime;
    
    // default random constructor
    public Truck() {
        truckID = countID++;
        Random r = new Random();
        licensePlate = (r.nextInt(900) + 100) + "-" + (r.nextInt(90) + 10) + "-" + (r.nextInt(900) + 100);
        truckModel = "M" + r.nextInt(5);
        System.out.print("Creating ");
    }

    public Truck(String licensePlate, String truckModel) {
        truckID = countID++;
        this.licensePlate = licensePlate;
        this.truckModel = truckModel;
        System.out.print("Creating ");
    }

    public Vector<Package> getPackages() {
        return packages;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    @Override
    public String toString() {
        return "truckID=" + truckID + ", licensePlate=" + licensePlate + ", truckModel=" + truckModel + ", available= " + available;
    }

    @Override
    public synchronized void collectPackage(Package p) {
        setAvailable(false);
        int time = p.getSenderAddress().getStreet() % 10 + 1;
        this.setTimeLeft(time);
        this.packages.add(p);
        p.setStatus(Status.COLLECTION);
        p.addTracking(new Tracking(MainOffice.getClock(), this, p.getStatus()));
        System.out.println(getName() + " is collecting package " + p.getPackageID() + ", time to arrive: " + getTimeLeft());
    }

    public boolean isAvailable() {
        return available;
    }

    public int getTruckID() {
        return truckID;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void addPackage(Package p) {
        this.packages.add(p);
    }

    public String getName() {
        return this.getClass().getSimpleName() + " " + getTruckID();
    }

    @Override
    public void run() {
        work();
    }

    public void setLocation(Point p) {
        this.location = p;
    }

    public Point getLocation() {
        return location;
    }

    // Abstract method that subclasses must implement
    public abstract void work();

    @Override
    public abstract void draw(Graphics2D g2, Point position);

    public boolean isEmpty(){
        return getPackages().isEmpty() == true;
    }



    public Point getStartPoint() { return startPoint; }
public void setStartPoint(Point startPoint) { this.startPoint = startPoint; }

public Point getEndPoint() { return endPoint; }
public void setEndPoint(Point endPoint) { this.endPoint = endPoint; }

public int getTotalTime() { return totalTime; }
public void setTotalTime(int totalTime) { this.totalTime = totalTime; }

// Method to calculate current position based on progress
public Point getCurrentPosition() {
    if (startPoint == null || endPoint == null || totalTime == 0 || isAvailable()) {
        Point currentLoc = getLocation();
        System.out.println("DEBUG: " + getName() + " using static location: " + currentLoc);
        return currentLoc;
    }
    
    // Calculate progress
    double progress = 1.0 - ((double) timeLeft / totalTime);
    if (progress > 1.0) progress = 1.0;
    if (progress < 0.0) progress = 0.0;
    
    // Interpolate between start and end points
    int currentX = (int) (startPoint.x + (endPoint.x - startPoint.x) * progress);
    int currentY = (int) (startPoint.y + (endPoint.y - startPoint.y) * progress);
    
    Point currentPos = new Point(currentX, currentY);
    System.out.println("DEBUG: " + getName() + " moving from " + startPoint + " to " + endPoint + ", progress: " + progress + ", current: " + currentPos);
    
    return currentPos;
}

}