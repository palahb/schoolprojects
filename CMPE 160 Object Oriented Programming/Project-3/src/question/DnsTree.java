
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package question;
import java.util.*;

/**
 * Represents the main DNS structure which is in a tree form.
 * 
 * @author Halil Burak Pala
 *
 */
public class DnsTree{
	
	DnsNode root;
	
	/**
	 * Creates a new DNS tree and initializes its root as null.
	 */
	public DnsTree(){
		root = null;
	}
	
	/**
	 * Inserts a new record for a given domain name. If a corresponding node 
	 * is not available in the tree, it is created and validity of the node 
	 * is marked as true. Otherwise, the IP address list of the node is updated 
	 * with this method.
	 * 
	 * @param domainName Name of the domain which is intended to be inserted.
	 * @param ipAddress IP address of the domain which is intended to be inserted.
	 */
	public void insertRecord(String domainName, String ipAddress) {
		
		if(root == null) {
			
			root = new DnsNode();
			
		}
		
		insertRecord(root, domainName, ipAddress);
		
	}
	
	/**
	 * An auxiliary method for actual insertion method. Inserts a given domain in a tree manner.
	 * (e.g. to insert "boun.edu.tr" domain, first creates a node for "tr" and a node for 
	 * "edu" as a branch of "tr" and then a node for "boun" as a branch of "edu")
	 * 
	 * @param node Where the node is inserted as a branch
	 * @param domainName Name of the domain to be inserted
	 * @param ipAddress IP address of the domain to be inserted
	 */
	private void insertRecord(DnsNode node, String domainName, String ipAddress) {
		
		Stack<String> domains = this.domainsStack(domainName);
		
		int size = domains.size(), i = 0;
		
		while(!domains.isEmpty()) {
			
			String domain = domains.pop();
			
			node = insertNode(node, domain, domainName, ipAddress, i, size-1);
			
			i++;
		}
		
		
	}
	
	/**
	 * An auxiliary method for actual insertion method. Inserts a node with given name and
	 * IP address to the child nodes list of given node. If the node which is intended to 
	 * insert is already there, adds the given IP address to its IP address list. Returns 
	 * the inserted node.
	 * 
	 * @param node Parent node of the node to be intended to insert
	 * @param domainName Name of the element of the domain to be intended to insert
	 * @param wholeName Whole domain name of the domain to be intended to insert
	 * @param ipAddress IP address of the domain to be intended to insert
	 * @param currentDepth Current depth of the node
	 * @param maxDepth Depth of the node where it should be
	 * @return Inserted node
	 */
	private DnsNode insertNode(DnsNode node, String domainName, String wholeName, String ipAddress, int currentDepth, int maxDepth) {
		
		if(!node.childNodeList.containsKey(domainName)) { // If the domain with given name is not present, creates
														  // a node with given name.
			DnsNode inserted = new DnsNode();
			
			if(currentDepth == maxDepth) {	// If the current depth is where the node should be, it means that node
											// is a valid one. Fixes its validity, adds given IP to its IP addresses
				inserted.validDomain = true;// list and gives it its whole name.
				inserted.ipAddresses.add(ipAddress);
				inserted.wholeName = wholeName;
				
			}
			
			node.childNodeList.put(domainName, inserted);
			
			return inserted;	// Returns the inserted node.
		}
		
		else {	// If the domain is already present, fixes its validity and adds given IP to its IP addresses list.
			
			if(currentDepth == maxDepth) {
				node.childNodeList.get(domainName).validDomain = true;
				node.childNodeList.get(domainName).ipAddresses.add(ipAddress);
				node.childNodeList.get(domainName).wholeName = wholeName;
			}
			
			return node.childNodeList.get(domainName);	// Returns the updated node.
			
		}
		
		
	}
	
	/**
 	 * Removes the given IP address of a DNS node with the given domainName. 
	 * If this node is a leaf node and has a single IP address which is removed 
	 * removes the node also. If the node is successfully removed, returns true, 
	 * otherwise, returns false.
	 * 
	 * @param domainName Name of the domain which is intended to be removed
	 * @param ipAddress IP address of the domain which is intended to be removed
	 * @return Whether the domain is successfully removed
	 */
	public boolean removeRecord(String domainName, String ipAddress) {

		Stack<String> domains = this.domainsStack(domainName);
		
		int depth = domains.size();
		DnsNode node = root;
		
		for(int i = 1 ; i < depth ; i++) { 					// First, finds the intended to be
															// removed IP's node. 
			String domain = domains.pop();
			
			if(node.childNodeList.containsKey(domain)) {
				
				node = node.childNodeList.get(domain);
				
			} else {			// If there is no such a node, returns false.
				
				return false;
				
			}
			
		}
		
		String domain = domains.pop();
		
		if(node.childNodeList.containsKey(domain)) {
			
			DnsNode parentNode = node;
			node = node.childNodeList.get(domain);
			
			if(node.ipAddresses.size() != 1 && node.ipAddresses.contains(ipAddress)) {  // If node does not have only a single
																						// IP and requested to be removed IP
				node.ipAddresses.remove(ipAddress);										// belongs to this node, removes the
																						// IP address and returns true.
				return true;
			} 
			else if(node.ipAddresses.size() == 1 && node.ipAddresses.contains(ipAddress)) {	// If node has only one IP address and 
																							// this IP is requested to be removed,
				node.ipAddresses.remove(ipAddress);											// IP is removed and validity of the node
				node.validDomain = false;													// is changed to false.
				
				if(node.childNodeList.size() == 0) {										// If the node is a leaf, the whole node is
																							// removed.
					parentNode.childNodeList.remove(domain);
					
				}

				return true;
			}
			else {				//	If IP does not belong to the node, returns false.
				return false; 
			}
		}
		else {				// If there is no such a node, returns false.
			return false;
		}
		
	}
	
	/**
	 * An auxiliary method for actual IP removal method. Finds the node at the given depth and
	 * then checks if given IP address is in this node's IP addresses list and if it is present
	 * removes it. If the node has only single IP address and it is removed, removes the node
	 * also. Returns true if the IP address is successfully removed.
	 * 
	 * @param node Starting node to find requested node
	 * @param domains Stack of the domain's elements
	 * @param ipAddress IP address to be removed
	 * @return True if the IP address is successfully removed
	 */
	
	
	/**
	 * Removes the node with the given domainName from the tree. If successfully 
	 * removed, returns true, o/w returns false.
	 * 
	 * @param domainName Name of the domain which is intended to be removed.
	 * @return Whether the domain is successfully removed.
	 */
	public boolean removeRecord(String domainName) {
		
		Stack<String> domains = this.domainsStack(domainName);
		
		int depth = domains.size();
		DnsNode node = root;
		
		for(int i = 0 ; i < depth-1 ; i++) {				// First finds the intended to be removed
															// node.
			String domain = domains.pop();
			
			if(node.childNodeList.containsKey(domain)) {
				
				node = node.childNodeList.get(domain);
				
			} else {			// If there is no such a node, returns false.
				
				return false;
				
			}
			
		}
		
		String domain = domains.pop();
		
		if(node.childNodeList.containsKey(domain)) {		// If the node is present, removes all IP
															// addresses of it. Changes its validity to 
			DnsNode parentNode = node;						// false.
			
			node = node.childNodeList.get(domain);
			
			node.ipAddresses.removeAll(node.ipAddresses);
			
			node.validDomain = false;
			
			if(node.childNodeList.size() == 0) {			// If the node does not have any child, the
															// whole node is removed. Returns true since
				parentNode.childNodeList.remove(domain);	// the node is successfully removed.
				
				return true;
				
			}
			
			return false;	// If it has children nodes, it cannot be removed. Returns false.
		}
		else {	// If there is no such a node, returns false.
			
			return false;
			
		}
	}
		
	
	/**
	 * It queries a domain name within the DNS, and returns the next IP address of 
	 * the domainName, following the Round Robin mechanism. If there is no such domain 
	 * name available in the tree, returns null.
	 * 
	 * @param domainName Name of the queried domain
	 * @return IP address of the domain which is obtained by Round Robin algorithm. If there
	 * is no such IP address, null.
	 */
	public String queryDomain(String domainName) {
		
		Stack<String> domains = this.domainsStack(domainName);
		
		int depth = domains.size();
		DnsNode node = root;
		
		for(int i = 0 ; i < depth-1 ; i++) {				// First, finds the node by searching parent nodes
															// step by step.
			String domain = domains.pop();
			
			if(node.childNodeList.containsKey(domain)) {
				
				node = node.childNodeList.get(domain);
				
			} else {			// If it couldn't find its any parent node, returns null.
				
				return null;
				
			}
			
		}
		
		String domain = domains.pop();
		
		if(node.childNodeList.containsKey(domain)) {

			node = node.childNodeList.get(domain);
			
			if(node.ipAddresses.isEmpty())	// If the node does not have any IP returns null.
				return null;
			 
			Iterator<String> itr = node.ipAddresses.iterator();
			
			String ip = itr.next();	// Takes the first IP in the IP addresses set.
			
			itr.remove();	// Removes it from the IP addresses set.
			
			node.ipAddresses.add(ip);	// Adds it again to the set. Since the set is a LinkedHashSet, it stores elements
										// in the order of insertion.
			return ip;	// Returns the IP.
			
			
		} else {		// If it couldn't find the requested node, returns null.		
			
			return null;
			
		}
		
	}
	
	/**
	 * Returns all the valid domain names in the DNS mechanism with at least 1 IP address. 
	 * The return type is a Map object whose keys represent the valid domain names, and 
	 * value is the set of IP addresses of a particular key (domain name).
	 * 
	 * @return All the valid domain names with at least 1 IP address.
	 */
	public Map<String, Set<String>> getAllRecords(){
		
		if(root == null || root.childNodeList == null) {
			
			return null;
			
		}
		
		Map<String, Set<String>> map =  new LinkedHashMap<String, Set<String>>();
		
		getAllRecords(root.childNodeList, map);
		
		return map;
		
	}
	
	/**
	 * An auxiliary method for actual getAllRecords method. Puts all valid domains with at least
	 * one IP address in the given String to DnsNode map to other given String to Set of Strings map.
	 * 
	 * @param nodeMap 
	 * @param allRecords
	 */
	private void getAllRecords(Map<String, DnsNode> nodeMap, Map<String, Set<String>> allRecords){
		
		Iterator<DnsNode> domainNodes = nodeMap.values().iterator();	// Investigates all nodes in the nodeMap.
		
		while(domainNodes.hasNext()) {
			
			DnsNode node = domainNodes.next();
			
			if(node.validDomain == true) {							// If the node is a valid node, puts it and
																	// its IPs to the records map.
				allRecords.put(node.wholeName, node.ipAddresses);
				
			}
			
			if(node.childNodeList != null) {					// If node has children nodes, recursively does
																// the same operations for its children.
				getAllRecords(node.childNodeList, allRecords);
				
			}
			else {
				return;
			}
			
		}
		
	}
	
	/**
	 * Divides the domain name to its elements (e.g. boun.edu.tr --> [tr, edu, boun]))
	 * and puts them into a stack.
	 * 
	 * @param domainName Name of the domain which is to be divided to its elements.
	 * @return Stack of the elements.
	 */
	public Stack<String> domainsStack(String domainName){
		
		Scanner scan = new Scanner(domainName);
		scan.useDelimiter("\\.");
		
		Stack<String> domains = new Stack<String>();
		
		while(scan.hasNext()) {
			
			domains.push(scan.next());
			
		}
		
		scan.close();
		
		return domains;
	}
	
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

