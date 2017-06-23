package net.dungeonrealms.game.item.items.core.setbonus;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class SetBonus {

    public static Map<Player, SetBonuses> activeSetBonuses = new HashMap<>();


    @Getter
    private String description;

    public SetBonus(String description) {
        this.description = description;
    }

    public void onSetBonusActivate(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 1.4F);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + ChatColor.BOLD.toString() + "Set Bonus Activated");
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + getDescription());
        }, 1);

    }

    public abstract void onSetBonusDeactivate(Player player);


    public static boolean hasSetBonus(Player player, SetBonuses bonuses) {
        SetBonuses bonus = activeSetBonuses.get(player);
        return bonus != null && bonuses.equals(bonus);
    }

    public static SetBonuses getSetBonus(Player player) {
        return activeSetBonuses.get(player);
    }


}
