package net.dungeonrealms.game.network.handlers;

import org.bukkit.Bukkit;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.packets.BroadcastPacket;
import net.dungeonrealms.game.network.packets.PartyPacket;

public class PacketHandler {

	public static void handlePacket(Object obj)
	{
		if(obj instanceof BroadcastPacket)
		{
			BroadcastPacket packet = (BroadcastPacket)obj;
			if(packet.serverid.contains(DungeonRealms.getInstance().bungeeName))
			{
				Bukkit.broadcastMessage(packet.message);
				return;
			}
		}
		else if(obj instanceof PartyPacket)
		{
			PartyPacket packet = (PartyPacket)obj;
			if(packet.getTo().equals(DungeonRealms.getInstance().bungeeName))
			{
				// Handle the Party packet here... 
				return;
			}
		}
		else {
			Utils.log.warning("Recieved a packet that the server couldn't handle!");
		}
	}
}
