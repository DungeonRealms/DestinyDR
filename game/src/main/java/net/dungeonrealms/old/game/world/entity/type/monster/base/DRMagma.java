package net.dungeonrealms.old.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.anticheat.AntiDuplication;
import net.dungeonrealms.old.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.item.Item;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 17, 2015
 */
public class DRMagma extends EntityMagmaCube implements DRMonster {

	private EnumMonster monsterType;
	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();

	/**
	 * @param name
	 * @param tier 
	 * @param enumMonster 
	 */
	public DRMagma(World name, EnumMonster enumMonster, int tier) {
		super(name);
		this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
		this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
		monsterType = EnumMonster.MagmaCube;
		setArmor(tier);
		String customName = monsterType.getPrefix() + " " + monsterType.name + " " + monsterType.getSuffix() + " ";
		this.setCustomName(customName);
		this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		setSize(4);
		super.setSize(4);
		this.setSize(0.51000005F * (float)4, 0.51000005F * (float)4);
		this.setPosition(this.locX, this.locY, this.locZ);
		this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double)(4 * 4));
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue((double)(0.2F + 0.1F * (float)4));
		this.setHealth(this.getMaxHealth());
		this.b_ = 4;
		this.noDamageTicks = 0;
		this.maxNoDamageTicks = 0;
	}

	public DRMagma(World world) {
		super(world);
	}

	public void setArmor(int tier) {
		ItemStack[] armor = GameAPI.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		boolean armorMissing = false;
		int chance = 6 + tier;
		if (tier >= 3 || random.nextInt(10) <= chance) {
			ItemStack armor0 = AntiDuplication.getInstance().applyAntiDupe(armor[0]);
			livingEntity.getEquipment().setBoots(armor0);
			this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor1 = AntiDuplication.getInstance().applyAntiDupe(armor[1]);
			livingEntity.getEquipment().setLeggings(armor1);
			this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor2 = AntiDuplication.getInstance().applyAntiDupe(armor[2]);
			livingEntity.getEquipment().setChestplate(armor2);
			this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
	}

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(Item.ItemType.getRandomWeapon()).setRarity(GameAPI.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

	@Override
	public void onMonsterAttack(Player p) {
	}

	@Override
	public void collide(Entity e) {}

	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);

		});	}
	@Override
	public EnumMonster getEnum() {
		return monsterType;
	}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}

}
