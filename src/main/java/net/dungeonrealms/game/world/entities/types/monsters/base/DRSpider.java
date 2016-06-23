package net.dungeonrealms.game.world.entities.types.monsters.base;

import lombok.Getter;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Chase on Oct 2, 2015
 */
public abstract class DRSpider extends EntitySpider implements DRMonster {

	protected String name;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;
	public int tier;
	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();

	public DRSpider(World world, EnumMonster monsterType, int tier) {
		this(world);
		this.monsterType = monsterType;
		this.name = monsterType.name;
		this.entityType = entityType;
		this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(24d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        String customName = monsterType.getPrefix() + " " + monsterType.name + " " + monsterType.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
		setArmor(tier);
		setStats();
		this.noDamageTicks = 0;
		this.maxNoDamageTicks = 0;
		attributes = API.calculateAllAttributes((LivingEntity) this.getBukkitEntity());
	}

	public DRSpider(World world) {
		super(world);
	}

	protected abstract void setStats();

	@Override
	protected Item getLoot() {
		return null;
	}


	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

	public void setArmor(int tier) {
		org.bukkit.inventory.ItemStack[] armor = API.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		org.bukkit.inventory.ItemStack weapon = getTierWeapon(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		boolean armorMissing = false;
		if (random.nextInt(10) <= 5) {
			org.bukkit.inventory.ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			livingEntity.getEquipment().setBoots(armor0);
			this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			org.bukkit.inventory.ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			livingEntity.getEquipment().setLeggings(armor1);
			this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			org.bukkit.inventory.ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			livingEntity.getEquipment().setChestplate(armor2);
			this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
	}

	private org.bukkit.inventory.ItemStack getTierWeapon(int tier) {
		net.dungeonrealms.game.world.items.Item.ItemType itemType = net.dungeonrealms.game.world.items.Item.ItemType.AXE;
		switch (new Random().nextInt(2)) {
			case 0:
				itemType = net.dungeonrealms.game.world.items.Item.ItemType.SWORD;
				break;
			case 1:
				itemType = net.dungeonrealms.game.world.items.Item.ItemType.POLEARM;
				break;
			case 2:
				itemType = net.dungeonrealms.game.world.items.Item.ItemType.AXE;
				break;
		}
		org.bukkit.inventory.ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
				.setTier(net.dungeonrealms.game.world.items.Item.ItemTier.getByTier(tier)).generateItem().getItem();
		AntiCheat.getInstance().applyAntiDupe(item);
		return item;
	}

    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);
		});
	}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}

}
