package components;

import java.util.Vector;
import java.util.Random;
import java.awt.Point;
import GUI.TruckVisual;

public abstract class Truck implements Node, Runnable {
    private static int countID = 2000;
    final private int truckID;
    private String licensePlate;
    private String truckModel;
    private boolean available = true;
    private int timeLeft = 0;
    private int initialTimeLeft = 0; // Store initial time for progress calculation
    private Vector<Package> packages = new Vector<Package>();
    private Point location;
    private TruckVisual truckVisual;
    
    // default random constructor
    public Truck() {
        truckID = countID++;
        Random r = new Random();
        licensePlate = (r.nextInt(900) + 100) + "-" + (r.nextInt(90) + 10) + "-" + (r.nextInt(900) + 100);
        truckModel = "M" + r.nextInt(5);
        truckVisual = new TruckVisual(null, null);
        System.out.print("Creating ");
    }

    public Truck(String licensePlate, String truckModel) {
        truckID = countID++;
        this.licensePlate = licensePlate;
        this.truckModel = truckModel;
        truckVisual = new TruckVisual(null, null);
        System.out.print("Creating ");
    }

    public Vector<Package> getPackages() {
        return packages;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        // If this is a new journey (timeLeft increased), store initial time
        if (timeLeft > this.timeLeft) {
            this.initialTimeLeft = timeLeft;
        }
        this.timeLeft = timeLeft;
        updateTruckVisualProgress();
    }

    private void updateTruckVisualProgress() {
        if (initialTimeLeft > 0 && truckVisual != null) {
            double progress = (double)(initialTimeLeft - timeLeft) / initialTimeLeft;
            truckVisual.setProgress(progress);
        }
    }

    // Method to start a new journey
    protected void startJourney(Point origin, Point destination, int timeToDestination) {
        truckVisual.setOrigin(origin);
        truckVisual.setDestination(destination);
        truckVisual.setProgress(0.0);
        this.initialTimeLeft = timeToDestination;
        this.timeLeft = timeToDestination;
        
        // Auto-set color based on truck type and state
        String truckType = this.getClass().getSimpleName().toUpperCase();
        boolean isLoaded = !packages.isEmpty();
        boolean isToSender = false; // This can be overridden in subclasses
        
        truckVisual.autoSetColor(truckType, isLoaded, isToSender);
        truckVisual.setVisible(true); // Only show truck when it starts moving
        System.out.println("DEBUG: " + getName() + " starting journey from " + origin + " to " + destination + " - now visible");
    }

    @Override
    public String toString() {
        return "truckID=" + truckID + ", licensePlate=" + licensePlate + ", truckModel=" + truckModel + ", available= " + available;
    }

    @Override
    public synchronized void collectPackage(Package p) {
        setAvailable(false);
        int time = p.getSenderAddress().getStreet() % 10 + 1;
        
        // Start journey to sender address
        Point senderPoint = p.getSenderPoint();
        if (senderPoint != null) {
            startJourney(this.location, senderPoint, time);
            System.out.println("DEBUG: " + getName() + " starting journey to " + senderPoint);
        } else {
            System.out.println("ERROR: " + getName() + " - senderPoint is null for package " + p.getPackageID());
        }
        
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
        if (available) {
            // When truck becomes available, hide its visual
            truckVisual.setVisible(false);
            System.out.println("DEBUG: " + getName() + " is now available - hiding visual");
        }
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
        // Don't automatically show trucks when setting location
        // Trucks should only be visible when they start a journey
    }

    public Point getLocation() {
        return location;
    }

    public TruckVisual getTruckVisual() {
        return truckVisual;
    }

    // Abstract method that subclasses must implement
    public abstract void work();
}