package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntitySilverfish;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Oct 21, 2015
 */
public class DRSilverfish extends EntitySilverfish implements DRMonster {

	public EnumMonster enumMonster;

	public DRSilverfish(World world, EnumMonster type, int tier) {
		super(world);
//        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
//        this.goalSelector.a(5, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
		this.enumMonster = type;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(14d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
		setArmor(tier);
        String customName = enumMonster.getPrefix() + " " + enumMonster.name + " " + enumMonster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));

	}

    private void setArmor(int tier) {
		ItemStack[] armor = API.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		boolean armorMissing = false;
		if (random.nextInt(10) <= 5) {
			ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
    }

	protected String getCustomEntityName() {
		return this.enumMonster.name;
	}


    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(ItemType.getRandomWeapon()).setRarity(API.getItemRarity(false))
                .setTier(ItemTier.getByTier(tier)).generateItem().getItem();
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
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});
	}

	@Override
	public EnumMonster getEnum() {
		return enumMonster;
	}
}
