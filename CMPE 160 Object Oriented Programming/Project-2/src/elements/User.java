
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package elements;

import boxes.*;
import java.util.*;
/**
 * User in this messaging system. A user can send and receive messages. S/he have an inbox, an outbox,
 * a friends list and can add friends to and remove friends from this list.
 * 
 * @author Halil Burak Pala
 *
 */
public class User{
	
	private int id;
	private Inbox inbox;
	private Outbox outbox;
	private ArrayList<User> friends;
	
	/**
	 * Creates a User with specified ID number. Initializes her/his inbox to a new empty inbox,
	 * outbox to a new empty outbox and friends list to a new empty list.
	 * 
	 * @param id ID number of the user.
	 */
	public User(int id) {
		this.id = id;
		this.inbox = new Inbox(this);
		this.outbox = new Outbox(this);
		this.friends = new ArrayList<User>();
	}
	
	/**
	 * Adds the other user to friends list of the user if they are not friends at the moment. 
	 * Also adds user to other user's friends list in the same condition.
	 * 
	 * @param other User wanted to be added to the friends list.
	 */
	public void addFriend(User other) {
		
		if(!this.isFriendsWith(other)) {
			
			this.friends.add(other);
			other.friends.add(this);
			
		}
		
	}
	
	/**
	 * Removes the other user from friends list of the user if they are friends at the moment. 
	 * Also removes user from other user's friends list in the same condition.
	 * 
	 * @param other User wanted to be removed from the friends list.
	 */
	public void removeFriend(User other) {
		
		if(this.isFriendsWith(other)) {
			
			this.friends.remove(other);
			other.friends.remove(this);
			
		}
		
	}
	
	/**
	 * Checks whether the other user is in the friends list of the user.
	 * @param other Other user
	 * @return Whether users are friends or not.
	 */
	public boolean isFriendsWith(User other) {
		return this.friends.contains(other); 
	}
	
	/**
	 * Creates and sends a new message with given message body. Adds the created message to user's
	 * outbox.
	 * 
	 * @param receiver Receiver of the message
	 * @param body Message body
	 * @param time Time stamp of sending
	 * @param server Server where message is sent to
	 */
	public void sendMessage(User receiver, String body, int time, Server server) {
		
		Message message = new Message(this, receiver, body, server, time);
	
		this.outbox.getSent().add(message);
		
		server.addMessage(message);
		
	}

	/**
	 * Returns ID of the user.
	 * @return ID number of the user.
	 */
	protected int getId() {
		return id;
	}

	/**
	 * Returns inbox of the user.
	 * @return Inbox of the user.
	 */
	public Inbox getInbox() {
		return inbox;
	}

}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

