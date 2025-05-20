package components;

import java.util.Random;

public class StandardTruck extends Truck{
	private int maxWeight;
	private Branch destination;
	
	
	public StandardTruck() {
		super();
		maxWeight=((new Random()).nextInt(2)+2)*100;
		System.out.println(this);

	}
	

	public StandardTruck(String licensePlate,String truckModel,int maxWeight) {
		super(licensePlate,truckModel);
		this.maxWeight=maxWeight;
		System.out.println(this);

	}
	
	
	public Branch getDestination() {
		return destination;
	}
	
	
	public void setDestination(Branch destination) {
		this.destination = destination;
	}
	

	public int getMaxWeight() {
		return maxWeight;
	}

	
	public void setMaxWeight(int maxWeight) {
		this.maxWeight = maxWeight;
	}

	
	@Override
	public String toString() {
		return "StandartTruck ["+ super.toString() +",maxWeight=" + maxWeight + "]";
	}
	
	
	public void unload () {
		for (Package p: getPackages()) {
			deliverPackage(p);
		}
		getPackages().removeAll(getPackages());
		System.out.println(getName() + " unloaded packages at " + destination.getName());
	}
	
	
	@Override
	public void deliverPackage(Package p) {
		if (destination==MainOffice.getHub())
			p.addRecords(Status.HUB_STORAGE, destination);
		else 
			p.addRecords(Status.DELIVERY, destination);
		destination.addPackage(p);
	}
	
	public void load (Branch sender, Branch dest, Status status) {
		double totalWeight=0;
		for (int i=0; i< sender.getPackages().size();i++) {
			Package p=sender.getPackages().get(i);
			if (p.getStatus()==Status.BRANCH_STORAGE || (p.getStatus()==Status.HUB_STORAGE && p.getDestBranch()==dest)) {
				if (p instanceof SmallPackage && totalWeight+1<=maxWeight || totalWeight+((StandardPackage)p).getWeight()<=maxWeight) {
					getPackages().add(p);
					sender.removePackage(p);
					i--;
					p.addRecords(status, this);
				}
			}
		}
		System.out.println(this.getName() + " loaded packages at " + sender.getName());
	}
	
	@Override
	public void work() {
		synchronized (this) {
			if (!isAvailable()) {
			setTimeLeft(getTimeLeft()-1);
			if (getTimeLeft()==0) {
				System.out.println("StandardTruck "+ getTruckID()+ " arrived to " + destination.getName());
				unload();
				if (destination==MainOffice.getHub()) {
					setAvailable(true);
					destination=null;
				}
					
				else {
					load(destination, MainOffice.getHub(), Status.HUB_TRANSPORT);
					setTimeLeft((new Random()).nextInt(6)+1);
					destination=MainOffice.getHub();
					System.out.println(this.getName() + " is on it's way to the HUB, time to arrive: "+ getTimeLeft());
				}			
			}
		}
		}
		
	}

	@Override
	public void run(){work();}


	
}
