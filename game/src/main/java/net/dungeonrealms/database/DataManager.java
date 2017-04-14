package net.dungeonrealms.database;

import lombok.Cleanup;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataManager {

    private volatile Set<PlayerStats> playerUpdateStats = new HashSet<>();

    private final ScheduledExecutorService STATS_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(8);

    public synchronized void addStatQuery(PlayerStats stats) {
        this.playerUpdateStats.add(stats);
    }

    public void startSaveTasks() {
        STATS_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (this.playerUpdateStats.isEmpty()) return;
            //Update all SQL stuff..
            Iterator<PlayerStats> statUpdateQueries = this.playerUpdateStats.iterator();
            try {
                @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
                while (statUpdateQueries.hasNext()) {
                    PlayerStats stats = statUpdateQueries.next();
                    statement.addBatch(stats.getUpdateStatement());
                    statUpdateQueries.remove();
                }
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

}
