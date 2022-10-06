
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package vehicles;

import passengers.*;
import locations.Location;

/**
 * A simulation of a Train.
 * 
 * @author Halil Burak Pala
 *
 */

public class Train extends PublicTransport{
	
	private int ID;
	private double x1, y1, x2, y2;
	
	public Train(int ID, double x1, double y1, double x2, double y2) {
		
		super(ID, x1, y1, x2, y2);
		
	}
	
	/**
	 * Train charges per stop. There are 15 kilometers between every stop and traveling one stop costs 5 TL.  
	 * Passengers will travel to the nearest stop to their targeted location. Discounted Passengers have 20% discount.
	 * 
	 * @param p				Passenger who wants to travel by the train.
	 * @param departure		Departure location.
	 * @param arrival		Arrival location.
	 * @return				Price of the train ride.
	 */
	
	public double getPrice(Passenger p, Location departure, Location arrival) {
		
		double distance = departure.getDistance(arrival);
		double price = 0;
		
		if(this.canRide(departure, arrival)) {
			
			if(distance % 15 <= 7) {
				distance = distance - distance % 15;
			}
			
			else {
				distance = distance + (15 - distance % 15);
			}
			
			int nofStops = (int)distance / 15;
			
			price = nofStops * 5;

		}
		
		if(p instanceof DiscountedPassenger) {
			
			return price * 0.8;
			
		}
		else
			
			return price;
		
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

