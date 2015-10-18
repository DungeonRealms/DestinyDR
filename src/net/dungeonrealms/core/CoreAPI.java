package net.dungeonrealms.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dungeonrealms.core.reply.BanReply;
import net.dungeonrealms.mastery.AsyncUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Nick on 10/17/2015.
 *
 * @apiNote This is the new DatabaseAPI. (IN THE WORKS).
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
                String line = in.readLine();
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        });

        try {
            String line = (String) result.get();
            callback.callback(null, new BanReply(BanResult.getByInt(Integer.valueOf(line.split(",")[0])), BanReason.getByInt(Integer.valueOf(line.split(",")[1]))));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * BanReason
     * @since 1.0
     */
    public enum BanReason {
        OTHER(-1, "Other"),
        DUPLICATIONS(1, "Duplications"),
        HACKING(0, "Hacking"),
        MIS_CONDUCT(3, "Misconduct");

        private int id;
        private String reason;

        BanReason(int id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        public static BanReason getByInt(int id) {
            for (BanReason br : values()) {
                if (br.getId() == id) {
                    return br;
                }
            }
            return BanReason.OTHER;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return reason;
        }
    }

    public enum BanResult {
        YES(1),
        NO(0),
        TEMP_BANNED(3);

        private int id;

        BanResult(int id) {
            this.id = id;
        }

        public static BanResult getByInt(int id) {
            for (BanResult br : values()) {
                if (br.getId() == id) {
                    return br;
                }
            }
            return BanResult.NO;
        }

        public int getId() {
            return id;
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
