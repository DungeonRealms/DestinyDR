package net.dungeonrealms.entities.types.monsters;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityMagmaCube;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 17, 2015
 */
public class BasicEntityMagma extends EntityMagmaCube{

	private EnumMonster monsterType;

	/**
	 * @param name
	 */
	public BasicEntityMagma(World name) {
		super(name);
	}

	/**
	 * @param name
	 */
	public BasicEntityMagma(World world, int tier) {
		super(world);
        monsterType = EnumMonster.MagmaCube;
        setArmor(tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, tier, level);
        EntityStats.setMonsterRandomStats(this, level, tier);
        this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "
				+ ChatColor.RESET + monsterType.getPrefix() + " " + "Magma Cube" + " " + monsterType.getSuffix());

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
        if (tier == 1) {
            return new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS, 1),
                    new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1),
                    new ItemStack(Material.LEATHER_HELMET, 1)};
        } else if (tier == 2) {
            return new ItemStack[]{new ItemStack(Material.CHAINMAIL_BOOTS, 1),
                    new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
                    new ItemStack(Material.CHAINMAIL_HELMET, 1)};
        } else if (tier == 3) {
            return new ItemStack[]{new ItemStack(Material.IRON_BOOTS, 1), new ItemStack(Material.IRON_LEGGINGS, 1),
                    new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_HELMET, 1)};
        } else if (tier == 4) {
            return new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS, 1),
                    new ItemStack(Material.DIAMOND_LEGGINGS, 1), new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
                    new ItemStack(Material.DIAMOND_HELMET, 1)};

        } else if (tier == 5) {
            return new ItemStack[]{new ItemStack(Material.GOLD_BOOTS, 1), new ItemStack(Material.GOLD_LEGGINGS, 1),
                    new ItemStack(Material.GOLD_CHESTPLATE, 1), new ItemStack(Material.GOLD_HELMET, 1)};
        }
        return null;
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

}
