package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRGhast;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalGhast extends DRGhast implements Boss {

	private InfernalAbyss boss;
	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();
	
	/**
	 * @param infernalAbyss
	 */
	public InfernalGhast(InfernalAbyss infernalAbyss) {
		super(infernalAbyss.getWorld());
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss");
		this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss"));
		this.boss = infernalAbyss;
		//TODO: Enable double armor (takes half damage from attacks) [same as above].
	}

	public void init(int hp) {
		this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
		this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
		maxHP = hp;
		HealthHandler.getInstance().setMonsterHPLive((LivingEntity) this.getBukkitEntity(), hp);
		this.getBukkitEntity().setPassenger(boss.getBukkitEntity());
		DamageAPI.setArmorBonus(getBukkitEntity(), 50);
		this.getBukkitEntity().setPassenger(boss.getBukkitEntity());
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.InfernalGhast;
	}

	@Override
	public void onBossDeath() {
	}

	public void setArmor(ItemStack[] armor, ItemStack weapon) {
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

	private int maxHP = 0;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
//		LivingEntity en = (LivingEntity) event.getEntity();
//		double totalHP = HealthHandler.getInstance().getMonsterMaxHPLive(en);
//		if (totalHP < 10000) {
//			totalHP = maxHP;
//		}
//		totalHP *= 0.5;
//		double currHP = HealthHandler.getInstance().getMonsterHPLive(en);
//
//		if (currHP <= totalHP) {
//			this.getBukkitEntity().eject();
//			this.getBukkitEntity().setPassenger(null);
//			boss.doFinalForm(currHP);
//			this.die();
//		}
	}
}
