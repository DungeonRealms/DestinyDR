package net.dungeonrealms.entities.types;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class EntityBlaze extends net.minecraft.server.v1_8_R3.EntityBlaze {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;

	protected EntityBlaze(World world, String mobName, String mobHead, int tier, EnumEntityType entityType,
	        boolean setArmor) {
		this(world);
		this.name = mobName;
		this.mobHead = mobHead;
		this.entityType = entityType;
		if (setArmor)
			setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
		EntityStats.setMonsterStats(this, level, tier);
		setStats();
		this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ChatColor.RESET
		        + getPrefix() + mobName + getSuffix());
	}

	@Override
	protected abstract Item getLoot();

	@Override
	protected abstract void getRareDrop();

	protected EntityBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	public static Object getPrivateField(String fieldName, Class clazz, Object object) {
		Field field;
		Object o = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(object);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return o;
	}

	protected String getCustomEntityName() {
		return this.name;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	private ItemStack getTierWeapon(int tier) {
		return new ItemGenerator().next(
		        net.dungeonrealms.items.Item.ItemType
		                .getById(new Random().nextInt(net.dungeonrealms.items.Item.ItemType.values().length - 2)),
		        net.dungeonrealms.items.Item.ItemTier.getById(tier));
		// TODO: MAKE THIS TAKE A TIER AND BASE IT ON THAT. DO THE SAME WITH
		// ARMOR DON'T JUST CREATE NEW SHITTY BUKKIT ONES.
		/*
		 * if (tier == 1) { return new ItemStack(Material.WOOD_SWORD, 1); } else
		 * if (tier == 2) { return new ItemStack(Material.STONE_SWORD, 1); }
		 * else if (tier == 3) { return new ItemStack(Material.IRON_SWORD, 1); }
		 * else if (tier == 4) { return new ItemStack(Material.DIAMOND_SWORD,
		 * 1); } else if (tier == 5) { return new ItemStack(Material.GOLD_SWORD,
		 * 1); } return new ItemStack(Material.WOOD_SWORD, 1);
		 */
	}

	public abstract String getPrefix();

	public abstract String getSuffix();

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

	@Override
	protected String z() {
		return "";
	}

	@Override
	protected String bo() {
		return "game.player.hurt";
	}

	@Override
	protected String bp() {
		return "mob.ghast.scream";
	}
}
