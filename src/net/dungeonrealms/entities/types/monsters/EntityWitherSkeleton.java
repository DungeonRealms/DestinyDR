package net.dungeonrealms.entities.types.monsters;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 3, 2015
 */
public class EntityWitherSkeleton extends EntitySkeleton {

	public EnumMonster enumMonster;

	public EntityWitherSkeleton(World world) {
		super(world);
	}

	public EntityWitherSkeleton(World world, EnumMonster mon, int tier) {
		super(world);
		enumMonster = mon;
		this.setSkeletonType(1);

		setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, tier, level);
		EntityStats.setMonsterRandomStats(this, level, tier);
		this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ChatColor.RESET
		        + enumMonster.getPrefix() + " " + enumMonster.name + " " + enumMonster.getSuffix());
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

	protected void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	protected String getCustomEntityName() {
		return this.enumMonster.name;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(enumMonster.mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private ItemStack getTierWeapon(int tier) {
		return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.BOW,
		        net.dungeonrealms.items.Item.ItemTier.getByTier(tier));
	}

	private ItemStack[] getTierArmor(int tier) {
		if (tier == 1) {
			return new ItemStack[] { new ItemStack(Material.LEATHER_BOOTS, 1),
			        new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1),
			        new ItemStack(Material.LEATHER_HELMET, 1) };
		} else if (tier == 2) {
			return new ItemStack[] { new ItemStack(Material.CHAINMAIL_BOOTS, 1),
			        new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
			        new ItemStack(Material.CHAINMAIL_HELMET, 1) };
		} else if (tier == 3) {
			return new ItemStack[] { new ItemStack(Material.IRON_BOOTS, 1), new ItemStack(Material.IRON_LEGGINGS, 1),
			        new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_HELMET, 1) };
		} else if (tier == 4) {
			return new ItemStack[] { new ItemStack(Material.DIAMOND_BOOTS, 1),
			        new ItemStack(Material.DIAMOND_LEGGINGS, 1), new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
			        new ItemStack(Material.DIAMOND_HELMET, 1) };

		} else if (tier == 5) {
			return new ItemStack[] { new ItemStack(Material.GOLD_BOOTS, 1), new ItemStack(Material.GOLD_LEGGINGS, 1),
			        new ItemStack(Material.GOLD_CHESTPLATE, 1), new ItemStack(Material.GOLD_HELMET, 1) };
		}
		return null;
	}
}
