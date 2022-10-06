
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package question;
import java.util.*;

/**
 * Represents each of the nodes in the DNS tree structure.
 * 
 * @author Halil Burak Pala
 *
 */
public class DnsNode{
	
	String wholeName; // This field represents the whole domain name
					  // of a valid node like "mail.google.com". It is
					  // used in getting all records of valid domains.
	Map<String, DnsNode> childNodeList;
	boolean validDomain;
	Set<String> ipAddresses;
	
	/**
	 * Creates a new DnsNode. Initializes its child node list 
	 * to an empty map and set of IP addresses list to an empty set.
	 * Initializes its validity as false.
	 */
	public DnsNode() {
		wholeName = "";
		childNodeList = new LinkedHashMap<String, DnsNode>();
		validDomain = false;
		ipAddresses = new LinkedHashSet<String>();
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

