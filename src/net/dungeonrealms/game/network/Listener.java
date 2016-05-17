package net.dungeonrealms.game.network;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

public class Listener implements SocketListener {

    @Override
    public void received(Connection con, Object object) {
    	if(object instanceof String)
    	{
    		String RAW = (String)object;
    		if(RAW.contains("@"))
    		{
                String thisserver = NetworkServer.id.toString();
                String id = RAW.substring(RAW.indexOf("@") + 1);
                id = id.substring(0, id.indexOf(":"));
                if(id.equals(thisserver))
                {
                	return; // Don't process packets from itself! LOL
                }
                String packet = RAW.substring(RAW.indexOf(":") + 1);
                System.out.println("Recieved packet from server: " + id + " >> " + packet);	
    		}	
    	}
    }

    @Override
    public void connected(Connection con) {
        System.out.println("Connected to DungeonRealms Master Server.");
    }

    @Override
    public void disconnected(Connection con) {
        System.out.println("Disconnected from DungeonRealms Master Server.");
    }

}
