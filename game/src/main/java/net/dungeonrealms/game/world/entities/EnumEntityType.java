package net.dungeonrealms.game.world.entities;

/**
 * Created by Kieran on 9/18/2015.
 */
public enum EnumEntityType {

	PET(0, "PET"), MOUNT(1, "MOUNT"), FRIENDLY_MOB(2, "FRIENDLY_MOB"), HOSTILE_MOB(3, "HOSTILE_MOB"), BUFF(4, "BUFF");

	private int id;
	private String rawName;

	EnumEntityType(int id, String rawName) {
		this.id = id;
		this.rawName = rawName;
	}

	public int getId() {
		return id;
	}

	public String getRawName() {
		return rawName;
	}

	private static EnumEntityType getByIdValue(int id) {
		for (EnumEntityType entityType : values()) {
			if (entityType.getId() == id) {
				return entityType;
			}
		}
		return getByIdValue(0);
	}
}
