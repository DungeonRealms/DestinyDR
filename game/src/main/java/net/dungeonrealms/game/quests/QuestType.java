package net.dungeonrealms.game.quests;

/**
 * Created by chase on 7/17/2016.
 */
public enum QuestType {

    KILLMOB("kill_mob"), GATHERITEM("get_item"), TALKTONPC("npc_talk"), DELIVERITEM("deliver");


    public String databaseName;

    QuestType(String databaseEntry) {
        this.databaseName = databaseEntry;
    }


    public static QuestType getQuestTypeFromString(String questTypeString) {
        for (QuestType type : values()) {
            if (type.databaseName.equalsIgnoreCase(questTypeString) || type.name().equalsIgnoreCase(questTypeString))
                return type;
        }
        return null;
    }

}
