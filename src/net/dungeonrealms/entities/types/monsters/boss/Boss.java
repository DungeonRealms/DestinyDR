package net.dungeonrealms.entities.types.monsters.boss;

import org.bukkit.entity.LivingEntity;

/**
 * Created by Chase on Oct 18, 2015
 */
public interface Boss {
	
	public void onBossDeath();
	
	public void onBossHit(LivingEntity en);
	
}
