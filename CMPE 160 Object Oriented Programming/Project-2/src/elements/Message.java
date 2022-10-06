
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package elements;

/**
 * Message in this messaging system. 
 * 
 * @author Halil Burak Pala
 *
 */
public class Message implements Comparable<Message>{
	
	private static int numOfMessages = 0; // This static field keeps track of number of messages
										  // created in this messaging system. It is used for 
										  // giving each message a specific ID.
	private int id;
	private String body;
	private User sender;
	private User receiver;
	private int timeStampSent, timeStampRead, timeStampReceived;
	
	/**
	 * Creates a new message with specified sender, receiver, message body, server and
	 * time stamp of sending, gives each message a specific ID.
	 * @param sender Sender of the message
	 * @param receiver Receiver of the message
	 * @param body Message body
	 * @param server Server that messages are sent to
	 * @param time Sending time of the message
	 */
	public Message(User sender, User receiver, String body, Server server, int time) {
		this.sender = sender;
		this.receiver = receiver;
		this.body = body;
		this.timeStampSent = time;
		this.id = numOfMessages;
		numOfMessages++;
	}
	
	/**
	 * Returns the ID of the message.
	 * 
	 * @return ID of the message
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the message body.
	 * @return Message body
	 */
	protected String getBody() {
		return body;
	}
	
	/**
	 * Returns the sender of the message.
	 * @return Sender of the message
	 */
	public User getSender() {
		return sender;
	}

	/**
	 * Returns the receiver of the message.
	 * @return Receiver of the message.
	 */
	public User getReceiver() {
		return receiver;
	}
	
	/**
	 * Sets time stamp of reading to given value.
	 * @param timeStampRead 
	 */
	public void setTimeStampRead(int timeStampRead) {
		this.timeStampRead = timeStampRead;
	}
	
	/**
	 * Sets time stamp of receiving to given value.
	 * @param timeStampReceived
	 */
	public void setTimeStampReceived(int timeStampReceived) {
		this.timeStampReceived = timeStampReceived;
	}
	
	/**
	 * Compares messages regarding their body length.
	 */
	public int compareTo(Message other) {
		if(this.body.length() > other.getBody().length())
			return 1;
		else if(this.body.length() < other.getBody().length())
			return -1;
		else
			return 0;
	}
	
	/**
	 * Looks for equality between messages regarding their ID number.
	 * @param other Other message.
	 * @return Whether other message is the same with the message.
	 */
	public boolean equals(Message other) {
		return this.id == other.getId();
	}
	
	@Override
	public String toString() {
		String line1 = "\tFrom: " + sender.getId() + " to: " + receiver.getId();
		String line2 = "\tReceived: " + timeStampReceived + " Read: " + timeStampRead;
		return line1 + "\n" + line2 + "\n\t" + body;
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

