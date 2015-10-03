package net.dungeonrealms.entities.types.monsters;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends MeleeEntityZombie{

	/**
	 * @param world
	 * @param mobName
	 * @param mobHead
	 * @param tier
	 */
	public BasicMeleeMonster(World world, String mobName, String mobHead, int tier) {
		super(world, mobName, mobHead, tier, EnumEntityType.HOSTILE_MOB, true);
      int level = Utils.getRandomFromTier(tier);
      MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
      EntityStats.setMonsterStats(this, level, tier);
	}
	public BasicMeleeMonster(World world){
		super(world);
	}
	@Override
	protected Item getLoot() {
      ItemStack item = BankMechanics.gem.clone();
      item.setAmount(this.random.nextInt(5));
      this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
		return null;
	}

	@Override
	protected void getRareDrop() {
		
	}

	@Override
	protected void setStats() {
		
	}

}
