package net.dungeonrealms.entities.types;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class MeleeEntityZombie extends EntityZombie {

    protected String name;
    protected String mobHead;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;

    protected MeleeEntityZombie(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
        this(world);
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        monsterType = monster;
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        if (setArmor)
            setArmor(tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
        EntityStats.setMonsterRandomStats(this, level, tier);
        setStats();
        this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "
				+ ChatColor.RESET + monster.getPrefix() + " " + name + " " + monster.getSuffix());
    }

    @Override
    protected abstract Item getLoot();

    @Override
    protected abstract void getRareDrop();

    protected MeleeEntityZombie(World world) {
        super(world);
    }

    protected abstract void setStats();

    public static Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object o = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    protected String getCustomEntityName() {
        return this.name;
    }

    protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(mobHead);
        head.setItemMeta(meta);
        return CraftItemStack.asNMSCopy(head);
    }

    private void setArmor(int tier) {
        ItemStack[] armor = getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
        this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
        this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
        this.setEquipment(4, this.getHead());
    }

    private ItemStack getTierWeapon(int tier) {
        return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.getById(new Random().nextInt(net.dungeonrealms.items.Item.ItemType.values().length - 2)), net.dungeonrealms.items.Item.ItemTier.getById(tier));
        //TODO: MAKE THIS TAKE A TIER AND BASE IT ON THAT. DO THE SAME WITH ARMOR DON'T JUST CREATE NEW SHITTY BUKKIT ONES.
        /*
		if (tier == 1) {
			return new ItemStack(Material.WOOD_SWORD, 1);
		} else if (tier == 2) {
			return new ItemStack(Material.STONE_SWORD, 1);
		} else if (tier == 3) {
			return new ItemStack(Material.IRON_SWORD, 1);
		} else if (tier == 4) {
			return new ItemStack(Material.DIAMOND_SWORD, 1);
		} else if (tier == 5) {
			return new ItemStack(Material.GOLD_SWORD, 1);
		}
		return new ItemStack(Material.WOOD_SWORD, 1);*/
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
