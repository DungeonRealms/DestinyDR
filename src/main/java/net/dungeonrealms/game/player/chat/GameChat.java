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

    public static final String GLOBAL = ChatColor.GRAY + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "G" + ChatColor.GRAY + ">" + ChatColor.RESET + " ";


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

        if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
            String clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
            message.append(ChatColor.GRAY + "<").append(ChatColor.DARK_AQUA.toString()).append(ChatColor.BOLD).append(clanTag).append(ChatColor.GRAY).append(">").append(ChatColor.RESET).append(" ");
        }

        Rank.RankBlob r = Rank.getInstance().getRank(player.getUniqueId());

        if (r != null && !r.getName().toLowerCase().contains("default")) {
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
            case "dev":
                return ChatColor.DARK_AQUA + player.getName() + ":" + ChatColor.RESET + " ";
            case "gm":
                return ChatColor.AQUA + player.getName() + ":" + ChatColor.RESET + " ";
            case "support":
                return ChatColor.BLUE + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "youtube":
                return ChatColor.RED + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
            case "pmod":
                return ChatColor.WHITE + player.getName() + ChatColor.GRAY + ":" + ChatColor.RESET + " ";
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
