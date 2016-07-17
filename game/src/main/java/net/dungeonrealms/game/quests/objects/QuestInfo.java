package net.dungeonrealms.game.quests.objects;

import java.util.UUID;

/**
 * Created by chase on 7/16/2016.
 */
public class QuestInfo {

    public UUID uuid;
    public Quest quest;

    public int goalCurrent;


    public QuestInfo(UUID uuid, Quest quest) {
        this.uuid = uuid;
        this.quest = quest;
        
    }
}
