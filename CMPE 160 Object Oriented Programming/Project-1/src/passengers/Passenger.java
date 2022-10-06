
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package passengers ;

import vehicles.*;
import locations.Location;
import interfaces.*;

/**
 * A simulation of a Passenger in this Public Transportation System.
 * @author Halil Burak Pala
 */

public class Passenger implements ownCar, usePublicTransport{
	
	private int ID;
	private boolean hasDriversLicense;
	private boolean hasCar;	
	private double cardBalance = 0;
	private Car car;
	private Location currentLocation;
	
	/**
	 * @param ID				ID of the passenger.
	 * @param hasDriverLicense	Whether the passenger has driver's license.
	 * @param l					Current location of the passenger.
	 */
	
	public Passenger(int ID, boolean hasDriverLicense, Location l) {
		
		this.ID = ID;
		this.hasDriversLicense = hasDriverLicense;
		currentLocation = l;
		hasCar = false;
		
	}
	
	/**
	 * A passenger who has a car.
	 * @param ID				ID of the passenger.
	 * @param l					Current location of the passenger.
	 * @param fuelConsumption	Fuel consumption rate of the passenger's car.
	 */
	
	public Passenger(int ID, Location l, double fuelConsumption) {
		
		this.ID = ID;
		currentLocation = l;
		hasDriversLicense = true;
		hasCar = true;
		car = new Car(ID, fuelConsumption);
		
	}
	
	// ownCar Interface methods
	
	/**
	 * A passenger who doesn't have a car purchases a car.
	 */
	
	public void purchaseCar(double fuelConsumption) {
			
		car = new Car(ID, fuelConsumption);
		hasCar = true;
		hasDriversLicense = true;
		
	}
	
	/**
	 * A passenger who has a car refuels her/his car.
	 */
	
	public void refuel(double amount) {
		
		if(hasCar == true) {
			
			car.refuel(amount);
			
		}
		
	}
	
	/**
	 * Passenger who has a car drives to a location if the car has enough fuel.
	 */
	
	public void drive (Location l) {
		
		if(hasCar == true) {
			
			double newFuelAmount = car.getFuelAmount() - car.getFuelConsumption() * currentLocation.getDistance(l);
			
			if(newFuelAmount >= 0) {
				
				car.setFuelAmount(newFuelAmount);
				l.incomingPassenger(this);
				currentLocation.outgoingPassenger(this);
				currentLocation = l;
				
				
				
			}
		}
		
	}
	
	// usePublicTransport methods
	
	/**
	 * Passenger who wants to travel to a location by a public transport vehicle rides a Bus or Train if 
	 * the vehicle is appropriate for this ride and s/he has enough balance in her/his traveling card. If 
	 * this traveling occurs, balance of the traveling card of this passenger is descended by the price of 
	 * this traveling. 
	 */
	
	public void ride(PublicTransport p, Location l){
		
		if(p instanceof Bus) {
			
			if(p.canRide(currentLocation, l) && this.cardBalance >= ((Bus) p).getPrice(this)) {

				l.incomingPassenger(this);
				currentLocation.outgoingPassenger(this);
				currentLocation = l;
				
				cardBalance -= ((Bus) p).getPrice(this);
				
			}
			
		}
		
		else if(p instanceof Train) {
			
			if(p.canRide(currentLocation, l) && this.cardBalance >= ((Train) p).getPrice(this, currentLocation, l)) {
				
				l.incomingPassenger(this);
				currentLocation.outgoingPassenger(this);
				cardBalance -= ((Train) p).getPrice(this, currentLocation, l);
				currentLocation = l;
				
			}
			
		}
		
	}
	
	/**
	 * Refills the passenger's traveling card by given amount.
	 */
	
	public void refillCard(double amount) {
		
		cardBalance += amount;
		
	}
	
	// getter and setter methods
	
	public int getID() {
		return ID;
	}

	public boolean getHasDriversLicense() {
		return hasDriversLicense;
	}

	public void setHasDriversLicense(boolean hasDriversLicense) {
		this.hasDriversLicense = hasDriversLicense;
	}

	public double getCardBalance() {
		return cardBalance;
	}

	public Car getCar() {
		return car;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	@Override
	public String toString() {
		
		// To print two digits after the decimal point without rounding, I do the following manipulation. 
		
		if(hasCar == false) {
			return ("Passenger " + ID + ": " + String.format("%.2f", ((int)(cardBalance * 100)) / 100.00));
		}
		
		else {
			return ("Passenger " + ID + ": " + String.format("%.2f", ((int)(car.getFuelAmount() * 100)) / 100.00));
		}
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

