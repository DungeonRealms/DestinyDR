package net.dungeonrealms.game.anticheat;

import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;

public class DebugUtil {
	
	public static void debugReport(Player p) {
		GamePlayer gp = GameAPI.getGamePlayer(p);
		String playerReport = p.getName() + " - Debug Report\n"
        		+ "Shard: " + DungeonRealms.getShard().getShardID() + "\n"
				+ "Position: " + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ() + "\n"
        		+ "World" + p.getWorld().getName() + "\n"
				+ "Open Inventory: " + (p.getOpenInventory() != null ? p.getOpenInventory().getTitle() : "None") + "\n"
        		+ "Inventory: " + (p.getInventory() != null ? p.getInventory().getTitle() : "None") + "\n"
        		+ (gp == null ? "GamePlayer is null!\n" : ""
        			+ "Level: " + gp.getLevel() + "\n"
        			+ "Health: " + HealthHandler.getInstance().getPlayerHPLive(p) + " / " + HealthHandler.getInstance().getPlayerMaxHPLive(p) + "\n")
        		+ "Packetlog Started for 30 seconds.";
        
        GameAPI.sendDevMessage(playerReport);
        PacketLogger.INSTANCE.logPlayerTime(p, 30);
	}
}
