package net.dungeonrealms.rank;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.SoundAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by Nick on 9/24/2015.
 */
public class Subscription {

    static Subscription instance = null;

    public static Subscription getInstance() {
        if (instance == null) {
            instance = new Subscription();
        }
        return instance;
    }

    public static ArrayList<UUID> PLAYER_SUBSCRIPTION = new ArrayList<>();

    public void doAdd(UUID uuid) {
        PLAYER_SUBSCRIPTION.add(uuid);
    }

    public void startInitialization() {
        Utils.log.info("[DUNGEON_REALMS] Starting up Subscription() ... STARTING");
        TimeZone.setDefault(TimeZone.getTimeZone("American/New_York"));
        startTimer();
        Utils.log.info("[DUNGEON_REALMS] Finished starting up Subscription() ... FINISHED");
    }

    void startTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (UUID uuid : PLAYER_SUBSCRIPTION) {
                if (Bukkit.getPlayer(uuid) == null) {
                    PLAYER_SUBSCRIPTION.remove(uuid);
                    return;
                }
                checkSubscription(Bukkit.getPlayer(uuid));
            }
        }, 20 * 3, 20 * 15l);
    }

    /**
     * Is used in the startTimer class that checks all players on
     * the server and their subscription time.
     *
     * @param player
     * @since 1.0
     */
    public void checkSubscription(Player player) {
        long currentTime = System.currentTimeMillis() / 1000l;
        long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.RANK_EXISTENCE, player.getUniqueId())));
        long time = (endTime - currentTime) / 1000l;
        if (time == 0 && PLAYER_SUBSCRIPTION.contains(player.getUniqueId())) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "rank.rank", "DEFAULT", true);
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "rank.lastPurchase", 0, true);
            player.sendMessage(ChatColor.RED + "Your subscription has expired!");
            SoundAPI.getInstance().playSound("random.anvil_break", player);
        }
    }

    /**
     * Returns the players remaining subscription time!
     *
     * @param player
     * @return
     * @since 1.0
     */
    public int getHoursLeft(Player player) {
        long currentTime = System.currentTimeMillis() / 1000l;
        long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.RANK_EXISTENCE, player.getUniqueId())));
        int hoursLeft = (int) ((endTime - currentTime) / 1000l);
        return hoursLeft;
    }

    /**
     * Takes PlayerJoinEvent from MainListener
     * and does shit.
     *
     * @param event
     * @since 1.0
     */
    public void handleJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Player player = event.getPlayer();
            if (player == null) return;
            long currentTime = System.currentTimeMillis() / 1000l;
            long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.RANK_EXISTENCE, player.getUniqueId())));
            int hoursLeft = (int) ((endTime - currentTime) / 1000l);

            if (hoursLeft > 10) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW.toString() + ChatColor.BOLD + "SUB" + ChatColor.WHITE + "] " + ChatColor.RED + "Click " + ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE" + ChatColor.RED + ". To view your current subscription plan!");
            } else if (hoursLeft <= 9 && hoursLeft >= 3) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW.toString() + ChatColor.BOLD + "SUB" + ChatColor.WHITE + "] " + ChatColor.RED + "Your subscription will end in " + ChatColor.AQUA + hoursLeft + ChatColor.RED + " hours. :-(");
            } else if (hoursLeft <= 0) {
                TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!");
                bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dungeonrealms.net/shop"));
                bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Dungeon Realms Store!").create()));
                TextComponent test = new TextComponent(ChatColor.WHITE + "[" + ChatColor.YELLOW + ChatColor.BOLD + "SUB" + ChatColor.RESET + ChatColor.WHITE + "] " + ChatColor.RED + "Your subscription has ended! Click ");
                test.addExtra(bungeeMessage);
                test.addExtra(ChatColor.RED + " to learn more!");
                player.spigot().sendMessage(test);
            }
        }, 20 * 5);
    }

}
