package net.dungeonrealms.entities.types.monsters.base;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.types.monsters.Monster;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class DRSpider extends DRZombie implements Monster{

	/**
	 * @param world
	 * @param mobName
	 * @param tier
	 */
	public DRSpider(World world, EnumMonster monst, int tier) {
		super(world, monst, tier, EnumEntityType.HOSTILE_MOB, true);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(14d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        String customName = monsterType.getPrefix() + " " + monsterType.name + " " + monsterType.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
	}

	public DRSpider(World world) {
		super(world);
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

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
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity());
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});
	}

}
