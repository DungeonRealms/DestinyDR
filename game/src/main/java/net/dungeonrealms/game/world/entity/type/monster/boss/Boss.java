package net.dungeonrealms.game.world.entity.type.monster.boss;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.type.monster.EnumBoss;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * Created by Chase on Oct 18, 2015
 */
public interface Boss {
	
	EnumBoss getEnumBoss();

	Map<String, Integer[]> getAttributes();
	
	void onBossDeath();
	
	void onBossHit(EntityDamageByEntityEvent event);
	
	default void say(Entity ent, String msg){
		for (Player p : GameAPI.getNearbyPlayers(ent.getLocation(), 50)) {
			p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
					+ ChatColor.GREEN +  msg);
		}
	}
	default void say(Entity ent, Location location, String msg){
		for (Player p : GameAPI.getNearbyPlayers(location, 50)) {
			p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
					+ ChatColor.GREEN +  msg);
		}
	}
}
