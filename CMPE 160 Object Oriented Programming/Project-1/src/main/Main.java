
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE

package main;

import java.util.*;
import vehicles.*;
import java.io.*;
import passengers.*;
import locations.*;

/**
 * Main class of the Public Transportation System Project.
 * 
 * @author Halil Burak Pala
 */

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException {

		Scanner input = new Scanner(new File(args[0]));
		PrintStream output = new PrintStream(new File(args[1]));
		
		/**
		 *	@param	passengerID	ID of a passenger. It ascends by every passenger added to passengers ArrayList.
		 *	@param	locationID	ID of a location. It ascends by every location added to locations ArrayList.
		 *	@param	vehicleID 	ID of a public transportation vehicle. It ascends by every vehicle added to vehicles ArrayList.
		 */
		
		int passengerID = 0, locationID = 1, vehicleID = 0;

		ArrayList<Passenger> passengers = new ArrayList<Passenger>();
		ArrayList<Location> locations = new ArrayList<Location>();
		ArrayList<PublicTransport> vehicles = new ArrayList<PublicTransport>();
		
		Location l = new Location(0, 0, 0); //	The first location is always (0,0).
		
		locations.add(l);
		
		/**
		 * 	@param	operations	Number of operations which is in the first line of input file.
		 */
		
		final int operations = input.nextInt();
		
		input.nextLine();	//	Passed to second line.
		
		for(int i = 0 ; i < operations ; i++) {	//	Loop through the action list.
			
			String line = input.nextLine();		//	I took the following line which contains specified actions.
			Scanner scan = new Scanner(line);	//	Reads the line.
			
			int actionCode = scan.nextInt();	//	This is the code of action which is between 1 - 7.
			
			/**
			 * 	If actionCode is 1, create a passenger according to specified conditions.
			 */
			
			if(actionCode == 1) {
				
				/**
				 * 	@param 	passengerType		Takes "S" if passenger is Standard, "D" if Discounted.
				 * 	@param	hasDriverLicense	Takes 1 if the passenger has Driver's License, 0 otherwise.
				 */
				
				String passengerType = scan.next();
				int hasDriversLicense = scan.nextInt();
				
				if(passengerType.equals("D")) {
					
					if(hasDriversLicense == 0) {
						
						/**
						 * 	If the passenger is Discounted and doesn't have Driver's License, create it and add
						 * 	to passengers ArrayList. (Use the first constructor in the Passenger class.)
						 */
						
						DiscountedPassenger passenger = new DiscountedPassenger(passengerID, false, l);
						passengers.add(passenger);
						
					}
					
					else if (hasDriversLicense == 1) {
						
						/**
						 * 	If the passenger is Discounted and has a Driver's License, look for whether s/he 
						 * 	has a car or not. hasCar is 1, if s/he has a car, 0 otherwise.
						 */
						
						int hasCar = scan.nextInt();
						
						if(hasCar == 0) {
							
							/**
							 * 	If s/he doesn't have a car, create a Discounted Passenger without a car. 
							 * 	(Use the first constructor in the Passenger class.) 
							 */
							
							DiscountedPassenger passenger = new DiscountedPassenger(passengerID, true, l);
							passengers.add(passenger);
							
						}
						
						else if(hasCar == 1) {
							
							/**
							 * 	If s/he has a car, take one more double for fuel consumption rate of this car.
							 * 	And create a Discounted Passenger with a car which has specified fuel consumption
							 * 	rate (Use the second constructor in the Passenger class.) 
							 */
							
							double fuelConsumption = scan.nextDouble();
							
							DiscountedPassenger passenger = new DiscountedPassenger(passengerID, l, fuelConsumption);
							passengers.add(passenger);
							
						}
						
					}
					
				} 
				
				else if(passengerType.equals("S")){
					
					if(hasDriversLicense == 0) {
						
						/**
						 * 	If the passenger is Standard and doesn't have Driver's License, create it and add
						 * 	to passengers ArrayList. (Use the first constructor in the Passenger class.)
						 */
						
						StandardPassenger passenger = new StandardPassenger(passengerID, false, l);
						passengers.add(passenger);
						
					}
					
					else if (hasDriversLicense == 1) {
						
						/**
						 * 	If the passenger is Standard and has a Driver's License, look for whether s/he 
						 * 	has a car or not. hasCar is 1, if s/he has a car, 0 otherwise.
						 */
						
						int hasCar = scan.nextInt();
						
						if(hasCar == 0) {
							
							/**
							 * 	If s/he doesn't have a car, create a Standard Passenger without a car. 
							 * 	(Use the first constructor in the Passenger class.) 
							 */
							
							StandardPassenger passenger = new StandardPassenger(passengerID, true, l);
							passengers.add(passenger);
							
						}
						
						else if(hasCar == 1) {
							
							/**
							 * 	If s/he has a car, take one more double for fuel consumption rate of this car.
							 * 	And create a Standard Passenger with a car which has specified fuel consumption
							 * 	rate (Use the second constructor in the Passenger class.) 
							 */
							
							double fuelConsumption = scan.nextDouble();
							
							StandardPassenger passenger = new StandardPassenger(passengerID, l, fuelConsumption);
							passengers.add(passenger);
							
						}
						
					}
					
				}
				
				passengerID++;	//	Ascend passengerID by 1 with every passenger created.
				
			}
			
			/**
			 * 	If actionCode is 2, create a location with given coordinates.
			 */
			
			else if(actionCode == 2) {
				
				double X = scan.nextDouble();
				double Y = scan.nextDouble();
				
				Location newL = new Location(locationID, X, Y);
				locations.add(newL);
				
				locationID++;	//	Ascend locationID by 1 with every location created.
				
			}
			
			/**
			 * 	If actionCode is 3, create a public transport vehicle according to type variable - which is 1 if it is a Bus, 
			 * 	2 if it is a Train - which has a operation range of (X1, Y1) - (X2, Y2).
			 * 
			 * 	@param vehicleType	1 if the vehicle is a Bus, 2 if it's a Train.
			 */
			
			else if(actionCode == 3) {
				
				int vehicleType = scan.nextInt();
				double X1 = scan.nextDouble();
				double Y1 = scan.nextDouble();
				double X2 = scan.nextDouble();
				double Y2 = scan.nextDouble();
				
				if(vehicleType == 1) {
					
					Bus bus = new Bus(vehicleID, X1, Y1, X2, Y2);
					vehicles.add(bus);
					
				}
				
				else if(vehicleType == 2) {
					
					Train train = new Train(vehicleID, X1, Y1, X2, Y2);
					vehicles.add(train);
					
				}
				
				vehicleID++;	//	Ascend vehicleID by 1 with every vehicle created.
				
			}
			
			/**
			 * 	If actionCode is 4, passenger will travel according to following codes. First, I will take ID of the passenger 
			 * 	who wants to travel; second, ID of the location where s/he wants to travel; third, type of the vehicle s/he wants
			 * 	to travel by. The last variable is 3 if s/he wants to drive her/his car. If it is 1 or 2, it means that s/he will ride
			 * 	a bus or train respectively and I will take one more input which is the ID of the public transport vehicle s/he 
			 * 	wants to ride.
			 */
			
			else if(actionCode == 4) {
				
				int passengerID4 = scan.nextInt();	//	ID of the passenger who wants to travel
				int locationID4 = scan.nextInt();	//	ID of the location where the passenger wants to travel
				int vehicleType4 = scan.nextInt();	//	Type of the vehicle the passenger wants to travel by.
				int locationsIndex = 0;		
				int passengersIndex = 0;
				int vehiclesIndex = 0;
				
				/**
				 * 	In the following for loop, I want to find where the given location is. locationsIndex holds the index of this
				 * 	location in the locations ArrayList.
				 */
				
				for(int j = 0 ; j < locations.size() ; j++) {
					
					if(locations.get(j).getID() == locationID4) {
						
						locationsIndex = j;
						
					}
					
				}
				
				/**
				 * 	In the following for loop, I want to find who the given passenger is. passengersIndex holds the index of this
				 * 	passenger in the passengers ArrayList.
				 */
				
				for(int j = 0 ; j < passengers.size() ; j++) {
					
					if(passengers.get(j).getID() == passengerID4) {
						
						passengersIndex = j;
						
					}
					
				}
				
				Location target = locations.get(locationsIndex);		// 	Location the passenger wants to travel to.
				Passenger passenger = passengers.get(passengersIndex);	//	Passenger who wants to travel.
				
				/**
				 * 	If the vehicleType is 3, it means that the passenger will travel by her/his own car. I will do this operation by
				 * 	"drive" method which is in the Passenger class. 
				 */
				
				if(vehicleType4 == 3) {
					
					passenger.drive(target);
					
				}
				
				
				else {
					
					/**
					 * 	If vehicleType is 1 or 2, a public transport vehicle (Bus or Train), I will take one more input, the ID of this public 
					 * 	transportation vehicle.
					 */
					
					int vehicleID4 = scan.nextInt();
					
					/**
					 * 	In the following for loop, I want to find which the given public transport vehicle is. vehiclesIndex holds 
					 * 	the index of this vehicle in the vehicles ArrayList.
					 */
					
					for(int j = 0 ; j < vehicles.size() ; j++) {
						
						if(vehicles.get(j).getID() == vehicleID4) {
							
							vehiclesIndex = j;
							
						}
						
					}
					
					PublicTransport newVehicle = vehicles.get(vehiclesIndex);	//	Public Transport vehicle the passenger wants to travel by.
					
					/**
					 * 	If the vehicleType is 1, the passenger rides a Bus to travel to given Location. 
					 */
					
					if(vehicleType4 == 1) {
						
						if(newVehicle instanceof Bus) {	//	I check whether this newVehicle is a Bus. If it is not, traveling won't happen.
							
							passenger.ride((Bus)newVehicle, target);
							
						}
						
					}
					
					/**
					 * 	If the vehicleType is 2, the passenger travels by a train. 
					 */
					
					else if(vehicleType4 == 2) {
						
						if(newVehicle instanceof Train) {	//	I check whether this newVehicle is a Train. If it is not, traveling won't happen.
							
							passenger.ride((Train)newVehicle, target);
							
						}
						
					}
					
				}
				
			}
			
			/**
			 * 	If actionCode is 5, the passenger purchases a car. Also if s/he doesn't have a driver's license, s/he gets one.
			 */
			
			else if(actionCode == 5) {
				
				int passengerID5 = scan.nextInt();				//	ID of the passenger who purchases a car.
				double fuelConsumption = scan.nextDouble();		//	Fuel Consumption rate of this car.
				
				/**
				 * 	In the following for loop, I want to find who the given passenger is according to her/his ID. If there is such a passenger
				 * 	s/he will purchase a car which has given fuel consumption rate. And if s/he doesn't have a driver's license, s/he gets one.
				 */
				
				for(int j = 0 ; j < passengers.size() ; j++) {
					
					if(passengers.get(j).getID() == passengerID5) {
						
						passengers.get(j).purchaseCar(fuelConsumption);
						
					}
					
					if(passengers.get(j).getHasDriversLicense() == false) {
						
						passengers.get(j).setHasDriversLicense(true);
						
					}
					
				}
				
			}
			
			/**
			 * 	If actionCode is 6, the passenger refuels her/his car.
			 */
			
			else if(actionCode == 6) {
				
				int passengerID6 = scan.nextInt();
				double amount = scan.nextDouble();
				
				/**
				 *	In the following for loop, I will find the corresponding passenger in the passengers ArrayList and refuel 
				 *	her/his car by given amount.
				 */
				
				for(int j = 0 ; j < passengers.size() ; j++) {	
					
					if(passengers.get(j).getID() == passengerID6) {
						
						passengers.get(j).refuel(amount);
						
					}
					
				}
				
			}
			
			/**
			 * 	If actionCode is 7, the passenger refills her/his traveling card.
			 */
			
			else if(actionCode == 7) {
				
				int passengerID7 = scan.nextInt();
				double amount = scan.nextDouble();
				
				/**
				 *	In the following for loop, I will find the corresponding passenger in the passengers ArrayList and refill 
				 *	her/his card by given amount.
				 */
				
				for(int j = 0 ; j < passengers.size() ; j++) {
					
					if(passengers.get(j).getID() == passengerID7) {
						
						passengers.get(j).refillCard(amount);
						
					}
					
				}
				
			}
			
		}
		
		/**
		 *	In the following for loop, my aim is to print in which locations passengers are to output file. To be able to do that 
		 * 	I first traverse through locations ArrayList and first print the location, then print the passengers in this location's
		 * 	"current" ArrayList.
		 */

		
		for(int i = 0 ; i < locations.size() ; i++) {
			
			output.println(locations.get(i));
			
			for(int j = 0 ; j < passengers.size(); j++) {
				
				if(locations.get(i).getCurrent().contains(passengers.get(j))) {

					output.println(passengers.get(j));
					
				}
				
			}
			
		}
		
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

