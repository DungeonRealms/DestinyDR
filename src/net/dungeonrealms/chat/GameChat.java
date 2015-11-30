package net.dungeonrealms.chat;

import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.rank.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/18/2015.
 */
public final class GameChat {

    public static final String GLOBAL = ChatColor.GRAY + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "G" + ChatColor.GRAY + ">" + ChatColor.RESET + " ";


    public static final String SUB = ChatColor.GREEN.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";
    public static final String SUBPLUS = ChatColor.GOLD.toString() + ChatColor.BOLD + "S+" + ChatColor.RESET + " ";
    public static final String GM = ChatColor.RED.toString() + ChatColor.BOLD + "GM" + ChatColor.RESET + " ";
    public static final String CM = ChatColor.BLUE.toString() + ChatColor.BOLD + "CM" + ChatColor.RESET + " ";
    public static final String DEVS = ChatColor.GOLD.toString() + ChatColor.BOLD + "DEV" + ChatColor.RESET + " ";
    public static final String YOUTUBE = ChatColor.RED.toString() + ChatColor.BOLD + "YT" + ChatColor.RESET + " ";

    /*
    So this bad boi..

    AsyncPlayerPreChatEvent?

    event.setFormat(getPreMessage(player) + event.getMessage());

     */
    public static String getPreMessage(Player player) {

        StringBuilder message = new StringBuilder();

        if (!Guild.getInstance().isGuildNull(player.getUniqueId())) {
            String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
            message.append(ChatColor.GRAY + "<").append(ChatColor.DARK_AQUA.toString()).append(ChatColor.BOLD).append(clanTag).append(ChatColor.GRAY).append(">").append(ChatColor.RESET).append(" ");
        }

        Rank.RankBlob r = Rank.getInstance().getRank(player.getUniqueId());

        if (!r.getName().toLowerCase().contains("default")) {
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(getRankPrefix(r.getName())).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(getRankPrefix(r.getName())).append(getName(player, r.getName().toLowerCase()));
            }
        } else {
            //Player is default.
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(getName(player, r.getName().toLowerCase()));
            }
        }

        return message.toString();

    }

    public static String getName(Player player, String rank) {
        switch (rank.toLowerCase()) {
            case "default":
                return ChatColor.GRAY + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "sub":
                return ChatColor.WHITE + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "sub+":
                return ChatColor.WHITE + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "devs":
                return ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "gm":
                return ChatColor.RED + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "cm":
                return ChatColor.BLUE + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "youtube":
                return ChatColor.RED + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
        }
        System.out.println(rank);
        return "NULL";
    }

    public static String getRankPrefix(String rank) {
        System.out.println(rank);
        switch (rank.toLowerCase()) {
            case "default":
                return "";
            case "sub":
                return SUB;
            case "sub+":
                return SUBPLUS;
            case "devs":
                return DEVS;
            case "gm":
                return GM;
            case "cm":
                return CM;
            case "youtube":
                return YOUTUBE;
            default:
                return "NULL";
        }
    }

}
