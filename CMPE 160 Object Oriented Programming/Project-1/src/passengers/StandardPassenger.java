
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package passengers;

import locations.Location;

/**
 * Standard passenger type. S/he has no discounts on her/his public transport travelings.
 * 
 * @author Halil Burak Pala
 */

public class StandardPassenger extends Passenger{
	
	public StandardPassenger(int ID, boolean hasDriverLicense, Location l){
		super(ID, hasDriverLicense, l);
	}
	
	public StandardPassenger( int ID, Location l, double fuelConsumption) {
		super(ID, l, fuelConsumption);
	}
	
}
//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

