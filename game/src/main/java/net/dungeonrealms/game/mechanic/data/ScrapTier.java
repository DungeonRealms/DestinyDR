package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public enum ScrapTier {

    TIER1(1, "Leather", ChatColor.WHITE, Material.LEATHER, (byte) 0, 25, 80, 70, -1),
    TIER2(2, "Chain", ChatColor.GREEN, Material.IRON_FENCE, (byte) 0, 30, 140, 125, -1),
    TIER3(3, "Iron", ChatColor.AQUA, Material.INK_SACK, (byte) 7, 42, 110, 100, 150),
    TIER4(4, "Diamond", ChatColor.LIGHT_PURPLE, Material.INK_SACK, DyeColor.LIGHT_BLUE.getDyeData(), 57, 88, 80, 60),
    TIER5(5, "Gold", ChatColor.YELLOW, Material.INK_SACK, DyeColor.YELLOW.getDyeData(), 41, 33, 30, 20);

    @Getter
    int tier;
    String name;
    @Getter
    ChatColor chatColor;
    @Getter
    Material material;
    @Getter
    byte data;
    @Getter
    int particleId;
    
    @Getter
    int wepEnchPrice;
    @Getter
    int armEnchPrice;
    @Getter
    int orbPrice;

    public ItemStack getRawStack() {
        return new ItemStack(this.material, 64, this.data);
    }

    /**
     * Returns the "downgraded" scraptier. Ie trade t3 scrap -> t2 scrap.
     * Aka, returns this tier - 1, unless it's tier 1 in which case it returns t2 since that's how that trade works.
     * @return
     */
    public ScrapTier downgrade() {
    	return this == values()[0] ? values()[1] : ScrapTier.values()[ordinal() - 1];
    }
    public ScrapTier getNext() {
    	return this == values()[values().length - 1] ? this : ScrapTier.values()[ordinal() + 1];
    }

    public String getName() {
        return this.chatColor + this.name;
    }

    public static ScrapTier getScrapTier(int tier) {
        for (ScrapTier scrap : ScrapTier.values())
            if (scrap.getTier() == tier)
            	return scrap;
        return null;
    }
}
