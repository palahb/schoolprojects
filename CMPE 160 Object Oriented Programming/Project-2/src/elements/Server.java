
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package elements;

import java.io.*;
import java.util.*;

/**
 * Server functions as the mechanism where all non-received messages are stored 
 * in a first come, first served (FCFS) manner. It has a finite capacity set at 
 * the beginning of the execution. If the sum of lengths of the messages stored 
 * in the server exceeds the capacity value, all messages in the server are deleted.
 * 
 * @author Halil Burak Pala
 */
public class Server{
	private final long capacity;
	private long currentSize;
	private Queue<Message> msgs;
	
	/**
	 * Creates a server with specified storage capacity, initializes its msgs queue
	 * to a new empty LinkedList and current size to zero.
	 * 
	 * @param capacity Capacity of the server. Every 
	 */
	public Server(long capacity) {
		this.capacity = capacity;
		this.msgs = new LinkedList<Message>();
		currentSize = 0;
	}
	
	/**
	 * Prints the warnings about the capacity. If capacity is full, deletes all messages
	 * in the server.
	 * 
	 * @param print Where the warnings are printed.
	 */
	public void checkServerLoad(PrintStream print) {
		
		double occupancy = this.load(); 
		
		if(occupancy >= 50 && occupancy < 80) {
			
			print.println("Warning! Server is 50% full.");
			
		} else if(occupancy >= 80 && occupancy < 100) {
			
			print.println("Warning! Server is 80% full.");
			
		} else if(occupancy >= 100) {
			
			print.println("Server is full. Deleting all messages...");
			
			this.flush();
			
		}
		
	}
	
	/**
	 * Deletes all messages in the server.
	 */
	public void flush() {
		
		while(!msgs.isEmpty()) {
			msgs.remove();
		}
		
		this.currentSize = 0;
	}
	
	/**
	 * Adds a new message to server and increases the current size by body 
	 * length of this message.
	 * 
	 * @param message Message that is wanted to be added to the server.
	 */
	public void addMessage(Message message){
		
		msgs.add(message);
		currentSize += message.getBody().length();
		
	}
	/**
	 * Removes a new message from server and decreases the current size by body 
	 * length of this message.
	 * 
	 * @param message Message that is wanted to be removed from the server.
	 */
	public void removeMessage(Message message) {
		
		if(msgs.contains(message)) {
			msgs.remove(message);
			currentSize -= message.getBody().length();
		}
		
	}
	
	/**
	 * Investigates the current load situation of the server.
	 * 
	 * @param load Current load of the server.
	 * @return 0 if load is less than 50% exclusive, 1 if it is between 50% inclusive 
	 * 		   and 80% exclusive, 2 if it is between 80% inclusive and 1000% exclusive and 3
	 * 		   otherwise.
	 */
	public int loadSituation(double load) {
		
		if(load < 50)
			return 0;
		else if(50 <= load && load < 80)
			return 1;
		else if(80 <= load && load < 100)
			return 2;
		else
			return 3;
		
	}
	
	/**
	 * Calculates the load percentage of the server's storage.
	 * 
	 * @return Current load of the server in percentage.
	 */
	public double load() {		
		return (currentSize / (double)capacity) * 100;	
	}
	
	/**
	 * Returns current size of the server.
	 * @return Current size of the server.
	 */
	public long getCurrentSize() {
		return currentSize;
	}
	
	/**
	 * Returns all messages in the server currently.
	 * @return All messages in the server in a FCFS manner.
	 */
	public Queue<Message> getMsgs() {
		return msgs;
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

