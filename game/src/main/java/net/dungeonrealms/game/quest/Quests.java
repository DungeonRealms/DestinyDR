package net.dungeonrealms.game.quest;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.quest.database.QuestDatabaseAPI;
import net.dungeonrealms.game.quest.objects.Quest;
import net.dungeonrealms.game.quest.objects.QuestInfo;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by chase on 7/15/2016.
 */
public class Quests implements GenericMechanic {
    private static final File file = new File(DungeonRealms.getInstance().getDataFolder() + "//quests.yml");
    private static final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

    private static final CopyOnWriteArrayList<Quest> allQuests = new CopyOnWriteArrayList<>();


    public static final ConcurrentHashMap<UUID, List<QuestInfo>> questsProgress = new ConcurrentHashMap<>();

    public static Quest getQuest(String identifier) {
        for (Quest quest : allQuests) {
            if (quest.uniqueIdentifier.equalsIgnoreCase(identifier))
                return quest;
        }
        return null;
    }


    public static void handleLogin(UUID uuid) {
        List questList = new ArrayList<QuestInfo>();
        for (Quest quest : allQuests) {
            if (!QuestDatabaseAPI.getInstance().hasQuestInfo(uuid, quest)) {
                QuestDatabaseAPI.getInstance().insertQuest(uuid, quest);
            }

            questList.add(new QuestInfo(uuid, quest));
        }
        questsProgress.put(uuid, questList);

    }


    public static void handleLogout(UUID uuid) {
        questsProgress.get(uuid).stream().forEach(quest -> QuestDatabaseAPI.getInstance().updateQuestInfo(uuid, quest));
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        allQuests.addAll(yml.getConfigurationSection("quests").getKeys(false).stream().map(Quest::new).collect(Collectors.toList()));

    }

    @Override
    public void stopInvocation() {

    }
}
