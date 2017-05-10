package net.dungeonrealms.game.anticheat;

import net.dungeonrealms.database.PlayerWrapper;

import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;

public class DebugUtil {
	
	public static void debugReport(Player p) {
		PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);
		String playerReport = p.getName() + " - Debug Report\n"
        		+ "Shard: " + DungeonRealms.getShard().getShardID() + "\n"
				+ "Position: " + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ() + "\n"
        		+ "World: " + p.getWorld().getName() + "\n"
				+ "Open Inventory: " + (p.getOpenInventory() != null ? p.getOpenInventory().getTitle() : "None") + "\n"
        		+ "Inventory: " + (p.getInventory() != null ? p.getInventory().getTitle() : "None") + "\n"
        		+ (wrapper == null ? "PlayerWrapper is null!\n" : ""
        			+ "Level: " + wrapper.getLevel() + "\n"
        			+ "Health: " + HealthHandler.getHP(p) + " / " + HealthHandler.getMaxHP(p) + "\n")
        		+ "Packetlog Started for 30 seconds.";
        
        GameAPI.sendDevMessage(playerReport);
        PacketLogger.logPlayerTime(p, 30);
	}
}
