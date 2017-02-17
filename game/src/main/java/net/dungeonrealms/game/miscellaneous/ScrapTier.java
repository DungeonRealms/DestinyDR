package net.dungeonrealms.game.miscellaneous;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.game.database.data.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public enum ScrapTier {

    TIER1(1, "Leather", ChatColor.WHITE, Material.LEATHER, (byte) 0, EnumData.CURRENCY_TAB_T1),
    TIER2(2, "Chain", ChatColor.GREEN, Material.IRON_FENCE, (byte) 0, EnumData.CURRENCY_TAB_T2),
    TIER3(3, "Iron", ChatColor.AQUA, Material.INK_SACK, (byte) 7, EnumData.CURRENCY_TAB_T3),
    TIER4(4, "Diamond", ChatColor.LIGHT_PURPLE, Material.INK_SACK, DyeColor.LIGHT_BLUE.getDyeData(), EnumData.CURRENCY_TAB_T4),
    TIER5(5, "Gold", ChatColor.YELLOW, Material.INK_SACK, DyeColor.YELLOW.getDyeData(), EnumData.CURRENCY_TAB_T5);

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
    EnumData dbData;

    public ItemStack getRawStack() {
        return new ItemStack(this.material, 64, this.data);
    }


    public String getName() {
        return this.chatColor + this.name;
    }

    public static ScrapTier getScrapTier(int tier) {
        for (ScrapTier scrap : ScrapTier.values()) {
            if (scrap.getTier() == tier) return scrap;
        }
        return null;
    }
}
