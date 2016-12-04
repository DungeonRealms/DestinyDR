package net.dungeonrealms.control.utils;

import net.dungeonrealms.control.DRControl;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class UtilSlack {

    private static String ADDRESS = "https://hooks.slack.com/services/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";

    public static void postMessage(String channel, String message) throws IOException {

        HttpsURLConnection con = (HttpsURLConnection) new URL(ADDRESS).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setDoOutput(true);

        String parameters = "payload={";

        parameters += "\"username\":\"DRCONTROL\", ";
        parameters += "\"channel\":\"" + channel + "\", ";
        parameters += "\"text\":\"" + message + "\", ";
        parameters += "\"icon_url\":\"http://i.imgur.com/4yJT9Dy.jpg?1\"";
        parameters += "}";

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());

        wr.writeBytes(parameters);
        wr.flush();
        wr.close();

        con.getResponseCode();

    }

    public static void networkPlayers() {
        int onlinePlayers = DRControl.getInstance().getServerManager().getOnlinePlayers();
        int maxPlayers = DRControl.getInstance().getServerManager().getMaxPlayers();

        try {
            postMessage("bots", "Current players online " + onlinePlayers + "/" + maxPlayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void networkStatus() {

        int onlinePlayers = DRControl.getInstance().getServerManager().getOnlinePlayers();
        int maxPlayers = DRControl.getInstance().getServerManager().getMaxPlayers();

        int onlineProxies = DRControl.getInstance().getServerManager().getOnlineProxies().size();
        int totalProxies = DRControl.getInstance().getServerManager().getProxyServers().size();

        int onlineServers = DRControl.getInstance().getServerManager().getOnlineServers().size();
        int totalServers = DRControl.getInstance().getServerManager().getGameServers().size();

        try {
            UtilSlack.postMessage("bots", "-------------[Network]-------------" + "\n"+ "Players: " + onlinePlayers + "/" + maxPlayers + "\n" + "Online Proxies: " + onlineProxies + "/" + totalProxies + "\n" + "Online Servers: " + onlineServers + "/" + totalServers + "\n" + "Cerberus Uptime: " + UtilTime.format((int) ((System.currentTimeMillis() - DRControl.getInstance().getUptime()) / 1000D)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
