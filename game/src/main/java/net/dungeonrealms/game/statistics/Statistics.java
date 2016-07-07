package net.dungeonrealms.game.statistics;

import java.util.UUID;

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
    }


}
