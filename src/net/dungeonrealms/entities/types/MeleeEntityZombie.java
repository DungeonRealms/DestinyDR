package net.dungeonrealms.entities.types;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class MeleeEntityZombie extends EntityZombie {

    public String name;
    public String mobHead;
    public EnumEntityType entityType;

    public MeleeEntityZombie(World world, String mobName, String mobHead, int tier, EnumEntityType entityType) {
        this(world);
        this.name = mobName;
        this.mobHead = mobHead;
        this.entityType = entityType;
        setArmor(tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(ChatColor.GOLD.toString() + ChatColor.UNDERLINE.toString() + mobName);
        // setGoals();
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
        EntityStats.setMonsterStats(this, level);
        setStats();
    }

    public MeleeEntityZombie(World world) {
        super(world);
    }

    public abstract void setStats();

    public void setGoals() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));

        this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));

        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 2, false));

        this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 2));

        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 2));

        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));

        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));

        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));

        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, false));

    }

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

    public abstract void setArmor(int tier);

    protected String getCustomEntityName() {
        return this.name;
    }

    protected net.minecraft.server.v1_8_R3.ItemStack getHead(String name) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(name);
        head.setItemMeta(meta);
        return CraftItemStack.asNMSCopy(head);
    }

    public abstract ItemStack getTierWeapon(int tier);

    public abstract ItemStack[] getTierArmor(int tier);
}
