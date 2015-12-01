package net.dungeonrealms.game.world.entities.types.monsters.boss;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.BasicEntitySkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.dungeonrealms.game.world.items.armor.ArmorGenerator;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorModifier;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends BasicEntitySkeleton implements Boss {

	public Location loc;

	public Burick(World world, Location loc) {
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
		this.goalSelector.a(5, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
		this.goalSelector.a(1, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
        this.getAttributeInstance(GenericAttributes.c).setValue(1d);
		this.setSkeletonType(1);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}

	}

	@Override
	public void setArmor(int tier) {
		ItemStack[] armor = getArmor();
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
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
		return new ItemGenerator().next(Item.ItemType.AXE, ItemTier.TIER_3);
	}

	@Override
	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner("Steve");
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private ItemStack[] getArmor() {
		return new ArmorGenerator().nextArmor(getEnumBoss().tier, ArmorModifier.LEGENDARY);
	}

	@Override
	public void onBossDeath() {
		say(this.getBukkitEntity(), getEnumBoss().death);
		List<Player> list = API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50);
		Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
			for(Player p : list){
			p.teleport(Teleportation.Cyrennica);
			}
		}, 20*30);
		for(Player p : list){
			p.sendMessage("You will be teleported out in 30 seconds");
		}
	}

	public boolean first = false;
	public boolean second = false;
	public boolean third = false;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();
		int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
		int hp = HealthHandler.getInstance().getMonsterHPLive(en);
		float tenPercentHP = (float) (health * .10);
		if (hp <= tenPercentHP) {
			if (!first || !second || !third) {
				for (Player p : API.getNearbyPlayers(en.getLocation(), 50)) {
					p.sendMessage(
					        this.getCustomName() + ChatColor.RESET.toString() + ": " + " Goragath give me strength!");
				}
				HealthHandler.getInstance().healMonsterByAmount(en,
				        HealthHandler.getInstance().getMonsterMaxHPLive(en));
				if (!first)
					first = true;
				else if (!second)
					second = true;
				else if (!third)
					third = true;
			}
		}
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Burick;
	}

}
