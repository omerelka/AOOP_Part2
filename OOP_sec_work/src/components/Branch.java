package components;

import java.awt.*;
import java.util.Vector;

public class Branch implements Node, Runnable {
    private Point location;
    private static int counter = -1;
    private int branchId;
    private String branchName;
    protected Vector<Package> packages = new Vector<Package>();
    protected Vector<Truck> listTrucks = new Vector<Truck>();

    public Branch() {
        this("Branch " + counter);
    }

    public Branch(String branchName) {
        this.branchId = counter++;
        this.branchName = branchName;
        System.out.println("\nCreating " + this);
    }

    public Branch(String branchName, Package[] plist, Truck[] tlist) {
        this.branchId = counter++;
        this.branchName = branchName;
        addPackages(plist);
        addTrucks(tlist);
    }

    public void printBranch() {
        System.out.println("\nBranch name: " + branchName);
        System.out.println("Packages list:");
        for (Package pack : packages)
            System.out.println(pack);
        System.out.println("Trucks list:");
        for (Truck trk : listTrucks)
            System.out.println(trk);
    }

    public synchronized void addPackage(Package pack) {
        packages.add(pack);
        // Notify waiting trucks that there's a new package
        notifyAll();
    }

    public synchronized void addTruck(Truck trk) {
        listTrucks.add(trk);
    }

    public synchronized void addPackages(Package[] plist) {
        for (Package pack : plist)
            packages.add(pack);
        // Notify waiting trucks
        notifyAll();
    }

    public synchronized void addTrucks(Truck[] tlist) {
        for (Truck trk : tlist)
            listTrucks.add(trk);
    }

    public int getBranchId() {
        return branchId;
    }

    public String getName() {
        return branchName;
    }

    @Override
    public String toString() {
        return "Branch " + branchId + ", branch name:" + branchName + ", packages: " + packages.size()
                + ", trucks: " + listTrucks.size();
    }

    @Override
    public void collectPackage(Package p) {
        synchronized (listTrucks) {
            for (Truck v : listTrucks) {
                if (v.isAvailable()) {
                    v.collectPackage(p);
                    return;
                }
            }
        }
    }

    @Override
    public void deliverPackage(Package p) {
        synchronized (listTrucks) {
            for (Truck v : listTrucks) {
                if (v.isAvailable()) {
                    v.deliverPackage(p);
                    return;
                }
            }
        }
    }

    @Override
    public void work() {
        // In threaded version, trucks handle themselves
        // Branch only does local work
        localWork();
    }

    public void localWork() {
        synchronized (packages) {
            for (Package p : packages) {
                if (p.getStatus() == Status.CREATION) {
                    collectPackage(p);
                }
                if (p.getStatus() == Status.DELIVERY) {
                    deliverPackage(p);
                }
            }
        }
    }

    public Vector<Package> getPackages() {
        return packages;
    }

    public synchronized void removePackage(Package p) {
        packages.remove(p);
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

    public Vector<Truck> getListTrucks() {
        return listTrucks;
    }
}