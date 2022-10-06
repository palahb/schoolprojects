
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package boxes;

import java.util.*;
import elements.*;

/**
 * Outbox stores all messages a user has sent. All sent messages are stored
 * in a queue.
 * *
 * @author Halil Burak Pala
 */
public class Outbox extends Box{
	
	Queue<Message> sent;
	
	/**
	 * Constructs a new outbox for the specified owner. Initializes the sent queue to a new empty
	 * LinkedList.
	 * @param owner Owner of the outbox.
	 */
	public Outbox(User owner) {
		super.owner = owner;
		sent = new LinkedList<Message>();
	}
	
	/**
	 * Returns messages that are stored in the outbox.
	 * @return All stored messages in this outbox.
	 */
	public Queue<Message> getSent(){
		return this.sent;
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

