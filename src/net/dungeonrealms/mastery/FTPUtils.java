package net.dungeonrealms.mastery;

import net.dungeonrealms.DungeonRealms;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

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
public class FTPUtils {

    public static volatile HashMap<UUID, FTPStatus> REALMS = new HashMap<>();
/**
 * NAUGHTEY, NAUGHTEY
 * Created by Chase on Sep 23, 2015
 */
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

    /**
     * Checks for proper local folders.
     * creates if don't exist.
     *
     * @since 1.0
     */
    public static void startInitialization() {
        File coreDirectory = DungeonRealms.getInstance().getDataFolder();
        try {
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "/realms/downloading"));
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "/realms/uploading"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadRealm(UUID uuid) {
        if (REALMS.get(uuid) != FTPStatus.DOWNLOADED) return;
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[REALM] [ASYNC] Starting Compression for player realm " + uuid.toString());
            zipFolder(DungeonRealms.getInstance().getDataFolder() + "/realms/" + uuid.toString(), DungeonRealms.getInstance().getDataFolder() + "/realms/uploading/" + uuid.toString() + ".zip");
            Utils.log.info("[REALM] [ASYNC] Deleting local cache of unzipped realm " + uuid.toString());
            deleteRecursive(new File(DungeonRealms.getInstance().getDataFolder() + "/realms/" + uuid.toString()));
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
                deleteRecursive(new File(DungeonRealms.getInstance().getDataFolder() + "/realms/uploading/" + uuid.toString() + ".zip"));
            }
        });
    }

    /**
     * Deletes file recursively.
     *
     * @param path
     * @since 1.0
     */
    public boolean deleteRecursive(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteRecursive(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Will download and extract a players realm zip.
     *
     * @param uuid
     * @since 1.0
     */
    public void downloadRealm(UUID uuid) {
        if (REALMS.containsKey(uuid)) return;
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
        Utils.log.info("[REALM] LEFT -> [ASYNC].. Unzipping Realm for " + uuid.toString());
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(DungeonRealms.getInstance().getDataFolder() + "/realms", entry.getName());
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            REALMS.put(uuid, FTPStatus.DOWNLOADED);
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
}
