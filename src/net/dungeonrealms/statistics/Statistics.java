package net.dungeonrealms.statistics;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;

/**
 * Created by Nick on 9/26/2015.
 */
public class Statistics {

    static Statistics instance = null;

    public static Statistics getInstance() {
        if (instance == null) {
            instance = new Statistics();
        }
        return instance;
    }

    //TODO: Need to add more
    enum StatisticType {
        MOB_KILL(0),
        PLAYER_KILL(1),
        DEATHS(2),;

        private int id;

        StatisticType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * Will allow us to create live feeds of action in-game events.
     * Such as a display log on each players profile displaying
     * their most recent events. I.e. Killed a Zombie, Killed Burick
     * Collected 23 emeralds.
     *
     * @param uuid
     * @param type
     * @param amount
     * @since 1.0
     */
    public void sendStatisticUpdate(UUID uuid, StatisticType type, int amount) {
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[MONITOR] [ASYNC] Sending update packet for " + uuid.toString() + " type " + type.name());
            try {
                URL url = new URL("http://www.dungeonrealms.net/api/statistics.php?uuid=" + uuid.toString() + "&type=" + type.getId() + "&object=" + amount + "&verification=" + "trump2016");
                URLConnection connection = url.openConnection();
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
