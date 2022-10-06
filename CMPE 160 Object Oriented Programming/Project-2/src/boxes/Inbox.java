
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package boxes;

import java.util.*;
import elements.*;

/**
 * Inbox is where the user's read and unread messages are stored. Unread messages are stored in a stack
 * and read messages are stored in a queue. An inbox has two main functionalities: receiving messages 
 * from the server, and reading messages.
 * 
 * @author Halil Burak Pala
 */

public class Inbox extends Box{
	
	Stack<Message> unread;
	Queue<Message> read;
	
	/**
	 * Constructs a new Inbox for the specified owner. Initializes unread stack to a new empty stack and 
	 * read Queue to a new empty LinkedList.
	 * 
	 * @param user Owner of the Inbox.
	 */
	
	public Inbox(User user) {
		
		super.owner = user;
		
		unread = new Stack<Message>();
		read = new LinkedList<Message>();
		
	}
	
	/**
	 * Receives messages from the server, adds to the unread stack. 
	 * Changes stamp of receiving time with the parameter time.
	 * 
	 * @param server Server where messages are stored.
	 * @param time Receiving time.
	 */
	
	public void receiveMessages(Server server, int time) {
		
		Queue<Message> messages = new LinkedList<Message>();
		messages.addAll(server.getMsgs());// To investigate all messages in the server, I add 
										  // all messages in the server to a new Queue "messages".
		
		while(!messages.isEmpty()) {
			
			Message message = messages.remove();
			
			// If the receiver of the investigated message is owner of the inbox and sender of the message
			// is a friend of him/her, removes the message from server, changes its received time stamp 
			// and pushes it to unread stack. 
			if(message.getReceiver().equals(this.owner) && message.getSender().isFriendsWith(owner)) {
				
				server.removeMessage(message);
				message.setTimeStampReceived(time);
				unread.push(message);
				
			}
		}
		
	}
	
	/**
	 * Reads a certain amount of message from the unread stack of the inbox. Changes time 
	 * stamp of reading of every message by first changing the first message by given time and 
	 * then changing each message's time stamp of reading by ascending previous time stamp by 1. 
	 * If the ​num parameter is 0, then all messages in ​unread are read. If the number of 
	 * messages in ​unread​ is less than read, still all messages are read. 
	 * 
	 * @param num Number of messages that are wanted to be read.
	 * @param time Starting time of reading.
	 * @return Number of read messages. If no message is read, it is 1.
	 */
	
	public int readMessages(int num, int time) {
		
		int noUnread = unread.size();
		int noReadMessages = 0;
		int clk = time;
		
		// If the given number is 0 or less than the actual unread message number, reads all
		// unread messages.
		if(num == 0 || noUnread < num) {
			
			while(!unread.isEmpty()) {
				
				Message message = unread.pop();
				message.setTimeStampRead(clk);
				read.add(message);
				
				clk++;
				noReadMessages++;
			}
			
		}
		
		else {
			
			for(int i  = 0 ; i < num ; i++) {
				
				Message message = unread.pop();
				message.setTimeStampRead(clk);
				read.add(message);
				
				clk++;
				noReadMessages++;
			}
			
		}
		
		//If no message is read, changes number of read messages from 0 to 1.
		if(noReadMessages == 0)
			noReadMessages = 1;
		
		return noReadMessages;
	}
	
	/**
	 * Reads the messages of certain sender from the unread stack of the inbox. Changes
	 * time stamp of reading of every message by first changing the first message by given time and 
	 * then changing each message's time stamp of reading by ascending previous time stamp by 1. 
	 * If the ​num parameter is 0, then all messages in ​unread are read. If the number of 
	 * messages in ​unread​ is less than read, still all messages are read.
	 * 
	 * @param sender Sender of messages whose messages are wanted to be read.
	 * @param time Starting time of reading.
	 * @return Number of read messages. If no message is read, it is 1.
	 */
	
	public int readMessages(User sender, int time) {
		
		int noReadMessages = 0;
		int clk = time;
		Stack<Message> messages = new Stack<Message>();
		messages.addAll(unread);// To investigate all messages in the unread stack, I add 
								// all messages in the unread stack to a new Queue "messages".
		
		while(!messages.isEmpty()) {
			
			Message message = messages.pop();
			
			// If the sender of the message is the same with specified sender, removes message 
			// from unread stack and adds to read Queue.
			if(message.getSender().equals(sender)) {
				
				unread.remove(message);
				message.setTimeStampRead(clk);
				read.add(message);
				
				noReadMessages++;
				clk++;
				
			}
			
		}
		
		if(noReadMessages == 0)
			noReadMessages = 1;
		
		return noReadMessages;
	}
	
	/**
	 * Reads a specific message from unread stack and changes time stamp of reading by given time.
	 *  
	 * @param msgId ID of the message which is wanted to be read.
	 * @param time Time at which the message is read.
	 */
	public void readMessage(int msgId, int time) {
		
		Stack<Message> messages = new Stack<Message>();
		messages.addAll(unread);// To investigate all messages in the unread stack, I add 
								// all messages in the unread stack to a new Queue "messages".
		
		while(!messages.isEmpty()) {
			
			Message message = messages.pop();
			
			if(message.getId() == msgId) {
				unread.remove(message);
				message.setTimeStampRead(time);
				read.add(message);
			}
		}
	}

	
	/**
	 * Returns read queue.
	 * @return Read queue of the inbox.
	 */
	public Queue<Message> getRead() {
		return read;
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

