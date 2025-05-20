package components;

import java.awt.*;
import java.util.ArrayList;


public abstract class Package {
	private Point senderPoint;
	private Point receiverPoint;
	private static int countID=1000;
	final private int packageID;
	private Priority priority;
	private Status status;
	private Address senderAddress;
	private Address destinationAddress;
	private ArrayList<Tracking> tracking = new ArrayList<Tracking>();
	
	
	public Package(Priority priority, Address senderAddress,Address destinationAdress) {
		packageID = countID++;
		this.priority=priority;
		this.status=Status.CREATION;
		this.senderAddress=senderAddress;
		this.destinationAddress=destinationAdress;
		tracking.add(new Tracking( MainOffice.getClock(), null, status));
	}

	public Point getSenderPoint() {return senderPoint;}

	public void setSenderPoint(Point senderPoint) {this.senderPoint = senderPoint;}

	public Point getDestinationPoint() {return receiverPoint;}

	public void setDestinationPoint(Point receiverPoint) {this.receiverPoint = receiverPoint;}

	public Priority getPriority() {
		return priority;
	}

	
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	
	public Status getStatus() {
		return status;
	}

	
	public void setStatus(Status status) {
		this.status = status;
	}

	
	public int getPackageID() {
		return packageID;
	}
	
	
	
	public Address getSenderAddress() {
		return senderAddress;
	}

	
	public void setSenderAddress(Address senderAddress) {
		this.senderAddress = senderAddress;
	}

	
	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public String getName() {
		return "package " + getPackageID(); 
	}
	
	
	public void setDestinationAddress(Address destinationAdress) {
		this.destinationAddress = destinationAdress;
	}

	
	public void addTracking(Node node, Status status) {
		tracking.add(new Tracking(MainOffice.getClock(), node, status));
	}
	
	
	public void addTracking(Tracking t) {
		tracking.add(t);
	}
	
	
	public ArrayList<Tracking> getTracking() {
		return tracking;
	}

	
	public void printTracking() {
		for (Tracking t: tracking)
			System.out.println(t);
	}
	
	
	public Branch getSenderBranch() {
		return MainOffice.getHub().getBranches().get(getSenderAddress().getZip());
	}
	
	
	public Branch getDestBranch() {
		return MainOffice.getHub().getBranches().get(getDestinationAddress().getZip());
	}
	
	
	@Override
	public String toString() {
		return "packageID=" + packageID + ", priority=" + priority + ", status=" + status + ", senderAddress=" + senderAddress + ", destinationAddress=" + destinationAddress;
	}

	public void addRecords(Status status, Node node) {
		setStatus(status);
		addTracking(node, status);
	}

    public String Type(){
		return "Package";
	}


}
