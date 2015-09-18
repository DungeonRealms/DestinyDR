package net.dungeonrealms.mastery;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

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
}
