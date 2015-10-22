package net.dungeonrealms.entities.types.monsters.boss;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.dungeonrealms.API;
import net.dungeonrealms.entities.types.monsters.EnumBoss;

/**
 * Created by Chase on Oct 18, 2015
 */
public interface Boss {
	
	public EnumBoss getEnumBoss();
	
	public void onBossDeath();
	
	public void onBossHit(LivingEntity en);
	
	public default void say(Entity ent, String msg){
		for (Player p : API.getNearbyPlayers(ent.getLocation(), 50)) {
			p.sendMessage(ent.getCustomName() + ChatColor.RESET.toString() + ": " + msg);
		}
	}
	
}
