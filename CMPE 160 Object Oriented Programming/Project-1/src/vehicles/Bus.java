
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package vehicles;
import passengers.*;

/**
 * A simulation of a Bus.
 * 
 * @author Halil Burak Pala
 *
 */

public class Bus extends PublicTransport{
	
	private int ID;
	private double x1, y1, x2, y2;
	
	public Bus(int ID, double x1, double y1, double x2, double y2) {
		
		super(ID, x1, y1, x2, y2);
		
	}
	
	/**
	 * Bus fare is 2 TL for a Standard Passenger. Discounted Passengers have a discount of 50%. 
	 * 
	 * @param p	Passenger who wants to ride the bus.
	 * @return	Price of the bus ride.
	 */
	
	public double getPrice(Passenger p) {
		
		if(p instanceof DiscountedPassenger)
			
			return 1.0;
			
		else
			
			return 2.0;
		
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

