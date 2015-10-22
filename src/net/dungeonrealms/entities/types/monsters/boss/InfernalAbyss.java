package net.dungeonrealms.entities.types.monsters.boss;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumBoss;
import net.dungeonrealms.entities.types.monsters.boss.subboss.InfernalLordsGuard;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends EntitySkeleton implements Boss {

	public InfernalGhast ghast;
	public InfernalLordsGuard guard;
	
	public InfernalAbyss(World world) {
		super(world);

	}
	/**
	 * @param world
	 */
	public InfernalAbyss(World world, Location loc) {
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
		this.goalSelector.a(5, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(1, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true));
		this.setSkeletonType(1);
		this.fireProof = true;
		this.setOnFire((int) System.currentTimeMillis() * 1);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 50;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
			if (!this.getBukkitEntity().isDead())
				this.getBukkitEntity().getLocation().add(0, 1, 0).getBlock().setType(Material.FIRE);
		} , 0, 20l);
		ghast = new InfernalGhast(this);
		guard = new InfernalLordsGuard(this);
		guard.isInvulnerable(DamageSource.FALL);
		guard.setLocation(locX, locY, locZ, 1, 1);

		setArmor(getEnumBoss().tier);
	}

	protected void setArmor(int tier) {
		ItemStack[] armor = getArmor();
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, getHead());
		ghast.setArmor(armor, weapon);
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
		return new ItemGenerator().next(ItemTier.TIER_4);
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner("Steve");
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private ItemStack[] getArmor() {
		return new ArmorGenerator().nextTier(getEnumBoss().tier);
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.InfernalAbyss;
	}

	@Override
	public void onBossDeath() {
		// Giant Explosion that deals massive damage
		if (hasFiredGhast)
			say(this.getBukkitEntity(), getEnumBoss().death);
	}

	public boolean hasFiredGhast = false;
	public boolean finalForm = false;
	
	
	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		Utils.log.info(this.ghast.isAddedToChunk() + " ghast");
		Utils.log.info(this.guard.isAddedToChunk() + " guard");
		if(!finalForm)
		if (this.ghast.isAddedToChunk() || this.guard.isAddedToChunk()) {
			say(this.getBukkitEntity(), "Hah! You must take out my ");
			event.setDamage(0);
			event.setCancelled(true);
		}

		LivingEntity en = (LivingEntity) event.getEntity();
		double seventyFivePercent = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.75;

		Bukkit.broadcastMessage(HealthHandler.getInstance().getMonsterHPLive(en) + " | "
		        + HealthHandler.getInstance().getMonsterMaxHPLive(en) + " max |" + seventyFivePercent + " 75%");
		if (HealthHandler.getInstance().getMonsterHPLive(en) <= seventyFivePercent && !hasFiredGhast) {
			say(this.getBukkitEntity(), "Taste FIRE!");
			ghast.setLocation(this.locX, this.locY + 4, this.locZ, 1, 1);
			this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
			ghast.init();
			this.isInvulnerable(DamageSource.STUCK);
			this.setLocation(locX, locY + 100, locZ, 1, 1);
			// this.damageEntity(DamageSource.GENERIC, 20F);
			// if (!this.dead) {
			// this.dead = true;
			// }
			hasFiredGhast = true;
		}
	}

}
