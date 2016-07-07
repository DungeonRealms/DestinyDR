package net.dungeonrealms.game.mastery;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.Item;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * Created by Kieran on 9/18/2015.
 */
public class MetadataUtils {

    /**
     * This method is used to register metadata on entities.
     *
     * @param entity
     * @param entityType
     * @param entityTier
     * @param level
     * @since 1.0
     */
    public static void registerEntityMetadata(Entity entity, EnumEntityType entityType, int entityTier, int level) {
        switch (entityType) {
            case PET: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "pet"));
                break;
            }
            case FRIENDLY_MOB: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "friendly"));
                entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), entityTier));
                entity.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
                break;
            }
            case MOUNT: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "mount"));
                break;
            }
            case HOSTILE_MOB: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "hostile"));
                entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), entityTier));
                entity.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
                API.calculateAllAttributes((LivingEntity)entity.getBukkitEntity(), ((DRMonster) entity).getAttributes());
                break;
            }
        }
    }

    /**
     * This method is used to create buffs.
     *
     * @param entity
     * @param potionEffectType
     * @param radius
     * @param duration
     * @since 1.0
     */
    public static void registerBuffMetadata(Entity entity, PotionEffectType potionEffectType, int radius, int duration) {
        entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "buff"));
        entity.getBukkitEntity().setMetadata("effectType", new FixedMetadataValue(DungeonRealms.getInstance(), potionEffectType.getName()));
        entity.getBukkitEntity().setMetadata("radius", new FixedMetadataValue(DungeonRealms.getInstance(), radius));
        entity.getBukkitEntity().setMetadata("duration", new FixedMetadataValue(DungeonRealms.getInstance(), duration));
    }

    /**
     * This method is used add metadata to projectiles based on their firing weapons nbt data.
     *
     * @param tag
     * @param projectile
     * @since 1.0
     */
    public static void registerProjectileMetadata(Map<String, Integer[]> attributes, NBTTagCompound tag, Projectile projectile) {
        projectile.setMetadata("drProjectile", new FixedMetadataValue(DungeonRealms.getInstance(), true));
        // transfer only the weapon attributes. The armor attributes will be grabbed in the calculateProjectileDamage
        // method.
        for (Item.WeaponAttributeType type : Item.WeaponAttributeType.values()) {
            String modifier = type.getNBTName();
            if (type.isRange()) {
                projectile.setMetadata(modifier + "Min", new FixedMetadataValue(DungeonRealms.getInstance(),
                        attributes.get(modifier)[0]));

                projectile.setMetadata(modifier + "Max", new FixedMetadataValue(DungeonRealms.getInstance(),
                        attributes.get(modifier)[1]));
            } else {
                projectile.setMetadata(modifier, new FixedMetadataValue(DungeonRealms.getInstance(), attributes
                        .get(modifier)[1]));
            }
        }
        // transfer the itemTier, itemType, and itemRarity
        projectile.setMetadata("itemTier", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("itemTier")));
        projectile.setMetadata("itemType", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("itemType")));
        projectile.setMetadata("itemRarity", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("itemRarity")));

    }
}
