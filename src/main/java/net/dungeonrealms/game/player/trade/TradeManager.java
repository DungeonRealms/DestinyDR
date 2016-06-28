package net.dungeonrealms.game.player.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

/**
 * Created by Chase on Nov 16, 2015
 */
public class TradeManager {

    public static ArrayList<Trade> trades = new ArrayList<>();

    /**
     * sender, receiver
     *
     * @param p1
     * @param p2
     */
    public static void openTrade(UUID p1, UUID p2) {
        Player sender = Bukkit.getPlayer(p1);
        Player requested = Bukkit.getPlayer(p2);
        if (sender == null || requested == null) {
            return;
        }
    }

    public static Player getTarget(Player trader) {
        ArrayList<Entity> list = new ArrayList<>();
        trader.getNearbyEntities(1.0D, 1.0D, 1.0D).stream().filter(e -> e instanceof Player && !e.hasMetadata("NPC") && canTrade(e.getUniqueId())).forEach(list::add);
        if (list.size() == 0)
            return null;
        return (Player) list.get(0);
    }

    public static boolean canTrade(UUID uniqueId) {
        Player p = Bukkit.getPlayer(uniqueId);
        if (p == null) {
            return false;
        }
        if (!(boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE, uniqueId)) {
            p.sendMessage(ChatColor.RED + "Trade attempted, but your trades are disabled.");
            p.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "/toggles " + ChatColor.RED + " to enable trades.");
            return false;
        }

        if (getTrade(uniqueId) != null) {
            p.sendMessage(ChatColor.RED + "You're already in a trade, and were attempted to trade again.");
            return false;
        }
        return true;
    }

    public static boolean canTradeItem(ItemStack stack) {
        return true;
    }

    public static void startTrade(Player p1, Player p2) {
        trades.add(new Trade(p1, p2));
    }

    public static Trade getTrade(UUID uuid) {
        for (Trade trade : trades) {
            if (trade.p1.getUniqueId().toString().equalsIgnoreCase(uuid.toString())
                    || trade.p2.getUniqueId().toString().equalsIgnoreCase(uuid.toString()))
                return trade;
        }
        return null;
    }

}
