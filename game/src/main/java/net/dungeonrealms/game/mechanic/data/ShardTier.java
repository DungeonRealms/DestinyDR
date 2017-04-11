package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.ChatColor;

import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.world.item.Item.ItemTier;

@AllArgsConstructor
public enum ShardTier {

	TIER_1(EnumData.PORTAL_SHARDS_T1),
	TIER_2(EnumData.PORTAL_SHARDS_T2),
	TIER_3(EnumData.PORTAL_SHARDS_T3),
	TIER_4(EnumData.PORTAL_SHARDS_T4),
	TIER_5(EnumData.PORTAL_SHARDS_T5);
	
	@Getter
	private EnumData shardData;
	
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
