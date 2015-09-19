package net.dungeonrealms.teleportation;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class TeleportAPI {

    public static boolean canUseHearthstone(UUID uuid) {
        if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.containsKey(uuid)) {
            if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(uuid) <= 0) {
                return true;
            }
        }
        return false;
    }

    public static void addPlayerHearthstoneCD(UUID uuid, int cooldown) {
        Teleportation.PLAYER_TELEPORT_COOLDOWNS.put(uuid, cooldown);
    }

    public static void addPlayerCurrentlyTeleporting(UUID uuid, Location location) {
        Teleportation.PLAYERS_TELEPORTING.put(uuid, location);
    }

    public static boolean isPlayerCurrentlyTeleporting(UUID uuid) {
        return Teleportation.PLAYERS_TELEPORTING.containsKey(uuid);
    }

    public static boolean removePlayerCurrentlyTeleporting(UUID uuid) {
        if (Teleportation.PLAYERS_TELEPORTING.containsKey(uuid)) {
            Teleportation.PLAYERS_TELEPORTING.remove(uuid);
            return true;
        }
        return false;
    }

    public static int getPlayerHearthstoneCD(UUID uuid) {
        return Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(uuid);
    }
}
