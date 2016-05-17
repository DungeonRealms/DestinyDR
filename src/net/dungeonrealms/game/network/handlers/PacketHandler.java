package net.dungeonrealms.game.network.handlers;

import org.bukkit.Bukkit;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.packets.BroadcastPacket;
import net.dungeonrealms.game.network.packets.PartyPacket;

public class PacketHandler {

	public static void handlePacket(Object obj)
	{
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
