
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package executable;

import elements.*;
import java.io.*;
import java.util.*;
/**
 * Main class of the messaging system.
 * 
 * @author Halil Burak Pala
 *
 */
public class Main{
	/**
	 * Main method of the main class of the messaging system.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String args[]) throws FileNotFoundException {
		
		Scanner input = new Scanner(new File(args[0]));
		PrintStream output = new PrintStream(new File(args[1]));
		
		int nofUsers = input.nextInt(); //
		int nofQueries = input.nextInt();
		int capacity = input.nextInt();
		
		ArrayList<User> users = new ArrayList<User>(); // Stores all users in an ArrayList in order of 
													   // their ID numbers.
		
		int time = 0;
		
		input.nextLine();
		
		Server server = new Server(capacity);
		
		int prevLoadSituation = 0; // This field will be used later for printing warning messages in 
								   // an appropriate manner.
		
		
		for(int i = 0 ; i < nofUsers ; i++) {
			
			users.add(new User(i));
			
		}
		
		for(int i = 0 ; i < nofQueries ; i++) {
			
			String line = input.nextLine();
			Scanner scan = new Scanner(line);
			
			int query = scan.nextInt();
			
			/*
			 * If query is 0, User with following ID number will send a message to other user with
			 * following ID number with given message body.
			 * 
			 * 0 <sender_id> <receiver_id> <message_body>
			 * 
			 * e.g. 0 1 4 Hi there!
			 * User#1 sends user#4 the message “Hi there!”.
			 */
			if(query == 0) {
				
				int senderId = scan.nextInt();
				int receiverId = scan.nextInt();
				String body = scan.next(); // Takes the body's first word.
				
				while(scan.hasNextLine()) {
					body = body + scan.nextLine(); // Takes the remaining words with spaces between
												   // them if there are.
				}
				
				User sender = users.get(senderId);
				User receiver = users.get(receiverId);
				
				sender.sendMessage(receiver, body, time, server);
				
				time++;
			}
			
			/*
			 * If query is 1, User with following ID number will receive all messages that are sent to
			 * her/him from server.
			 * 
			 * 1 <reciever_id>
			 * 
			 * e.g. 1 25
			 * User#25 receives all the messages that are sent to him/her from the server.
			 */
			else if(query == 1) {
				
				int receiverId = scan.nextInt();
				
				User receiver = users.get(receiverId);
				
				receiver.getInbox().receiveMessages(server, time);
				
				time++;
			}
			
			/*
			 * If query is 2, User with following ID number will read following number of messages from
			 * him/her inbox's unread stack. If the number of messages is 0, s/he will read all messages.
			 * 
			 * 2 <receiver_id> <numberOfMessages>
			 * 
			 */
			else if(query == 2) {
				 
				int receiverId = scan.nextInt();
				int nofMessages = scan.nextInt();
				
				User receiver = users.get(receiverId);
				
				int timePassed = receiver.getInbox().readMessages(nofMessages, time);
				
				time += timePassed;
			}
			
			/*
			 * If query is 21, User with following ID number will read all messages that are sent by the
			 * user with following ID number.
			 * 
			 * 21 <receiver_id> <sender_id>
			 * 
			 */
			else if(query == 21) {
				
				int receiverId = scan.nextInt();
				int senderId = scan.nextInt();
				
				User receiver = users.get(receiverId);
				User sender = users.get(senderId);
				
				int timePassed = receiver.getInbox().readMessages(sender, time);
				
				time += timePassed;
				
			}
			
			/*
			 * If query is 22, User with following ID number will read the message with following ID 
			 * number.
			 * 
			 * 22 <receiver_id> <message_id>
			 * 
			 */
			else if(query == 22) {
				
				int receiverId = scan.nextInt();
				int messageId = scan.nextInt();
				
				User receiver = users.get(receiverId);
				
				receiver.getInbox().readMessage(messageId, time);
				
				time++;
			}
			
			/*
			 * If query is 3, users with following ID numbers will add each other to their friends list
			 * if they are not friends. Otherwise nothing will happen. 
			 * 
			 * 3 <id1> <id2>
			 * 
			 */
			else if(query == 3) {
				
				int id1 = scan.nextInt();
				int id2 = scan.nextInt();
				
				User user1 = users.get(id1);
				User user2 = users.get(id2);
				
				user1.addFriend(user2);
				
				time++;
			}
			
			/*
			 * If query is 4, users with following ID numbers will remove each other from their friends
			 * list if they are friends. Otherwise nothing will happen
			 * 
			 * 4 <id1> <id2>
			 * 
			 */
			else if(query == 4) {
				
				int id1 = scan.nextInt();
				int id2 = scan.nextInt();
				
				User user1 = users.get(id1);
				User user2 = users.get(id2);
				
				user1.removeFriend(user2);
				
				time++;
			}
			
			/*
			 * If query is 5, all messages in the server will be flushed.
			 */
			else if(query == 5) {
				
				server.flush();
				
				time++;
			}
			
			/*
			 * If query is 6, current load of the server will be printed.
			 */
			else if(query == 6) {
				
				output.println("Current load of the server is " + server.getCurrentSize() + " characters.");
				
				time++;
			}
			
			/*
			 * If query is 61, last message of the user with following ID number has read will be
			 * printed. 
			 * 
			 *  61 <user_id>
			 */
			else if(query == 61) {
				
				int userId = scan.nextInt();
				
				User user = users.get(userId);
				
				Stack<Message> messages = new Stack<Message>(); // To get the last read message, I convert
																// FCFS manner of messages to LIFO manner.
				
				messages.addAll(user.getInbox().getRead());
				
				if(!user.getInbox().getRead().isEmpty()) {
					
					output.println(messages.peek());
					
				}
				
				time++;
			}
			
			/*
			 * To print the warning messages of the server in an appropriate manner, previous load
			 * situation -which is checked in the Server class- should be changed. To check this, I 
			 * will compare the previous and current load situations. If they are not the same, I will
			 * print the warning message. 
			 */
			int currentLoadSituation = server.loadSituation(server.load());
			
			if(currentLoadSituation != prevLoadSituation) {
				
				server.checkServerLoad(output);
				
			}
			
			prevLoadSituation = currentLoadSituation;
			
			scan.close();
		}
		
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

