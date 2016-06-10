package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalLordsGuard extends EntitySkeleton implements Boss {
	
	public boolean died = false;
	public InfernalAbyss boss;
	
	public InfernalLordsGuard(InfernalAbyss boss) {
		super(boss.getWorld());
		this.boss = boss;
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
		this.setSkeletonType(1);
		this.fireProof = true;
		this.setOnFire(Integer.MAX_VALUE);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 40;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(boss.getBukkitEntity().getLocation(), 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
        return new ItemGenerator().setType(ItemType.SWORD).setTier(ItemTier.TIER_4).setRarity(API.getItemRarity(false))
                .generateItem().getItem();
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
		this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
	}

	private ItemStack[] getArmor() {
        return new ItemGenerator().setTier(ItemTier.getByTier(getEnumBoss().tier)).setRarity(ItemRarity.UNIQUE)
                .getArmorSet();
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.LordsGuard;
	}

	@Override
	public void onBossDeath() {
		for (Player p : API.getNearbyPlayers(this.getBukkitEntity().getLocation(), 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().death);
		}
		boss.say(boss.getBukkitEntity(), this.getBukkitEntity().getLocation(), "I'll handle it on my own then!");
		boss.setLocation(locX, locY, locZ, 1, 1);
		int maxHP = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt() / 2;
		boss.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
		boss.isInvulnerable(DamageSource.FALL);
		boss.finalForm = true;
	}

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();	

	}

}
