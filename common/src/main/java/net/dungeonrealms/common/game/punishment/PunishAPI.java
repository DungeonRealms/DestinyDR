package net.dungeonrealms.common.game.punishment;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseDriver;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/1/2016
 */

public class PunishAPI {

    /**
     * Method to ban players asynchronously.
     *
     * @param uuid       UUID
     * @param playerName Target
     * @param sourceThatBanned
     * @param duration   Set as -1 for permanent ban
     * @param reason     Leave empty for no reason
     */
    public static void ban(UUID uuid, String playerName, String sourceThatBanned, long duration, String reason, Consumer<UUID> doBefore) {
        if (uuid == null) return;

        // KICK PLAYER //
        if (duration == -1)
            kick(playerName, ChatColor.RED + "You have been permanently banned from DungeonRealms." + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);
        else
            kick(playerName, ChatColor.RED + "You are banned until " + timeString((int) (duration / 60)) + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);

        DatabaseAPI.getInstance().getPool().submit(() -> {
            UpdateOptions uo = new UpdateOptions();
            uo.upsert(true);

            DatabaseDriver.bans.updateMany(Filters.eq("bans.uuid", uuid.toString()), new Document(EnumOperators.$SET.getUO
                    (), new Document("bans.bannedUntil", (duration != -1 ? System.currentTimeMillis() + (duration *
                    1000) : -1)).append("bans.bannedBy", sourceThatBanned)), uo);

            if (!reason.equals(""))
                DatabaseDriver.bans.updateOne(Filters.eq("bans.uuid", uuid.toString()), new Document(EnumOperators.$SET.getUO
                        (), new Document("bans.reason", reason)));
        });
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
        // MUTE PLAYER //
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_TIME, System.currentTimeMillis() + (duration * 1000), false);

        if (!reason.equals(""))
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_REASON, reason, false);

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
        long muteTime = (long) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_TIME);
        String reason = (String) DatabaseAPI.getInstance().getValue(uuid, EnumData.MUTE_REASON);

        return ChatColor.RED + "You will be unmuted until " + timeString((int) ((muteTime - System.currentTimeMillis()) / 60000)) + (reason != null ? " for " + reason : "");
    }

    /**
     * Method to unban players asynchronously (no callback).
     *
     * @param uuid Target
     */
    public static void unban(UUID uuid) {
        if (uuid == null) return;

        DatabaseAPI.getInstance().getPool().submit(() -> {
            UpdateOptions uo = new UpdateOptions();
            uo.upsert(true);

            DatabaseDriver.bans.updateMany(Filters.eq("bans.uuid", uuid.toString()), new Document(EnumOperators.$SET
                    .getUO(), new Document("bans.bannedUntil", 0L).append("bans.reason", "")), uo);
        });
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
        if (doBefore != null)
            doBefore.accept(uuid);

        //SEND BUNGEE MESSAGE TO KICK PLAYER FROM PROXY //
        BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", playerName, kickMessage);
    }

    /**
     * Method to check if a player is banned (sync).
     *
     * @param uuid
     * @return
     */
    public static boolean isBanned(UUID uuid) {
        try {
            Document bansDoc = DatabaseDriver.bans.find(Filters.eq("bans.uuid", uuid.toString())).first();
            if (bansDoc == null) return false;
            Long banTime = ((Document)bansDoc.get("bans")).getLong("bannedUntil");
            Constants.log.info(String.valueOf(banTime == -1 || banTime != 0 && System.currentTimeMillis() < banTime) + "");
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

        return timeStr.toLowerCase();
    }

}
