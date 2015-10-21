package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMonster;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.boss.Boss;

/**
 * Created by Chase on Oct 18, 2015
 */
public class BossListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("boss")) {
			event.getEntity().removeMetadata("boss", DungeonRealms.getInstance());
			Boss b = (Boss) ((CraftMonster) event.getEntity()).getHandle();
			b.onBossDeath();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
		if (API.isPlayer(event.getDamager())) {
			Player p = (Player) event.getDamager();
			if (event.getEntity() instanceof LivingEntity) {
				if (event.getEntity().hasMetadata("boss")) {
					Boss b = (Boss) ((CraftMonster) event.getEntity()).getHandle();
					b.onBossHit((LivingEntity) event.getEntity());
				}
			}
		}
	}
}
