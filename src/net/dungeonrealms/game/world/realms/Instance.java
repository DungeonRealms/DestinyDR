package net.dungeonrealms.game.world.realms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.Bogdacutu.VoidGenerator.VoidGeneratorGenerator;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.RealmManager;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 11/12/2015.
 */
public class Instance implements GenericMechanic, Listener {

    static Instance instance = null;

    public static Instance getInstance() {
        if (instance == null) {
            instance = new Instance();
        }
        return instance;
    }

    public CopyOnWriteArrayList<RealmObject> CURRENT_REALMS = new CopyOnWriteArrayList<>();
    public List<Player> PENDING_REALMS = new ArrayList<>();

    File pluginFolder = null;
    File rootFolder = null;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
        pluginFolder = DungeonRealms.getInstance().getDataFolder();
        rootFolder = new File(System.getProperty("user.dir"));

        try {
            FileUtils.forceMkdir(new File(RealmManager.getInstance().ROOT_DIR + "/realms/down"));
            FileUtils.forceMkdir(new File(RealmManager.getInstance().ROOT_DIR + "/realms/up"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public class RealmObject {

        private Player realmOwner;
        private Location portalLocation;
        private ArrayList<Player> playerList;
        private Hologram realmHologram;
        private ArrayList<Player> realmBuilders;
        private boolean isRealmPortalOpen;

        public RealmObject(Player realmOwner, Location portalLocation, ArrayList<Player> playerList, Hologram realmHologram, ArrayList<Player> realmBuilders, boolean isRealmPortalOpen) {
            this.realmOwner = realmOwner;
            this.portalLocation = portalLocation;
            this.playerList = playerList;
            this.realmHologram = realmHologram;
            this.realmBuilders = realmBuilders;
            this.isRealmPortalOpen = isRealmPortalOpen;
        }

        public Player getRealmOwner() {
            return realmOwner;
        }

        public Location getLocation() {
            return portalLocation;
        }

        public ArrayList<Player> getPlayerList() {
            return playerList;
        }

        public Hologram getRealmHologram() {
            return realmHologram;
        }

        public ArrayList<Player> getRealmBuilders() {
            return realmBuilders;
        }

        public boolean isRealmPortalOpen() {
            return isRealmPortalOpen;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void changeWorld(PlayerChangedWorldEvent event) {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDropItemInRealm(PlayerDropItemEvent event) {
        if (API.getGamePlayer(event.getPlayer()) == null) return;
        if (!API.getGamePlayer(event.getPlayer()).isInRealm()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items in Realms. If you wish give them to another player, please trade them.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelPlayersBlockOpenInRealm(PlayerInteractEvent event) {
        if (API.getGamePlayer(event.getPlayer()) == null) return;
        if (!API.getGamePlayer(event.getPlayer()).isInRealm()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Material mat = block.getType();
        if (mat != Material.CHEST) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "This block shouldn't be in a Realm... How'd it get here?");
    }

    /**
     * Handles a player breaking a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBreakBlockInRealm(BlockBreakEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().getWorld().getName().contains("DUEL")) return;
        if (event.getBlock().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break Portal blocks!");
        }
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (!FriendHandler.getInstance().areFriends(event.getPlayer(), Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmOwner().getUniqueId())) {
            event.setCancelled(true);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to their friends list!");
        }
        /*if (!Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
            event.setCancelled(true);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(net.md_5.bungee.api.ChatColor.RED + "You cannot break blocks in this realm, please ask the owner to add you to the builders list!");
        }*/
    }

    /**
     * Handles a player placing a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPlaceBlockInRealm(BlockPlaceEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getBlockPlaced().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place Portal blocks!");
            return;
        }
        if (event.getBlockAgainst().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks on-top of Portal blocks!");
            return;
        }
        if (!FriendHandler.getInstance().areFriends(event.getPlayer(), Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmOwner().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to their friends list!");
        }
    }


    /**
     * Handles a player entering a portal,
     * teleports them to wherever they should
     * be, or cancels it if they're in combat
     * etc.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }
            if (!CombatLog.isInCombat(event.getPlayer())) {
                if (Instance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()) != null) {
                    String locationAsString = event.getFrom().getX() + "," + (event.getFrom().getY() + 1) + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
                    DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
                    event.setTo(Instance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()));
                    Instance.getInstance().addPlayerToRealmList(event.getPlayer(), Instance.getInstance().getRealmViaLocation(event.getFrom()));
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
        } else {
            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId()).equals("")) {
                String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId())).split(",");
                event.setTo(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                Instance.getInstance().removePlayerFromRealmList(event.getPlayer(), Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()));
            } else {
                Location realmPortalLocation = Instance.getInstance().getPortalLocationFromRealmWorld(event.getPlayer());
                event.setTo(realmPortalLocation.clone().add(0, 2, 0));
            }
            event.getPlayer().setFlying(false);
        }
    }

    public void openRealm(Player player) {
        if (!doesRemoteRealmExist(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Your realm does not exist remotely! Creating you a new realm!");
            createTemplate(player);
            generateBlankRealmWorld(player);
        } else {
            player.sendMessage(ChatColor.RED + "Your realm exist remotely! Downloading it now ...");
            downloadRealm(player.getUniqueId());
        }
    }

    public void createTemplate(Player player) {
        //Create the player realm folder
        new File(player.getUniqueId().toString()).mkdir();
        //Unzip the local template.
        try {
            ZipFile zipFile = new ZipFile(pluginFolder.getAbsolutePath() + "/realms/" + "realm_template.zip");
            zipFile.extractAll(rootFolder.getAbsolutePath() + "/" + player.getUniqueId().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateBlankRealmWorld(Player owner) {
        WorldCreator worldCreator = new WorldCreator(owner.getUniqueId().toString());
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        worldCreator.generator(new VoidGeneratorGenerator());
        World world = Bukkit.createWorld(worldCreator);
        world.setSpawnLocation(24, 130, 24);
        world.getBlockAt(0, 64, 0).setType(Material.AIR);
        int x, y = 128, z;
        Vector vector = new Vector(16, 128, 16);

        for (x = vector.getBlockX(); x < 32; x++) {
            for (z = vector.getBlockZ(); z < 32; z++) {
                world.getBlockAt(new Location(world, x, y, z)).setType(Material.GRASS);
            }
        }
        for (x = vector.getBlockX(); x < 32; x++) {
            for (y = 127; y >= 112; y--) {
                for (z = vector.getBlockZ(); z < 32; z++) {
                    world.getBlockAt(new Location(world, x, y, z)).setType(Material.DIRT);
                }
            }
        }
        for (x = vector.getBlockX(); x < 32; x++) {
            for (z = vector.getBlockZ(); z < 32; z++) {
                world.getBlockAt(new Location(world, x, y, z)).setType(Material.BEDROCK);
            }
        }

        Location portalLocation = world.getSpawnLocation().clone();
        portalLocation.getBlock().setType(Material.PORTAL);
        portalLocation.subtract(0, 1, 0).getBlock().setType(Material.PORTAL);
        portalLocation.add(0, 1, 0);

        Utils.log.info("[REALMS] Blank Realm generated for player " + owner.getUniqueId().toString());
    }

    public void loadInWorld(Player player) {
        Utils.log.info("[REALM] [ASYNC] Loading realm into world for: " + player.getName());
        WorldCreator worldCreator = new WorldCreator(player.getUniqueId().toString());
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        World w = Bukkit.getServer().createWorld(worldCreator);
        w.setKeepSpawnInMemory(false);
        w.setAutoSave(false);
        w.setStorm(false);
        w.setMonsterSpawnLimit(0);
        Bukkit.getWorlds().add(w);
    }

	public void uploadRealm(UUID uuid) {
		RealmManager.getInstance().uploadRealm(uuid);
	}
	public void downloadRealm(UUID uuid) {
        RealmManager.getInstance().downloadRealm(uuid);
	}
	
    public boolean doesRemoteRealmExist(String uuid) {
		try {
			URL url = new URL("ftp://" + RealmManager.USER + ":" + RealmManager.PASSWORD + "@" + RealmManager.HOST + RealmManager.ROOT_DIR + uuid.toString() + ".zip");
			url.openConnection();

		} catch (IOException first_login) {
            return false;
		}
		return false;
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param realmObject The Realm.
     * @since 1.0
     */
    public void removeRealm(RealmObject realmObject, boolean playerLoggingOut) {
        realmObject.isRealmPortalOpen = false;
        Location portalLocation = realmObject.getLocation();
        realmObject.portalLocation.setX(0);
        realmObject.portalLocation.setY(0);
        realmObject.portalLocation.setZ(0);
        portalLocation.add(0, 1, 0).getBlock().setType(Material.AIR);
        portalLocation.add(0, 1, 0).getBlock().setType(Material.AIR);
        realmObject.getRealmHologram().delete();
        if (playerLoggingOut) {
            realmObject.getPlayerList().stream().forEach(player -> {
                if (!player.getWorld().getName().contains("DUNGEON") && !player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    player.sendMessage(ChatColor.RED + "This Realm has been closed!");
                    player.setFlying(false);
                    String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, player.getUniqueId())).split(",");
                    player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                }
            });
            Bukkit.getWorld(realmObject.getRealmOwner().getUniqueId().toString()).save();
            Bukkit.unloadWorld(realmObject.getRealmOwner().getUniqueId().toString(), true);
            Utils.log.info("[REALMS] Unloading world: " + realmObject.getRealmOwner().getUniqueId().toString() + " in preparation for deletion!");
            CURRENT_REALMS.remove(realmObject);
            uploadRealm(realmObject.getRealmOwner().getUniqueId());
        }
    }

    /**
     * Gets the realm of a player
     *
     * @since 1.0
     */
    public RealmObject getPlayerRealm(Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (realmObject.getRealmOwner().getUniqueId().equals(player.getUniqueId())) {
                    return realmObject;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Gets the current world (realm) a player is in
     *
     * @since 1.0
     */
    public RealmObject getPlayersCurrentRealm(Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (realmObject.getRealmOwner().getUniqueId().toString().equals(player.getWorld().getName())) {
                    return realmObject;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Removes a players realm
     *
     * @since 1.0
     */
    public void removePlayerRealm(Player player, boolean playerLoggingOut) {
        if (getPlayerRealm(player) != null) {
            removeRealm(getPlayerRealm(player), playerLoggingOut);
        }
    }

    /**
     * Opens a players realm and creates
     * the Portal Blocks.
     *
     * @since 1.0
     */
    public void tryToOpenRealm(Player player, Location clickLocation) {
        if (getPlayerRealm(player) == null || !getPlayerRealm(player).isRealmPortalOpen()) {
            if (CombatLog.isInCombat(player)) {
                player.sendMessage(ChatColor.RED + "Cannot open Realm while in Combat!");
                return;
            }
            if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                player.sendMessage(ChatColor.RED + "You can only open a realm portal in the main world!");
                return;
            }
            final Location portalLocation = clickLocation.clone();
            if (!(portalLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) || !(portalLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR)
                    || clickLocation.clone().getBlock().getType() == Material.CHEST || clickLocation.clone().getBlock().getType() == Material.ENDER_CHEST
                    || clickLocation.clone().getBlock().getType() == Material.PORTAL || clickLocation.clone().getBlock().getType() == Material.ANVIL) {
                player.sendMessage(ChatColor.RED + "You cannot open a realm portal here!");
                return;
            }
            if (LootManager.checkLocationForLootSpawner(clickLocation.clone())) {
                player.sendMessage(ChatColor.RED + "You cannot place a realm portal this close to a Loot Spawning location");
                return;
            }
            if (API.isMaterialNearby(clickLocation.clone().getBlock(), 3, Material.LADDER) || API.isMaterialNearby(clickLocation.clone().getBlock(), 10, Material.ENDER_CHEST)) {
                player.sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                return;
            }
            if (isThereARealmPortalNearby(clickLocation.clone().add(0, 1, 0), 6) || API.isMaterialNearby(clickLocation.clone().getBlock(), 6, Material.PORTAL)) {
                player.sendMessage(ChatColor.RED + "You cannot place a portal so close to another! (Min 3 Blocks)");
                return;
            }
            for (Player player1 : Bukkit.getWorlds().get(0).getPlayers()) {
                if (player1.getName().equals(player.getName())) {
                    continue;
                }
                if (!player1.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (player1.getLocation().distanceSquared(player.getLocation()) <= 2) {
                    player.sendMessage(ChatColor.RED + "You cannot place your realm portal near another player");
                    return;
                }
            }
            if (PENDING_REALMS.contains(player)) {
                player.sendMessage(ChatColor.RED + "Your realm is currently being generated, please be patient.");
                return;
            }
            PENDING_REALMS.add(player);
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is loading now!");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
                portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
                Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), portalLocation.add(0.5, 1.5, 0.5));
                KarmaHandler.EnumPlayerAlignments playerAlignment = KarmaHandler.EnumPlayerAlignments.getByName(KarmaHandler.getInstance().getPlayerRawAlignment(player));
                realmHologram.appendTextLine(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " [" + playerAlignment.getAlignmentColor() + playerAlignment.name().toUpperCase() + ChatColor.GOLD + "]");
                realmHologram.getVisibilityManager().setVisibleByDefault(true);
                RealmObject realmObject = new RealmObject(player, clickLocation, new ArrayList<>(), realmHologram, new ArrayList<>(), true);
                realmObject.getRealmBuilders().add(player);
                realmObject.getPlayerList().add(player);
                CURRENT_REALMS.add(realmObject);
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready!");
                if (PENDING_REALMS.contains(player)) {
                    PENDING_REALMS.remove(player);
                }
            }, 100L);
            if (!doesRemoteRealmExist(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "Your realm does not exist remotely! Creating you a new realm!");
                createTemplate(player);
                generateBlankRealmWorld(player);
            } else if (doesRemoteRealmExist(player.getUniqueId().toString()) && !isRealmLoaded(player.getUniqueId())) {
                Utils.log.info("[REALMS] Player " + player.getUniqueId().toString() + "'s Realm existed locally, loading it!");
                downloadRealm(player.getUniqueId());
            }
        } else {
            player.sendMessage(ChatColor.RED + "You already have a Realm Portal in the world, please destroy it!");
        }
    }

    /**
     * Checks if there is a Realm Portal nearby.
     *
     * @since 1.0
     */
    public boolean isThereARealmPortalNearby(Location location, int radius) {
        double rad = Math.pow(radius, 2);
        for (RealmObject realmObject : CURRENT_REALMS) {
            if (realmObject.getLocation().distanceSquared(location.clone()) <= rad) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a realm is loaded.
     *
     * @since 1.0
     */
    public boolean isRealmLoaded(UUID uuid) {
        return Bukkit.getServer().getWorlds().contains(Bukkit.getWorld(uuid.toString()));
    }

    /**
     * Removes a realm via its portal location.
     *
     * @since 1.0
     */
    public void removeRealmViaPortalLocation(Location location) {
        CURRENT_REALMS.stream().filter(realmObject -> location.distanceSquared(realmObject.getLocation()) <= 4).forEach(realmObject -> removeRealm(realmObject, false));
    }


    /**
     * Gets a Realm Spawn Location from
     * the location of a Portal in the
     * main world.
     *
     * @return Location (The Location)
     * @since 1.0
     */
    public Location getRealmLocation(Location location, Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (location.distanceSquared(realmObject.getLocation()) <= 4) {
                    if (Bukkit.getWorld(realmObject.getRealmOwner().getUniqueId().toString()) == null) {
                        return null;
                    }
                    realmObject.getPlayerList().add(player);
                    return Bukkit.getWorld(realmObject.getRealmOwner().getUniqueId().toString()).getSpawnLocation();
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "There are no Realms currently, no clue how this portal got here!");
            location.getBlock().setType(Material.AIR);
            if (location.subtract(0, 1, 0).getBlock().getType() == Material.PORTAL) {
                location.getBlock().setType(Material.AIR);
            }
            if (location.add(0, 2, 0).getBlock().getType() == Material.PORTAL) {
                location.getBlock().setType(Material.AIR);
            }
            return null;
        }
        return null;
    }

    /**
     * Gets a Realm Portal location from
     * a player object that is currently
     * in THE realm.
     *
     * @return Location (The Location)
     * @since 1.0
     */
    public Location getPortalLocationFromRealmWorld(Player player) {
        for (RealmObject realmObject : CURRENT_REALMS) {
            if (player.getWorld().getName().equalsIgnoreCase(realmObject.getRealmOwner().getUniqueId().toString())) {
                realmObject.getPlayerList().remove(player);
                return realmObject.getLocation();
            }
        }
        return Teleportation.Cyrennica;
    }

    /**
     * Gets a Realm Object from
     * the location of a Portal in the
     * main world.
     *
     * @return Location (The Location)
     * @since 1.0
     */
    public RealmObject getRealmViaLocation(Location location) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (location.distanceSquared(realmObject.getLocation()) <= 4) {
                    return realmObject;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    public void addPlayerToRealmList(Player player, RealmObject realmObject) {
        if (!realmObject.getPlayerList().contains(player)) {
            realmObject.getPlayerList().add(player);
        }
    }

    public void removePlayerFromRealmList(Player player, RealmObject realmObject) {
        if (realmObject.getPlayerList().contains(player)) {
            realmObject.getPlayerList().remove(player);
        }
    }

    public void addPlayerToRealmBuilders(Player player, RealmObject realmObject) {
        if (!realmObject.getRealmBuilders().contains(player)) {
            realmObject.getRealmBuilders().add(player);
        }
    }

    public void removePlayerFromRealmBuilders(Player player, RealmObject realmObject) {
        if (realmObject.getRealmBuilders().contains(player)) {
            realmObject.getRealmBuilders().remove(player);
        }
    }


    @Override
    public void stopInvocation() {

    }
}
