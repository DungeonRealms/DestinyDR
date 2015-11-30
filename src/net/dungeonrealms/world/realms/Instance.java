package net.dungeonrealms.world.realms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.Bogdacutu.VoidGenerator.VoidGeneratorGenerator;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.teleportation.Teleportation;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.io.*;
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

    String host = "167.114.65.102", user = "dr.23", password = "devpass123";
    int port = 21;

    File pluginFolder = null;
    File rootFolder = null;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
        pluginFolder = DungeonRealms.getInstance().getDataFolder();
        rootFolder = new File(System.getProperty("user.dir"));

        try {
            FileUtils.forceMkdir(new File(pluginFolder + File.separator + "/realms/downloading"));
            FileUtils.forceMkdir(new File(pluginFolder + File.separator + "/realms/uploading"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent event) {
        removePlayerRealm(event.getPlayer(), true);
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
        player.sendMessage(ChatColor.GREEN + "Teleporting you to your realm!");
    }

    public void downloadRealm(UUID uuid) {
        FTPClient ftpClient = new FTPClient();
        FileOutputStream fos = null;
        String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";
        try {
            ftpClient.connect(host, port);
            boolean login = ftpClient.login(user, password);
            if (login) {
                Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid.toString());
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Utils.log.info("[REALM] [ASYNC] Downloading " + uuid.toString() + "'s Realm ... STARTING");
            File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloading/" + uuid.toString() + ".zip");
            fos = new FileOutputStream(TEMP_LOCAL_LOCATION);
            ftpClient.retrieveFile(REMOTE_FILE, fos);
            fos.close();
            Utils.log.info("[REALM] [ASYNC] Realm downloaded for " + uuid.toString());

            ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
            Utils.log.info("[REALM] [ASYNC] Extracting Realm for " + uuid.toString());
            zipFile.extractAll(rootFolder.getAbsolutePath() + "/" + uuid.toString());
            Utils.log.info("[REALM] [ASYNC] Realm Extracted for " + uuid.toString());


        } catch (IOException | ZipException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> loadInWorld(Bukkit.getPlayer(uuid)), 5);
            }
        }
    }

    public void uploadRealm(Player player) {
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[REALM] [ASYNC] Starting Compression for player realm " + player.getName());
            zip(rootFolder.getAbsolutePath() + "/" + player.getUniqueId().toString() + "/", pluginFolder.getAbsolutePath() + "/" + "realms/" + "uploading" + "/" + player.getUniqueId().toString() + ".zip", "");
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(host);
                ftpClient.login(user, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                String REMOTE_FILE = "/" + "realms" + "/" + player.getUniqueId() + ".zip";

                InputStream inputStream = new FileInputStream(pluginFolder.getAbsolutePath() + "/realms/uploading/" + player.getUniqueId() + ".zip");

                Utils.log.info("[REALM] [ASYNC] Started upload for player realm " + player.getUniqueId() + " ... STARTING");
                ftpClient.storeFile(REMOTE_FILE, inputStream);
                inputStream.close();
                Utils.log.info("[REALM] [ASYNC] Successfully uploaded player realm " + player.getUniqueId());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Utils.log.info("[REALM] [ASYNC] Deleting local cache of realm " + player.getUniqueId());
                try {
                    FileUtils.forceDelete(new File(pluginFolder.getAbsolutePath() + "/realms/uploading/" + player.getUniqueId() + ".zip"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void zip(String targetFolderPath, String destinationFilePath, String password) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                parameters.setPassword(password);
            }

            ZipFile zipFile = new ZipFile(destinationFilePath);

            File targetFile = new File(targetFolderPath);
            if (targetFile.isFile()) {
                zipFile.addFile(targetFile, parameters);
            } else if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            } else {
                System.out.println("ERROR ERROR, HOLY SHIT");
            }

        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public boolean doesRemoteRealmExist(String uuid) {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            boolean login = ftpClient.login(user, password);
            if (login) {
                Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid);
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            InputStream inputStream = null;
            try {
                inputStream = ftpClient.retrieveFileStream("/" + "realms" + "/" + uuid + ".zip");
            } catch (IOException e) {
                e.printStackTrace();
            }
            int returnCode = ftpClient.getReplyCode();
            if (inputStream == null || returnCode == 550) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param realmObject The Realm.
     * @since 1.0
     */
    public void removeRealm(RealmObject realmObject, boolean playerLoggingOut) {
        realmObject.isRealmPortalOpen = false;
        realmObject.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        realmObject.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
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
            uploadRealm(realmObject.getRealmOwner());
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
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is loading now!");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
                portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
                Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), portalLocation.add(0.5, 1.5, 0.5));
                realmHologram.appendTextLine(player.getName() + "(s) REALM");
                realmHologram.getVisibilityManager().setVisibleByDefault(true);
                RealmObject realmObject = new RealmObject(player, clickLocation, new ArrayList<>(), realmHologram, new ArrayList<>(), true);
                realmObject.getRealmBuilders().add(player);
                realmObject.getPlayerList().add(player);
                CURRENT_REALMS.add(realmObject);
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready!");
            }, 200L);
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
        CURRENT_REALMS.stream().filter(realmObject -> location.distanceSquared(realmObject.getLocation()) <= 4).forEach(realmObject -> {
            removeRealm(realmObject, false);
        });
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
