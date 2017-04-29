package net.dungeonrealms.common.game.database.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.internal.ConcurrentSet;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SQLDatabaseAPI {

    private static SQLDatabaseAPI instance;

    @Getter
    private SQLDatabase database;

    @Getter
    public static final ExecutorService SERVER_EXECUTOR_SERVICE = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("SQL Data Thread").build());

    private final ScheduledExecutorService QUERY_QUEUE_THREAD = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("SQL Query Queue Thread").build());

    private Cache<String, UUID> cachedNames = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();


    private Runnable saveRunnable;

    @Getter
    private Map<Integer, UUIDName> accountIdNames = new HashMap<>();

    public void shutdown() {
        Bukkit.getLogger().info("Shutting down SQL Server Executor Thread...");
        SERVER_EXECUTOR_SERVICE.shutdown();
        Bukkit.getLogger().info("SQL Server Executor Thread shutdown.");

        long start = System.currentTimeMillis();
        Bukkit.getLogger().info("Shutting SQL Server Query Executor Thread with " + this.sqlQueries.size() + " Queries remaining.");
        this.QUERY_QUEUE_THREAD.execute(() -> {
            if (this.sqlQueries.size() > 0)
                System.out.println("Executing " + sqlQueries.size() + " Queries before shutting down threads..");
            this.saveRunnable.run();
            this.QUERY_QUEUE_THREAD.shutdown();
            Constants.log.info("Shut down SQL Query Thread, took " + (System.currentTimeMillis() - start) + "ms to finish queries.");
        });
    }

    public static SQLDatabaseAPI getInstance() {
        if (instance == null) {
            instance = new SQLDatabaseAPI();
        }
        return instance;
    }


    @Getter
    public Set<UUID> pendingPlayerCreations = new HashSet<>();

    private DecimalFormat format = new DecimalFormat("#,###");

    /**
     * Returns the account_id created in the callback.
     *
     * @param uuid
     * @param username
     * @param createdCallback
     */
    public void createDataForPlayer(UUID uuid, String username, String ipAddress, Consumer<Integer> createdCallback) {

        SQLDatabaseAPI.getInstance().executeQuery("SELECT account_id, selected_character_id FROM users WHERE users.uuid = '" + uuid + "';", false, rs -> {
            try {
                if (rs.first()) {
                    //Data exists.
                    int charID = rs.getInt("selected_character_id");
                    if (charID == 0) {
                        long started = System.currentTimeMillis();
                        pendingPlayerCreations.add(uuid);
                        int accountID = rs.getInt("account_id");
                        //NO USERRRRR????????
                        Constants.log.info("No SELECTED character_id for " + accountID);

                        SQLDatabaseAPI.getInstance().executeUpdate(complete -> {
                            SQLDatabaseAPI.getInstance().executeQuery(String.format("SELECT character_id FROM characters WHERE account_id = '%s' ORDER BY created DESC LIMIT 1;", accountID), false, results -> {
                                try {
                                    if(results.first()) {
                                        int newCharID = results.getInt("character_id");
                                        SQLDatabaseAPI.getInstance().executeBatch(completed -> {
                                                    pendingPlayerCreations.remove(uuid);
                                                    Bukkit.getLogger().info("Executed new player create queries in " + format.format(System.currentTimeMillis() - started) + "ms");
                                                },
                                                String.format("UPDATE users SET selected_character_id = '%s' WHERE account_id = '%s';", newCharID, accountID),
                                                String.format("INSERT IGNORE INTO attributes(character_id) VALUES ('%s');", newCharID),
                                                String.format("INSERT IGNORE INTO ranks(account_id) VALUES ('%s');", accountID),
                                                String.format("INSERT IGNORE INTO toggles(account_id) VALUES ('%s');", accountID),
                                                String.format("INSERT IGNORE INTO realm(character_id) VALUES ('%s');", newCharID),
                                                String.format("INSERT IGNORE INTO statistics(character_id) VALUES ('%s');", newCharID));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }, String.format("INSERT IGNORE INTO characters(account_id, created) VALUES ('%s', '%s');", accountID, System.currentTimeMillis()), false);
                    }
                } else {
                    //No user... CREATE NEW??????
                    SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
                        //It didnt exist, so we can just create everything and grab the newly created account id.
                        if (updates != null && updates > 0) {
                            pendingPlayerCreations.add(uuid);
                            long start = System.currentTimeMillis();
                            SQLDatabaseAPI.getInstance().executeQuery(String.format("SELECT account_id FROM users WHERE uuid = '%s';", uuid.toString()), false, results -> {
                                try {
                                    if (results.first()) {
                                        int accountID = results.getInt("account_id");

                                        //Accept this so we can get that callback going..
                                        createdCallback.accept(accountID);
                                        SQLDatabaseAPI.getInstance().executeUpdate(rowsAffected -> {
                                            if (rowsAffected > 0) {
                                                //We created it!!!
                                                SQLDatabaseAPI.getInstance().executeQuery("SELECT character_id FROM characters WHERE account_id = '" + accountID + "';", false, charResult -> {
                                                    try {
                                                        if (charResult.first()) {
                                                            int character_id = charResult.getInt("character_id");
                                                            Bukkit.getLogger().info("Creating Character ID for " + username + " (" + accountID + ") CharID = " + character_id);
                                                            SQLDatabaseAPI.getInstance().executeBatch(completed -> {
                                                                        pendingPlayerCreations.remove(uuid);
                                                                        Bukkit.getLogger().info("Executed new player create queries in " + format.format(System.currentTimeMillis() - start) + "ms");
                                                                    },
                                                                    String.format("UPDATE users SET selected_character_id = '%s' WHERE account_id = '%s';", character_id, accountID),
                                                                    String.format("INSERT INTO attributes(character_id) VALUES ('%s');", character_id),
                                                                    String.format("INSERT INTO ranks(account_id) VALUES ('%s');", accountID),
                                                                    String.format("INSERT INTO toggles(account_id) VALUES ('%s');", accountID),
                                                                    String.format("INSERT INTO realm(character_id) VALUES ('%s');", character_id),
                                                                    String.format("INSERT INTO statistics(character_id) VALUES ('%s');", character_id),
                                                                    String.format("INSERT INTO ip_addresses(account_id, ip_address, last_used) VALUES ('%s', '%s', '%s');", accountID, ipAddress, System.currentTimeMillis()));
                                                        } else {
                                                            Bukkit.getLogger().info("Null resultSet for " + username);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }
                                        }, String.format("INSERT IGNORE INTO characters(account_id, created) VALUES ('%s', '%s');", accountID, System.currentTimeMillis()));

                                        Bukkit.getLogger().info("Created new account ID for " + username + " (" + uuid.toString() + ")");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }, String.format("INSERT IGNORE INTO users(uuid, username, joined, last_login) VALUES ('%s', '%s', '%s', '%s');", uuid.toString(), username, System.currentTimeMillis(), System.currentTimeMillis()), false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }


    public void addQuery(QueryType type, Object... values) {
        String query = type.getQuery(values);
        if (query != null) {
            this.sqlQueries.add(query);
        }
    }


    public void executeQuery(String query, boolean async, @NonNull Consumer<ResultSet> callback) {
        if (async) {
            CompletableFuture.runAsync(() -> executeQuery(query, false, callback), SERVER_EXECUTOR_SERVICE);
            return;
        }
        System.out.println("Attempting to execute query: " + query);
        try {
            @Cleanup PreparedStatement statement = getDatabase().getConnection().prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            callback.accept(rs);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.accept(null);
    }

    public void executeQuery(String query, @NonNull Consumer<ResultSet> callback) {
        executeQuery(query, true, callback);
    }

    public void executeBatch(Consumer<Boolean> callback, String... queries) {
        //Need to update data NOW!!!!!!!!!!!
        CompletableFuture.runAsync(() -> {
            try {
                @Cleanup PreparedStatement statement = getDatabase().getConnection().prepareStatement("");
                for (String query : queries) {
                    statement.addBatch(query);
                }
                statement.executeBatch();
                if (callback != null)
                    callback.accept(true);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (callback != null)
                callback.accept(false);
        }, SERVER_EXECUTOR_SERVICE);

    }

    public void executeUpdate(Consumer<Integer> callback, String query) {
        executeUpdate(callback, query, true);
    }

    public void executeUpdate(Consumer<Integer> callback, String query, boolean async) {
        //Need to update data NOW!!!!!!!!!!!
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> executeUpdate(callback, query, false), SERVER_EXECUTOR_SERVICE);
            return;
        }
        try {
            PreparedStatement statement = getDatabase().getConnection().prepareStatement(query);

            if (Constants.debug)
                Constants.log.info("Updating database with query: " + query);

            int toReturn = statement.executeUpdate();
            if (callback != null)
                callback.accept(toReturn);

            //Just manually close statement?
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null)
                callback.accept(-1);
        }
    }

    @Getter
    private volatile Set<String> sqlQueries = new ConcurrentSet<>();

    public void init() {
        Bukkit.getLogger().info("Attempting to connect to MySQL database...");
        this.database = new SQLDatabase("158.69.121.40", "root", "N963GSvR2xwM9D5S5b4934HfDH", "dungeonrealms");
//        this.database = new SQLDatabase("127.0.0.1", "dev", "3HCKkPc6mWr63E924C", "dungeonrealms");

        this.saveRunnable = () -> {
            //Dont do anything.. no queries..
            if (sqlQueries.isEmpty()) return;
            //Execute these random updates we want to do.
            try {
                long start = System.currentTimeMillis();
                PreparedStatement statement = getDatabase().getConnection().prepareStatement("");
                int added = 0;
                for (String query : sqlQueries) {
                    statement.addBatch(query);
//                    if(Constants.debug)
//                        Constants.log.info("Adding");
                    //Remove the query after its been applied to the batch.
                    sqlQueries.remove(query);
                    if (Constants.debug) {
                        Constants.log.info("Adding query: " + query);
                    }
                    added++;
                }
                statement.executeBatch();
                if (added > 0 && Constants.debug) {
                    Bukkit.getLogger().info("Executed a batch of " + added + " statements in " + (System.currentTimeMillis() - start) + "ms");
                }
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        QUERY_QUEUE_THREAD.scheduleAtFixedRate(this.saveRunnable, 50, 50, TimeUnit.MILLISECONDS);

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
                        this.cachedNames.put(name, uuid);
                        Bukkit.getLogger().info("Loaded id " + id + " for " + name);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("Problem loading id " + rs.getString("users.username") + " from database!");
                        e.printStackTrace();
                    }
                }

                Bukkit.getLogger().info("Loaded " + this.accountIdNames.size() + " Account Id + UUID's into memory in " + (System.currentTimeMillis() - time) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, SERVER_EXECUTOR_SERVICE);
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

    public String getNameFromUUID(UUID uuid) {
        for (Map.Entry<Integer, UUIDName> entry : this.accountIdNames.entrySet()) {
            if (uuid.equals(entry.getValue().getUuid())) return entry.getValue().getName();
        }
        return null;
    }

    public String getUsernameFromUUID(UUID uuid) {
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
                    @Cleanup PreparedStatement statement = this.getDatabase().getConnection().prepareStatement("SELECT uuid FROM users WHERE username = ? ORDER BY users.last_logout DESC LIMIT 1;");
                    statement.setString(1, SQLDatabaseAPI.filterSQLInjection(name));
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

    public static String filterSQLInjection(String string) {
        return string.replaceAll("'", "").replace("\"", "");
    }
}
