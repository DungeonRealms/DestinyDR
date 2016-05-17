package net.dungeonrealms.game.network.handlers;

import org.bukkit.Bukkit;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.packets.BroadcastPacket;

public class PacketHandler {

	public static void handlePacket(Object obj)
	{
		if(obj instanceof BroadcastPacket)
		{
			BroadcastPacket packet = (BroadcastPacket)obj;
			Bukkit.broadcastMessage(packet.message);
		}
		else {
			Utils.log.warning("Recieved a packet that the server couldn't handle!");
		}
	}
}
