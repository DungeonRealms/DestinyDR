package net.dungeonrealms.game.world.entity;

import lombok.Getter;
import net.dungeonrealms.game.mastery.MetadataUtils.EnumMetaValue;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;

import org.bukkit.entity.Entity;

/**
 * EnumEntityType - Defines the type of a custom entity.
 * 
 * Redone on April 20th, 2017.
 * @author Kneesnap
 */
public enum EnumEntityType implements EnumMetaValue {

	PET,
	MOUNT,
	MULE,
	FRIENDLY_MOB(true),
	HOSTILE_MOB(true),
	BUFF,
	SPAWNER(true);

	@Getter private boolean combat;
	
	EnumEntityType() {
		this(false);
	}
	
	EnumEntityType(boolean combat) {
		this.combat = true;
	}
	
	public boolean isType(Entity ent) {
		EnumEntityType type = Metadata.ENTITY_TYPE.getEnum(ent);
		return type == this;
	}
}
