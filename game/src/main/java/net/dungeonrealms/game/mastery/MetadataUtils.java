package net.dungeonrealms.game.mastery;

import java.util.UUID;

import lombok.AllArgsConstructor;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

/**
 * MetadataUtils - Registers basic entity metadata.
 * 
 * Redone on April 20th, 2017.
 * @author Kneesnap
 * @param <T>
 */
public class MetadataUtils<T> {

	/**
	 * Registry of most basic metadata.
	 * Enums MUST either be a default enum value or its class.
	 * 
	 * @author Kneesnap
	 */
	@AllArgsConstructor
	public enum Metadata {
		
		// Entity
		CUSTOM_NAME("Error"),
		DUNGEON(false),
		NAMED_ELITE(EnumNamedElite.class),
		ELITE(false),
		BOSS(false),
		LEVEL(1),
		ENTITY_TYPE(EnumEntityType.class),
		ELEMENT(ElementalAttribute.class),
		PASSIVE(false),
		
		CURRENT_HP(50),
		MAX_HP(50),
		HP_REGEN(5),
		
		// Items
		WHITELIST(""),
		
		// Mounts
		MOUNT(EnumMounts.class),
		OWNER(UUID.randomUUID()),
		
		// Projectile
		DR_PROJECTILE(false),
		
		// Spawners
		SPAWN_TYPE(null),
		
		// General
		TIER(1);
		
		private Object defaultValue;
		
		public MetadataValue get(Metadatable m) {
			if (!has(m)) { //If the value isn't set
				if (isDefClass()) // A class will only be set if there is no value.
					return null;
				setDefault(m);
			}
			return m.getMetadata(getKey()).get(0);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Enum<T>> T getEnum(Metadatable m) {
			// Since the default value can be an enum class or an enum value, we must get the class dependent on that.
			Class<T> cls = isDefClass() ? (Class<T>) defaultValue : (Class<T>) ((Enum<T>) defaultValue).getClass();
			return (T) getEnum(m, cls);
		}
		
		@SuppressWarnings({ "unchecked", "static-access" })
		public <T extends Enum<T>> T getEnum(Metadatable m, Class<T> c) {
			return ((T) defaultValue).valueOf(c, get(m).asString());
		}
		
		/**
		 * Does this object have this metadata?
		 */
		public boolean has(Metadatable m) {
			return m.hasMetadata(getKey());
		}
		
		/**
		 * Sets default value.
		 */
		public void setDefault(Metadatable m) {
			set(m, defaultValue);
		}
		
		/**
		 * Sets metadata. If the value impements MetaValue, set it to that instead.
		 */
		public void set(Metadatable m, Object value) {
			if (value instanceof MetaValue)
				value = ((MetaValue)value).getValue();
			else if (value instanceof Enum<?>)
				value = ((Enum<?>)value).name();
			m.setMetadata(getKey(), new FixedMetadataValue(DungeonRealms.getInstance(), value));
		}
		
		/**
		 * Removes metadata.
		 */
		public void remove(Metadatable m) {
			m.removeMetadata(getKey(), DungeonRealms.getInstance());
		}
		
		private String getKey() {
			return name().toLowerCase();
		}
		
		/**
		 * Is the default value a class?
		 */
		private boolean isDefClass() {
			return defaultValue instanceof Class<?>;
		}
	}
	
	public interface MetaValue {
		public Object getValue();
	}
	
	public interface EnumMetaValue extends MetaValue {
		@Override
		default Object getValue() {
			assert this instanceof Enum<?>;
			return ((Enum<?>)this).name();
		}
	}
	
	public static void registerEntityMetadata(Entity entity, EnumEntityType type) {
		assert !type.isCombat();
		registerEntityMetadata(entity, type, 0, 0);
	}
	
    /**
     * Registers basic entity metadata.
     */
    public static void registerEntityMetadata(Entity entity, EnumEntityType type, int tier, int level) {
    	if (type.isCombat()) {
    		Metadata.TIER.set(entity, tier);
    		Metadata.LEVEL.set(entity, level);
    		if (type == EnumEntityType.HOSTILE_MOB)
    			GameAPI.calculateAllAttributes((LivingEntity) entity);
    	}
    	
    	Metadata.ENTITY_TYPE.set(entity, type);
    }

    /**
     * Sets projectile attributes from the weapon it was shot from.
     */
    public static void registerProjectileMetadata(ItemGear dataFrom, Projectile projectile) {
    	registerProjectileMetadata(dataFrom.getAttributes(), dataFrom.getTier().getId(), projectile);
    }
    
    /**
     * Saves attributes into projectile metadata.
     */
    public static void registerProjectileMetadata(AttributeList attributes, int tier, Projectile projectile) {
    	Metadata.DR_PROJECTILE.set(projectile, true);
        Metadata.TIER.set(projectile, tier);
        
        // Transfer weapon attributes.
    	for (AttributeType type : attributes.keySet()) {
        	if (!(type instanceof WeaponAttributeType)) // Since this is a weapon, only transfer offensive attributes.
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
    }
}
