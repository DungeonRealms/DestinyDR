package net.dungeonrealms.game.punishment;

import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/1/2016
 */

public class PunishAPI {

    /**
     * Method to ban players
     *
     * @param uuid       UUID
     * @param playerName Target
     * @param duration   Set as -1 for permanent ban
     * @param reason     Leave empty for no reason
     */
    public static void ban(UUID uuid, String playerName, long duration, String reason, Consumer<UUID> doBefore) {
        if (uuid == null) return;
        if (isBanned(uuid)) return;

        // KICK PLAYER //
        if (duration == -1)
            kick(playerName, ChatColor.RED + "You have been permanently banned from DungeonRealms." + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);
        else
            kick(playerName, ChatColor.RED + "You are banned until " + timeString((int) (duration / 60)) + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.BANNED_TIME, (duration != -1 ? System.currentTimeMillis() + (duration * 1000) : -1), true);

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
    public static void mute(UUID uuid, long duration, String reason, Consumer<UUID> doAfter) {
        if (uuid == null) return;
        if (isMuted(uuid)) return;

        // MUTE PLAYER //
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_TIME, System.currentTimeMillis() + (duration * 1000), true);


        if (!reason.equals(""))
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_REASON, reason, true);

        doAfter.accept(uuid);
    }

    public static String getBannedMessage(UUID uuid) {
        if (!isBanned(uuid)) return null;

        long banTime = (long) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME);
        String reason = (String) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_REASON);

        String message;

        if (banTime != -1)
            message = ChatColor.RED + "You will be unbanned in " + timeString((int) ((banTime - System.currentTimeMillis()) / 60000)) + (reason != null ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net";
        else
            message = ChatColor.RED + "You have been permanently banned from DungeonRealms." + (reason != null && !reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net";

        return message;
    }

    public static String getMutedMessage(UUID uuid) {
        if (!isMuted(uuid)) return null;

        long muteTime = (long) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_TIME);
        String reason = (String) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_REASON);

        return ChatColor.RED + "You will be unmuted until " + timeString((int) ((muteTime - System.currentTimeMillis()) / 60000)) + (reason != null ? " for " + reason : "");
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
    public static void kick(String playerName, String kickMessage, Consumer<UUID> doBefore) {
        String uuidString = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        UUID uuid = !uuidString.equals("") ? UUID.fromString(uuidString) : null;

        // HANDLE LOG OUT //
        doBefore.accept(uuid);

        //SEND BUNGEE MESSAGE TO KICK PLAYER FROM PROXY //
        BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", playerName, kickMessage);
    }


    public static boolean isBanned(UUID uuid) {
        try {
            long banTime = ((Long) DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME));
            return banTime == -1 || banTime != 0 && System.currentTimeMillis() < banTime;
        } catch (NullPointerException ignored) {
            return false;
        }
    }

    public static boolean isMuted(UUID uuid) {
        long muteTime = ((Long) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_TIME));
        return (muteTime != 0 && System.currentTimeMillis() < muteTime);
    }

    public static String timeString(int totalMinutes) {
        String timeStr = "";

        int totalMins = totalMinutes;
        int totalHours = totalMins / 60;
        int totalDays = totalHours / 24;
        int remainingMins = totalMins % 60;
        int remainingHours = totalHours % 24;
        if (totalDays > 0) {
            timeStr = timeStr + Integer.toString(totalDays) + " day";
            if (totalDays > 1) {
                timeStr = timeStr + "s";
            }
        }
        if (totalHours > 0) {
            int hours = totalHours;
            if (totalDays > 0) {
                hours = remainingHours;
                if (remainingHours > 0) {
                    if (remainingMins > 0) {
                        timeStr = timeStr + ", ";
                    } else {
                        timeStr = timeStr + " and ";
                    }
                    timeStr = timeStr + Integer.toString(hours) + " hour";
                    if (hours > 1) {
                        timeStr = timeStr + "s";
                    }
                }
            } else {
                timeStr = timeStr + Integer.toString(hours) + " hour";
                if (hours > 1) {
                    timeStr = timeStr + "s";
                }
            }
        }
        if (totalMins > 0) {
            if (totalDays > 0) {
                if (remainingMins > 0) {
                    if (remainingHours > 0) {
                        timeStr = timeStr + ", and ";
                    } else {
                        timeStr = timeStr + " and ";
                    }
                }
            } else if ((totalHours > 0) &&
                    (remainingMins > 0)) {
                timeStr = timeStr + " and ";
            }
            int mins = totalMins;
            if ((totalDays > 0) || (totalHours > 0)) {
                mins = remainingMins;
            }
            if (mins > 0) {
                timeStr = timeStr + Integer.toString(mins) + " minute";
                if (mins > 1) {
                    timeStr = timeStr + "s";
                }
            }
        }
        if (totalMins < 1) {
            timeStr = "less than a minute";
        }

        return Character.toUpperCase(timeStr.charAt(0)) + timeStr.substring(1).toLowerCase();
    }

}
