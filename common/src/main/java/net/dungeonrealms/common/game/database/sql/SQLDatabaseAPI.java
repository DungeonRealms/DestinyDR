package net.dungeonrealms.common.game.database.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Cleanup;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SQLDatabaseAPI {

    private static SQLDatabaseAPI instance;

    @Getter
    private SQLDatabase database;

    private Map<UUID, Map<DataType, Object>> storedData = new HashMap<>();

    private final ExecutorService SERVER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SQL Thread").build());

    private Cache<String, UUID> cachedNames = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    public static SQLDatabaseAPI getInstance() {
        if (instance == null) {
            instance = new SQLDatabaseAPI();
        }
        return instance;
    }


    public void init() {
        this.database = new SQLDatabase("158.69.121.40", "dev", "3HCKkPc6mWr63E924C", "dungeonrealms");
    }


    /**
     * Thread safe method, if the uuid had to be loaded in from the database then the callback will be called asynchronously
     * @param name name to search for.
     * @param loadCallback
     */
    public void getUUIDFromName(String name, Consumer<UUID> loadCallback) {
        Player pl = Bukkit.getPlayer(name);
        if (pl != null) {
            if (loadCallback != null) {
                //Cache this for ourselves as well, so we dont need to hitup db..
                this.cachedNames.put(name.toLowerCase(), pl.getUniqueId());
                loadCallback.accept(pl.getUniqueId());
            }
            return;
        }

        UUID stored = this.cachedNames.getIfPresent(name.toLowerCase());
        if (stored != null) {
            if (loadCallback != null) {
                loadCallback.accept(stored);
                return;
            }
        } else {
            //Pull from DB?
            CompletableFuture.runAsync(() -> {
                try {
                    @Cleanup PreparedStatement statement = this.getDatabase().getConnection().prepareStatement("SELECT uuid FROM users WHERE username = ? ORDER BY users.lastLogout DESC LIMIT 1;");
                    statement.setString(1, name);
                    ResultSet rs = statement.executeQuery();
                    if (rs.first()) {
                        UUID found = UUID.fromString(rs.getString("uuid"));
                        this.cachedNames.put(name.toLowerCase(), found);
                        loadCallback.accept(found);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadCallback != null)
                    loadCallback.accept(null);
            }, SERVER_EXECUTOR_SERVICE);
        }
    }

    public void loadData(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try {
                //Essentially make it pull
                @Cleanup PreparedStatement statement = this.getDatabase().getConnection().prepareStatement("SELECT * FROM characters LEFT JOIN attributes");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, SERVER_EXECUTOR_SERVICE);
    }
}
