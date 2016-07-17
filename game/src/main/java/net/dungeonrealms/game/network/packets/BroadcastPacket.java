package net.dungeonrealms.game.network.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BroadcastPacket implements Serializable {
	
    /**
	 * Broadcast a message across a shard
	 * Server id (-1) for all shards
	 */
	private static final long serialVersionUID = -8185820884718187088L;
	public List<String> serverid = new ArrayList<>();
	public String message;

    public BroadcastPacket(List<String> id, String message) {
    	this.serverid = id;
        this.message = message;
    }


}
