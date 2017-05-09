package net.dungeonrealms.game.world.teleportation;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class TeleportAPI {

    /**
     * Checks if the player can use their hearthstone
     *
     * @param player
     * @return boolean
     * @since 1.0
     */
    public static boolean canUseHearthstone(Player player) {
        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            player.sendMessage(ChatColor.RED + "You have been jailed.");
            return false;
        }
        if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(player.getUniqueId()) > 0) {
            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because it has not finished its cooldown" + " (" + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s)");
            return false;
        }
        if (TutorialIsland.onTutorialIsland(player.getLocation())) {
            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because have not yet completed our tutorial.");
            return false;
        }
        if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because you are not in the main world.");
            return false;
        }

        if (PlayerWrapper.getPlayerWrapper(player).getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because you are currently Chaotic.");
            return false;
        }

        if (!Teleportation.PLAYER_TELEPORT_COOLDOWNS.containsKey(player.getUniqueId())) {
            return false;
        }
        return true;
    }

    /**
     * Adds a cooldown to the players hearthstone
     *
     * @param uuid
     * @since 1.0
     */
    public static void addPlayerHearthstoneCD(UUID uuid, int cooldown) {
        Teleportation.PLAYER_TELEPORT_COOLDOWNS.put(uuid, cooldown);
    }

    /**
     * Adds the player to the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @since 1.0
     */
    public static void addPlayerCurrentlyTeleporting(UUID uuid, Location location) {
        Teleportation.PLAYERS_TELEPORTING.put(uuid, location);
    }

    /**
     * Checks if the player is in the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean isPlayerCurrentlyTeleporting(UUID uuid) {
        return Teleportation.PLAYERS_TELEPORTING.containsKey(uuid);
    }

    /**
     * Removes the player to the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @@return boolean
     * @since 1.0
     */
    public static void removePlayerCurrentlyTeleporting(UUID uuid) {
        if (Teleportation.PLAYERS_TELEPORTING.containsKey(uuid)) {
            Teleportation.PLAYERS_TELEPORTING.remove(uuid);
        }
    }

    /**
     * Gets the players cooldown on hearthstone usage
     *
     * @param uuid
     * @return int
     * @since 1.0
     */
    public static int getPlayerHearthstoneCD(UUID uuid) {
        return Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(uuid);
    }
    
    /**
     * Gets the location of a players hearthstone from Mongo
     *
     * @param uuid
     * @return String
     * @since 1.0
     */
    public static String getLocationFromDatabase(UUID uuid) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        return wrapper != null && wrapper.getHearthstone() != null ? wrapper.getHearthstone().getDisplayName() : "Cyrennica";
    }
}
