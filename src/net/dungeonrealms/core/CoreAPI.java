package net.dungeonrealms.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        findBan("Proxying", new Callback<BanResult>(BanResult.class) {
            @Override
            public void callback(Throwable failCause, BanResult result) {
                if (result == BanResult.YES) {
                    Utils.log.warning("[BAN] [ASYNC] " + "Proxying " + "tried to log in.. but was DENIED. BECAUSE HE IS BANNED!");
                }
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
    public void findBan(String playerName, Callback<BanResult> callback) {
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?isBanned=" + playerName);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = in.readLine();
                int rbt = Integer.valueOf(line);

                callback.callback(null, BanResult.getByInt(rbt));
            } catch (IOException e) {
                callback.callback(null, null);
                e.printStackTrace();
            }
        });
    }

    enum BanResult {
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
