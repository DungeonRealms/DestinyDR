package net.dungeonrealms.common.game.database.type;

/**
 * Created by Nick on 10/7/2015.
 */
public enum EnumGuildData {

    NAME("info.name"),
    DISPLAY_NAME("info.displayName"),
    MOTD("info.motd"),
    TAG("info.tag"),
    BANNER("info.banner"),

    OWNER("info.owner"),

    OFFICERS("info.officers"),
    MEMBERS("info.members"),

    LEVEL("info.netLevel"),
    EXPERIENCE("info.experience");

    public String key;

    EnumGuildData(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
