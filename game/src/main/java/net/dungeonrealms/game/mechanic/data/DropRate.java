package net.dungeonrealms.game.mechanic.data;

import net.dungeonrealms.game.world.item.Item.ItemTier;
import lombok.Getter;

@Getter
public enum DropRate {

	TIER_1(ItemTier.TIER_1, 1, 3, 50, 120/*Drop chance*/, 750, 2, 2, 1),
	TIER_2(ItemTier.TIER_2, 2, 12, 40, 50, 360, 2, 4, 5),
	TIER_3(ItemTier.TIER_3, 10, 30, 30, 30, 81, 2, 7, 10),
	TIER_4(ItemTier.TIER_4, 40, 90, 30, 15, 27, 1, 11, 15),
	TIER_5(ItemTier.TIER_5, 75, 200, 35, 5, 10, 1, 14, 20);
	
	private ItemTier tier;
	
	//  MOB STUFF  //
	private int gemDropMin;
	private int gemDropMax;
	private int mobGemChance;
	private int normalDropChance;
	private int eliteDropChance;
	private int teleportBookChance;
	private int clueDropChance;
	private int recipeItemDropChance;
	
	
	DropRate(ItemTier tier, int gemMin, int gemMax, int gemChance, int normalDrop, int eliteDrop, int tpBookChance, int clueChance, int recipeItemChance) {
		this.tier = tier;
		this.gemDropMin = gemMin;
		this.gemDropMax = gemMax;
		this.mobGemChance = gemChance;
		this.normalDropChance = normalDrop;
		this.eliteDropChance = eliteDrop;
		this.teleportBookChance = tpBookChance;
		this.clueDropChance = clueChance;
		this.recipeItemDropChance = recipeItemChance;
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
