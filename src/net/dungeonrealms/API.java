package net.dungeonrealms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.activation.UnknownObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.pets.EnumPets;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mastery.NameFetcher;
import net.dungeonrealms.mastery.RealmManager;
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

/**
 * Created by Nick on 9/17/2015.
 */
public class API {

	public static CopyOnWriteArrayList<GamePlayer> GAMEPLAYERS = new CopyOnWriteArrayList<>();
	
	
    /**
     * To get the players region.
     *
     * @param location The location
     * @return The region name
     * @since 1.0
     */
    public static String getRegionName(Location location) {

        try {
            ApplicableRegionSet set = WorldGuardPlugin.inst().getRegionManager(location.getWorld()).getApplicableRegions(location);
            if (set.size() == 0)
                return "";

            String returning = "";
            int priority = -1;
            for (ProtectedRegion s : set) {
                if (s.getPriority() > priority) {
                    if (!s.getId().equals("")) {
                        returning = s.getId();
                        priority = s.getPriority();
                    }
                }
            }

            return returning;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

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

            JSONObject object = (JSONObject) json;

            String rawInput = (String) object.get("id");
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
     * @param playerUuid
     * @return
     */
    public static String getNameFromUUID(String playerUuid) {

        NameFetcher fetcher = new NameFetcher(Collections.singletonList(UUID.fromString(playerUuid)));

        try {
            return fetcher.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "BOB";
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
     * Checks if player is in a region that
     * denies PvP and Mob Damage
     *
     * @param location
     * @since 1.0
     */
    public static boolean isInSafeRegion(Location location) {
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
        return region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP) && region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
    }

    public static boolean isNonPvPRegion(Location location) {
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
        return region.getFlag(DefaultFlag.PVP) != null && !region.allows(DefaultFlag.PVP);
    }

    public static boolean isNonMobDamageRegion(Location location) {
        ApplicableRegionSet region = getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
        return region.getFlag(DefaultFlag.MOB_DAMAGE) != null && !region.allows(DefaultFlag.MOB_DAMAGE);
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
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv, false);
            }
        }
        PlayerInventory inv = player.getInventory();
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, ItemSerialization.toString(inv), false);
        String locationAsString = "-367,84,390,0,0"; //Cyrennica
        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            locationAsString = player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
        }
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, false);
        RealmManager.getInstance().removePlayerRealm(player);
        EnergyHandler.getInstance().handleLogoutEvents(player);
        HealthHandler.getInstance().handleLogoutEvents(player);
        KarmaHandler.getInstance().handleLogoutEvents(player);
        Party.getInstance().handleLogout(player);
        ScoreboardHandler.getInstance().removePlayerScoreboard(player);
        if (EntityAPI.hasPetOut(uuid)) {
            net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(uuid);
            pet.dead = true;
            EntityAPI.removePlayerPetList(uuid);
        }
        if (EntityAPI.hasMountOut(uuid)) {
            net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(uuid);
            mount.dead = true;
            EntityAPI.removePlayerMountList(uuid);
        }
        if(GAMEPLAYERS.size() > 0)
        for(GamePlayer gPlayer : GAMEPLAYERS){
        	if(gPlayer.getPlayer().getName().equalsIgnoreCase(player.getName())){
        		gPlayer.getStats().onLogOff();
        		GAMEPLAYERS.remove(gPlayer);
        	}
        }
        
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
        if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid).equals("")) {
            String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, uuid)).split(",");
            player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
        }
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
        try {
            return Bukkit.getServer().getPlayer(uuid).isOnline();
        } catch (Exception e) {
            return false;
        }
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


    /**
     * Returns if the entity is an actual player
     * and not a Citizens NPC
     *
     * @param entity
     * @return boolean
     * @since 1.0
     */
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
     * @since 1.0
     */
    public static List<Entity> getNearbyMonsters(Location location, int radius) {
        return location.getWorld().getEntities().stream().filter(mons -> mons.getLocation().distance(location) <= radius && mons.hasMetadata("type") && mons.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")).collect(Collectors.toList());
    }

    public static GamePlayer getGamePlayer(Player p ){
    	for(GamePlayer gPlayer : GAMEPLAYERS){
    		if(gPlayer.getPlayer().getName().equalsIgnoreCase(p.getName()))
    			return gPlayer;
    	}
		return null;
    }
    
    /**
     * Checks if there is a certain material nearby.
     *
     * @param block
     * @param maxradius
     * @param materialToSearchFor
     * @return Boolean (If the material is nearby).
     * @since 1.0
     */
    public static boolean isMaterialNearby(Block block, int maxradius, Material materialToSearchFor) {
        BlockFace[] faces = { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST };
        BlockFace[][] orth = { { BlockFace.NORTH, BlockFace.EAST }, { BlockFace.UP, BlockFace.EAST }, { BlockFace.NORTH, BlockFace.UP } };
        for (int r = 0; r <= maxradius; r++) {
            for (int s = 0; s < 6; s++) {
                BlockFace f = faces[s % 3];
                BlockFace[] o = orth[s % 3];
                if (s >= 3) {
                    f = f.getOppositeFace();
                }
                if (!(block.getRelative(f, r) == null)) {
                    Block c = block.getRelative(f, r);
                    for (int x = -r; x <= r; x++) {
                        for (int y = -r; y <= r; y++) {
                            Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                            if (a.getType() == materialToSearchFor) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
}
