package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.util.Log;

/**
 * POJO Node class which has the DHT node properties
 * and methods 
 * @author Babu Prasad
 *
 */
public class Node {
	private String nodeID;
	private String deviceID;
	private String emulatorPort;
	
	private String prevNodeID;
	private String nextNodeID;
	private String prevDeviceID;
	private String nextDeviceID;
	
	private String[] quorumReplicas;
	private String[] parentNodes;
	
	// Statically declaring membership details as every node knows about each other
	// node in the network
	private static String[] membership = {"5562", "5556", "5554", "5558", "5560"};
	
	/**
	 * Singleton Instance as we need only one Node object per App/Device
	 */
	private static Node nodeInstance;
	
	
	private Node() {
		// Private constructor to defeat instantiation
		quorumReplicas = new String[2];
		parentNodes = new String[2];
	}
	
	/**
	 * Initialize Singleton Node instance
	 * @return Singleton Node Instance
	 */
	public static Node initNodeInstance(String emulatorPort)
	{
		Node node = null;		
		try {				
			String deviceID = DeviceInfo.getDeviceName(emulatorPort);
			if(deviceID.compareTo("InvalidPortNo") == 0)
				throw new Exception("Node Initailization failed -- "+deviceID);
			
			node = new Node();
			node.emulatorPort = emulatorPort;
			node.deviceID = deviceID;				
							 
			node.nodeID =  genHash(deviceID);
			
			
			// As the hash value decreases as the device id increases, highest device id becomes the lowest hash 
			// value. Based on this storing the successor 2 nodes as quorum replicas
			if(node.findNodeInMembership(deviceID) != -1)
			{
				node.setQuorumReplicas(node.getQuorumReplicas(node.findNodeInMembership(deviceID)));
				node.setParentNodes(node.getParentNodes(node.findNodeInMembership(deviceID)));
			}
			else
				Log.e("Node","Node not present in membership");
			
			int prevIndex = (node.findNodeInMembership(deviceID) == 0)? (membership.length - 1) : (node.findNodeInMembership(deviceID) - 1);
			node.prevDeviceID =  membership[prevIndex];
			node.nextDeviceID = node.quorumReplicas[0];
			
			node.prevNodeID =  genHash(node.prevDeviceID);
			node.nextNodeID =  genHash(node.nextDeviceID);
			
		} 
		catch (NoSuchAlgorithmException e) {
			Log.e("Node", "Node instance creation failed : "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("Node", "Node instance creation failed : "+e.getMessage());
			e.printStackTrace();
		}
		
		if(nodeInstance == null)
			nodeInstance = node;
		return node;			
	}
	
	/**
	 * To check whether the node is initialized or not
	 */
	public static boolean isNodeInitialized()
	{
		if(nodeInstance != null)
			return true;
		return false;
	}
	
	/**
	 * Get Singleton Node instance
	 * @return Node Instance
	 * @throws Exception - No Node initialization
	 */
	public static Node getInstance() throws Exception
	{
		/* Make sure that initNodeInstance is called before getInstance*/
		if(nodeInstance == null)		
			throw new Exception("No Node initialization");
		
		return nodeInstance;
	}
	
	/**
	 * Get Singleton Node instance for the device id specified
	 * @return Node Instance
	 * @throws Exception - No Node initialization
	 */
	public static Node getInstance(String deviceID) throws Exception
	{
		// Initiate the node object with the device id passed and return
		if(nodeInstance == null)		
			throw new Exception("Custom Error : Node instance not initialized.");
		
		if(nodeInstance.deviceID.compareTo(deviceID) != 0)
			return Node.initNodeInstance(DeviceInfo.getDevicePortNo(deviceID));
		else
			return nodeInstance;		
		
	}

	/********************************************************************
	 * Node ID Helper Functions - Babu
	 *******************************************************************/
	
	/**
	 * @return the nodeID
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	/**
	 * @return the emulatorPort
	 */
	public String getEmulatorPort() {
		return emulatorPort;
	}

	/**
	 * @param emulatorPort the emulatorPort to set
	 */
	public void setEmulatorPort(String emulatorPort) {
		this.emulatorPort = emulatorPort;
	}

	/**
	 * @return the prevNodeID
	 */
	public String getPrevNodeID() {
		return prevNodeID;
	}

	/**
	 * @param prevNodeID the prevNodeID to set
	 */
	public void setPrevNodeID(String prevNodeID) {
		this.prevNodeID = prevNodeID;
	}

	/**
	 * @return the nextNodeID
	 */
	public String getNextNodeID() {
		return nextNodeID;
	}

	/**
	 * @param nextNodeID the next node id to set
	 */
	public void setNextNodeID(String nextNodeID) {
		this.nextNodeID = nextNodeID;
	}
	
	
	/********************************************************************
	 * Device ID Helper Functions - Babu
	 *******************************************************************/
	
	/**
	 * @return the device id
	 */
	public String getDeviceID() {
		return deviceID;
	}
	

	/**
	 * @param deviceID the emulatorPort to set
	 */
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	/**
	 * @return the prevDeviceID
	 */
	public String getPrevDeviceID() {
		return prevDeviceID;
	}

	/**
	 * @param prevDeviceID the previous device id to set
	 */
	public void setPrevDeviceID(String prevDeviceID) {
		this.prevDeviceID = prevDeviceID;
	}

	/**
	 * @return the next Device Id
	 */
	public String getNextDeviceID() {
		return nextDeviceID;
	}

	/**
	 * @param nextDeviceID the next device id to set
	 */
	public void setNextDeviceID(String nextDeviceID) {
		this.nextDeviceID = nextDeviceID;
	}
	
	/**
	 * @return the quorumReplicas
	 */
	public String[] getQuorumReplicas() {
		return quorumReplicas;
	}

	/**
	 * @param quorumReplicas the quorumReplicas to set
	 */
	public void setQuorumReplicas(String[] quorumReplicas) {
		this.quorumReplicas = quorumReplicas;
	}


	/**
	 * Find parent nodes for which the current node is a replica
	 * @return Parent nodes
	 */
	public String[] getParentNodes() {
		return parentNodes;
	}

	/**
	 * @param parentNodes the parentNodes to set
	 */
	public void setParentNodes(String[] parentNodes) {
		this.parentNodes = parentNodes;
	}

	/**
     * Generate Hash function using SHA1 for key in DHT implementation
     * @param input - key
     * @return Hash value
     * @throws NoSuchAlgorithmException
     */
    public static String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        
        String formattedString = formatter.toString();
        formatter.close();
        return formattedString;
    }
	
    /**
    * Node Lookup for the key insert/query/delete operation
   	* @param key - Key to be inserted/queried/deleted
   	* @param node - Node for which the lookup has to be performed
   	* @return NODE - CURRENT, PREVIOUS or NEXT
   	* @throws Exception
   	* 
   	* @author Babu
   	*/
	public static NODE nodeLookup(String key, Node node) throws Exception
	{		
		String hashkey = Node.genHash(key);
		
		/* 
		 * If wildcard is used then return current node else check whether
		 * key to be inserted lies between the previous node id and the current node id 
		 * or greater than the current node
		 */
		//Log.i("NodeLookup", "Already visited : "+node.alreadyVisited.keySet());
		if(key.compareTo("*") == 0 || key.compareTo("@") == 0)
		{
			return NODE.CURRENT;
		}
		else if(node.getNodeID().compareTo(node.getNextNodeID()) == 0 || node.getNodeID().compareTo(node.getPrevNodeID()) == 0)
		{
			return NODE.CURRENT;
		}
		
		else if (hashkey.compareTo(node.getNodeID()) <= 0 && hashkey.compareTo(node.getPrevNodeID()) > 0)		
		{
			return NODE.CURRENT;
		}

		else if(node.getPrevNodeID().compareTo(node.getNodeID()) > 0 && hashkey.compareTo(node.getPrevNodeID()) > 0
				&& hashkey.compareTo(node.getNodeID()) > 0)
		{
			return NODE.CURRENT;
		}
		else if(node.getPrevNodeID().compareTo(node.getNodeID()) > 0 && hashkey.compareTo(node.getPrevNodeID()) < 0
				&& hashkey.compareTo(node.getNodeID()) < 0)
		{
			return NODE.CURRENT;
		}
		
		else
		{
			return NODE.NEXT;
		}
	}
	
	/**
	 * Node lookup in membership
	 */
	public static Node nodeLookupInMembership(String key) throws Exception
	{
		if(key.compareTo("*") == 0 || key.compareTo("@") == 0)
		{
			Log.i("NodeLookup", "key is * or @. So returning current node in lookupmembership");
			return nodeInstance;
		}
		
		for (String	member : membership) 
		{
			Node node = getInstance(member);
			if(nodeLookup(key, node) == NODE.CURRENT)
			{
				Log.i("NodeLookup", "node looked up : "+member);
				return node;
			}
		}
		return null;
		
	}
	
	
	/**
	 * Get membership details
	 */
	public String[] getMembershipDetails()
	{
		return membership;
	}
	
	/**
	 * Find node id in the membership
	 * @param deviceID - Device id for which the index has to be found
	 * @return Node index in membership list
	 */
	public int findNodeInMembership(String deviceID)
	{
		for (int i = 0; i < membership.length; i++) 
		{
			if(membership[i].compareTo(deviceID) == 0)
				return i;
		}
		return -1;
	}
	
	/**
	 * Find quorum replicas for give node index	 * 
	 */
	public String[] getQuorumReplicas(int nodeIndex)
	{
		int successorId1, successorId2;
		if(nodeIndex == membership.length - 1)
			successorId1 = 0;
		else
			successorId1 = nodeIndex + 1;
		
		if(successorId1 == membership.length - 1)
			successorId2 = 0;
		else
			successorId2 = successorId1 + 1;
		
		return new String[] {membership[successorId1], membership[successorId2]};
		
	}
    
	/**
	 * Find parent nodes for which the current node is a replica
	 */
	public String[] getParentNodes(int nodeIndex)
	{
		int prevIndex1 = (nodeIndex == 0)? (membership.length - 1) : (nodeIndex - 1);
		int prevIndex2 = (prevIndex1 == 0)? (membership.length - 1) : (prevIndex1 - 1);
		return new String[] {membership[prevIndex1], membership[prevIndex2]};
	}
	
	
	
	/**
	 * Checks whether the device id passed is a replica member of current node
	 */
	public boolean isReplicaMember(String deviceId)
	{
		for (String replica : quorumReplicas) {
			if(replica.compareTo(deviceId) == 0)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks whether the device id passed is a parent member of current node
	 */
	public boolean isParentMember(String deviceId)
	{
		for (String parent : parentNodes) {
			if(parent.compareTo(deviceId) == 0)
				return true;
		}
		return false;
	}
	
	
	/**
	 * Enum for NODE 
	 * @author Babu 
	 *
	 */
    public enum NODE
    {
    	PREVIOUS,
    	CURRENT,
    	NEXT
    };
}
