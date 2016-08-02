package net.dungeonrealms.game.quest.objects;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.quest.QuestType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Created by chase on 7/15/2016.
 */
public class Quest {

    private static final File file = new File(DungeonRealms.getInstance().getDataFolder() + "//quests.yml");
    private static final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);


    public Reward reward;
    public String uniqueIdentifier;
    public String[] preReqs;
    public QuestType questType;

    public Quest(String questIdentifier) {
        this.uniqueIdentifier = questIdentifier;
        questType = QuestType.getQuestTypeFromString(yml.getString("quest." + questIdentifier + ".type"));
        reward = new Reward(this);
    }
}
