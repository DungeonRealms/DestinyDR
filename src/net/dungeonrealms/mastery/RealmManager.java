package net.dungeonrealms.mastery;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.mechanics.LootManager;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.teleportation.Teleportation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by Nick on 9/22/2015.
 */
public class RealmManager implements GenericMechanic {

    static RealmManager instance = null;

    public static RealmManager getInstance() {
        if (instance == null) {
            instance = new RealmManager();
        }
        return instance;
    }

    public static volatile HashMap<UUID, FTPStatus> REALM_STATUS = new HashMap<>();
    public CopyOnWriteArrayList<RealmObject> CURRENT_REALMS = new CopyOnWriteArrayList<>();

    enum FTPStatus {
        FAILED("Failed"),
        DOWNLOADING("Downloading"),
        EXTRACTING("Extracting"),
        DOWNLOADED("Downloaded");

        private String name;

        FTPStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    String HOST = "167.114.65.102";
    int port = 21;
    String USER = "dr.23";
    String PASSWORD = "devpass123";

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    /**
     * Checks for proper local folders.
     * creates if don't exist.
     *
     * @since 1.0
     */
    public void startInitialization() {
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
        File coreDirectory = DungeonRealms.getInstance().getDataFolder();
        try {
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "/realms/downloading"));
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "/realms/uploading"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");
    }

    public class RealmObject {

        private UUID realmOwner;
        private Location portalLocation;
        private List<Player> playerList;
        private Hologram realmHologram;
        private List<Player> realmBuilders;
        private boolean isRealmPortalOpen;

        public RealmObject(UUID realmOwner, Location portalLocation, List<Player> playerList, Hologram realmHologram, List<Player> realmBuilders, boolean isRealmPortalOpen) {
            this.realmOwner = realmOwner;
            this.portalLocation = portalLocation;
            this.playerList = playerList;
            this.realmHologram = realmHologram;
            this.realmBuilders = realmBuilders;
            this.isRealmPortalOpen = isRealmPortalOpen;
        }

        public UUID getRealmOwner() {
            return realmOwner;
        }

        public Location getLocation() {
            return portalLocation;
        }

        public List<Player> getPlayerList() {
            return playerList;
        }

        public Hologram getRealmHologram() {
            return realmHologram;
        }

        public List<Player> getRealmBuilders() {
            return realmBuilders;
        }

        public boolean isRealmPortalOpen() {
            return isRealmPortalOpen;
        }
    }

    @Override
    public void stopInvocation() {

    }

    public void uploadRealm(UUID uuid) {
        if (REALM_STATUS.get(uuid) != FTPStatus.DOWNLOADED) return;
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[REALM] [ASYNC] Starting Compression for player realm " + uuid.toString());
            zipFolder(DungeonRealms.getInstance().getDataFolder() + "/realms/" + uuid.toString(), DungeonRealms.getInstance().getDataFolder() + "/realms/uploading/" + uuid.toString() + ".zip");
            Utils.log.info("[REALM] [ASYNC] Deleting local cache of unzipped realm " + uuid.toString());
            try {
                FileUtils.deleteDirectory(new File(DungeonRealms.getInstance().getDataFolder() + "/realms/" + uuid.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(HOST);
                ftpClient.login(USER, PASSWORD);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";
                File TEMP_UPLOAD_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/uploading/" + uuid.toString() + ".zip");

                InputStream inputStream = new FileInputStream(TEMP_UPLOAD_LOCATION);

                Utils.log.info("[REALM] [ASYNC] Started upload for player realm " + uuid.toString() + " ... STARTING");
                ftpClient.storeFile(REMOTE_FILE, inputStream);
                inputStream.close();
                Utils.log.info("[REALM] [ASYNC] Successfully uploaded player realm " + uuid.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Utils.log.info("[REALM] [ASYNC] Deleting local cache of realm " + uuid.toString());
                try {
                    FileUtils.deleteDirectory(new File(DungeonRealms.getInstance().getDataFolder() + "/realms/uploading/" + uuid.toString() + ".zip"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Will download and extract a players realm zip.
     *
     * @param uuid
     * @since 1.0
     */
    public void downloadRealm(UUID uuid) {
        if (REALM_STATUS.containsKey(uuid) && REALM_STATUS.get(uuid) == FTPStatus.DOWNLOADED) return;
        AsyncUtils.pool.submit(() -> {
            REALM_STATUS.put(uuid, FTPStatus.DOWNLOADING);
            FTPClient ftpClient = new FTPClient();
            FileOutputStream fos = null;
            String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";
            try {
                ftpClient.connect(HOST, port);
                boolean login = ftpClient.login(USER, PASSWORD);
                if (login) {
                    Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid.toString());
                }
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                if (!checkFileExists(ftpClient, REMOTE_FILE)) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    Utils.log.warning("[REALM] [ASYNC] Player: " + uuid.toString() + " doesn't exist remotely!");
                    generateBlankRealm(uuid);
                    return;
                }

                Utils.log.info("[REALM] [ASYNC] Downloading " + uuid.toString() + "'s Realm ... STARTING");
                File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloading/" + uuid.toString() + ".zip");
                fos = new FileOutputStream(TEMP_LOCAL_LOCATION);
                ftpClient.retrieveFile(REMOTE_FILE, fos);
                fos.close();
                Utils.log.info("[REALM] [ASYNC] Realm downloaded for " + uuid.toString());

                REALM_STATUS.put(uuid, FTPStatus.EXTRACTING);
                ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                unZip(zipFile, uuid);


            } catch (IOException e) {
                REALM_STATUS.put(uuid, FTPStatus.FAILED);
                e.printStackTrace();
            } finally {
                deleteLocalCache(uuid);
                if (ftpClient.isConnected()) {
                    try {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                loadInWorld(uuid.toString(), Bukkit.getPlayer(uuid));
            }
        });
    }

    /**
     * Checks the remote server for existance.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    boolean checkFileExists(FTPClient ftpClient, String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = ftpClient.retrieveFileStream(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

    /**
     * Will extract a players realm .zip to the correct folder.
     *
     * @param zipFile
     * @since 1.0
     */
    public void unZip(ZipFile zipFile, UUID uuid) {
        Utils.log.info("[REALMS] Unzipping instance for " + uuid.toString());
        new File(uuid.toString()).mkdir();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(uuid.toString(), entry.getName());
                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the players realm.zip after it's been downloaded
     * and extracted.
     *
     * @param uuid
     * @since 1.0
     */
    public void deleteLocalCache(UUID uuid) {
        Utils.log.info("[REALM] Removing cached realm for " + uuid.toString());
        File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloading/" + uuid.toString() + ".zip");
        if (TEMP_LOCAL_LOCATION.exists()) {
            TEMP_LOCAL_LOCATION.delete();
        } else {
            Utils.log.warning("[REALM] Unable to find local cache to remove Realm for player " + uuid.toString());
        }
    }

    /**
     * The final Zip..
     *
     * @param srcFolder
     * @param destZipFile
     * @throws Exception
     * @since 1.0
     */
    public void zipFolder(String srcFolder, String destZipFile) {
        try {
            ZipOutputStream zip = null;
            FileOutputStream fileWriter = null;

            fileWriter = new FileOutputStream(destZipFile);
            zip = new ZipOutputStream(fileWriter);

            addFolderToZip("", srcFolder, zip);
            zip.flush();
            zip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds file to zip
     *
     * @param path
     * @param srcFile
     * @param zip
     * @throws Exception
     * @since 1.0
     */
    public void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            in.close();
        }
    }

    /**
     * Add Realm World to Zip
     *
     * @param path
     * @param srcFolder
     * @param zip
     * @throws Exception
     * @since 1.0
     */
    public void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }

    /**
     * Loads a players realm into BUKKIT.
     *
     * @param worldName name of the world, player.UUID.
     * @param player    the player!
     * @since 1.0
     */
    public void loadInWorld(String worldName, Player player) {
        Bukkit.createWorld(new WorldCreator(worldName));
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready! Teleporting you now...");
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
                if (new GamePlayer(player).isInRealm()) {
                    player.sendMessage(ChatColor.RED + "This Realm has been closed!");
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    player.setFlying(false);
                }
            });
            Bukkit.getWorlds().remove(Bukkit.getWorld(realmObject.getRealmOwner().toString()));
            Utils.log.info("[REALMS] Removing world: " + realmObject.getRealmOwner().toString() + " from worldList().");
            Bukkit.unloadWorld(realmObject.getRealmOwner().toString(), false);
            Utils.log.info("[REALMS] Unloading world: " + realmObject.getRealmOwner().toString() + " in preparation for deletion!");
            try {
                FileUtils.deleteDirectory(new File(realmObject.getRealmOwner().toString()));
                Utils.log.info("[REALMS] Deleted world: " + realmObject.getRealmOwner().toString() + " final stage.");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                if (realmObject.getRealmOwner().equals(player.getUniqueId())) {
                    return realmObject;
                }
            }
            return null;
        }
        return null;
    }

    public RealmObject getPlayersCurrentRealm(Player player) {
        if (!CURRENT_REALMS.isEmpty()) {
            for (RealmObject realmObject : CURRENT_REALMS) {
                if (realmObject.getRealmOwner().toString().equals(player.getWorld().getName())) {
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

            downloadRealm(player.getUniqueId());

            portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
            portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
            Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), portalLocation.add(0.5, 1.5, 0.5));
            realmHologram.appendTextLine(new GamePlayer(player).getPlayerAlignment().getAlignmentColor() + player.getName() + "(s) REALM");
            realmHologram.getVisibilityManager().setVisibleByDefault(true);
            RealmObject realmObject = new RealmObject(player.getUniqueId(), clickLocation, new ArrayList<>(), realmHologram, new ArrayList<>(), true);
            realmObject.getRealmBuilders().add(player);
            realmObject.getPlayerList().add(player);
            CURRENT_REALMS.add(realmObject);

            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready!");
        } else {
            player.sendMessage(ChatColor.RED + "You already have a Realm Portal in the world, please destroy it!");
        }
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
                    if (Bukkit.getWorld(realmObject.getRealmOwner().toString()) == null) {
                        return null;
                    }
                    realmObject.getPlayerList().add(player);
                    return Bukkit.getWorld(realmObject.getRealmOwner().toString()).getSpawnLocation();
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "There are no Realms currently, no clue how this portal got here!");
            location.getBlock().setType(Material.AIR);
            if (location.subtract(0, 1, 0).getBlock().getType() == Material.PORTAL) {
                location.getBlock().setType(Material.AIR);
            }
            if (location.add(0 , 2, 0).getBlock().getType() == Material.PORTAL) {
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
            if (player.getWorld().getName().equalsIgnoreCase(realmObject.getRealmOwner().toString())) {
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

    /**
     * Removes a realm via its portal location.
     *
     * @since 1.0
     */
    public void removeRealmViaPortalLocation(Location location) {
        for (RealmObject realmObject : CURRENT_REALMS) {
            if (location.distanceSquared(realmObject.getLocation()) <= 4) {
                removeRealm(realmObject, false);
            }
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
     * Will download and extract a players realm zip.
     *
     * @param uuid
     * @since 1.0
     */
    public void downloadRealmTemplate(UUID uuid) {
        if (REALM_STATUS.containsKey(uuid) && REALM_STATUS.get(uuid) == FTPStatus.DOWNLOADED) return;
        AsyncUtils.pool.submit(() -> {
            REALM_STATUS.put(uuid, FTPStatus.DOWNLOADING);
            FTPClient ftpClient = new FTPClient();
            FileOutputStream fos = null;
            String REMOTE_FILE = "/" + "realms" + "/" + "REALM_TEMPLATE" + ".zip";
            try {
                ftpClient.connect(HOST, port);
                boolean login = ftpClient.login(USER, PASSWORD);
                if (login) {
                    Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid.toString() + " [TEMPLATE]");
                }
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                if (!checkFileExists(ftpClient, REMOTE_FILE)) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    Utils.log.warning("[REALM] [ASYNC] Realm Template doesn't exist remotely!");
                    return;
                }

                Utils.log.info("[REALM] [ASYNC] Downloading Template Realm for player + " + uuid.toString() + " ... STARTING");
                File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloading/" + uuid.toString() + ".zip");
                fos = new FileOutputStream(TEMP_LOCAL_LOCATION);
                ftpClient.retrieveFile(REMOTE_FILE, fos);
                fos.close();
                Utils.log.info("[REALM] [ASYNC] Template Realm downloaded for " + uuid.toString());

                REALM_STATUS.put(uuid, FTPStatus.EXTRACTING);
                ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                unZip(zipFile, uuid);


            } catch (IOException e) {
                REALM_STATUS.put(uuid, FTPStatus.FAILED);
                e.printStackTrace();
            } finally {
                deleteLocalCache(uuid);
                if (ftpClient.isConnected()) {
                    try {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void generateBlankRealm(UUID ownerUUID) {
        downloadRealmTemplate(ownerUUID);

        WorldCreator worldCreator = new WorldCreator(ownerUUID.toString());
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        worldCreator.generatorSettings("3;minecraft:air;2");
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

        world.getSpawnLocation().getBlock().setType(Material.PORTAL);
        world.getSpawnLocation().subtract(0, 1, 0);
        world.getSpawnLocation().getBlock().setType(Material.PORTAL);
        world.getSpawnLocation().add(0, 1, 0);

        Utils.log.info("[REALMS] Blank Realm generated for player " + ownerUUID.toString());
    }
}
