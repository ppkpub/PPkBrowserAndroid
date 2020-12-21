package org.ppkpub.ppkbrowser;

import android.util.Log;
import kademlia.DefaultConfiguration;
import kademlia.JKademliaNode;
import kademlia.dht.*;
import kademlia.node.*;
import kademlia.simulations.*;


//网络内容本地缓存管理
public class P2P {
  
  /* Kademlia instance */
  public static JKademliaNode kad1;
  private final static String BOOTSTRAP_OWNER_ID = "DOSNA";   // Owner id of the bootstrap kademlia instance
  private final static int BOOTSTRAP_NODE_PORT = 15049;

  public static void start( String  storage_folder ) {
	   
	    //Start P2P node
	    try{
	    	DefaultConfiguration.setStorageFolder(storage_folder);
	    	
	    	kad1 = new JKademliaNode(
	                                BOOTSTRAP_OWNER_ID, 
	                                new KademliaId("BOOTSTRAPBOOTSTRAPBO"), 
	                                BOOTSTRAP_NODE_PORT
	                             );
	        Log.d("P2P",Language.getLangLabel("P2P node started : "+ kad1.getNode()));
	        
	        JKademliaNode kad2 = new JKademliaNode(
	                                "ppk02", 
	                                new KademliaId(), 
	                                10772
	                             );
	        Log.d("P2P",Language.getLangLabel("P2P node started : "+ kad2.getNode()));
	        
	        //Connecting Nodes
	        kad2.bootstrap(kad1.getNode());
	        
	        //Storing Content
	        DHTContentImpl c = new DHTContentImpl(kad2.getOwnerId(), "Some Data");  // Create a content
	        kad2.put(c);    // Put the content on the network
	        
	        Log.d("P2P",Language.getLangLabel("Put content : key="+ c.getKey()));

	        //Retrieving Content
	        
	        GetParameter gp = new GetParameter(c);   // Lets look for content by key
	        gp.setType(DHTContentImpl.TYPE);                  // We also only want content of this type
	        gp.setOwnerId(c.getOwnerId());                    // And content from this owner

	        // Now we call get specifying the GetParameters and the Number of results we want
	        JKademliaStorageEntry conte = kad2.get(gp);
	        Log.d("P2P",Language.getLangLabel("Get content : "+ conte.toString() ));
	        
	        //kad1.shutdown(true);
	        //kad2.shutdown(true);
	        
	        
	    }catch (Exception exx){
	    	Log.d("P2P", exx.toString());
	        
	    }
  }
  
  
  
}