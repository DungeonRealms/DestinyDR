package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
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
public class InfernalLordsGuard extends MeleeWitherSkeleton implements Boss {
	
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
	public EnumBoss getEnumBoss() {
		return EnumBoss.LordsGuard;
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
	public void onBossHit(EntityDamageByEntityEvent event) {
		//LivingEntity en = (LivingEntity) event.getEntity();
	}

}
