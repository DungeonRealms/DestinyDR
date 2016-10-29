package net.dungeonrealms.old.game.quest.database;

import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.old.game.quest.objects.Quest;
import net.dungeonrealms.old.game.quest.objects.QuestInfo;
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

    public boolean hasQuestInfo(UUID uuid, Quest quest) {
        return false;
    }

    public void insertQuest(UUID uuid, Quest quest) {
        DatabaseInstance.quests.insertOne(getQuestDocument(quest));
    }

    private Document getQuestDocument(Quest quest) {
        return null;
    }

    public void updateQuestInfo(UUID uuid, QuestInfo quest) {

    }
}
