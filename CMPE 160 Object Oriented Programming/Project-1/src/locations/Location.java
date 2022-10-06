
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package locations;
import java.util.*;
import passengers.Passenger;
/**
 * A point in the 2D coordinate system.
 * 
 * @author Halil Burak Pala
 */

public class Location{
	
	private int ID;
	private double locationX;
	private double locationY;
	private ArrayList<Passenger> history = new ArrayList<Passenger>();
	private ArrayList<Passenger> current = new ArrayList<Passenger>();
	
	public Location(int ID, double locationX, double locationY) {
		
		this.ID = ID;
		this.locationX = locationX;
		this.locationY = locationY;
		
	}
	
	/**
	 * Distance between two locations.
	 * @return	Distance between two locations.
	 */
	
	public double getDistance(Location other) {
		 
		 double diffX = Math.abs(locationX - other.getLocationX());
		 double diffY = Math.abs(locationY - other.getLocationY());
		 return Math.sqrt(diffX*diffX + diffY*diffY);
		 
	 }
	 
	 public void incomingPassenger(Passenger p) {
		 
		 current.add(p);
		 
		 if(!history.contains(p)) {
			 
			 history.add(p);
			 
		 }
		 
	 }
	 
	 public void outgoingPassenger(Passenger p) {
		 
		 current.remove(p);
		 
	 }
	 
	 public ArrayList<Passenger> getHistory(){
		 return history;
	 }
	 
	 public ArrayList<Passenger> getCurrent(){
		 return current;
	 }
	 
	 public int getID() {
		 return ID;
	 }
	 
	 public double getLocationX() {
		 return locationX;
	 }
	 
	 public double getLocationY() {
		 return locationY;
	 }
	 
	 public boolean equals(Location other) {
		 return other.getID() == ID;
	 }

	@Override
	public String toString() {
		// To print two digits after the decimal point without rounding, I do the following manipulation. 
		locationX += 0.000000001; // to avoid wrong rounding
		locationY += 0.000000001; // to avoid wrong rounding
		return "Location " + ID + ": (" + String.format("%.2f", ((int)(locationX * 100)) / 100.00) + ", " + String.format("%.2f",  ((int)(locationY * 100)) / 100.00) + ")";
		
	}
}

//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

