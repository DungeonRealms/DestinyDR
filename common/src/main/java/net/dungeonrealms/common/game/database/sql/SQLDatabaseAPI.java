package net.dungeonrealms.common.game.database.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.internal.ConcurrentSet;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.concurrent.Query;
import net.dungeonrealms.common.game.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SQLDatabaseAPI {

    private static SQLDatabaseAPI instance;

    @Getter
    private SQLDatabase database;

    @Getter
    private final ExecutorService SERVER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("UUID Thread").build());

    private final ScheduledExecutorService QUERY_QUEUE_THREAD = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("SQL Query Queue Thread").build());

    private Cache<String, UUID> cachedNames = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();


    @Getter
    private Map<Integer, UUIDName> accountIdNames = new HashMap<>();

    public static SQLDatabaseAPI getInstance() {
        if (instance == null) {
            instance = new SQLDatabaseAPI();
        }
        return instance;
    }


    public void addQuery(QueryType type, Object... values){
        String query = type.getQuery(values);
        if(query != null){
            this.sqlQueries.add(query);
        }
    }

    @Getter
    private volatile Set<String> sqlQueries = new ConcurrentSet<>();
    public void init() {
        this.database = new SQLDatabase("158.69.121.40", "dev", "3HCKkPc6mWr63E924C", "dungeonrealms");

        QUERY_QUEUE_THREAD.scheduleAtFixedRate(() -> {
            //Dont do anything.. no queries..
            if(sqlQueries.isEmpty())return;

            //Execute these random updates we want to do.
            try {
                long start = System.currentTimeMillis();
                @Cleanup PreparedStatement statement = getDatabase().getConnection().prepareStatement("");
                int added = 0;
                for(String query : sqlQueries){
                    statement.addBatch(query);
                    //Remove the query after its been applied to the batch.
                    sqlQueries.remove(query);
                    added++;
                }
                statement.executeBatch();
                if(added > 0 && Constants.debug){
                    Bukkit.getLogger().info("Executed a batch of " + added + " statements in " + (System.currentTimeMillis() - start) + "ms");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Every tick try to process a batch of the lot..
        }, 50, 50, TimeUnit.MILLISECONDS);

        //Load all account names in so that we do NOT need to call on the SQL database every time we want to resolve an ID to a name or uuid..
        CompletableFuture.runAsync(() -> {
            try {
                @Cleanup PreparedStatement statement = this.database.getConnection().prepareStatement("SELECT account_id, username, uuid FROM users;");

                long time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    try {
                        int id = rs.getInt("users.account_id");
                        String name = rs.getString("users.username");
                        UUID uuid = UUID.fromString(rs.getString("users.uuid"));
                        this.accountIdNames.put(id, new UUIDName(uuid, name));
                    } catch (Exception e) {
                        Bukkit.getLogger().info("Problem loading id " + rs.getString("users.username") + " from database!");
                        e.printStackTrace();
                    }
                }

                Bukkit.getLogger().info("Loaded " + this.accountIdNames.size() + " Account Id + UUID's into memory in " + (System.currentTimeMillis() - time) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public UUID getUUIDFromAccountID(int id) {
        UUIDName name = this.accountIdNames.get(id);
        if (name == null) return null;
        return name.getUuid();
    }

    public String getUsernameFromAccountID(int id) {
        UUIDName name = this.accountIdNames.get(id);
        if (name == null) return null;
        return name.getName();
    }

    /**
     * Returns null if the ID for the given UUID was unable to be found.
     *
     * @param uuid
     * @return
     */
    public Integer getAccountIdFromUUID(UUID uuid) {
        for (Map.Entry<Integer, UUIDName> entry : this.accountIdNames.entrySet()) {
            if (uuid.equals(entry.getValue().getUuid())) return entry.getKey();
        }
        return null;
    }

    public String getUsernameFromUUID(UUID uuid){
        for (Map.Entry<Integer, UUIDName> entry : this.accountIdNames.entrySet()) {
            if (uuid.equals(entry.getValue().getUuid())) return entry.getValue().getName();
        }
        return null;
    }
    /**
     * Thread safe method, if the uuid had to be loaded in from the database then the callback will be called asynchronously
     *
     * @param name         name to search for.
     * @param loadCallback
     */
    public void getUUIDFromName(String name, boolean contactMojang, @NonNull Consumer<UUID> loadCallback) {
        UUID stored;
        if (name.contains("-") && name.length() > 25) {
            try {
                stored = UUID.fromString(name);
                if (loadCallback != null) {
                    //Just translate the given uuid?
                    loadCallback.accept(stored);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        Player pl = Bukkit.getPlayer(name);
        if (pl != null) {
            if (loadCallback != null) {
                //Cache this for ourselves as well, so we dont need to hitup db..
                this.cachedNames.put(name.toLowerCase(), pl.getUniqueId());
                loadCallback.accept(pl.getUniqueId());
            }
            return;
        }

        stored = this.cachedNames.getIfPresent(name.toLowerCase());
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
                    } else if (contactMojang) {
                        //Get UUID from Mojang? Prefer not to do this since they rate limit pretty easily..
                        UUIDFetcher.getUUID(name, loadCallback);
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
