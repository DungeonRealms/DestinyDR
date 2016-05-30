package net.dungeonrealms.game.network.handlers;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.packets.BroadcastPacket;
import net.dungeonrealms.game.network.packets.PartyPacket;
import net.dungeonrealms.game.network.packets.WhoisUpdateSendPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler {

	public static void handlePacket(Object obj)
	{
    	if(obj instanceof String)
    	{
    		String RAW = (String)obj;
    		if(RAW.contains("@"))
    		{
    			//NetworkServer.getInstance().client.getServerConnection().sendTcp("@" + DungeonRealms.getInstance().bungeeName);
    		}
    		return; // Ignore any other string based packets that we don't understand. (Temporary)
    	}
		if(obj instanceof BroadcastPacket)
		{
			BroadcastPacket packet = (BroadcastPacket)obj;
			String colored = ChatColor.translateAlternateColorCodes('&', packet.message);
			Bukkit.broadcastMessage(colored);
			return;
		}
		else if(obj instanceof PartyPacket)
		{
			return;
		}
		else if(obj instanceof WhoisUpdateSendPacket)
		{
			if(Bukkit.getOnlinePlayers().size() == 0) return; // Don't bother.. This server is empty.
			List<String> playersSend = new ArrayList<String>();
            Bukkit.getOnlinePlayers().stream().forEach(newPlayer ->{
                playersSend.add(newPlayer.getName());
            });
			//NetworkServer.getInstance().client.getServerConnection().sendTcp(new WhoisUpdateRecievePacket(DungeonRealms.getInstance().bungeeName, playersSend));
			return;
		}
		else {
			Utils.log.warning("Recieved a packet that the server couldn't handle!");
		}
	}
}
