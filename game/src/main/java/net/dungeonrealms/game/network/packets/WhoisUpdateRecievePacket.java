package net.dungeonrealms.game.network.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WhoisUpdateRecievePacket implements Serializable {

	/**
	 * This requests an update to the online player list for that server 
	 */
	private static final long serialVersionUID = 186210770612894514L;
	public String bungeeName;
	public List<String> players = new ArrayList<>();
	
    public WhoisUpdateRecievePacket(String bungee, List<String> playernames) {
    	this.bungeeName = bungee;
    	this.players = playernames;
    }


}
