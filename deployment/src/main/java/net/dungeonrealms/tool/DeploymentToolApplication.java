package net.dungeonrealms.tool;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import net.dungeonrealms.common.Constants;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.Pastebin;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/7/2016
 */

public class DeploymentToolApplication {

    public static void main(String[] args) throws IOException {
        boolean DEV_DEPLOYMENT = false;

        if (args.length > 0)
            if (args[0].equals("-development"))
                DEV_DEPLOYMENT = true;

        File BUILD_JAR = new File(System.getProperty("user.dir"), "game/target/DungeonRealms.jar");
        String REMOTE_LOCATION = !DEV_DEPLOYMENT ? "/update/DungeonRealms.jar" : "/development/DungeonRealms.jar";
        String[] NOTIFICATION_CHANNELS = new String[]{"G191V775M", "C1H00KN6S"};

        System.out.println("[DEPLOYMENT] Initiating build application tool for DungeonRealms " + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER);

        InputStream in = new FileInputStream(BUILD_JAR);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(Constants.FTP_HOST_NAME, Constants.FTP_PORT);

        if (ftpClient.login(Constants.FTP_USER_NAME, Constants.FTP_PASSWORD))
            System.out.println("[DEPLOYMENT] FTP Connection Established");

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        System.out.println("[DEPLOYMENT] Charging me laser...");
        System.out.println("[DEPLOYMENT] Uploading DungeonRealms Build " + Constants.BUILD_NUMBER + " to remote master FTP server...");

        final long start = System.currentTimeMillis();

        if (ftpClient.storeFile(REMOTE_LOCATION, in)) {
            System.out.println("[DEPLOYMENT] Successfully deployed " + (DEV_DEPLOYMENT ? "to development server " : "") + "to remote master FTP server (Took " + ((System.currentTimeMillis() - start)) + "ms) !");

            if (DEV_DEPLOYMENT) {
                System.exit(1);
                return;
            }

            SlackSession session = SlackSessionFactory.createWebSocketSlackSession("xoxb-66008293216-GTP9wV6kFuw1FAk09qzXeaV2");
            session.connect();

            System.out.println("[DEPLOYMENT] Generating patch notes...");
            URL pastebinURL = null;

            try {
                pastebinURL = getPatchNotes();
            } catch (IOException | PasteException e) {
                System.out.println("Unable to generate patch notes!");
                e.printStackTrace();
            }

            if (pastebinURL != null)
                System.out.println("[DEPLOYMENT] Patch notes have been generated");

            URL patchnotesURL = pastebinURL;

            System.out.println("[DEPLOYMENT] Notifying slack channels for deployment of Build " + Constants.BUILD_NUMBER);
            Arrays.stream(NOTIFICATION_CHANNELS).forEach(
                    channelID -> {

                        SlackChannel channel = session.findChannelById(channelID);
                        session.sendMessage(channel, "Dungeon Realms " + " Build " + Constants.BUILD_NUMBER + " has been deployed to the remote master FTP server.");
                        session.sendMessage(channel, "This deployed build will be propagated on the network when the servers reboot.");
                        if (patchnotesURL != null)
                            session.sendMessage(channel, "Latest patch notes for this build are available here " + patchnotesURL.toString());
                    }
            );
            session.disconnect();
        } else
            System.out.println("[DEPLOYMENT] Failed to deployed " + (DEV_DEPLOYMENT ? "to development server " : ""));
        System.exit(1);
    }


    public static URL getPatchNotes() throws IOException, PasteException {
        File PATCH_NOTES = new File(System.getProperty("user.dir"), "game/src/main/resources/patchnotes.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PATCH_NOTES)));
        StringBuilder builder = new StringBuilder();

        Pattern pattern = Pattern.compile("(?i)" + String.valueOf('&') + "[0-9A-FK-OR]");
        String str;

        while ((str = reader.readLine()) != null) {
            str = pattern.matcher(str).replaceAll("");
            builder.append(str.replace("<build>", Constants.BUILD_NUMBER)).append("\n");
        }

        builder.append("[Tool created by the one and only apollooooooo]");
        return Pastebin.pastePaste("3e7fbbeaeb6b59a4f29b2f724e3c364f", builder.toString());
    }


}
