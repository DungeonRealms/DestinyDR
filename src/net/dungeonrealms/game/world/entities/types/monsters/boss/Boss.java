package net.dungeonrealms.game.world.entities.types.monsters.boss;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;

/**
 * Created by Chase on Oct 18, 2015
 */
public interface Boss {
	
	public EnumBoss getEnumBoss();
	
	public void onBossDeath();
	
	public void onBossHit(EntityDamageByEntityEvent event);
	
	public default void say(Entity ent, String msg){
		for (Player p : API.getNearbyPlayers(ent.getLocation(), 50)) {
			p.sendMessage(ent.getCustomName() + ChatColor.RESET.toString() + ": " + msg);
		}
	}
	public default void say(Entity ent, Location location, String msg){
		for (Player p : API.getNearbyPlayers(location, 50)) {
			p.sendMessage(ChatColor.GREEN + ent.getCustomName() + ChatColor.RESET.toString() + ": " + msg);
		}
	}
}
