/**
 * 
 */
package net.dungeonrealms.entities.types.monsters;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.RangedEntitySkeleton;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 19, 2015
 */
public class EntityRangedPirate extends RangedEntitySkeleton {

	public EntityRangedPirate(World world, EnumEntityType entityType, int tier) {
		super(world, "pirate", getRandomHead(), tier, entityType);
		this.entityType = entityType;
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
		EntityStats.setMonsterStats(this, level, tier);
		this.setCustomName(ChatColor.GOLD + "Ranged Pirate");
		this.setCustomNameVisible(true);
		setArmor(1);
	}

	public EntityRangedPirate(World world) {
		super(world);
	}
	private static String getRandomHead() {
		String[] list = new String[] { "samsamsam1234" };
		return list[Utils.randInt(0, list.length - 1)];
	}
	@Override
	public void a(EntityLiving entityliving, float f) { // 14 -
														// world.difficulty.a()
														// * 4
		EntityArrow entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, (float) (14 - 2 * 4));
		entityarrow.b((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) 2 * 0.11F));
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entityarrow);
	}

	@Override
	public void setStats() {

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
		switch (this.random.nextInt(3)) {
		case 0:
			this.a(Items.GOLD_NUGGET, 1);
			break;
		case 1:
			this.a(Items.WOODEN_SWORD, 1);
			break;
		case 2:
			this.a(Items.BOAT, 1);
		}
	}

	@Override
	protected String z() {
		return "mob.zombie.say";
	}

	@Override
	protected String bo() {
		return "game.player.hurt";
	}

	@Override
	protected String bp() {
		return "mob.zombie.death";
	}
}
