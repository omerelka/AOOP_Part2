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
    
    System.out.println("DEBUG: " + t.getName() + " checking branch " + targetBranch.getName() + " for BRANCH_STORAGE packages");
    
    // בדוק אם יש חבילות בסניף שצריכות איסוף
    boolean hasBranchPackages = false;
    synchronized (targetBranch.getPackages()) {
        System.out.println("DEBUG: Branch " + targetBranch.getName() + " has " + targetBranch.getPackages().size() + " packages");
        for (Package p : targetBranch.getPackages()) {
            System.out.println("DEBUG: Package " + p.getPackageID() + " status: " + p.getStatus());
            if (p.getStatus() == Status.BRANCH_STORAGE) {
                hasBranchPackages = true;
                break;
            }
        }
    }
    
    if (hasBranchPackages) {
        // שלח משאית לאסוף מהסניף
        int travelTime = (new Random()).nextInt(10) + 1;
        t.startJourneyToBranch(targetBranch, travelTime);
        System.out.println(t.getName() + " is going to " + targetBranch.getName() + " to collect BRANCH_STORAGE packages, time to arrive: " + travelTime);
    } else {
        System.out.println("DEBUG: No BRANCH_STORAGE packages found in " + targetBranch.getName());
        // הלוגיקה הרגילה - טען מההאב ושלח לסניף
        t.load(this, targetBranch, Status.BRANCH_TRANSPORT);
        
        if (!t.getPackages().isEmpty()) {
            int travelTime = (new Random()).nextInt(10) + 1;
            t.startJourneyToBranch(targetBranch, travelTime);
            System.out.println(t.getName() + " is on it's way to " + targetBranch.getName() + ", time to arrive: " + travelTime);
        } else {
            System.out.println(t.getName() + " - no packages to transport, staying at hub");
        }
    }
    
    currentIndex = (currentIndex + 1) % branches.size();
}
}