package net.dungeonrealms.entities.types.monsters.boss.subboss;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumBoss;
import net.dungeonrealms.entities.types.monsters.boss.Boss;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
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
 * Created by Chase on Oct 19, 2015
 */
public class Pyromancer extends EntitySkeleton implements Boss {

	public Location loc;

	public Pyromancer(World world, Location loc) {
		super(world);
		this.loc = loc;
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
		this.goalSelector.a(7, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
		this.goalSelector.a(3, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));

		this.setSkeletonType(1);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 40;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
	}

	protected void setArmor(int tier) {
		ItemStack[] armor = getArmor();
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		weapon.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, getHead());
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
		return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.BOW,
		        net.dungeonrealms.items.Item.ItemTier.getByTier(1));
	}

	/**
	 * Called when entity fires a projectile.
	 */
	@Override
	public void a(EntityLiving entityliving, float f) {
		EntityArrow entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, 14 - 2 * 4);
		entityarrow.setOnFire(10);
		entityarrow.b(f * 2.0F + this.random.nextGaussian() * 0.25D + 2 * 0.11F);
		Projectile arrowProjectile = (Projectile) entityarrow.getBukkitEntity();
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
		NBTTagCompound tag = nmsItem.getTag();
		MetadataUtils.registerProjectileMetadata(tag, arrowProjectile, 2);
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entityarrow);

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
	public void onBossDeath() {
		Block b1 = this.getBukkitEntity().getWorld().getBlockAt((int) locX + 1, (int) locY, (int) locZ + 1);
		b1.setType(Material.TNT);
		this.getBukkitEntity().getWorld().getBlockAt((int) locX + 1, (int) locY - 1, (int) locZ + 1).setType(Material.REDSTONE_TORCH_ON);
		
		
		Block b2 = this.getBukkitEntity().getWorld().getBlockAt((int) locX + 1, (int) locY, (int) locZ - 1);
		b1.setType(Material.TNT);
		this.getBukkitEntity().getWorld().getBlockAt((int) locX + 1, (int) locY-1, (int) locZ - 1).setType(Material.REDSTONE_TORCH_ON);
		
		Block b3 = this.getBukkitEntity().getWorld().getBlockAt((int) locX - 1, (int) locY, (int) locZ + 1);
		b1.setType(Material.TNT);
		this.getBukkitEntity().getWorld().getBlockAt((int) locX - 1, (int) locY, (int) locZ + 1).setType(Material.REDSTONE_TORCH_ON);
		
		Block b4 = this.getBukkitEntity().getWorld().getBlockAt((int) locX - 1, (int) locY, (int) locZ - 1);
		b1.setType(Material.TNT);
		this.getBukkitEntity().getWorld().getBlockAt((int) locX - 1, (int) locY, (int) locZ - 1).setType(Material.REDSTONE_TORCH_ON);
		
		for (Player p : API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().death);
		}
	}

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();		
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Pyromancer;
	}

}
