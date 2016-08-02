package net.dungeonrealms.game.world.teleportation;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
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
        if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.containsKey(player.getUniqueId())) {
            if (GameAPI.getGamePlayer(Bukkit.getPlayer(player.getUniqueId())).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    if (!TutorialIsland.getInstance().onTutorialIsland(player.getLocation())) {
                        if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(player.getUniqueId()) <= 0) {
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because it has not finished its cooldown" + " (" + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s)");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because have not yet completed our tutorial.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because you are not in the main world.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because of your unlawful alignment.");
            }
        }
        return false;
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
     * Checks if the item is a teleportation book
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isTeleportBook(ItemStack itemStack) {
        if (itemStack.getType() != Material.BOOK) {
            return false;
        }
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !(tag == null) && tag.getString("type").equalsIgnoreCase("teleport");
    }

    /**
     * Checks if the item is a hearthstone
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isHearthstone(ItemStack itemStack) {
        if (itemStack.getType() != Material.QUARTZ) {
            return false;
        }
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !(tag == null) && tag.getString("type").equalsIgnoreCase("important") && tag.getString("usage").equalsIgnoreCase("hearthstone");
    }

    /**
     * Gets the location of a players hearthstone from Mongo
     *
     * @param uuid
     * @return String
     * @since 1.0
     */
    public static String getLocationFromDatabase(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid) != null) {
            return Utils.ucfirst(DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString());
        } else {
            return "Cyrennica";
        }
    }

    public static boolean canTeleportToLocation(Player player, NBTTagCompound nbt) {
        String locationName;
        if (GameAPI.getGamePlayer(player) == null) {
            return false;
        }
        if (GameAPI.getGamePlayer(player).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            return true;
        }
        if (nbt != null) {
            locationName = nbt.getString("usage").toLowerCase();
        } else {
            locationName = "cyrennica";
        }
        Location location = TeleportAPI.getLocationFromString(locationName);
        return location.equals(Teleportation.Deadpeaks_Mountain_Camp);
    }

    /**
     * Gets the location of a teleport from a given string
     *
     * @param location
     * @return Location
     * @since 1.0
     */
    public static Location getLocationFromString(String location) {
        switch (location.toLowerCase()) {
            case "starter": {
                return Teleportation.Tutorial;
            }
            case "cyrennica": {
                return Teleportation.Cyrennica;
            }
            case "harrison_field": {
                return Teleportation.Harrison_Field;
            }
            case "dark_oak": {
                return Teleportation.Dark_Oak_Tavern;
            }
            case "trollsbane": {
                return Teleportation.Trollsbane_tavern;
            }
            case "tripoli": {
                return Teleportation.Tripoli;
            }
            case "gloomy_hollows": {
                return Teleportation.Gloomy_Hollows;
            }
            case "crestguard": {
                return Teleportation.Crestguard_Keep;
            }
            case "deadpeaks": {
                return Teleportation.Deadpeaks_Mountain_Camp;
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Gets the display name of a teleport location
     *
     * @param location
     * @return Location
     * @since 1.0
     */
    public static String getDisplayNameOfLocation(String location) {
        switch (location.toLowerCase()) {
            case "starter": {
                return "Tutorial Island";
            }
            case "cyrennica": {
                return "City of Cyrennica";
            }
            case "harrison_field": {
                return "Harrison Field";
            }
            case "dark_oak": {
                return "Dark Oak Tavern";
            }
            case "trollsbane": {
                return "Trollsbane Tavern";
            }
            case "tripoli": {
                return "Tripoli";
            }
            case "gloomy_hollows": {
                return "Gloomy Hollows";
            }
            case "crestguard": {
                return "Crestguard Keep";
            }
            case "deadpeaks": {
                return "DeadPeaks Mountain Camp" + ChatColor.RED + " WARNING: CHAOTIC ZONE";
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Returns a random string "location"
     *
     * @return String
     * @since 1.0
     */
    public static String getRandomTeleportString() {
        switch (new Random().nextInt(8)) {
            case 0: {
                return "Cyrennica";
            }
            case 1: {
                return "Harrison_Field";
            }
            case 2: {
                return "Dark_Oak";
            }
            case 3: {
                return "Trollsbane";
            }
            case 4: {
                return "Tripoli";
            }
            case 5: {
                return "Gloomy_Hollows";
            }
            case 6: {
                return "Crestguard";
            }
            case 7: {
                return "Deadpeaks";
            }
            default: {
                return "Cyrennica";
            }
        }
    }

    public static boolean canSetHearthstoneLocation(Player player, String hearthstoneLocation) {
        switch (hearthstoneLocation.toLowerCase()) {
            case "starter":
                return false;
            case "cyrennica":
                return true;
            case "harrison_field":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.HARRISONS_FIELD);
            case "dark_oak":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.DARKOAK);
            case "trollsbane":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.JAGGED_ROCKS);
            case "tripoli":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.TRIPOLI);
            case "gloomy_hollows":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.GLOOMY_HOLLOWS);
            case "crestguard":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.CREST_GUARD);
            case "deadpeaks":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.DEAD_PEAKS);
            default:
                return false;
        }
    }
}
