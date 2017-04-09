package net.dungeonrealms.game.mechanic.data;

import org.bukkit.ChatColor;

import lombok.Getter;

@Getter
public enum EnumTier {
	
	TIER_1(ChatColor.WHITE, 1.2, 5),
	TIER_2(ChatColor.GREEN, 1.5, 7),
	TIER_3(ChatColor.AQUA, 1.8, 10),
	TIER_4(ChatColor.LIGHT_PURPLE, 2, 13),
	TIER_5(ChatColor.YELLOW, 2.2, 20);

	private final ChatColor color;
	private final int powerMoveChance;
	private final double bowHitKnockback;
	
	EnumTier(ChatColor c, double bowHitKB, int powerChance) {
		this.color = c;
		this.bowHitKnockback = bowHitKB;
		this.powerMoveChance = powerChance;
	}
	
	public int getId() {
		return ordinal() + 1;
	}
	
	public static EnumTier getById(int tier) {
		return values()[tier - 1];
	}
	
	public static EnumTier getTier(Enum<?> tier) {
		return valueOf(tier.name());
	}
}
