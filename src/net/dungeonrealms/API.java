package net.dungeonrealms;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.teleportation.TeleportAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.rmi.activation.UnknownObjectException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class API {

    /**
     * Gets the WorldGuard plugin.
     *
     * @return
     * @since 1.0
     */
    private static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = DungeonRealms.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            try {
                throw new UnknownObjectException("getWorldGuard() of API.class is RETURNING NULL!");
            } catch (UnknownObjectException e) {
                e.printStackTrace();
            }
        }
        return (WorldGuardPlugin) plugin;
    }

    /**
     * Checks if player is in a region that denys PVP
     *
     * @param uuid
     * @since 1.0
     */
    public static boolean isInSafeRegion(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        ApplicableRegionSet region = getWorldGuard().getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation());
        return region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP);
    }

    /**
     * Will check the players region
     *
     * @param uuid
     * @param region
     * @return
     * @since 1.0
     */
    public static boolean isPlayerInRegion(UUID uuid, String region) {
        return getWorldGuard().getRegionManager(Bukkit.getPlayer(uuid).getWorld())
                .getApplicableRegions(Bukkit.getPlayer(uuid).getLocation()).getRegions().contains(region);
    }


    /**
     * Gets the a list of nearby players from a location within a given radius
     *
     * @param location
     * @param radius
     * @since 1.0
     */
    public static List<Player> getNearbyPlayers(Location location, int radius) {
        return location.getWorld().getPlayers().stream().filter(player -> location.distance(player.getLocation()) <= radius).collect(Collectors.toList());
    }


    /**
     * Safely logs out the player, updates their mongo inventories etc.
     *
     * @param uuid
     * @since 1.0
     */
    public static void handleLogout(UUID uuid) {
        if (BankMechanics.storage.containsKey(uuid)) {
            Inventory inv = BankMechanics.storage.get(uuid).inv;
            if (inv != null) {
                String serializedInv = ItemSerialization.toString(inv);
                BankMechanics.storage.remove(uuid);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "inventory.storage", serializedInv, false);
            }
        }
        PlayerInventory inv = Bukkit.getPlayer(uuid).getInventory();
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "inventory.player", ItemSerialization.toString(inv), false);
    }


    /**
     * Safely logs out all players when the server restarts
     *
     * @since 1.0
     */
    public static void logoutAllPlayers() {
        for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {
            Player p = (Player) Bukkit.getOnlinePlayers().toArray()[i];
            handleLogout(p.getUniqueId());
            p.kickPlayer("Server Restarting!");
        }
    }


    /**
     * Safely logs in the player, giving them their items, their storage
     * and their cooldowns
     *
     * @since 1.0
     */
    public static void handleLogin(UUID uuid) {
        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv).getContents();
            Bukkit.getPlayer(uuid).getInventory().setContents(items);
        }
        String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, uuid);
        if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
            Inventory inv = ItemSerialization.fromString(source);
            Storage storageTemp = new Storage(uuid, inv);
            BankMechanics.storage.put(uuid, storageTemp);
        } else {
            Storage storageTemp = new Storage(uuid);
            BankMechanics.storage.put(uuid, storageTemp);
        }
        TeleportAPI.addPlayerHearthstoneCD(uuid, 120);
        PlayerManager.checkInventory(uuid);

    }
}
