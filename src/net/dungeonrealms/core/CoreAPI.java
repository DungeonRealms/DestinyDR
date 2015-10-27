package net.dungeonrealms.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dungeonrealms.core.reply.BanReply;
import net.dungeonrealms.core.reply.ProxyReply;
import net.dungeonrealms.mastery.AsyncUtils;

/**
 * Created by Nick on 10/17/2015.
 *
 * A[n] Async callback API.
 *
 */
public class CoreAPI {

    static CoreAPI instance = null;

    public static CoreAPI getInstance() {
        if (instance == null) {
            instance = new CoreAPI();
        }
        return instance;
    }

    public void test() {
    }

    /**
     * Will return the tax for mailing.
     *
     * @param callback The integer
     * @since 1.0
     */
    public void findMailTax(Callback<Integer> callback) {
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?type=tax");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = in.readLine();
                int rbt = Integer.valueOf(line);
                callback.callback(null, rbt);
            } catch (IOException e) {
                callback.callback(e, 1);
                e.printStackTrace();
            }
        });
    }

    /**
     * Because Kayaba and Red are going to be such bitches.
     * ANd we all know that negros like them will follow
     * the path of niggerish nigger. We'll build in a automagically
     * proxyifier detector to detect dem negro from 2 farm fields
     * away.
     *
     * @param ip       Players ip
     * @param callback Result
     * @since 1.0
     */
    public void isProxying(InetAddress ip, Callback<ProxyReply> callback) {
        Future<Boolean> isProxy = AsyncUtils.pool.submit(() -> {
            try {
                if (ip.toString().contains("127.0.0.1")) return false;
                URL url = new URL("http://www.shroomery.org/ythan/proxycheck.php?ip=" + ip.toString().replace("/", ""));
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = in.readLine();
                in.close();
                if (inputLine == null) return false;
                switch (inputLine) {
                    case "Y":
                        return true;
                    case "N":
                        return false;
                    case "X":
                        return false;
                    default:
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });


        try {
            callback.callback(null, new ProxyReply(ProxyReply.Result.getByBoolean((Boolean) isProxy.get())));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    /**
     * Checks if a player has a ban.
     *
     * @param playerName The player you're checking.
     * @param callback   The callback of BanResult.
     * @since 1.0
     */
    public void findBan(String playerName, Callback<BanReply> callback) {
        Future<?> result = AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?type=ban&player=" + playerName);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                return in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "0,0";
        });

        try {
            String line = (String) result.get();
            callback.callback(null, new BanReply(BanReply.BanResult.getByInt(Integer.valueOf(line.split(",")[0])), BanReply.BanReason.getByInt(Integer.valueOf(line.split(",")[1]))));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * Will get a players guild Async.
     *
     * @param playerName
     * @param callback
     * @since 1.0
     */
    public void findGuild(String playerName, Callback<String> callback) {
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?getGuild=" + playerName);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject obj = root.getAsJsonObject();
                callback.callback(null, obj.get("info.guild").getAsString());
            } catch (IOException e) {
                callback.callback(e, "");
                e.printStackTrace();
            }
        });
    }

}
