package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.ChatColor;

@AllArgsConstructor
public enum MuleTier {

    OLD(ChatColor.GREEN, "Old Storage Mule", 9, 0),
    ADVENTURER(ChatColor.AQUA, "Adventurer's Storage Mule", 18, 5000),
    ROYAL(ChatColor.LIGHT_PURPLE, "Royal Storage Mule", 27, 8000);

    @Getter private ChatColor color;
    private String name;
    @Getter private int size;
    @Getter private int price;
    
    public int getTier() {
    	return ordinal() + 1;
    }

    public String getName() {
        return color + name;
    }
    
    public MuleTier getLast() {
    	return this == values()[0] ? this : values()[ordinal() - 1];
    }
    
    public static MuleTier getByTier(int tier) {
        for (MuleTier muleTier : values())
            if (muleTier.getTier() == tier) 
            	return muleTier;
        return null;
    }
}
