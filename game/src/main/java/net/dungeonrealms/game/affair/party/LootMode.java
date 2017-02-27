package net.dungeonrealms.game.affair.party;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
public enum LootMode {
    KEEP("Keep", ChatColor.GREEN, Material.CHEST, Lists.newArrayList(
            ChatColor.GRAY + "All items picked up are kept.")),
    LEADER("Leader",
            ChatColor.RED,
            Material.DIAMOND,
            Lists.newArrayList(
                    ChatColor.GRAY + "All items picked up are",
                    ChatColor.GRAY + "sent to the Party Leader ({LEADER})")),
    RANDOM("Random",
            ChatColor.AQUA,
            Material.COMPASS,
            Lists.newArrayList(
                    ChatColor.GRAY + "All items picked up are",
                    ChatColor.GRAY + "sent to a random party member."));

    @Getter
    String name;

    @Getter
    ChatColor color;
    @Getter
    Material material;

    @Getter
    List<String> lore;
}