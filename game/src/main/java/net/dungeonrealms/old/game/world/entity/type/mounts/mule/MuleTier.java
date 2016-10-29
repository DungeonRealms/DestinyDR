package net.dungeonrealms.old.game.world.entity.type.mounts.mule;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum MuleTier {

    OLD(1, ChatColor.GREEN, "Old Storage Mule", 9),
    ADVENTURER(2, ChatColor.AQUA, "Adventurer's Storage Mule", 18),
    ROYAL(3, ChatColor.LIGHT_PURPLE, "Royal Storage Mule", 27);

    @Getter
    int tier;
    @Getter
    ChatColor color;

    String name;

    @Getter
    int size;

    public String getName() {
        return color + name;
    }

    public static MuleTier getByTier(int tier) {
        for (MuleTier muleTier : values()) {
            if (muleTier.getTier() == tier) return muleTier;
        }
        return null;
    }
}
