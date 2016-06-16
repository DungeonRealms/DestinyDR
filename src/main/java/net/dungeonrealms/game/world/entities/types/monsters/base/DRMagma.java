package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.minecraft.server.v1_9_R2.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityMagmaCube;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Chase on Oct 17, 2015
 */
public class DRMagma extends EntityMagmaCube implements DRMonster {

	private EnumMonster monsterType;

	/**
	 * @param name
	 * @param tier 
	 * @param enumMonster 
	 */
	public DRMagma(World name, EnumMonster enumMonster, int tier) {
		super(name);
        setArmor(tier);
	}

	/**
	 * @param name
	 */
	public DRMagma(World world, int tier) {
		super(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(14d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        monsterType = EnumMonster.MagmaCube;
        setArmor(tier);
        String customName = monsterType.getPrefix() + " " + monsterType.name + " " + monsterType.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		setSize(4);
		super.setSize(4);
	}
	public void setArmor(int tier) {
		ItemStack[] armor = API.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		boolean armorMissing = false;
		if (random.nextInt(10) <= 5) {
			ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			livingEntity.getEquipment().setBoots(armor0);
			this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			livingEntity.getEquipment().setLeggings(armor1);
			this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			livingEntity.getEquipment().setChestplate(armor2);
			this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
	}

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(ItemType.getRandomWeapon()).setRarity(API.getItemRarity(false))
                .setTier(ItemTier.getByTier(tier)).generateItem().getItem();
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

		});	}
	@Override
	public EnumMonster getEnum() {
		return monsterType;
	}

}
