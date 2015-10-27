package net.dungeonrealms.mastery;

import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

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
    public static void registerProjectileMetadata(NBTTagCompound tag, Projectile projectile, int weaponTier) {
        projectile.setMetadata("damage", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getDouble("damage")));
        projectile.setMetadata("vsPlayers", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("vsPlayers")));
        projectile.setMetadata("vsMonsters", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("vsMonsters")));
        projectile.setMetadata("pureDamage", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("pureDamage")));
        projectile.setMetadata("armorPenetration", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("armorPenetration")));
        projectile.setMetadata("accuracy", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("accuracy")));
        projectile.setMetadata("fireDamage", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("fireDamage")));
        projectile.setMetadata("iceDamage", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("iceDamage")));
        projectile.setMetadata("poisonDamage", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("poisonDamage")));
        projectile.setMetadata("criticalHit", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("criticalHit")));
        projectile.setMetadata("lifesteal", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("lifesteal")));
        projectile.setMetadata("intellect", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("intellect")));
        projectile.setMetadata("strength", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("strength")));
        projectile.setMetadata("vitality", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("vitality")));
        projectile.setMetadata("accuracy", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("accuracy")));
        projectile.setMetadata("dexterity", new FixedMetadataValue(DungeonRealms.getInstance(), tag.getInt("dexterity")));
        projectile.setMetadata("itemTier", new FixedMetadataValue(DungeonRealms.getInstance(), weaponTier));
    }
}
