package components;

import java.util.Random;

public class NonStandardTruck extends Truck{


	public NonStandardTruck() {
		super();
		Random r=new Random();
		System.out.println(this);
	}
	
	
	public NonStandardTruck(String licensePlate,String truckModel, int length, int width, int height) {
		super(licensePlate,truckModel);
		
		System.out.println(this);
	}

	
	
	
	
	@Override
	public void work() {
		synchronized (this) {
			if (!this.isAvailable()) {
			Package p=this.getPackages().get(0);
			this.setTimeLeft(this.getTimeLeft()-1);
			if (this.getTimeLeft()==0) {
				if (p.getStatus()==Status.COLLECTION) {
					System.out.println(getName() + " has collected "+p.getName());
					deliverPackage(p);
				}
					
				else {
					System.out.println(getName() + " has delivered "+p.getName() + " to the destination");
					this.getPackages().remove(p);
					p.addRecords(Status.DELIVERED, null);
					setAvailable(true);
				}
			}
		}
		}
		
	}
	
	
	@Override
	public void deliverPackage (Package p)  {
		int time=Math.abs(p.getDestinationAddress().getStreet()-p.getSenderAddress().getStreet())%10+1;
		this.setTimeLeft(time);
		p.addRecords(Status.DISTRIBUTION, this);
		System.out.println(getName() + " is delivering " + p.getName() + ", time left: "+ this.getTimeLeft()  );
	}
	
	
	@Override
	public String toString() {
		return "NonStandardTruck ["+ super.toString()+ "]";
	}

	@Override
	public void run(){work();}
	
}

