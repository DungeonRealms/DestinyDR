package net.dungeonrealms.game.world.realms;

import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;

public enum RealmTier {
	
	TIER_1(0, 16),
	TIER_2(800, 22, EnumAchievements.REALM_EXPANSION_I),
	TIER_3(1600, 32),
	TIER_4(8000, 45, EnumAchievements.REALM_EXPANSION_II),
	TIER_5(15000, 64),
	TIER_6(35000, 82, EnumAchievements.REALM_EXPANSION_III),
	TIER_7(70000, 128, EnumAchievements.REALM_EXPANSION_IV);
	
	private final int gemPrice;
	private final int dimensions;
	private final EnumAchievements achievement;
	
	RealmTier(int gemPrice, int dimensionSize) {
		this(gemPrice, dimensionSize, null);
	}
	
	RealmTier(int gemPrice, int dimensionSize, EnumAchievements achievement) {
		this.gemPrice = gemPrice;
		this.dimensions = dimensionSize;
		this.achievement = achievement;
	}
	
	public int getTier() {
		return ordinal() + 1;
	}
	
	public static RealmTier getByTier(int tier) {
		return values()[tier - 1];
	}
	
	/**
	 * Get the realm dimensions (X,Z)
	 */
	public int getDimensions() {
		return this.dimensions;
	}
	
	/**
	 * Returns the achievement for upgrading to this tier, if any.
	 */
	public EnumAchievements getAchievement() {
		return this.achievement;
	}
	
	/**
	 * Gets the price to upgrade this realm, in gems.
	 * @return
	 */
	public int getPrice() {
		return this.gemPrice;
	}
}
