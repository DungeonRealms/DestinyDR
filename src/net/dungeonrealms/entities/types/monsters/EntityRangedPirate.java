/**
 * 
 */
package net.dungeonrealms.entities.types.monsters;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.RangedEntityZombie;
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
public class EntityRangedPirate extends RangedEntityZombie {

	public EntityRangedPirate(World world, EnumEntityType entityType, int tier) {
		super(world);
		this.entityType = entityType;
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
		EntityStats.setMonsterStats(this, level);
		this.setCustomName(ChatColor.GOLD + "Ranged SPirate");
		this.setCustomNameVisible(true);
		setArmor(1);
	}

	public EntityRangedPirate(World world) {
		super(world);
	}

	@Override
	public void a(EntityLiving entityliving, float f) {															//14 - world.difficulty.a() * 4
		EntityArrow entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, (float) (14 - 2 * 4));
		entityarrow.b((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) 2 * 0.11F));
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entityarrow);
	}

	@Override
	public ItemStack[] getTierArmor(int tier) {
		if (tier == 1) {
			return new ItemStack[] { new ItemStack(Material.LEATHER_BOOTS, 1),
				new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1),
				new ItemStack(Material.LEATHER_HELMET, 1) };
		}
		return null;
	}

	@Override
	public void setStats() {

	}

	@Override
	protected Item getLoot() {
		ItemStack item = BankMechanics.gem.clone();
		item.setAmount(this.random.nextInt(5));
		return CraftItemStack.asNMSCopy(item).getItem();
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
	public void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead("samsamsam1234"));
	}

	@Override
	public ItemStack getTierWeapon(int tier) {
		return new ItemStack(Material.BOW, 1);
	}

	@Override
	protected String z() {
		return "mob.zombie.say";
	}

	@Override
	protected String bo() {
		return "random.bowhit";
	}

	@Override
	protected String bp() {
		return "mob.zombie.death";
	}
}
