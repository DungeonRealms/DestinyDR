package net.dungeonrealms.mongo;

/**
 * Created by Nick on 10/7/2015.
 */
public enum EnumGuildData {

    NAME("info.name"),
    MOTD("info.motd"),
    CLAN_TAG("info.clanTag"),

    OWNER("info.owner"),
    CO_OWNER("info.coOwner"),

    OFFICERS("info.officers"),
    MEMBERS("info.members"),
    CREATION_UNIX_DATA("info.unixCreation"),
    INVITATIONS("info.invitations"),

    BOOSTERS_ACTIVE("boosters.active"),
    BOOSTERS_AVAILABLE("boosters.available"),

    ICON("logs.icon"),

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
