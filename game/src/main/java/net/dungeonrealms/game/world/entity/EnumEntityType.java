package net.dungeonrealms.game.world.entity;

import org.bukkit.entity.Entity;

import lombok.Getter;

/**
 * Created by Kieran on 9/18/2015.
 */
public enum EnumEntityType {

	PET("PET", "pet"),
	MOUNT("MOUNT", "mount"),
	FRIENDLY_MOB("FRIENDLY_MOB", "friendly"),
	HOSTILE_MOB("HOSTILE_MOB", "hostile"),
	BUFF("BUFF", "buff");

	@Getter private String rawName;
	@Getter private String typeName;
	
	EnumEntityType(String rawName, String typeName) {
		this.rawName = rawName;
		this.typeName = typeName;
	}

	public int getId() {
		return ordinal();
	}
	
	public boolean isType(Entity ent) {
		return ent.hasMetadata("type") && ent.getMetadata("type").get(0).asString().equals(getTypeName());
	}
}
