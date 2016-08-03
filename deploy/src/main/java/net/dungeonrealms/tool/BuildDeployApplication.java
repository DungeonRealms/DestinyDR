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

public class BuildDeployApplication {

    public static void main(String[] args) throws IOException {

        File BUILD_JAR = new File(System.getProperty("user.dir"), "game/target/DungeonRealms.jar");
        String REMOTE_LOCATION = "/update/DungeonRealms.jar";
        String[] NOTIFICATION_CHANNELS = new String[]{"G191V775M", "C1H00KN6S"};

        System.out.println("[BUILD] Initiating build application tool for DungeonRealms " + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER);

        InputStream in =  new FileInputStream(BUILD_JAR);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(Constants.FTP_HOST_NAME, Constants.FTP_PORT);

        if (ftpClient.login(Constants.FTP_USER_NAME, Constants.FTP_PASSWORD))
            System.out.println("[BUILD] FTP Connection Established");

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        System.out.println("[BUILD] Charging me laser...");
        System.out.println("[BUILD] Uploading Build " + Constants.BUILD_NUMBER + " to remote master FTP server...");

        final long start = System.currentTimeMillis();

        if(ftpClient.storeFile(REMOTE_LOCATION, in)) {
            System.out.println("[BUILD] Successfully deployed Build " + Constants.BUILD_NUMBER  + " to remote master FTP server (Took " + (System.currentTimeMillis() - start) + "ms)");

            SlackSession session = SlackSessionFactory.createWebSocketSlackSession("xoxb-66008293216-GTP9wV6kFuw1FAk09qzXeaV2");
            session.connect();

            Arrays.stream(NOTIFICATION_CHANNELS).forEach(
                    channelID -> {
                        SlackChannel channel = session.findChannelById(channelID);
                        session.sendMessage(channel, "Dungeon Realms "  + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER + " has been deployed to the remote master FTP server.");
                        session.sendMessage(channel, "This build will be propagated on the next reboot.");

                        try {
                            session.sendMessage(channel, "Latest patch notes for this build are available here " + getPatchNotes().toString());
                        } catch (IOException | PasteException e) {
                            System.out.print("Unable to generate patch notes!");
                        }
                    }
            );

            System.exit(1);
        }
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

        builder.append("Tool created by the one and only apollooooooo.");
        return Pastebin.pastePaste("3e7fbbeaeb6b59a4f29b2f724e3c364f", builder.toString(), "Build " + Constants.BUILD_NUMBER + " Patch Notes");
    }


}
