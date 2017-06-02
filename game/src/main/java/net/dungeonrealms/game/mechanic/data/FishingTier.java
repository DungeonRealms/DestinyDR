package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public enum FishingTier implements ProfessionTier {

    TIER_1(0, 20, 10, 100, EnumAchievements.FISHINGROD_LEVEL_I, "Basic", "wood and thread"),
    TIER_2(20, 18, 20, 250, EnumAchievements.FISHINGROD_LEVEL_II, "Advanced", "oak wood and thread"),
    TIER_3(40, 15, 30, 300, EnumAchievements.FISHINGROD_LEVEL_III, "Expert", "ancient oak wood and spider silk"),
    TIER_4(60, 15, 40, 500, EnumAchievements.FISHINGROD_LEVEL_IV, "Supreme", "jungle bamboo and spider silk"),
    TIER_5(80, 15, 50, 600, EnumAchievements.FISHINGROD_LEVEL_IV, "Master", "rich mahogany and enchanted silk");

    @Getter
    private int level;
    @Getter
    private int buffChance;
    @Getter
    private int hungerAmount;
    private int xpInc;
    @Getter
    private EnumAchievements achievement;

    private String name;
    private String description;

    public int getTier() {
        return ordinal() + 1;
    }

    public String getItemName() {
        return this.name + " Rod";
    }

    public String getDescription() {
        return ChatColor.ITALIC + "A fishing rod made of " + this.description + ".";
    }

    public ChatColor getColor() {
        return ItemTier.getByTier(getTier()).getColor();
    }

    public int getXP() {
        return (int) (2D * (this.xpInc + ThreadLocalRandom.current().nextInt((int) (this.xpInc * 0.3D))));
    }

    public static FishingTier getByTier(int tier) {
        return Arrays.stream(values()).filter(t -> t.getTier() == tier).findFirst().orElse(null);
    }

    public static FishingTier getTierByLevel(int level) {
        for (int i = values().length - 1; i >= 0; i--)
            if (MiningTier.values()[i].getLevel() <= level)
                return FishingTier.values()[i];
        return FishingTier.TIER_1;
    }
}
