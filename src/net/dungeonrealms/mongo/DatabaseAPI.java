package net.dungeonrealms.mongo;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class DatabaseAPI {

    static DatabaseAPI instance = null;

    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    public static HashMap<UUID, Document> PLAYERS = new HashMap<>();

    public static ArrayList<UUID> REQUEST_NEW_DATA = new ArrayList<>();

    public void update(UUID uuid, EnumOperators EO, String variable, Object object) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable, object)),
                (result, t) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (t == null) {
                        REQUEST_NEW_DATA.add(uuid);
                    }
                });
    }

    public Object getData(EnumData data, UUID uuid) {
        switch (data) {
            /*
            Player Variables
             */
            case HEALTH:
                return ((Document) PLAYERS.get(uuid).get("info")).get("health", Integer.class);
            case FIRST_LOGIN:
                return ((Document) PLAYERS.get(uuid).get("info")).get("firstLogin", Long.class);
            case LAST_LOGIN:
                return ((Document) PLAYERS.get(uuid).get("info")).get("lastLogin", Long.class);
            case IS_PLAYING:
                return ((Document) PLAYERS.get(uuid).get("info")).get("isPlaying", Boolean.class);
            case LEVEL:
                return ((Document) PLAYERS.get(uuid).get("info")).get("netLevel", Integer.class);
            case RANK:
                return ((Document) PLAYERS.get(uuid).get("info")).get("rank", String.class);
            case GEMS:
                return ((Document) PLAYERS.get(uuid).get("info")).get("gems", Integer.class);
            case WAYSHRINE:
                return ((Document) PLAYERS.get(uuid).get("info")).get("wayshrine", Location.class);
            /*
            Player Attribute Variables
             */
            default:
        }
        return null;
    }

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> REQUEST_NEW_DATA.forEach(this::requestPlayer), 0, 20l);
    }

    public void requestPlayer(UUID uuid) {
        Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document, throwable) -> {
            if (document == null) {
                addNewPlayer(uuid);
                REQUEST_NEW_DATA.add(uuid);
            } else if (document != null) {
                if (REQUEST_NEW_DATA.contains(uuid)) {
                    PLAYERS.put(uuid, document);
                    REQUEST_NEW_DATA.remove(uuid);
                }
            }
        });
    }

    public void addNewPlayer(UUID uuid) {
        Document newPlayerDocument =
                new Document("info",
                        new Document("uuid", uuid.toString())
                                .append("health", 50)
                                .append("gems", 100)
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0l)
                                .append("netLevel", 0)
                                .append("rank", "DEFAULT")
                                .append("wayshrine", new Location(Bukkit.getWorlds().get(0), -367, 83, 390))
                                .append("isPlaying", true)

                );
        Database.collection.insertOne(newPlayerDocument, (aVoid, throwable) -> Utils.log.info("Injected new player!"));
    }

}
