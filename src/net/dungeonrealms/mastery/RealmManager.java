package net.dungeonrealms.mastery;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
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

    public static volatile HashMap<UUID, FTPStatus> REALMS = new HashMap<>();

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

    @Override
    public void stopInvocation() {

    }

    public void uploadRealm(UUID uuid) {
        if (REALMS.get(uuid) != FTPStatus.DOWNLOADED) return;
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
        if (REALMS.containsKey(uuid) && REALMS.get(uuid) == FTPStatus.DOWNLOADED) return;
        AsyncUtils.pool.submit(() -> {
            REALMS.put(uuid, FTPStatus.DOWNLOADING);
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

                REALMS.put(uuid, FTPStatus.EXTRACTING);
                ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                unZip(zipFile, uuid);


            } catch (IOException e) {
                REALMS.put(uuid, FTPStatus.FAILED);
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
            //loadInWorld(uuid.toString(), Bukkit.getPlayer(uuid));
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
        System.out.println("1");
        World w = Bukkit.getServer().createWorld(new WorldCreator(worldName));
        System.out.println("2");
        w.setKeepSpawnInMemory(true);
        System.out.println("3");
        w.setAutoSave(false);
        System.out.println("4");
        w.setPVP(false);
        System.out.println("5");
        w.setStorm(false);
        System.out.println("6");
        w.setMonsterSpawnLimit(0);
        System.out.println("7");
        Bukkit.getWorlds().add(w);
        System.out.println("8");

        player.teleport(w.getSpawnLocation());
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "REALMS" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your realm is ready! Teleporting you now...");
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param worldName The players UUID.
     * @since 1.0
     */
    public void removeRealm(String worldName) {
        Bukkit.getWorlds().remove(Bukkit.getWorld(worldName));
        Utils.log.info("[REALMS] Removing world: " + worldName + " from worldList().");
        Bukkit.unloadWorld(worldName, false);
        Utils.log.info("[REALMS] Unloading world: " + worldName + " in preparation for deletion!");
        try {
            FileUtils.deleteDirectory(new File(worldName));
            Utils.log.info("[REALMS] Deleted world: " + worldName + " final stage.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
