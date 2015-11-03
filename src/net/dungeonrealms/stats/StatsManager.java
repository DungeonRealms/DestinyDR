package net.dungeonrealms.stats;

import net.dungeonrealms.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Nov 1, 2015
 */
public class StatsManager {

    public static Inventory getInventory(Player p) {
        PlayerStats stats = getPlayerStats(p);
        Inventory inv = Bukkit.createInventory(null, 18, "Stat Points");
        stats.tempFreePoints = (stats.freePoints);
        ItemStack confirmItem = stats.loadConfirmItem();
        ItemStack dexItem = stats.loadDexItem();
        ItemStack dexStatsItem = stats.loadDexStatsItem();
        ItemStack intItem = stats.loadIntItem();
        ItemStack intStatsItem = stats.loadIntStatsItem();
        ItemStack statsInfoItem = stats.loadStatsInfoItem();
        ItemStack strItem = stats.loadStrItem();
        ItemStack strStatsItem = stats.loadStrStatsItem();
        ItemStack vitItem = stats.loadVitItem();
        ItemStack vitStatsItem = stats.loadVitStatsItem();
        inv.setItem(2, strItem);
        inv.setItem(3, dexItem);
        inv.setItem(4, intItem);
        inv.setItem(5, vitItem);
        inv.setItem(6, confirmItem);
        inv.setItem(11, strStatsItem);
        inv.setItem(12, dexStatsItem);
        inv.setItem(13, intStatsItem);
        inv.setItem(14, vitStatsItem);
        inv.setItem(15, statsInfoItem);
        return inv;
    }

    /**
     * @return PlayerStats
     */
    public static PlayerStats getPlayerStats(Player p) {
        return API.getGamePlayer(p).getStats();
    }

}
