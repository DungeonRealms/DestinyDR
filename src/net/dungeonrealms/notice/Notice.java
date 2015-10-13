package net.dungeonrealms.notice;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 10/11/2015.
 */
@SuppressWarnings("unchecked")
public class Notice {

    static Notice instance = null;

    public static Notice getInstance() {
        if (instance == null) {
            instance = new Notice();
        }
        return instance;
    }

    private volatile HashMap<UUID, Integer> REFRESH = new HashMap<>();

    private int MAX_THRESH_HOLD = 5;

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            REFRESH.entrySet().stream().filter(entry -> entry.getValue() >= 1).forEach(entry -> {
                REFRESH.put(entry.getKey(), REFRESH.get(entry.getKey()) - 1);
            });
        }, 0l, 20l);
    }

    /**
     * We plan to have multiple inventories, so.. if aplayer wants to refresh
     * the data that's in their inventory we should give them the ability to.
     * Whilst, making sure that they aren't spamming the clickable item!
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public boolean refreshData(UUID uuid) {
        if (REFRESH.get(uuid) >= MAX_THRESH_HOLD) {
            if (API.isOnline(uuid)) {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "You are requesting data from US MegaServer #1 at a rate which has been blocked!");
            }
            return false;
        } else {
            DatabaseAPI.getInstance().requestPlayer(uuid);
            return true;
        }
    }

    //TODO: Friends, Guilds and clickable acceptance.

    /**
     * This method handles notices per player.
     * E.g. A player logs in and has a guild invite,
     * or a player logs in and has pending friend request.
     *
     * @param player
     * @since 1.0
     */
    public void doLogin(Player player) {

        ArrayList<String> friendRequests = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, player.getUniqueId());
        ArrayList<String> guildInvitations = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITES, player.getUniqueId());

        if (guildInvitations.size() > 0) {
            for (String s : guildInvitations) {
                String guildName = s.split(",")[0];

                long inviteSent = Long.valueOf(s.split(",")[1]);

                long hoursLeft = (((System.currentTimeMillis() / 1000l) / inviteSent) / 60) / 60;
                player.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " you have " + hoursLeft + " hours to accept!");
            }
        }
    }

}
