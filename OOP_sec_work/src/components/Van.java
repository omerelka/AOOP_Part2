package components;

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
        this.setTimeLeft(time);
        p.addRecords(Status.DISTRIBUTION, this);
        System.out.println(getName() + " is delivering " + p.getName() + ", time left: " + getTimeLeft());
    }
    
    @Override
    public synchronized void collectPackage(Package p) {
        setAvailable(false);
        int time = p.getSenderAddress().getStreet() % 10 + 1;
        this.setTimeLeft(time);
        this.getPackages().add(p);
        // Don't change status yet - package is still at sender
        p.addTracking(new Tracking(MainOffice.getClock(), this, Status.COLLECTION));
        System.out.println(getName() + " is going to collect package " + p.getPackageID() + ", time to arrive: " + getTimeLeft());
    }
    
    @Override
    public void work() {
        if (!isAvailable()) {
            setTimeLeft(getTimeLeft() - 1);
            if (this.getTimeLeft() == 0) {
                for (Package p : this.getPackages()) {
                    if (p.getStatus() == Status.CREATION) {
                        // Van has arrived at sender and picked up the package
                        p.addRecords(Status.BRANCH_STORAGE, p.getSenderBranch());
                        System.out.println(getName() + " has collected " + p.getName() + " and arrived back to " + p.getSenderBranch().getName());
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
                this.setAvailable(true);
            }
        }
    }

    @Override
    public void run() {
        work();
    }
}