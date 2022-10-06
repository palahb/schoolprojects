
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package vehicles;

import locations.Location;

/**
 * An abstract class which simulates a public transport vehicle.
 * @author Halil Burak Pala
 *
 */

public abstract class PublicTransport {
	private int ID;
	private double x1, y1, x2, y2;
	
	public PublicTransport(int ID, double x1, double y1, double x2, double y2) {
		this.ID = ID;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * 
	 * @param departure	Departure location.
	 * @param arrival	Arrival location.
	 * @return			Whether this vehicle can travel between these coordinates.
	 */
	
	public boolean canRide(Location departure, Location arrival){
		
		if(!departure.equals(arrival)) {
			arrangeCoordinates(x1, y1, x2, y2);
			
			return departure.getLocationX() <= x2 && departure.getLocationX() >= x1 && departure.getLocationY() <= y2 && departure.getLocationY() >= y1 && 
					arrival.getLocationX()<= x2 && arrival.getLocationX() >= x1 && arrival.getLocationY() <= y2 && arrival.getLocationY() >= y1;
		} else
			return false;
		
	}
	
	/**
	 * I wrote the following arrangeCoordinates method in order to arrange the coordinates so that
	 * x1 is always less than or equal to x2 and y1 is always less than or equal to y2. (x1 <= x2 and y1 <= y2)
	 */
	
	public void arrangeCoordinates(double cX1, double cY1, double cX2, double cY2) { 
		
		if(cX1 > cX2) {
			x1 = cX2;
			x2 = cX1;
		}
		
		if(y1 > y2) {
			y1 = cY2;
			y2 = cY1;
		}
		
	}

	public int getID() {
		return ID;
	}

}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE





