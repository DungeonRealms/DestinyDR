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

    public static final String GLOBAL = ChatColor.AQUA + "<" + ChatColor.BOLD + "G" + ChatColor.AQUA + ">" + ChatColor.RESET + " ";
    public static final String RECRUIT = ChatColor.RED + "<" + ChatColor.BOLD + "GR" + ChatColor.RED + ">" + ChatColor.RESET + " ";
    public static final String TRADE = ChatColor.GREEN + "<" + ChatColor.BOLD + "T" + ChatColor.GREEN + ">" + ChatColor.RESET + " ";

    public static final String SUB = ChatColor.GREEN.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";
    public static final String SUBPLUS = ChatColor.GOLD.toString() + ChatColor.BOLD + "S+" + ChatColor.RESET + " ";
    public static final String SUBPLUSPLUS = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "S++" + ChatColor.RESET + " ";
    public static final String GM = ChatColor.AQUA.toString() + ChatColor.BOLD + "GM" + ChatColor.RESET + " ";
    public static final String SUPPORT = ChatColor.BLUE.toString() + ChatColor.BOLD + "SUPPORT" + ChatColor.RESET + " ";
    public static final String DEV = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "DEV" + ChatColor.RESET + " ";
    public static final String YOUTUBE = ChatColor.RED.toString() + ChatColor.BOLD + "YT" + ChatColor.RESET + " ";
    public static final String BUILDER = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "BUILDER" + ChatColor.RESET + " ";
    public static final String PMOD = ChatColor.WHITE.toString() + ChatColor.BOLD + "PMOD" + ChatColor.RESET + " ";

    /*
    So this bad boi..

    AsyncPlayerPreChatEvent?

    event.setFormat(getPreMessage(player) + event.getMessage());
     */

    public static String getPreMessage(Player player) {
        return GameChat.getPreMessage(player, false);
    }

    public static String getPreMessage(Player player, boolean isGlobal) {

        StringBuilder message = new StringBuilder();
        Rank.RankBlob r = Rank.getInstance().getRank(player.getUniqueId());

        String clanTag = "";
        if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
            clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
        }

        // We're using global chat, append global prefix.
        boolean gChat =  isGlobal || (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
        if (gChat) {
            message.append(GLOBAL);
        }

        // The user is in a clan, append their clan tag.
        if (clanTag != "") {
            message.append(org.bukkit.ChatColor.WHITE + "[" + clanTag + "] " + org.bukkit.ChatColor.RESET);
        }

        // The user has a rank, append their rank.
        if (r != null && !r.getName().toLowerCase().contains("default")) {
            message.append(getRankPrefix(r.getName()));
        }

        // Finally, we need to append their name.
        message.append(getName(player, (r == null ? "default" : r.getName().toLowerCase())));

        return message.toString();
    }

    public static String getName(Player player, String rank) {
        switch (rank.toLowerCase()) {
            case "pmod":
                return ChatColor.WHITE + player.getName() + ":" + ChatColor.WHITE + " ";

            case "gm":
            case "dev":
                return ChatColor.AQUA + player.getName() + ":" + ChatColor.WHITE + " ";

            case "youtube":
                return ChatColor.RED + player.getName() + ":" + ChatColor.WHITE + " ";

            case "support":
                return ChatColor.BLUE + player.getName() + ":" + ChatColor.WHITE + " ";

            case "default":
            case "sub":
            case "sub+":
            case "sub++":
            case "builder":
            default:
                return ChatColor.GRAY + player.getName() + ":" +    ChatColor.WHITE + " ";
        }
    }

    public static String getRankPrefix(String rank) {
        switch (rank.toLowerCase()) {
            case "sub":
                return SUB;
            case "sub+":
                return SUBPLUS;
            case "sub++":
                return SUBPLUSPLUS;
            case "builder":
                return BUILDER;
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
            case "default":
            default:
                return ""; // Invalid rank
        }
    }

    /**
     * Returns true/false depending if the chat starts with a word that should be marked for trading.
     *
     * @param message
     * @return boolean
     */
    public static boolean isTradeChat(String message) {
        return (message.startsWith("wtb") || message.startsWith("wts") || message.startsWith("wtt") || message.startsWith("trade")
                || message.startsWith("trading") || message.startsWith("buying") || message.startsWith("selling"));
    }

    /**
     * Returns true/false depending if the chat starts with a word that should be marked for guild recruiting.
     *
     * @param message
     * @return boolean
     */
    public static boolean isRecruiting(String message) {
        message = message.toLowerCase();
        return (message.startsWith("recruiting") || message.startsWith("guild") || message.startsWith("guilds"));
    }

}
