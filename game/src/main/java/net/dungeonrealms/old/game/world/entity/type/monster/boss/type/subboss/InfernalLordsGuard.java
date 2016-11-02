package net.dungeonrealms.old.game.world.entity.type.monster.boss.type.subboss;

import lombok.Getter;
import net.dungeonrealms.old.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.mastery.MetadataUtils;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.old.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.old.game.world.entity.util.EntityStats;
import net.dungeonrealms.old.game.world.item.Item.ItemRarity;
import net.dungeonrealms.old.game.world.item.Item.ItemTier;
import net.dungeonrealms.old.game.world.item.Item.ItemType;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalLordsGuard extends MeleeWitherSkeleton implements DungeonBoss {
	
	public boolean died = false;
	public InfernalAbyss boss;
	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();
	
	public InfernalLordsGuard(InfernalAbyss boss) {
		super(boss.getWorld());
		this.boss = boss;
		this.setSkeletonType(1);
		this.fireProof = true;
		this.setOnFire(Integer.MAX_VALUE);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 40;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "The Infernal Lords Guard" + ChatColor.RESET.toString() + ": " + "I shall protect you my lord.");
		}
		this.setSize(0.7F, 2.4F);
		this.fireProof = true;
		this.setSkeletonType(1);
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
        return new ItemGenerator().setType(ItemType.SWORD).setTier(ItemTier.TIER_4).setRarity(GameAPI.getItemRarity(false)).generateItem().getItem();
	}

	public void setArmor(int tier) {
		ItemStack[] armor = getArmor();
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(armor[3]));
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		livingEntity.getEquipment().setItemInMainHand(weapon);
		livingEntity.getEquipment().setBoots(armor[0]);
		livingEntity.getEquipment().setLeggings(armor[1]);
		livingEntity.getEquipment().setChestplate(armor[2]);
		livingEntity.getEquipment().setHelmet(armor[3]);
	}

	private ItemStack[] getArmor() {
        return new ItemGenerator().setTier(ItemTier.getByTier(getEnumBoss().tier)).setRarity(ItemRarity.UNIQUE).getArmorSet();
	}

	@Override
	public EnumDungeonBoss getEnumBoss() {
		return EnumDungeonBoss.LordsGuard;
	}

	@Override
	public void onBossDeath() {
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "The Infernal Lords Guard" + ChatColor.RESET.toString() + ": " + "I have failed you...");
		}
		for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
			p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "I'll handle this on my own then!");
		}
		boss.setLocation(locX, locY, locZ, 1, 1);
		int maxHP = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt() / 2;
		boss.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
		boss.isInvulnerable(DamageSource.FALL);
		boss.finalForm = true;
	}

	@Override
	public void onBossAttack(EntityDamageByEntityEvent event) {
		//LivingEntity en = (LivingEntity) event.getEntity();
	}

}
