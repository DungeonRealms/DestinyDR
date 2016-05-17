package net.dungeonrealms.game.network.handlers;

import org.bukkit.Bukkit;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.NetworkServer;
import net.dungeonrealms.game.network.packets.BroadcastPacket;
import net.dungeonrealms.game.network.packets.PartyPacket;

public class PacketHandler {

	public static void handlePacket(Object obj)
	{
    	if(obj instanceof String)
    	{
    		String RAW = (String)obj;
    		if(RAW.contains("@"))
    		{
    			NetworkServer.getInstance().client.getServerConnection().sendTcp("@" + DungeonRealms.getInstance().bungeeName);
    		}
    	}
		if(obj instanceof BroadcastPacket)
		{
			BroadcastPacket packet = (BroadcastPacket)obj;
			Bukkit.broadcastMessage(packet.message);
			return;
		}
		else if(obj instanceof PartyPacket)
		{
			return;
		}
		else {
			Utils.log.warning("Recieved a packet that the server couldn't handle!");
		}
	}
}
