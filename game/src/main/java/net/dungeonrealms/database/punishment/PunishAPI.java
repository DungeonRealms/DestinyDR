package net.dungeonrealms.database.punishment;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/1/2016
 */

public class PunishAPI {

    /**
     * Method to ban players asynchronously.
     *
     * @param uuid             UUID
     * @param playerName       Target
     * @param duration         Set as 0 for permanent ban
     * @param reason           Leave empty for no reason
     */
    public static void ban(UUID uuid, String playerName, int punisherID, long duration, String reason, Consumer<UUID> doBefore) {
        if (uuid == null) return;

        SQLDatabaseAPI.getInstance().executeUpdate(updates -> Bukkit.getLogger().info("Updated " + uuid.toString() + "'s Ban to the database.."), QueryType.INSERT_BAN.getQuery(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid), "ban", System.currentTimeMillis(), duration != 0 ? System.currentTimeMillis() + duration * 1000 : duration, punisherID, reason.isEmpty() ? "N/A" : reason, 0));

        // KICK PLAYER //
        if (duration == 0)
            kick(playerName, ChatColor.RED + "You have been permanently banned from DungeonRealms." + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);
        else
            kick(playerName, ChatColor.RED + "You are banned until " + timeString((int) (duration / 60)) + (!reason.equals("") ? " for " + reason : "") + "\n\n Appeal at: www.dungeonrealms.net", doBefore);

        PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
            //Update if online for some reason?
            wrapper.setWhoBannedMeID(punisherID);
            wrapper.setBanExpire(duration);
            wrapper.setBanReason(reason);
        });
    }

    /**
     * Method to mute players (only works for currently online players)
     *
     * @param duration Set as 0 for permanent ban
     * @param reason   Leave empty for no reason
     */
    public static void mute(PlayerWrapper wrapper, long duration, String reason, Consumer<PlayerWrapper> doAfter) {
        if (wrapper == null) return;
        // MUTE PLAYER //

        wrapper.setMuteExpire(System.currentTimeMillis() + duration * 1000);
//        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_TIME, System.currentTimeMillis() + duration * 1000, false);

        if (!reason.equals(""))
            wrapper.setMuteReason(reason);
//            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.MUTE_REASON, reason, false);

        doAfter.accept(wrapper);
    }

    public static String getMutedMessage(UUID uuid) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);

        return ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "You will be unmuted in " + wrapper.getTimeWhenMuteExpires() + (wrapper.getMuteReason() != null ? ", Mute reason: " + wrapper.getMuteReason() : "");
    }

    /**
     * Method to unban players asynchronously (no callback).
     *
     * @param uuid Target
     */
    public static void unban(UUID uuid) {
        if (uuid == null) return;

        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
        PlayerWrapper.getPlayerWrapper(uuid, false, false,
                wrapper -> SQLDatabaseAPI.getInstance().executeUpdate(updates -> Bukkit.getLogger().info("Unbanned " + wrapper.getUsername() + "... CACHED: " + accountID),
                QueryType.UNBAN_PLAYER.getQuery(wrapper.getAccountID())));
    }

    /**
     * Method to unmute players
     *
     * @param uuid Target
     */
    public static void unmute(UUID uuid) {
        if (uuid == null) return;

        PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
            if (!wrapper.isMuted()) return;
            SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
                //Update the playerData across shards.
                if (wrapper.getPlayer() == null || !wrapper.getPlayer().isOnline())
                    GameAPI.updatePlayerData(uuid, UpdateType.MUTE);
                else {
                    wrapper.setMuteReason(null);
                    wrapper.setMuteExpire(0);
                    wrapper.getPlayer().sendMessage(ChatColor.RED + "You have been unmuted.");
                }

            }, QueryType.UNMUTE_PLAYER.getQuery(wrapper.getAccountID()));
        });
    }

    /**
     * Kicks player from proxy
     *
     * @param playerName  Target
     * @param kickMessage Kick message for player if they're connected to the proxy
     */
    public static void kick(String playerName, String kickMessage, Consumer<UUID> doBefore) {
        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (UUID uuid) -> {
            if (uuid != null) {
                if (doBefore != null)
                    doBefore.accept(uuid);
            }
            //Kick from bungee..
            Player local = Bukkit.getPlayer(playerName);
            if(local != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> local.kickPlayer("kickMessage"));
            }
            BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", playerName, kickMessage);
        });
    }

    /**
     * Method to check if a player is banned (async callback).
     *
     * @param uuid
     * @return
     */
    public static void isBanned(UUID uuid, Consumer<Boolean> bannedCallback) {
        PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
            wrapper.loadPunishment(false, null);
            bannedCallback.accept(wrapper.isBanned());
        });
//        try {
//
//            Document bansDoc = DatabaseInstance.bans.find(Filters.eq("bans.uuid", uuid.toString())).first();
//            if (bansDoc == null) return false;
//            Long banTime = ((Document) bansDoc.get("bans")).getLong("bannedUntil");
//            Constants.log.info(String.valueOf(banTime == 0 || banTime != 0 && System.currentTimeMillis() < banTime) + "");
//            return banTime == 0 || System.currentTimeMillis() < banTime;
//        } catch (NullPointerException ignored) {
//            return false;
//        }
    }

    public static boolean isMuted(UUID uuid) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        return wrapper != null && wrapper.isMuted();
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
