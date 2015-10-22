package net.dungeonrealms.notice;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.MailHandler;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
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
        int MAX_THRESH_HOLD = 5;
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
        ArrayList<String> mailbox = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, player.getUniqueId());

        if (guildInvitations.size() > 0) {
            for (String s : guildInvitations) {
                String guildName = s.split(",")[0];

                long inviteSent = Long.valueOf(s.split(",")[1]);

                long currentTime = System.currentTimeMillis();

                long differenceInTime = currentTime - inviteSent;
                long diffHours = differenceInTime / (60 * 60 * 1000);

                if (24 - diffHours >= 0) {
                    player.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " you have " + (24 - diffHours) + " hours to accept!");
                } else {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, "notices.guildInvites", guildName + "," + inviteSent, true);
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "invitations", player.getUniqueId().toString(), true);
                    player.sendMessage(ChatColor.YELLOW + "Your invite from " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " has expired!");
                }
            }
        }

        if (friendRequests.size() > 0) {
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "FRIENDS" + ChatColor.WHITE + "] " + ChatColor.GREEN + "You have " + ChatColor.AQUA + friendRequests.size() + ChatColor.GREEN + " pending friend request!");
        }

        if (mailbox.size() > 0) {
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail!");
        }
    }

}
