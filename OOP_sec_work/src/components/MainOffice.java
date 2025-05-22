package components;

import GUI.SystemPanel;
import java.awt.Point;
import java.util.Random;
import java.util.Vector;

public class MainOffice implements Runnable {
    private int maxPackages;
    private static int clock = 0;
    private static Hub hub;
    private Vector<Package> packages = new Vector<>();
    private SystemPanel systemPanel = new SystemPanel();
    private Vector<Branch> branches = new Vector<>();
    private volatile boolean running = false;
    private Thread simulationThread;
    private int packagesCreated = 0;
    private final Object pauseLock = new Object();
    private volatile boolean paused = false;
    
    // Thread management
    private Vector<Thread> branchThreads = new Vector<>();
    private Vector<Thread> truckThreads = new Vector<>();

    public MainOffice(int branches, int trucksForBranch, int maxPackages) {
        this.maxPackages = maxPackages;
        systemPanel.setPackages(this.getPackages());
        systemPanel.setNumBranches(this.getBranches().size());

        addHub(trucksForBranch);
        addBranches(branches, trucksForBranch);
        System.out.println("\n\n========================== START ==========================");
    }

    public static Hub getHub() {
        return hub;
    }

    public Vector<Branch> getBranches() {
        return branches;
    }

    public static int getClock() {
        return clock;
    }

    public void play(int playTime) {
        for (int i = 0; i < playTime; i++) {
            tick();
        }
        System.out.println("\n========================== STOP ==========================\n\n");
        printReport();
    }

    public void printReport() {
        synchronized (packages) {
            for (Package p : packages) {
                System.out.println("\nTRACKING " + p);
                for (Tracking t : p.getTracking())
                    System.out.println(t);
            }
        }
    }

    public String clockString() {
        String s = "";
        int minutes = clock / 60;
        int seconds = clock % 60;
        s += (minutes < 10) ? "0" + minutes : minutes;
        s += ":";
        s += (seconds < 10) ? "0" + seconds : seconds;
        return s;
    }

    public void tick() {
        System.out.println(clockString());
        if (clock % 5 == 0 && packagesCreated < maxPackages) {
            addPackage();
            packagesCreated++;
            System.out.println("DEBUG: Created package " + packagesCreated + " of " + maxPackages);
        }
        // Note: Individual work() calls are now handled by separate threads
        // Hub and branches will work independently
        clock++;
    }

    public void addHub(int trucksForBranch) {
        hub = new Hub();
        for (int i = 0; i < trucksForBranch; i++) {
            hub.addTruck(new StandardTruck());
        }
        hub.addTruck(new NonStandardTruck());
    }

    public void addBranches(int branches, int trucks) {
        for (int i = 0; i < branches; i++) {
            Branch branch = new Branch();
            for (int j = 0; j < trucks; j++) {
                branch.addTruck(new Van());
            }
            hub.add_branch(branch);
            this.branches.add(branch);
        }
    }

    public void addPackage() {
        Random r = new Random();
        Package p;
        Priority priority = Priority.values()[r.nextInt(3)];
        Address sender = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999) + 100000);
        Address dest = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999) + 100000);

        switch (r.nextInt(3)) {
            case 0:
                p = new SmallPackage(priority, sender, dest, r.nextBoolean());
                synchronized (packages) {
                    this.packages.add(p);
                    // Set screen coordinates immediately after adding to packages list
                    setPackageScreenCoordinates(p);
                    p.getSenderBranch().addPackage(p);
                }
                break;
            case 1:
                p = new StandardPackage(priority, sender, dest, r.nextFloat() + (r.nextInt(9) + 1));
                synchronized (packages) {
                    this.packages.add(p);
                    // Set screen coordinates immediately after adding to packages list
                    setPackageScreenCoordinates(p);
                    p.getSenderBranch().addPackage(p);
                }
                break;
            case 2:
                p = new NonStandardPackage(priority, sender, dest, r.nextInt(1000), r.nextInt(500), r.nextInt(400));
                synchronized (packages) {
                    this.packages.add(p);
                    // Set screen coordinates immediately after adding to packages list
                    setPackageScreenCoordinates(p);
                    hub.addPackage(p);
                }
                break;
            default:
                p = null;
                return;
        }
    }
    
    // Helper method to set package screen coordinates
    private void setPackageScreenCoordinates(Package p) {
        int packageIndex = packages.size() - 1; // Current package index
        int baseX = 150 + packageIndex * 70;
        int topY = 65;
        int bottomY = 635; // Assuming window height around 700
        
        p.setSenderPoint(new Point(baseX, topY));
        p.setDestinationPoint(new Point(baseX, bottomY));
        
        System.out.println("DEBUG: Set coordinates for package " + p.getPackageID() + 
                         " - sender: " + p.getSenderPoint() + ", dest: " + p.getDestinationPoint());
    }

    public Vector<Package> getPackages() {
        return packages;
    }

    public void startSimulation() {
        if (simulationThread == null || !simulationThread.isAlive()) {
            running = true;
            
            // Start all branch threads
            startBranchThreads();
            
            // Start all truck threads
            startTruckThreads();
            
            // Start main simulation thread
            simulationThread = new Thread(() -> runSimulationLoop());
            simulationThread.start();
            
            System.out.println("Simulation started with " + branchThreads.size() + " branch threads and " + truckThreads.size() + " truck threads.");
        }
    }

    private void startBranchThreads() {
        // Start hub thread
        Thread hubThread = new Thread(new BranchWorker(hub));
        hubThread.setName("Hub-Thread");
        hubThread.start();
        branchThreads.add(hubThread);
        
        // Start all branch threads
        for (int i = 0; i < branches.size(); i++) {
            Branch branch = branches.get(i);
            Thread branchThread = new Thread(new BranchWorker(branch));
            branchThread.setName("Branch-" + i + "-Thread");
            branchThread.start();
            branchThreads.add(branchThread);
        }
    }

    private void startTruckThreads() {
        // Start hub truck threads
        for (int i = 0; i < hub.getListTrucks().size(); i++) {
            Truck truck = hub.getListTrucks().get(i);
            Thread truckThread = new Thread(new TruckWorker(truck));
            truckThread.setName("Hub-" + truck.getName() + "-Thread");
            truckThread.start();
            truckThreads.add(truckThread);
        }
        
        // Start branch truck threads
        for (int i = 0; i < branches.size(); i++) {
            Branch branch = branches.get(i);
            for (int j = 0; j < branch.getListTrucks().size(); j++) {
                Truck truck = branch.getListTrucks().get(j);
                Thread truckThread = new Thread(new TruckWorker(truck));
                truckThread.setName("Branch-" + i + "-" + truck.getName() + "-Thread");
                truckThread.start();
                truckThreads.add(truckThread);
            }
        }
    }

    public void stopSimulation() {
        running = false;
        System.out.println("Simulation stopped.");
        
        // Interrupt all threads
        for (Thread t : branchThreads) {
            t.interrupt();
        }
        for (Thread t : truckThreads) {
            t.interrupt();
        }
        
        branchThreads.clear();
        truckThreads.clear();
    }

    public void pauseSimulation() {
        paused = true;
        System.out.println("Simulation paused.");
    }

    public void resumeSimulation() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
        System.out.println("Simulation resumed.");
    }

    @Override
    public void run() {
        running = true;
        runSimulationLoop();
    }

    private void runSimulationLoop() {
        while (running) {
            synchronized (pauseLock) {
                while (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            tick();

            if (allPackagesDelivered()) {
                System.out.println("All packages delivered. Stopping simulation.");
                running = false;
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean allPackagesDelivered() {
        if (packagesCreated < maxPackages){return false;}
        synchronized (packages) {
            for (Package p : packages) {
                if (p.getStatus() != Status.DELIVERED)
                    return false;
            }
        }
        return true;
    }

    // Worker classes for threads
    private class BranchWorker implements Runnable {
        private Branch branch;
        
        public BranchWorker(Branch branch) {
            this.branch = branch;
        }
        
        @Override
        public void run() {
            while (running) {
                synchronized (pauseLock) {
                    while (paused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                
                // Only do local work, trucks handle themselves
                branch.localWork();
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private class TruckWorker implements Runnable {
        private Truck truck;
        
        public TruckWorker(Truck truck) {
            this.truck = truck;
        }
        
        @Override
        public void run() {
            while (running) {
                synchronized (pauseLock) {
                    while (paused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                
                truck.work();
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}