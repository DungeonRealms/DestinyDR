package net.dungeonrealms.mastery;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.teleportation.Teleportation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Player;

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

    private class RealmObject {

        private UUID realmOwner;
        private Location portalLocation;
        private List<Player> playerList;
        private Hologram realmHologram;

        public RealmObject(UUID realmOwner, Location portalLocation, List<Player> playerList, Hologram realmHologram) {
            this.realmOwner = realmOwner;
            this.portalLocation = portalLocation;
            this.playerList = playerList;
            this.realmHologram = realmHologram;
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
        /*
        Only creates a world if the contents of a world don't already exist.
        This method loadInWorld() is called in the actual object load().
         */
        WorldCreator worldCreator = new WorldCreator(worldName);
        System.out.println("1");
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        World w = Bukkit.createWorld(worldCreator);
        System.out.println("2");
        w.setKeepSpawnInMemory(true);
        System.out.println("3");
        w.setAutoSave(false);
        System.out.println("4");
        w.setPVP(true);
        System.out.println("5");
        w.setStorm(false);
        System.out.println("6");
        w.setMonsterSpawnLimit(0);
        System.out.println("7");
        w.setTime(0L);
        System.out.println("8");
        Bukkit.getWorlds().add(w);

        Location spawnLocation = w.getSpawnLocation();
        spawnLocation.setY(w.getHighestBlockYAt(w.getSpawnLocation()) + 1);
        spawnLocation.getBlock().setType(Material.PORTAL);
        spawnLocation.subtract(0, 1, 0);
        spawnLocation.getBlock().setType(Material.PORTAL);

        //player.teleport(w.getSpawnLocation());
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready! Teleporting you now...");
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param realmObject The Realm.
     * @since 1.0
     */
    public void removeRealm(RealmObject realmObject) {
        realmObject.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        realmObject.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        realmObject.getRealmHologram().delete();
        realmObject.getPlayerList().stream().forEach(player -> {
            if (new GamePlayer(player).isInRealm()) {
                player.sendMessage(ChatColor.RED + "This Realm has been closed!");
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
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
    }

    /**
     * Removes all active realms.
     *
     * @since 1.0
     */
    public void removeAllActiveRealms() {
        if (!CURRENT_REALMS.isEmpty()) {
            CURRENT_REALMS.forEach(this::removeRealm);
        }
    }

    /**
     * Gets the realm of a player
     *
     * @since 1.0
     */
    public RealmObject getPlayerRealmPlayer(Player player) {
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

    /**
     * Removes a players realm
     *
     * @since 1.0
     */
    public void removePlayerRealm(Player player) {
        if (getPlayerRealmPlayer(player) != null) {
            removeRealm(getPlayerRealmPlayer(player));
        }
    }

    public void openPlayerRealm(Player player, Location clickLocation) {
        if (getPlayerRealmPlayer(player) == null) {
            downloadRealm(player.getUniqueId());

            Location portalLocation = clickLocation.clone();
            portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
            portalLocation.add(0, 1, 0).getBlock().setType(Material.PORTAL);
            Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), portalLocation.add(0.5, 1.5, 0.5));
            realmHologram.appendTextLine(ChatColor.WHITE + player.getName() + " REALM");
            realmHologram.getVisibilityManager().setVisibleByDefault(true);
            RealmObject realmObject = new RealmObject(player.getUniqueId(), clickLocation, new ArrayList<>(), realmHologram);
            CURRENT_REALMS.add(realmObject);

            player.sendMessage(ChatColor.AQUA + "Your Portal Has Been Opened!");

            loadInWorld(player.getUniqueId().toString(), player);
        } else {
            player.sendMessage(ChatColor.RED + "For some reason you already have a realm?!?");
        }
    }

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

    public Location getPortalLocationFromRealmWorld(Player player) {
        for (RealmObject realmObject : CURRENT_REALMS) {
            if (player.getWorld().getName().equalsIgnoreCase(realmObject.getRealmOwner().toString())) {
                realmObject.getPlayerList().remove(player);
                return realmObject.getLocation();
            }
        }
        return Teleportation.Cyrennica;
    }

    public void removeRealmViaPortalLocation(Location location) {
        CURRENT_REALMS.stream().filter(realmObject -> location.distanceSquared(realmObject.getLocation()) <= 4).forEach(this::removeRealm);
    }
}
