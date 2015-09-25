package net.dungeonrealms.rank;

import org.bukkit.ChatColor;

/**
 * Created by Nick on 9/24/2015.
 */
public enum EnumRank {

    DEFAULT(0, "DEFAULT", ChatColor.GRAY + "[DEFAULT]" + ChatColor.RESET),
    SUB(1, "SUB", "[SUB]"),
    SUBPLUS(2, "SUB+", "[SUB+]"),
    MOD(3, "MOD", "[CHAT]"),
    ADMIN(4, "ADMIN", "[ADMIN]"),
    DEVELOPER(5, "DEVELOPER", "[DEVELOPER]"),
    OWNER(6, "OWNER", "[OWNER]"),;

    private int id;
    private String mongoName;
    private String prefix;

    EnumRank(int id, String mongoName, String prefix) {
        this.id = id;
        this.mongoName = mongoName;
        this.prefix = prefix;
    }

    public int getId() {
        return id;
    }

    public String getMongoName() {
        return mongoName;
    }

    public String getPrefix() {
        return prefix;
    }

    public static EnumRank getByName(String name) {
        for (EnumRank er : values()) {
            if (er.getMongoName().equalsIgnoreCase(name)) {
                return er;
            }
        }
        return null;
    }
}
