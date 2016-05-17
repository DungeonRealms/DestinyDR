package net.dungeonrealms.game.network.packets;

import java.io.Serializable;

public class BroadcastPacket implements Serializable {
	
    /**
	 * Broadcast a message across a shard
	 * Server id (-1) for all shards
	 */
	private static final long serialVersionUID = -8185820884718187088L;
	public int serverid;
	public String message;

    public BroadcastPacket(int id, String message) {
    	this.serverid = id;
        this.message = message;
    }


}
