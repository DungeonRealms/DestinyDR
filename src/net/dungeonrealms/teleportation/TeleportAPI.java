package net.dungeonrealms.teleportation;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
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

    public static boolean isTeleportBook(ItemStack itemStack) {
        if (itemStack.getType() != Material.BOOK) {
            return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        Bukkit.broadcastMessage(tag.getString("type"));
        Bukkit.broadcastMessage(tag.getString("usage"));
        if (tag == null || nmsItem == null) {
            return false;
        }
        if (!(tag.getString("type").equalsIgnoreCase("teleport"))) {
            return false;
        }
        return true;
    }

    public static boolean isHearthstone(ItemStack itemStack) {
        if (itemStack.getType() != Material.QUARTZ) {
            return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || nmsItem == null) {
            return false;
        }
        if (!(tag.getString("type").equalsIgnoreCase("important") && tag.getString("usage").equalsIgnoreCase("hearthstone"))) {
            return false;
        }
        return true;
    }

    public static String getLocationFromDatabase(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid) != null) {
            return DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString();
        } else {
            return "cyrennica";
        }
    }

    public static Location getLocationFromString(String location) {
        switch (location) {
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
            default: {
                return Teleportation.Cyrennica;
            }
        }
    }

    public static String getRandomTeleportString() {
        switch (new Random().nextInt(6)) {
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
            default: {
                return "Cyrennica";
            }
        }
    }
}
