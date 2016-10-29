package net.dungeonrealms.old.game.listener.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.old.game.mechanic.DungeonManager;
import net.dungeonrealms.old.game.mechanic.DungeonManager.DungeonObject;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumDungeonBoss;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by Chase on Oct 18, 2015
 */
public class BossListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBossDeath(EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("boss")) {
			event.getEntity().removeMetadata("boss", DungeonRealms.getInstance());
			if (event.getEntity() instanceof CraftLivingEntity) {
				DungeonBoss b = (DungeonBoss) ((CraftLivingEntity) event.getEntity()).getHandle();
				if (b.getEnumBoss() != EnumDungeonBoss.Pyromancer && b.getEnumBoss() != EnumDungeonBoss.InfernalGhast && b.getEnumBoss()!= EnumDungeonBoss.LordsGuard)
				if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) != null) {
					DungeonObject dungeon = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
					dungeon.teleportPlayersOut(false);
					dungeon.giveShards();
				}
				b.onBossDeath();
			}
		}
	}

}
