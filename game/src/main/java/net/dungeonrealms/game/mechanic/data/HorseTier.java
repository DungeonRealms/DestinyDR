package net.dungeonrealms.game.mechanic.data;

import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum HorseTier {

	TIER_1(EnumMounts.TIER1_HORSE, "An old brown starter horse.", 3000, 120, 100),
	TIER_2(EnumMounts.TIER2_HORSE, "A horse fit for a humble squire.", 7000, 140, 110),
	TIER_3(EnumMounts.TIER3_HORSE, "A well versed travelling companion.", 15000, 170, 110),
	TIER_4(EnumMounts.TIER4_HORSE, "A mount fit for even the best of adventurers.", 30000, 200, 110);
	
	@Getter private EnumMounts mount;
	@Getter private String description;
	@Getter private int price;
	@Getter private int speed;
	@Getter private int jump;
	
	public int getId() {
		return ordinal() + 1;
	}
	
	public static HorseTier getByTier(int tier) {
		return values()[tier - 1];
	}
}
