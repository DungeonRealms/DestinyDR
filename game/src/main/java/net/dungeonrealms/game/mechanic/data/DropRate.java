package net.dungeonrealms.game.mechanic.data;

import net.dungeonrealms.game.world.item.Item.ItemTier;
import lombok.Getter;

@Getter
public enum DropRate {

	TIER_1(ItemTier.TIER_1, 1, 3, 50, 120 * 2/*Drop chance*/, 750 * 2, 2),
	TIER_2(ItemTier.TIER_2, 2, 12, 40, 50 * 2, 400 * 2, 2),
	TIER_3(ItemTier.TIER_3, 10, 30, 30, 30 * 2, 90 * 2, 2),
	TIER_4(ItemTier.TIER_4, 40, 90, 30, 15 * 2, 30 * 2, 1),
	TIER_5(ItemTier.TIER_5, 75, 200, 35, 5 * 2, 10 * 2, 1);
	
	private ItemTier tier;
	
	//  MOB STUFF  //
	private int gemDropMin;
	private int gemDropMax;
	private int mobGemChance;
	private int normalDropChance;
	private int eliteDropChance;
	private int teleportBookChance;
	
	
	DropRate(ItemTier tier, int gemMin, int gemMax, int gemChance, int normalDrop, int eliteDrop, int tpBookChance) {
		this.tier = tier;
		this.gemDropMin = gemMin;
		this.gemDropMax = gemMax;
		this.mobGemChance = gemChance;
		this.normalDropChance = normalDrop;
		this.eliteDropChance = eliteDrop;
		this.teleportBookChance = tpBookChance;
	}
	
	public static DropRate getRate(ItemTier tier) {
		for(DropRate dr : values())
			if(dr.getTier() == tier)
				return dr;
		return null;
	}
	
	public static DropRate getRate(int tier) {
		return getRate(ItemTier.getByTier(tier));
	}
}
