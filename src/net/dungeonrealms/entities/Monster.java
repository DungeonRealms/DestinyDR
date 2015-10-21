package net.dungeonrealms.entities;

import org.bukkit.entity.Player;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface Monster {

	public void onMonsterAttack(Player p);

	public void onMonsterDeath();
}
