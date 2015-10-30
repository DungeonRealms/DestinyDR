package net.dungeonrealms.entities.types.monsters;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.Monster;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityMagmaCube;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 17, 2015
 */
public class BasicEntityMagma extends EntityMagmaCube implements Monster{

	private EnumMonster monsterType;

	/**
	 * @param name
	 * @param tier 
	 * @param enumMonster 
	 */
	public BasicEntityMagma(World name, EnumMonster enumMonster, int tier) {
		super(name);
        setArmor(tier);
	}

	/**
	 * @param name
	 */
	public BasicEntityMagma(World world, int tier) {
		super(world);
        monsterType = EnumMonster.MagmaCube;
        setArmor(tier);

	}
    private void setArmor(int tier) {
        ItemStack[] armor = getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
        this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
        this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
    }

    private ItemStack getTierWeapon(int tier) {
        return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.getById(new Random().nextInt(net.dungeonrealms.items.Item.ItemType.values().length - 2)), net.dungeonrealms.items.Item.ItemTier.getByTier(tier));
    }

	private ItemStack[] getTierArmor(int tier) {
		return new ArmorGenerator().nextTier(tier);
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
        return "mob.ghast.scream";
    }

	@Override
	public void onMonsterAttack(Player p) {
		
	}

	@Override
	public void onMonsterDeath() {
		
	}

	@Override
	public EnumMonster getEnum() {
		return monsterType;
	}

}
