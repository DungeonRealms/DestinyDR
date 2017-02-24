package net.dungeonrealms.game.world.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum ElementalDamage {

    FIRE(ChatColor.RED, "Fire"),
    ICE(ChatColor.BLUE, "Ice"),
    POISON(ChatColor.DARK_GREEN, "Poison"),
    PURE(ChatColor.GOLD, "Pure");


    @Getter
    ChatColor prefixColor;

    @Getter
    String name;

    public String getElementalDamagePrefix() {
        if(this == PURE){
            return "Holy";
        }

        return getName();
    }

    public static ElementalDamage getFromName(String name) {
        for (ElementalDamage damage : values()) {
            if (damage.getName().equalsIgnoreCase(name)) return damage;
        }
        return null;
    }
}
