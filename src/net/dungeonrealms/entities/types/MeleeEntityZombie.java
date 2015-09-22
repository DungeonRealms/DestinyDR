package net.dungeonrealms.entities.types;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class MeleeEntityZombie extends EntityZombie {

	public String name;
	public String mobHead;
	public EnumEntityType entityType;

	public MeleeEntityZombie(World world, String mobName, String mobHead, int tier, EnumEntityType entityType) {
		this(world);
		try {
			Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
			bField.setAccessible(true);
			Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
			cField.setAccessible(true);
			bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));

		this.name = mobName;
		this.mobHead = mobHead;
		this.entityType = entityType;
		setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		this.getBukkitEntity().setCustomName(ChatColor.GOLD.toString() + ChatColor.UNDERLINE.toString() + mobName);
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
		EntityStats.setMonsterStats(this, level);
		setStats();
	}

	@Override
	protected abstract Item getLoot();

	@Override
	protected abstract void getRareDrop();

	public MeleeEntityZombie(World world) {
		super(world);
	}

	public abstract void setStats();

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

	public void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	public ItemStack getTierWeapon(int tier) {
		if (tier == 1) {
			return new ItemStack(Material.WOOD_SWORD, 1);
		} else if (tier == 2) {
			return new ItemStack(Material.STONE_SWORD, 1);
		} else if (tier == 3) {
			return new ItemStack(Material.IRON_SWORD, 1);
		} else if (tier == 4) {
			return new ItemStack(Material.DIAMOND_SWORD, 1);
		} else if (tier == 5) {
			return new ItemStack(Material.GOLD_SWORD, 1);
		}
		return new ItemStack(Material.WOOD_SWORD, 1);
	}

	public ItemStack[] getTierArmor(int tier) {
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
