package net.dungeonrealms.game.item.items.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.Particle;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AuraType {
    LOOT(ChatColor.AQUA, "Loot", "Increases all drop rates in a\n%sx%s Area around the center", Particle.FLAME),
    PROFESSION(ChatColor.GREEN, "Profession", "Increases all profession xp rates\nin a %sx%s Area around the center", Particle.FIREWORKS_SPARK);

    private ChatColor color;
    private String name;
    private String description;
    private Particle particle;

    public static AuraType getFromName(String name) {
        return Arrays.stream(AuraType.values()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public String getName(boolean bold) {
        return (bold ? ChatColor.BOLD.toString() : "") + this.name;
    }
}