package net.dungeonrealms.game.quests.database;

import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chase on 7/15/2016.
 */
public class QuestDatabaseAPI {
    private static QuestDatabaseAPI instance = null;
    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();

    public static QuestDatabaseAPI getInstance() {
        if (instance == null) {
            instance = new QuestDatabaseAPI();
        }
        return instance;
    }

}
