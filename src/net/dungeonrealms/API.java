package net.dungeonrealms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.pets.EnumPets;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.notice.Notice;
import net.dungeonrealms.party.Party;
import net.dungeonrealms.rank.Rank;
import net.dungeonrealms.rank.Subscription;
import net.dungeonrealms.teleportation.TeleportAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class API {

    /**
     * Will return the players
     * IP,Country,Zipcode,region,region_name,City,time_zone, and geo cordinates in the world.
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public static JsonObject getPlayerCredentials(UUID uuid) {
        URL url = null;
        try {
            url = new URL("http://freegeoip.net/json/");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            return root.getAsJsonObject();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets players UUID from Name. ASYNC.
     *
     * @param name
     * @return
     */
    public static UUID getUUIDFromName(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + System.currentTimeMillis() / 1000l);

            Reader in = new InputStreamReader(url.openStream());
            Object json = JSONValue.parse(in);

            JSONObject array = (JSONObject) json;

            String rawInput = (String) array.get("id");
            StringBuilder input = new StringBuilder(rawInput);

            input.insert(8, "-");
            input.insert(13, "-");
            input.insert(18, "-");
            input.insert(23, "-");

            return UUID.fromString(input.toString());
        } catch (Exception ex) {
            Utils.log.warning("[API] [getUUIDFromName] an invalid name has been inputted!");
        }
        return null;
    }

    /**
     * Gets players name from UUID. ASYNC.
     *
     * @param UUID
     * @return
     */
    public static String getNameFromUUID(String UUID) {
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + UUID.replaceAll("-", "") + "/names");

            Reader in = new InputStreamReader(url.openStream());
            Object json = JSONValue.parse(in);

            JSONArray array = (JSONArray) json;

            return array.get(array.size() - 1).toString().split("\"")[3];
        } catch (Exception ex) {
            Utils.log.warning("[API] [getNameFromUUID] Unable to find name with UUID.");
        }
        return null;
    }

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
        Player player = Bukkit.getPlayer(uuid);
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
        EnergyHandler.getInstance().handleLogoutEvents(player);
        HealthHandler.getInstance().handleLogoutEvents(player);
        KarmaHandler.getInstance().handleLogoutEvents(player);
        Party.getInstance().handleLogout(player);
        ScoreboardHandler.getInstance().removePlayerScoreboard(player);
    }


    /**
     * Safely logs out all players when the server restarts
     *
     * @since 1.0
     */
    public static void logoutAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleLogout(player.getUniqueId());
            player.kickPlayer("Server Restarting!");
        }
    }


    /**
     * Safely logs in the player, giving them their items, their storage
     * and their cooldowns
     *
     * @since 1.0
     */
    public static void handleLogin(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
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
        TeleportAPI.addPlayerHearthstoneCD(uuid, 150);
        PlayerManager.checkInventory(uuid);
        EnergyHandler.getInstance().handleLoginEvents(player);
        HealthHandler.getInstance().handleLoginEvents(player);
        KarmaHandler.getInstance().handleLoginEvents(player);

        //Essentials
        Subscription.getInstance().doAdd(uuid);
        Subscription.getInstance().handleJoin(player);
        Rank.getInstance().doGet(uuid);
        //Guilds
        Guild.getInstance().doGet(uuid);
        Guild.getInstance().doLogin(player);

        //Notices
        Notice.getInstance().doLogin(player);

        //Scoreboard Safety
        ScoreboardHandler.getInstance().matchMainScoreboard(player);
    }

    /**
     * Returns if a player is online. (LOCAL SERVER)
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * Returns the string is a Pet
     *
     * @param petType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringPet(String petType) {
        return EnumPets.getByName(petType.toUpperCase()) != null;
    }

    /**
     * Returns the string is a Mount
     *
     * @param mountType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringMount(String mountType) {
        return EnumMounts.getByName(mountType.toUpperCase()) != null;
    }

    /**
     * Returns the string is a Particle Trail
     *
     * @param trailType
     * @return boolean
     * @since 1.0
     */
    public static boolean isStringTrail(String trailType) {
        return ParticleAPI.ParticleEffect.getByName(trailType.toUpperCase()) != null;
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof Player && !(entity.hasMetadata("NPC") || entity.hasMetadata("npc"));
    }

	/**
     * Returns a list of nearby monsters
     * defined via their "type" metadata.
     *
	 * @param location
	 * @param radius
	 * @return List
     *@since 1.0
	 */
    public static List<Entity> getNearbyMonsters(Location location, int radius) {
        return location.getWorld().getEntities().stream().filter(mons -> mons.getLocation().distance(location) <= radius && mons.hasMetadata("type") && mons.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")).collect(Collectors.toList());
    }

}
