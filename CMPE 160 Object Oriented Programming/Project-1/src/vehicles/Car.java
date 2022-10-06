
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package vehicles;

/**
 * A simulation of a Car.
 * 
 * @author Halil Burak Pala
 *
 */

public class Car{
	private int ownerID;
	private double fuelAmount;
	private double fuelConsumption;
	
	/**
	 * @param ID				ID of the owner of the car.
	 * @param fuelConsumption	Fuel consumption rate of the car.
	 */
	
	public Car(int ID, double fuelConsumption) {
		
		this.ownerID = ID;
		this.fuelConsumption = fuelConsumption;
		fuelAmount = 0;
		
	}
	
	/**
	 * @param amount	Amount of added fuel.
	 */
	
	public void refuel(double amount) {
		
		fuelAmount += amount;
		
	}
	
	//getters and setters
	
	public double getFuelAmount() {
		
		return fuelAmount;
		
	}
	
	public double getFuelConsumption() {
		
		return fuelConsumption;
		
	}
	
	public void setFuelAmount(double newFuelAmount) {
		
		fuelAmount = newFuelAmount;
		
	}
	
}

//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

