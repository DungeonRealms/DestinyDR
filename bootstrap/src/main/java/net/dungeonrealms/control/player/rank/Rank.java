package net.dungeonrealms.control.player.rank;

/**
 * Created by Evoltr on 11/15/2016.
 */
public enum Rank {

    // I dont know all ranks so this will suffice for now
    OWNER(3, "Owner", "&c"),
    DEV(2, "Dev", "&c"),
    DEFAULT(1, "Default", "&7");

    private int id;
    private String name;
    private String color;

    Rank(int id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public static Rank getRank(String name) {
        for (Rank rank : values()) {
            if (rank.getName().equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return null;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean hasRank(Rank rank) {
        return compareTo(rank) >= 0;
    }
}
