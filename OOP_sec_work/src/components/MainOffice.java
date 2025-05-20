package components;

import GUI.SystemPanel;
import java.util.Random;
import java.util.Vector;

public class MainOffice implements Runnable {
	private int maxPackages;
	private static int clock=0;
	private static Hub hub;
	private Vector<Package> packages=new Vector<>();
	private SystemPanel systemPanel = new SystemPanel();
	private Vector<Branch> branches = new Vector<>();
	private volatile boolean running = false;
	private Thread simulationThread;
	private int packagesCreated = 0;
	private final Object pauseLock = new Object();
	private volatile boolean paused = false;



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
		for (int i=0; i<playTime; i++) {
			tick();
		}
		System.out.println("\n========================== STOP ==========================\n\n");
		printReport();
	}
	
	public void printReport() {
		synchronized(packages){
			for (Package p: packages) {
			System.out.println("\nTRACKING " +p);
			for (Tracking t: p.getTracking())
				System.out.println(t);
			}
		}
		
	}
	
	public String clockString() {
		String s="";
		int minutes=clock/60;
		int seconds=clock%60;
		s+=(minutes<10) ? "0" + minutes : minutes;
		s+=":";
		s+=(seconds<10) ? "0" + seconds : seconds;
		return s;
	}
	
	
	public void tick() {
		System.out.println(clockString());
		if (clock % 5 == 0 && packagesCreated < maxPackages) {
			addPackage();
			packagesCreated++;
		}
		hub.work();
		for (Branch b: hub.getBranches()) {
			b.work();
		}
		clock++;
	}
		
	
	public void addHub(int trucksForBranch) {
		hub=new Hub();
		for (int i=0; i<trucksForBranch; i++) {
			hub.addTruck(new StandardTruck());
		}
		hub.addTruck(new NonStandardTruck());
	}
	
	
	public void addBranches(int branches, int trucks) {
		for (int i=0; i<branches; i++) {
			Branch branch=new Branch();
			for (int j=0; j<trucks; j++) {
				branch.addTruck(new Van());
			}
			hub.add_branch(branch);
			this.branches.add(branch);

		}
	}
	
	
	public void addPackage() {
		Random r = new Random();
		Package p;
		Priority priority=Priority.values()[r.nextInt(3)];
		Address sender = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);
		Address dest = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);

		switch (r.nextInt(3)){
		case 0:
			p = new SmallPackage(priority,  sender, dest, r.nextBoolean() );
			synchronized(packages){
				p.getSenderBranch().addPackage(p);
			}
			break;
		case 1:
			p = new StandardPackage(priority,  sender, dest, r.nextFloat()+(r.nextInt(9)+1));
			synchronized(packages){
				p.getSenderBranch().addPackage(p);
			}
			break;
		case 2:
			p=new NonStandardPackage(priority,  sender, dest,  r.nextInt(1000), r.nextInt(500), r.nextInt(400));
			synchronized(packages){
				hub.addPackage(p);
			}
			break;
		default:
			p=null;
			return;
		}
		synchronized(packages){
			this.packages.add(p);
		}
		
	}
	public Vector<Package> getPackages(){
		return packages;
	}

	public void startSimulation() {
		if (simulationThread == null || !simulationThread.isAlive()) {
			running = true;
			simulationThread = new Thread(() -> runSimulationLoop());
			simulationThread.start();
			System.out.println("Simulation started.");
		}
	}
	public void stopSimulation() {
		running = false;
		System.out.println("Simulation stopped.");
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
		synchronized(packages){
			for (Package p : packages) {
        if (p.getStatus() != Status.DELIVERED)
            return false;
    		}
		}
    
    return true;
}


}
