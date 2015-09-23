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

/**
 * Created by Nick on 9/22/2015.
 */
public class FTPUtils {

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

    /**
     * Checks for proper local folders.
     *
     * @since 1.0
     */
    public static void startInitialization() {
        File coreDirectory = DungeonRealms.getInstance().getDataFolder();
        try {
            FileUtils.forceMkdir(new File(coreDirectory + File.separator + "/realms/downloading"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            try {
                ftpClient.connect(HOST, port);
                boolean login = ftpClient.login(USER, PASSWORD);
                if (login) {
                    Utils.log.warning("[ASYNC] FTP Connection Established for " + uuid.toString());
                }
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";
                File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloading/" + uuid.toString() + ".zip");

                fos = new FileOutputStream(TEMP_LOCAL_LOCATION);

                ftpClient.retrieveFile(REMOTE_FILE, fos);

                fos.close();

                Utils.log.info("[ASYNC] Realm downloaded for " + uuid.toString());

                REALMS.put(uuid, FTPStatus.EXTRACTING);
                ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                unZip(zipFile, uuid);


            } catch (IOException e) {
                REALMS.put(uuid, FTPStatus.FAILED);
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

    /**
     * Will extract a players realm .zip to the correct folder.
     *
     * @param zipFile
     * @since 1.0
     */
    public void unZip(ZipFile zipFile, UUID uuid) {
        Utils.log.info("Left [ASYNC] Thread.. Unzipping Realm for " + uuid.toString());
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

}
