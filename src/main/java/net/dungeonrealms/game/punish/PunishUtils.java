package net.dungeonrealms.game.punish;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/1/2016
 */

public class PunishUtils {

    /**
     * Method to ban players
     *
     * @param uuid       UUID
     * @param playerName Target
     * @param duration   Set as -1 for permanent ban
     * @param reason     Leave empty for no reason
     */
    public static void ban(UUID uuid, String playerName, long duration, String reason) {
        if (uuid == null) return;
        if (isBanned(uuid)) return;

        // KICK PLAYER //
        if (duration != -1)
            kick(playerName, ChatColor.RED + "You are banned until " + Utils.timeString((int) (duration / 60)) + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net");
        else
            kick(playerName, ChatColor.RED + "You have been permanently banned from DungeonRealms." + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net");

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.BANNED_TIME, System.currentTimeMillis() + (duration * 1000), true);

        if (!reason.equals(""))
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.BANNED_REASON, reason, true);
    }


    /**
     * Method to mute players
     *
     * @param uuid     UUID
     * @param duration Set as -1 for permanent ban
     * @param reason   Leave empty for no reason
     */
    public static void mute(UUID uuid, long duration, String reason) {
        if (uuid == null) return;
        if (isMuted(uuid)) return;

        // MUTE PLAYER //
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_TIME, System.currentTimeMillis() + (duration * 1000), true);
        API.updatePlayerData(uuid);

        if (!reason.equals(""))
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_REASON, reason, true);
    }

    public static String getBannedMessage(UUID uuid) {
        if (!isBanned(uuid)) return null;

        long banTime = (long) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME);
        String reason = (String) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_REASON);

        String message;

        if (banTime != -1)
            message = ChatColor.RED + "You will be unbanned in " + Utils.timeString((int) ((banTime - System.currentTimeMillis()) / 60000)) + (reason != null ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net";
        else
            message = ChatColor.RED + "You have been permanently banned from DungeonRealms." + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net";

        return message;

    }

    public static String getMutedMessage(UUID uuid) {
        if (!isMuted(uuid)) return null;

        long muteTime = (long) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_TIME);
        String reason = (String) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_REASON);

        return ChatColor.RED + "You will be unmuted until " + Utils.timeString((int) ((muteTime - System.currentTimeMillis()) / 60000)) + (reason != null ? " for " + reason : "");
    }

    /**
     * Method to unban players
     *
     * @param uuid Target
     */
    public static void unban(UUID uuid) {
        if (uuid == null) return;
        if (!isBanned(uuid)) return;

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.BANNED_TIME, 0L, true);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.BANNED_REASON, "", true);
    }

    /**
     * Method to unmute players
     *
     * @param uuid Target
     */
    public static void unmute(UUID uuid) {
        if (uuid == null) return;
        if (!isMuted(uuid)) return;

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_TIME, 0L, true);
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_REASON, "", true);
    }

    /**
     * Kicks player from proxy
     *
     * @param playerName  Target
     * @param kickMessage Kick message for player if they're connected to the proxy
     */
    public static void kick(String playerName, String kickMessage) {
        String uuidString = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        UUID uuid = !uuidString.equals("") ? UUID.fromString(uuidString) : null;

        // HANDLE LOG OUT IF THEIR ONLINE //
        if (uuid != null && Bukkit.getPlayer(uuid) != null)
            API.handleLogout(uuid);

        //SEND BUNGEE MESSAGE TO KICK PLAYER FROM PROXY SO THEY DON'T GET LOAD BALANCED //
        NetworkAPI.getInstance().sendNetworkMessage("BungeeCord", "KickPlayer", playerName, kickMessage);
    }


    public static boolean isBanned(UUID uuid) {
        long banTime = ((Long) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME));
        return (banTime == -1) || (banTime != 0 && System.currentTimeMillis() < banTime);
    }


    public static boolean isMuted(UUID uuid) {
        long muteTime = ((Long) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_TIME));
        return (muteTime == -1) || (muteTime != 0 && System.currentTimeMillis() < muteTime);
    }


}
