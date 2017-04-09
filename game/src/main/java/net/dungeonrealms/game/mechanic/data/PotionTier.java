package net.dungeonrealms.game.mechanic.data;

import org.bukkit.potion.PotionType;

import lombok.Getter;

@Getter
public enum PotionTier {
	TIER_1("Poor", PotionType.REGEN, 40, 10, 50),
	TIER_2("Inferior", PotionType.INSTANT_HEAL, 90, 60, 120),
	TIER_3("Modest", PotionType.STRENGTH, 350, 250, 400),
	TIER_4("Superior", PotionType.INSTANT_DAMAGE, 850, 700, 950),
	TIER_5("Legendary", PotionType.FIRE_RESISTANCE, 1900, 1600, 2100);
	
	private final String name;
	private final int defaultHealth;
	private final int shopHealthMin;
	private final int shopHealthMax;
	private final PotionType potionType;

	PotionTier(String name, PotionType type, int health, int shopMin, int shopMax) {
		this.name = name;
		this.defaultHealth = health;
		this.shopHealthMin = shopMin;
		this.shopHealthMax = shopMax;
		this.potionType = type;
	}
	
	public int getId() {
		return ordinal();
	}

	public static PotionTier getById(int tier) {
		return values()[tier - 1];
	}
}
