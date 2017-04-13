package net.dungeonrealms.game.mechanic.data;

import org.bukkit.potion.PotionType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum PotionTier {
	TIER_1("Poor", PotionType.REGEN, 1, 40, 10, 50),
	TIER_2("Inferior", PotionType.INSTANT_HEAL, 5, 90, 60, 120),
	TIER_3("Modest", PotionType.STRENGTH, 9, 350, 250, 400),
	TIER_4("Superior", PotionType.INSTANT_DAMAGE, 12, 850, 700, 950),
	TIER_5("Legendary", PotionType.FIRE_RESISTANCE, 3, 1900, 1600, 2100);
	
	private final String name;
	private final PotionType potionType;
	private final int itemMeta;
	private final int defaultHealth;
	private final int shopHealthMin;
	private final int shopHealthMax;
	
	public int getId() {
		return ordinal();
	}

	public static PotionTier getById(int tier) {
		return values()[tier - 1];
	}
}
