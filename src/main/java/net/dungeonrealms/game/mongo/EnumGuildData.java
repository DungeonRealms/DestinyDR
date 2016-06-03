package net.dungeonrealms.game.mongo;

/**
 * Created by Nick on 10/7/2015.
 */
public enum EnumGuildData {

    NAME("info.name"),
    MOTD("info.motd"),
    CLAN_TAG("info.clanTag"),

    OWNER("info.owner"),

    OFFICERS("info.officers"),
    MEMBERS("info.members"),

    LEVEL("info.netLevel"),
    EXPERIENCE("info.experience")
    ;

    public String key;

    EnumGuildData(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
