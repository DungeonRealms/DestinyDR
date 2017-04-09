package net.dungeonrealms.game.mastery;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.minecraft.server.v1_9_R2.Entity;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Kieran on 9/18/2015.
 */
public class MetadataUtils {

    /**
     * This type is used to register metadata on entities.
     *
     * @param entity
     * @param entityType
     * @param entityTier
     * @param level
     * @since 1.0
     */
    public static void registerEntityMetadata(Entity entity, EnumEntityType entityType, int entityTier, int level) {
    	CraftEntity ent = entity.getBukkitEntity();
    	
    	if(entityType == EnumEntityType.HOSTILE_MOB || entityType == EnumEntityType.FRIENDLY_MOB) {
    		entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), entityTier));
            entity.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
            if(entityType == EnumEntityType.HOSTILE_MOB)
            	GameAPI.calculateAllAttributes((LivingEntity)ent, ((DRMonster) entity).getAttributes());
    	}
    	
        ent.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), entityType.getTypeName()));
    }

    /**
     * This type is used to create buffs.
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
     * This type is used add metadata to projectiles based on their firing weapons nbt data.
     *
     * @param tag
     * @param projectile
     * @since 1.0
     */
    public static void registerProjectileMetadata(ItemGear dataFrom, Projectile projectile) {
    	registerProjectileMetadata(dataFrom.getAttributes(), dataFrom.getTier().getId(), projectile);
    }
    
    public static void registerProjectileMetadata(AttributeList attributes, int tier, Projectile projectile) {
        projectile.setMetadata("drProjectile", new FixedMetadataValue(DungeonRealms.getInstance(), true));
        
        // transfer only the weapon attributes. The armor attributes will be grabbed in the calculateProjectileDamage
        for (AttributeType type : attributes.keySet()) {
        	if (!(type instanceof WeaponAttributeType))
        		continue;
            String modifier = type.getNBTName();
            if (type.isRange()) {
                projectile.setMetadata(modifier + "Min", new FixedMetadataValue(DungeonRealms.getInstance(),
                        attributes.getAttribute(type).getValLow()));

                projectile.setMetadata(modifier + "Max", new FixedMetadataValue(DungeonRealms.getInstance(),
                        attributes.getAttribute(type).getValHigh()));
            } else {
                projectile.setMetadata(modifier, new FixedMetadataValue(DungeonRealms.getInstance(), attributes
                        .getAttribute(type).getValue()));
            }
        }
        
        projectile.setMetadata("itemTier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
    }
}
