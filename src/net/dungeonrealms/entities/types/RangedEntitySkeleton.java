/**
 * 
 */
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
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 19, 2015
 */
public abstract class RangedEntitySkeleton extends EntitySkeleton {
	public String name;
	public String mobHead;
	public EnumEntityType entityType;

	/**
	 * @param world
	 */
	public RangedEntitySkeleton(World world, String mobName, String mobHead, int tier, EnumEntityType entityType) {
		super(world);
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

		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
		this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
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

	public RangedEntitySkeleton(World world) {
		super(world);
	}

	public abstract void setStats();

	public abstract void setArmor(int tier);

	protected String getCustomEntityName() {
		return this.name;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead(String name) {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(name);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	public abstract ItemStack getTierWeapon(int tier);

	public abstract ItemStack[] getTierArmor(int tier);
}
