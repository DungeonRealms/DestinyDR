package net.dungeonrealms.chat;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.rank.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/18/2015.
 */
public final class GameChat {

    public static final String GLOBAL = ChatColor.GRAY + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "G" + ChatColor.GRAY + ">" + ChatColor.RESET + " ";
    public static final String LOCAL = ChatColor.GRAY + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "L" + ChatColor.GRAY + ">" + ChatColor.RESET + " ";


    public static final String SUB = ChatColor.GREEN.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";
    public static final String SUBPLUS = ChatColor.GOLD.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";

    /*
    So this bad boi..

    AsyncPlayerPreChatEvent?

    event.setformat(getPreMessage(player) + event.getMessage();

     */
    public static String getPreMessage(Player player) {

        StringBuilder message = new StringBuilder();

        Rank.RankBlob r = Rank.getInstance().getRank(player.getUniqueId());

        if (!r.getName().contains("DEFAULT")) {
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(getRankPrefix(r.getName())).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(LOCAL).append(getRankPrefix(r.getName())).append(getName(player, r.getName().toLowerCase()));
            }
        } else {
            //Player is default.
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(LOCAL).append(getName(player, r.getName().toLowerCase()));
            }
        }

        return message.toString();

    }

    public static String getName(Player player, String rank) {
        switch (rank.toLowerCase()) {
            case "sub":
                return ChatColor.GRAY + player.getName() + ChatColor.RESET + " ";
            case "sub+":
                return ChatColor.GRAY + player.getName() + ChatColor.RESET + " ";
        }
        return "NULL";
    }

    public static String getRankPrefix(String rank) {
        switch (rank) {
            case "sub":
                return SUB;
            case "sub+":
                return SUBPLUS;
            default:
                return "NULL";
        }
    }

}
