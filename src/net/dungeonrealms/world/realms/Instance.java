package net.dungeonrealms.world.realms;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
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

import java.io.*;
import java.util.UUID;

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void changeWorld(PlayerChangedWorldEvent event) {
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent event) {
        uploadRealm(event.getPlayer());
    }


    public void openRealm(Player player) {

        if (!doesRemoteRealmExist(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Your realm does not exist remotely! Creating you a new realm!");

            createTemplate(player);

            loadInWorld(player);

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
        player.teleport(w.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Teleporting you to your realm!");
    }

    public void downloadRealm(UUID uuid) {
        AsyncUtils.pool.submit(() -> {
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

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    loadInWorld(Bukkit.getPlayer(uuid));
                }, 5);


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
                }
            }
        });
    }

    public void uploadRealm(Player player) {
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[REALM] [ASYNC] Starting Compression for player realm " + player.getName());
            //zip(rootFolder.getAbsolutePath() + "/" + player.getUniqueId().toString(), pluginFolder.getAbsolutePath() + "/" + "realms/" + "uploading" + "/" + player.getUniqueId().toString() + ".zip", "");
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
                //show error message
            }

        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    boolean doesRemoteRealmExist(String uuid) {
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

    @Override
    public void stopInvocation() {

    }
}
