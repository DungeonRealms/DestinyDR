package net.dungeonrealms.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.anticheat.AntiCheat;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.types.monsters.Monster;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityMagmaCube;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Oct 17, 2015
 */
public class DRMagma extends EntityMagmaCube implements Monster{

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

	}
    private void setArmor(int tier) {
        ItemStack[] armor = API.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        
        ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
        ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
        ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);

        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
        this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
        this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
    }

    private ItemStack getTierWeapon(int tier) {
    	ItemStack item = new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.getById(new Random().nextInt(net.dungeonrealms.items.Item.ItemType.values().length - 2)), net.dungeonrealms.items.Item.ItemTier.getByTier(tier), Item.ItemModifier.COMMON);
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    protected String z() {
        return "";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "";
    }

	@Override
	public void onMonsterAttack(Player p) {
		
	}

	@Override
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity());
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});	}

	@Override
	public EnumMonster getEnum() {
		return monsterType;
	}

}
