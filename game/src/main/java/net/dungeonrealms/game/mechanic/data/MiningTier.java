package net.dungeonrealms.game.mechanic.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.world.item.Item.ItemTier;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MiningTier implements ProfessionTier {
	
	TIER_1(0, 90, 35, 120, EnumAchievements.PICKAXE_LEVEL_I, Material.COAL_ORE, "Coal", "A chunk of coal ore.", Material.WOOD_PICKAXE, "Novice", "sturdy wood", new int[] {100}, 25, 75, CosmeticOverrides.COAL_ORE_HAT),
	TIER_2(20, 275, 35, 300,EnumAchievements.PICKAXE_LEVEL_II, Material.EMERALD_ORE, "Emerald", "An unrefined piece of emerald ore.", Material.STONE_PICKAXE, "Apprentice", "cave stone", new int[] {150, 70}, 75, 150, CosmeticOverrides.EMERALD_ORE_HAT),
	TIER_3(40, 460, 80, 600, EnumAchievements.PICKAXE_LEVEL_III, Material.IRON_ORE, "Iron", "A piece of raw iron.", Material.IRON_PICKAXE, "Expert", "forged iron", new int[] {200, 100, 40}, 150, 300, CosmeticOverrides.IRON_ORE_HAT),
	TIER_4(60, 820, 40, 1200, EnumAchievements.PICKAXE_LEVEL_IV, Material.DIAMOND_ORE, "Diamond", "A sharp chunk of diamond ore.", Material.DIAMOND_PICKAXE, "Supreme", "hardened diamond", new int[] {140, 80, 35}, 200,500, CosmeticOverrides.DIAMOND_ORE_HAT),
	TIER_5(80, 1025, 55, 2400, EnumAchievements.PICKAXE_LEVEL_V, Material.GOLD_ORE, "Gold", "A sparking piece of gold ore", Material.GOLD_PICKAXE, "Master", "reinforced gold", new int[] {80, 60, 40, 20}, 300, 750, CosmeticOverrides.GOLD_ORE_HAT);
	
	@Getter private int level;
	private int baseXP;
	private int randXP;
	@Getter private int oreRespawnTime;
	@Getter private EnumAchievements achievement;
	@Getter private Material ore;
	private String oreName;
	private String oreDesc;
	@Getter private Material pickaxeType;
	
	private String name;
	private String description;
	@Getter private int[] pouchCosts;
	@Getter
	private int minXPBottle;
	@Getter
	private int maxXPBottle;
	@Getter
	CosmeticOverrides hat;

	public int getTier() {
		return ordinal() + 1;
	}
	
	public String getItemName() {
		return this.name + " Pickaxe";
	}
	
	public String getDescription() {
		return ChatColor.ITALIC + "A pickaxe made out of" + this.description + ".";
	}
	
	public ChatColor getColor() {
		return ItemTier.getByTier(getTier()).getColor();
	}
	
	public int getXP() {
		return this.baseXP + ThreadLocalRandom.current().nextInt(this.randXP);
	}
	
	public ItemStack createOreItem() {
		ItemStack ore = new ItemStack(getOre());
		ItemMeta meta = ore.getItemMeta();
		meta.setDisplayName(EnumTier.getById(ordinal() + 1).getColor() + oreName + " Ore");
		//  SET LORE  //
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + oreDesc);
		meta.setLore(lore);
		ore.setItemMeta(meta);
		return ore;
	}
	
	public static MiningTier getTierByLevel(int level) {
		for (int i = values().length - 1; i >= 0; i--)
			if (MiningTier.values()[i].getLevel() <= level)
				return MiningTier.values()[i];
		return MiningTier.TIER_1;
	}
	
	public static MiningTier getTierFromOre(Material ore) {
		for (MiningTier tier : values())
			if (tier.getOre() == ore)
				return tier;
		return null;
	}

	public static MiningTier getTierFromPickaxe(ItemPickaxe pick) {
		Material mat = pick.getItem().getType();
		for (MiningTier tier : values())
			if (tier.getPickaxeType() == mat)
				return tier;
		return null;
	}
}
