package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.rank.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/18/2015.
 */
public final class GameChat {

    public static final String GLOBAL = ChatColor.AQUA + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "G" + ChatColor.AQUA + ">" + ChatColor.RESET;


    public static final String SUB = ChatColor.GREEN.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";
    public static final String SUBPLUS = ChatColor.GOLD.toString() + ChatColor.BOLD + "S+" + ChatColor.RESET + " ";
    public static final String GM = ChatColor.AQUA.toString() + ChatColor.BOLD + "GM" + ChatColor.RESET + " ";
    public static final String SUPPORT = ChatColor.BLUE.toString() + ChatColor.BOLD + "SUPPORT" + ChatColor.RESET + " ";
    public static final String DEV = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "DEV" + ChatColor.RESET + " ";
    public static final String YOUTUBE = ChatColor.RED.toString() + ChatColor.BOLD + "YT" + ChatColor.RESET + " ";
    public static final String PMOD = ChatColor.WHITE.toString() + ChatColor.BOLD + "PMOD" + ChatColor.RESET + " ";

    /*
    So this bad boi..

    AsyncPlayerPreChatEvent?

    event.setFormat(getPreMessage(player) + event.getMessage());
     */
    public static String getPreMessage(Player player) {

        StringBuilder message = new StringBuilder();
        Rank.RankBlob r = Rank.getInstance().getRank(player.getUniqueId());


        String clanTag = "";
        if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
            clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
        }

        if (r == null) {
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, "default"));
            } else {
                message.append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, "default"));
            }
        } else if (!r.getName().toLowerCase().contains("default")) {
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(getRankPrefix(r.getName())).append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(getRankPrefix(r.getName())).append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, r.getName().toLowerCase()));
            }
        } else {
            //Player is default.
            boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
            if (gChat) {
                message.append(GLOBAL).append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, r.getName().toLowerCase()));
            } else {
                message.append(org.bukkit.ChatColor.translateAlternateColorCodes('&', org.bukkit.ChatColor.WHITE + "[" + clanTag + org.bukkit.ChatColor.RESET + "] ")).append(getName(player, r.getName().toLowerCase()));
            }
        }

        return message.toString();

    }

    public static String getName(Player player, String rank) {
        switch (rank.toLowerCase()) {
            case "default":
                return ChatColor.GRAY + player.getName() + ":" + ChatColor.WHITE + " ";
            case "sub":
                return ChatColor.GRAY + player.getName() + ":" + ChatColor.WHITE + " ";
            case "sub+":
                return ChatColor.GRAY + player.getName() + ":" + ChatColor.WHITE + " ";
            case "dev":
                return ChatColor.AQUA + player.getName() + ":" + ChatColor.WHITE + " ";
            case "gm":
                return ChatColor.AQUA + player.getName() + ":" + ChatColor.WHITE + " ";
            case "support":
                return ChatColor.BLUE + player.getName() + ":" + ChatColor.WHITE + " ";
            case "youtube":
                return ChatColor.RED + player.getName() + ":" + ChatColor.WHITE + " ";
            case "pmod":
                return ChatColor.WHITE + player.getName() + ":" + ChatColor.WHITE + " ";
        }
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
            case "dev":
                return DEV;
            case "gm":
                return GM;
            case "support":
                return SUPPORT;
            case "youtube":
                return YOUTUBE;
            case "pmod":
                return PMOD;
            default:
                for (int i = 0; i < 5; i++) {
                    Utils.log.warning("getRankPrefix() FAILED for rank: " + rank);
                }
                return "NULL";
        }
    }

}
