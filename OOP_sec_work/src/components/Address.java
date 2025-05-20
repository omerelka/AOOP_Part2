package components;

import java.awt.*;

public class Address {
	Point location;
	private final int zip;
	private final int street;
	
	public Address(int zip, int street ) {
		this.zip=zip;
		this.street=street;
	}

	public Point getLocation() {return location;}
	public void setLocation(Point location) {this.location=location;}

	public int getZip() {
		return zip;
	}

	public int getStreet() {
		return street;
	}
	
	@Override
	public String toString() {
		return zip + "-" + street;
	}	

}
