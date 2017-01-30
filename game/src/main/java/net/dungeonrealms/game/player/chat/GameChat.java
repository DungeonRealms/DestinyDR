package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by Nick on 11/18/2015.
 */
public final class GameChat {

    public static final String GLOBAL = ChatColor.AQUA + "<" + ChatColor.BOLD + "G" + ChatColor.AQUA + ">" + ChatColor.RESET + " ";
    public static final String RECRUIT = ChatColor.RED + "<" + ChatColor.BOLD + "GR" + ChatColor.RED + ">" + ChatColor.RESET + " ";
    public static final String TRADE = ChatColor.GREEN + "<" + ChatColor.BOLD + "T" + ChatColor.GREEN + ">" + ChatColor.RESET + " ";

    public static final String SUB = ChatColor.GREEN.toString() + ChatColor.BOLD + "S" + ChatColor.RESET + " ";
    public static final String SUBPLUS = ChatColor.GOLD.toString() + ChatColor.BOLD + "S+" + ChatColor.RESET + " ";
    public static final String SUBPLUSPLUS = ChatColor.YELLOW.toString() + ChatColor.BOLD + "S++" + ChatColor.RESET + " ";
    public static final String GM = ChatColor.AQUA.toString() + ChatColor.BOLD + "GM" + ChatColor.RESET + " ";
    public static final String SUPPORT = ChatColor.BLUE.toString() + ChatColor.BOLD + "SUPPORT" + ChatColor.RESET + " ";
    public static final String DEV = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "DEV" + ChatColor.RESET + " ";
    public static final String YOUTUBE = ChatColor.RED.toString() + ChatColor.BOLD + "YOUTUBE" + ChatColor.RESET + " ";
    public static final String BUILDER = ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "BUILDER" + ChatColor.RESET + " ";
    public static final String PMOD = ChatColor.WHITE.toString() + ChatColor.BOLD + "PMOD" + ChatColor.RESET + " ";

    /*
    So this bad boi..

    AsyncPlayerPreChatEvent?

    event.setFormat(getPreMessage(player) + event.getMessage());
     */

    public static String getPreMessage(Player player) {
        return GameChat.getPreMessage(player, false, null);
    }

    public static String getPreMessage(Player player, boolean isGlobal) {
        return GameChat.getPreMessage(player, isGlobal, null);
    }

    public static String getPreMessage(Player player, boolean isGlobal, String globalType) {
        globalType = (globalType == null ? "global" : globalType);

        StringBuilder message = new StringBuilder();
        String r = Rank.getInstance().getRank(player.getUniqueId());

        String clanTag = "";
        if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId())) {
            clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
        }

        // We're using global chat, append global prefix.
        boolean gChat = isGlobal || (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
        if (gChat) {
            // Determine which global type we should use, default is GLOBAL.
            switch (globalType.toLowerCase()) {
                case "local": // This allows us to do a cheap hack for "/l".
                    break;
                case "trade":
                    message.append(TRADE);
                    break;
                case "recruit":
                    message.append(RECRUIT);
                    break;
                default:
                    message.append(GLOBAL);
                    break;
            }
        }

        // The user is in a clan, append their clan tag.
        if (!Objects.equals(clanTag, "")) {
            message.append(org.bukkit.ChatColor.WHITE + "[").append(clanTag).append("] ").append(org.bukkit.ChatColor.RESET);
        }

        // The user has a rank, append their rank.
        if (r != null && !r.toLowerCase().contains("default")) {
            message.append(getRankPrefix(r));
        }

        // Finally, we need to append their name.
        message.append(getName(player, (r == null ? "default" : r.toLowerCase())));

        return message.toString();
    }

    public static String getName(Player player) {
        return getName(player, Rank.getInstance().getRank(player.getUniqueId()));
    }

    public static String getName(Player player, String rank) {
        return getName(player, rank, false);
    }

    public static String getName(Player player, boolean onlyName) {
        return getName(player, Rank.getInstance().getRank(player.getUniqueId()), onlyName);
    }

    public static String getName(String name, String rank, boolean onlyName) {
        switch (rank.toLowerCase()) {
            case "headgm":
            case "gm":
            case "trialgm":
            case "dev":
                return ChatColor.AQUA + name + (onlyName ? "" : ":" + ChatColor.WHITE + " ");
            case "default":
            case "sub":
            case "sub+":
            case "sub++":
            case "builder":
            case "support":
            case "youtube":
            case "pmod":
            default:
                String alignmentName = (String) DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(name)));
                return (alignmentName.equalsIgnoreCase("chaotic") ? ChatColor.RED : (alignmentName.equalsIgnoreCase("neutral") ? ChatColor.YELLOW : ChatColor.GRAY)) + name + (onlyName ? "" : ":" + ChatColor.WHITE + " ");
        }
    }

    public static String getName(Player player, String rank, boolean onlyName) {
        switch (rank.toLowerCase()) {
            case "headgm":
            case "gm":
            case "trialgm":
            case "dev":
                return ChatColor.AQUA + player.getName() + (onlyName ? "" : ":" + ChatColor.WHITE + " ");

            case "default":
            case "sub":
            case "sub+":
            case "sub++":
            case "builder":
            case "support":
            case "youtube":
            case "pmod":
            default:
                String alignmentName = GameAPI.getGamePlayer(player).getPlayerAlignment().name();
                return (alignmentName.equalsIgnoreCase("chaotic") ? ChatColor.RED : (alignmentName.equalsIgnoreCase("neutral") ? ChatColor.YELLOW : ChatColor.GRAY)) + player.getName() + (onlyName ? "" : ":" + ChatColor.WHITE + " ");
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
            case "headgm":
            case "gm":
            case "trialgm":
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
     * Returns a player's guild and rank prefix if any followed by a space followed by their username with the correct color
     *
     * @param player
     * @return
     */
    public static String getFormattedName(Player player) {
        String guild = "";
        if (!GuildDatabase.getAPI().isGuildNull(player.getUniqueId()))
            guild = ChatColor.WHITE + "[" + GuildDatabase.getAPI().getTagOf(GuildDatabase.getAPI().getGuildOf(player
                    .getUniqueId())) + "] ";
        final String rank = Rank.getInstance().getRank(player.getUniqueId());
        return guild + getRankPrefix(rank) + getName(player, rank, true);
    }

    public static String getFormattedName(String playerName) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
        } catch (Exception e) {
            Utils.log.warning("EXCEPTION NOT PRINTED - ERROR AT: GameChat.java(getFormattedName(string playerName)");
        }
        if (uuid != null) {
            String guild = "";
            if (!GuildDatabase.getAPI().isGuildNull(uuid))
                guild = ChatColor.WHITE + "[" + GuildDatabase.getAPI().getTagOf(GuildDatabase.getAPI().getGuildOf(uuid)) + "] ";
            final String rank = Rank.getInstance().getRank(uuid);
            return guild + getRankPrefix(rank) + getName(playerName, rank, true);
        }
        return "";
    }

    /**
     * Returns true/false depending if the chat starts with a word that should be marked for trading.
     *
     * @param message
     * @return boolean
     */
    public static boolean isTradeChat(String message) {
        message = message.toLowerCase();
        return (message.startsWith("wtb") || message.startsWith("wts") || message.startsWith("wtt") || message.startsWith("trade")
                || message.startsWith("trading") || message.startsWith("buying") || message.startsWith("selling")
                || message.contains("casino"));
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

    public static String getGlobalType(String message) {
        if (GameChat.isTradeChat(message)) {
            return "trade";
        } else if (GameChat.isRecruiting(message)) {
            return "recruit";
        }

        return "global";
    }

}
