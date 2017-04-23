package net.dungeonrealms.game.mechanic.data;

import org.bukkit.Material;

import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum HorseTier {

	TIER_1(EnumMounts.TIER1_HORSE, Material.IRON_BARDING, "An old brown starter horse.", 3000, 120, 0.2F, 100),
	TIER_2(EnumMounts.TIER2_HORSE, Material.IRON_BARDING, "A horse fit for a humble squire.", 7000, 140, 0.218F, 110),
	TIER_3(EnumMounts.TIER3_HORSE, Material.DIAMOND_BARDING, "A well versed travelling companion.", 15000, 170, .23F, 110),
	TIER_4(EnumMounts.TIER4_HORSE, Material.GOLD_BARDING, "A mount fit for even the best of adventurers.", 30000, 200, .245F, 110);
	
	private EnumMounts mount;
	private Material armor;
	private String description;
	private int price;
	private int speed;
	private float rawSpeed;
	private int jump;
	
	public int getTier() {
		return getId();
	}
	
	public int getId() {
		return ordinal() + 1;
	}
	
	public static HorseTier getByMount(EnumMounts mounts) {
		for (HorseTier t : values())
			if (t.getMount() == mounts)
				return t;
		return null;
	}
	
	public static HorseTier getByTier(int tier) {
		return values()[tier - 1];
	}
}
