package net.dungeonrealms.game.mechanic.data;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.item.items.core.ItemWoodAxe;
import net.dungeonrealms.game.world.item.Item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WoodcutTier
 * @author Kihz
 */
@AllArgsConstructor
public enum WoodcutTier implements ProfessionTier {

    TIER_1(0, 90, 35, 120, EnumAchievements.WOODCUTTING_LEVEL_I, Material.LOG, (short) 0, "Oak log", "A brittle log of oak.", Material.WOOD_AXE, "Novice", "sturdy wood", 25 ,75),
    TIER_2(20, 275, 35, 300, EnumAchievements.WOODCUTTING_LEVEL_II, Material.LOG, (short) 1,"Spruce Log", "A log of spruce.", Material.STONE_AXE, "Apprentice", "cave stone", 75, 150),
    TIER_3(40, 460, 80, 600, EnumAchievements.WOODCUTTING_LEVEL_III, Material.LOG, (short) 2,"Birch Log", "A log of birch.", Material.IRON_AXE, "Expert", "forged iron", 150, 300),
    TIER_4(60, 825, 40, 1200, EnumAchievements.WOODCUTTING_LEVEL_IV, Material.LOG, (short) 3,"Jungle Log", "A log from the jungle.", Material.DIAMOND_AXE, "Supreme", "hardened diamond", 200, 500),
    TIER_5(80, 1035, 55, 2400, EnumAchievements.WOODCUTTING_LEVEL_V, Material.LOG_2, (short) 0,"Acacia Log", "A log of acacia wood.", Material.GOLD_AXE, "Master", "reinforced gold", 300, 750);

    @Getter private int level;
    private int baseXP;
    private int randXP;
    @Getter private int logRespawnTime;
    @Getter private EnumAchievements achievement;
    @Getter private Material log;
    @Getter private short logType;
    private String logName;
    private String logDesc;
    @Getter private Material axeType;

    private String name;
    private String description;
    @Getter
    private int minXPBottle;
    @Getter
    private int maxXPBottle;

    public int getTier() {
        return ordinal() + 1;
    }

    public String getItemName() {
        return  this.name + " Axe";
    }

    public String getDescription() {
        return ChatColor.ITALIC + "An axe made out of " + this.description + ".";
    }

    public ChatColor getColor() {
        return Item.ItemTier.getByTier(getTier()).getColor();
    }

    public int getXP() {
        return this.baseXP + ThreadLocalRandom.current().nextInt(this.randXP);
    }

    public ItemStack createLogItem() {
        ItemStack log = new ItemStack(getLog(), 1, getLogType());
        ItemMeta meta = log.getItemMeta();
        meta.setDisplayName(EnumTier.getById(ordinal() + 1).getColor() + logName + " Log");
        // SET LORE //
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + logDesc);
        meta.setLore(lore);
        log.setItemMeta(meta);
        return log;
    }

    public static WoodcutTier getTierByLevel(int level) {
        for(int i = values().length - 1; i >= 0; i--)
            if(WoodcutTier.values()[i].getLevel() <= level)
                return WoodcutTier.values()[i];
        return WoodcutTier.TIER_1;
    }

    public static WoodcutTier getTierByLog(Material log, short logType) {
        for(WoodcutTier tier : values())
            if(tier.getLog() == log && tier.getLogType() == logType)
                return tier;

        return  null;
    }

    public static WoodcutTier getTierFromAxe(ItemWoodAxe axe) {
        Material mat = axe.getItem().getType();
        for(WoodcutTier tier : values())
            if(tier.getAxeType() == mat)
                return tier;

        return  null;
    }


}
