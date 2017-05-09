package net.dungeonrealms.game.mechanic.data;

import lombok.Getter;

@Getter
public enum PouchTier {
	TIER_1("Small", 100),
	TIER_2("Medium", 150),
	TIER_3("Large", 200),
	TIER_4("Gigantic", 300),
	TIER_5("Colossal", 400); //Unused. Kept for consistency.
	
	private final String name;
	private final int size;

	PouchTier(String name, int size) {
		this.name = name;
		this.size = size;
	}
	
	public int getId() {
		return ordinal() + 1;
	}

	public static PouchTier getById(int tier) {
		return values()[tier - 1];
	}
}
