package components;

import java.util.ArrayList;
import java.util.Random;

public class Hub extends Branch {

    private ArrayList<Branch> branches = new ArrayList<Branch>();
    private int currentIndex = 0;

    public Hub() {
        super("HUB");
    }

    public ArrayList<Branch> getBranches() {
        return branches;
    }

    public synchronized void add_branch(Branch branch) {
        branches.add(branch);
    }

    public synchronized void shipNonStandard(NonStandardTruck t) {
        synchronized (packages) {
            for (Package p : packages) {
                if (p instanceof NonStandardPackage) {
                    t.collectPackage(p);
                    packages.remove(p);
                    return;
                }
            }
        }
    }

    @Override
    public void localWork() {
        synchronized (listTrucks) {
            for (Truck t : listTrucks) {
                if (t.isAvailable()) {
                    if (t instanceof NonStandardTruck)
                        shipNonStandard((NonStandardTruck) t);
                    else
                        sendTruck((StandardTruck) t);
                }
            }
        }
    }

    public synchronized void sendTruck(StandardTruck t) {
        Branch targetBranch = branches.get(currentIndex);
        int travelTime = (new Random()).nextInt(10) + 1;
        
        // Load packages going to this branch
        t.load(this, targetBranch, Status.BRANCH_TRANSPORT);
        
        // Start the journey using the new method
        t.startJourneyToBranch(targetBranch, travelTime);
        
        System.out.println(t.getName() + " is on it's way to " + targetBranch.getName() + ", time to arrive: " + travelTime);
        currentIndex = (currentIndex + 1) % branches.size();
    }
}