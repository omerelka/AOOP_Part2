package components;

public class Tracking {
	public final int time;
	public final Node node;
	public final Status status;
	
	public Tracking(int time, Node node, Status status) {
		super();
		this.time = time;
		this.node = node;
		this.status = status;
	}

	public Tracking (Node node, Status status) {
		this(MainOffice.getClock(),node,status);
	}
	
	@Override
	public String toString() {
		String name = (node==null)? "Customer" : node.getName();
		return time + ": " + name + ", status=" + status;
	}

	
}
