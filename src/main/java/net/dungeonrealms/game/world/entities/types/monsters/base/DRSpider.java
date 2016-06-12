package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.minecraft.server.v1_9_R2.PathfinderGoalRandomStroll;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.Item;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class DRSpider extends DRZombie implements DRMonster {

	/**
	 * @param world
	 * @param mobName
	 * @param tier
	 */
	public DRSpider(World world, EnumMonster monst, int tier) {
		super(world, monst, tier, EnumEntityType.HOSTILE_MOB, true);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        String customName = monsterType.getPrefix() + " " + monsterType.name + " " + monsterType.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
	}

	public DRSpider(World world) {
		super(world);
	}

	@Override
	protected Item getLoot() {
		return null;
	}


	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

	@Override
	protected void setStats() {

	}
    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);
		});
	}

}
