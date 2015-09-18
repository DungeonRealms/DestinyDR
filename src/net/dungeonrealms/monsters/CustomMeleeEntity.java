package net.dungeonrealms.monsters;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class CustomMeleeEntity extends EntityZombie {

	public String name;
	public String mobHead;

	public CustomMeleeEntity(World world, String mobName, String mobHead, int tier) {
		this(world);
		this.name = mobName;
		this.mobHead = mobHead;
		setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		this.getBukkitEntity().setCustomName(ChatColor.GOLD.toString() + ChatColor.UNDERLINE.toString() + mobName);
		// setGoals();
		setBasicStats(getRandomFromTier(tier));
		setStats();
	}

	private int getRandomFromTier(int tier) {
		Random r = new Random();
		int Low = 1;
		int High = 10;
		int R = r.nextInt(High - Low) + Low;
		switch (tier) {
		case 1:
			Low = 1;
			High = 10;
			R = r.nextInt(High - Low) + Low;
			return R;
		case 2:
			Low = 10;
			High = 20;
			R = r.nextInt(High - Low) + Low;
			return R;
		case 3:
			Low = 20;
			High = 30;
			R = r.nextInt(High - Low) + Low;
			return R;
		case 4:
			Low = 30;
			High = 40;
			R = r.nextInt(High - Low) + Low;
			return R;
		case 5:
			Low = 40;
			High = 50;
			R = r.nextInt(High - Low) + Low;
			return R;
		}
		return 1;
	}

	private void setBasicStats(int level) {
		this.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "mob"));
		MonsterStats.setMonsterStats(this, level);
		this.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
	}

	public CustomMeleeEntity(World world) {
		super(world);
	}

	public abstract void setStats();

	public void setGoals() {
		this.goalSelector.a(0, new PathfinderGoalFloat(this));

		this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));

		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 2, false));

		this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 2));

		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 2));

		this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));

		this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));

		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));

		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));

		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, false));

	}

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
