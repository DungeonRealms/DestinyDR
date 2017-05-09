package net.dungeonrealms.game.mechanic.data;

import org.bukkit.ChatColor;

import net.dungeonrealms.game.world.item.Item.ItemTier;

public enum ShardTier {

	TIER_1,
	TIER_2,
	TIER_3,
	TIER_4,
	TIER_5;
	
	public String getDBField() {
		return "characters.portalShardsT" + getId();
	}
	
	public int getId() {
		return getTier();
	}
	
	public int getTier() {
		return ordinal() + 1;
	}
	
	public ChatColor getColor() {
		return ItemTier.getByTier(getTier()).getColor();
	}
	
	public static ShardTier getByTier(int tier) {
		return values()[tier - 1];
	}
}
