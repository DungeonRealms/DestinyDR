package net.dungeonrealms.game.world.entities.types.monsters.base;

import lombok.Getter;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Chase on Oct 18, 2015
 */
public class DRPigman extends EntityPigZombie implements DRMonster {

	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();

	/**
	 * @param name
	 */
	public DRPigman(World name) {
		super(name);
	}
	
	public EnumMonster enumMonster;

	/**
	 * @param world
	 * @param mon
	 * @param tier
	 */
	public DRPigman(World world, EnumMonster mon, int tier) {
		super(world);
		enumMonster = mon;
		this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        //this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
		setArmor(tier);
		this.angerLevel = 30000;
        String customName = enumMonster.getPrefix() + " " + enumMonster.name + " " + enumMonster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
		this.noDamageTicks = 0;
		this.maxNoDamageTicks = 0;
	}

	public void setArmor(int tier) {
		ItemStack[] armor = API.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		boolean armorMissing = false;
		int chance = 6 + tier;
		if (tier >= 3 || random.nextInt(10) <= chance) {
			ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			livingEntity.getEquipment().setBoots(armor0);
			this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			livingEntity.getEquipment().setLeggings(armor1);
			this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			livingEntity.getEquipment().setChestplate(armor2);
			this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
		livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
	}

	protected String getCustomEntityName() {
		return this.enumMonster.name;
	}

	private ItemStack getTierWeapon(int tier) {
		Item.ItemType itemType = Item.ItemType.AXE;
		switch (new Random().nextInt(2)) {
			case 0:
				itemType = Item.ItemType.SWORD;
				break;
			case 1:
				itemType = Item.ItemType.AXE;
				break;
		}
		ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
				.setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
		AntiCheat.getInstance().applyAntiDupe(item);
		return item;
	}

	@Override
	public void onMonsterAttack(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), enumMonster, this.getBukkitEntity(), killer);
		});
	}

	@Override
	public EnumMonster getEnum() {
		return this.enumMonster;
	}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}
}
