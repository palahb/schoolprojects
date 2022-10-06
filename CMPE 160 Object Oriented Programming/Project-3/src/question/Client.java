
//DO_NOT_EDIT_ANYTHING_ABOVE_THIS_LINE
package question;
/**
 * Represents a client of the Domain Name System.
 * 
 * @author Halil Burak Pala
 *
 */
public class Client{
	
	/**
	 * Represents a cached domain in the client's caching list.
	 * 
	 * @author Halil Burak Pala
	 *
	 */
	private class CachedContent{
		
		String domainName;
		String ipAddress;
		int hitNo;
		
		/**
		 * Constructs a new cached domain with specified domain name and 
		 * IP addres which will be stored in the client's cached content 
		 * list if it is available in the DNS tree. Initializes its number 
		 * of hits to zero.
		 * 
		 * @param domainName Name of the cached content
		 * @param ipAddress IP Addres of the cached content
		 */
		public CachedContent(String domainName, String ipAddress) {
			this.domainName = domainName;
			this.ipAddress = ipAddress;
			hitNo = 0;
		}
		
	}
	
	DnsTree root;
	String ipAddress;
	CachedContent[] cacheList;
	
	/**
	 * Constructs a new client with specified ipAddress and DNS Tree. Creates a
	 * new cache list that can store 10 different domain-IP information.
	 * 
	 * @param ipAddress IP Address of the client
	 * @param root DNS Tree of the user by which s/he posseses necessary
	 * 			   informations of web addresses.
	 */
	public Client(String ipAddress, DnsTree root){
		
		this.root = root;
		this.ipAddress = ipAddress;
		this.cacheList = new Client.CachedContent[10];
		
	}
	
	/**
	 * Returns the IP address of the requested domain name. If it is available in 
	 * the cache, it directly returns the corresponding IP address in the cache record. 
	 * If there are multiple records belonging to the same domain name, the one with 
	 * the minimum index is returned. The hit number of the content is incremented by 
	 * 1 if it is fetched from the cache. If it is not available in the cache, then a 
	 * request is sent to the DNS. If the requested domain has multiple IP addresses, 
	 * it returns an IP address according to Round Robin algorithm. If cannot find
	 * the requested domain, returns null.
	 * 
	 * @param domainName Name of the requested domain.
	 * @return The IP address of the requested domain name if it is reachable. O/w null.
	 */
	public String sendRequest(String domainName) {
		
		for(int i = 0 ; i < cacheList.length ; i++) {
			
			if(cacheList[i] == null) { // If cacheList is not full and requested cache is 
									   // not in the array it stops searching the array and 
				break;				   // adds the cache to the end of the array.
				
			}
			
			if(cacheList[i].domainName.equals(domainName)) { // If requested domain is in the
															 // cacheList, increments the hitNo
				cacheList[i].hitNo++;						 // of the cache and returns its
															 // corresponding IP address.
				return cacheList[i].ipAddress;
			}
			
		}
		
		if(root.getAllRecords().containsKey(domainName)) {		// If requested domain is not 
																// in the cacheList sends request
			String newIpAddress = root.queryDomain(domainName); // to the DNS tree. 
			
			this.addToCache(domainName, newIpAddress);
			
			return newIpAddress;
			
		}
		else {
			
			return null;
			
		}
		
		
	}
	
	/**
	 * Adds the domain with specified name and IP address to the cache list of the client. If
	 * cache list is full, removes the least used cache from array and adds given domain to 
	 * cache list.
	 * 
	 * @param domainName Name of the domain.
	 * @param ipAddress IP address of the domain.
	 */
	public void addToCache(String domainName, String ipAddress) {
		
		CachedContent cache = new CachedContent(domainName, ipAddress);
		
		boolean isNotFull = false; 

		for(int i = 0; i < cacheList.length; i++){ // If the cache list is not full It should
		    if(cacheList[i] == null){			   // include a null element. If found a null
		        isNotFull = true;				   // element, it means that cache list is not
		    }									   // full.
		}
		
		if(isNotFull == false) {				// If the cache list is full, finds the least
												// used cache and replaces it with given domain.
			CachedContent min = cacheList[0];
			int minIndex = 0;
			
			for(int i = 1 ; i < 10 ; i++) {
				
				if(cacheList[i].hitNo < min.hitNo) {
					min = cacheList[i];
					minIndex = i;
				}
				
			}
			
			cacheList[minIndex] = cache;
			
		} 
		else { 					// If the cache list is not full finds the first null
								// element and adds given domain to its place.
			int firstNull = 0;
			
			for(int i = 0 ; i < 10 ; i++) {
				
				if(cacheList[i] == null) {
					firstNull = i;
					break;
				}
				
			}
			cacheList[firstNull] = cache;
			
		}
	}
}


//DO_NOT_EDIT_ANYTHING_BELOW_THIS_LINE

