package net.dungeonrealms.game.network.handlers;

import org.bukkit.Bukkit;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.packets.BroadcastPacket;

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
		else {
			Utils.log.warning("Recieved a packet that the server couldn't handle!");
		}
	}
}
