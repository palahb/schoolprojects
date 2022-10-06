
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package passengers;

import locations.Location;

/**
 * A passenger type which has discounts in her/his public transport travelings.
 * 
 * @author Halil Burak Pala
 */

public class DiscountedPassenger extends Passenger{
	
	public DiscountedPassenger(int ID, boolean hasDriverLicense, Location l){
		super(ID, hasDriverLicense, l);
	}
	
	public DiscountedPassenger( int ID, Location l, double fuelConsumption) {
		super(ID, l, fuelConsumption);
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

