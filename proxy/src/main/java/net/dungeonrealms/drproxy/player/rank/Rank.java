package net.dungeonrealms.drproxy.player.rank;

/**
 * Created by Evoltr on 11/30/2016.
 */
public enum Rank {

    DEFAULT(0, "", "&7"),
    SUB(1, "S", "&a&l"),
    SUB_2(2, "S+", "&6&l"),
    SUB_3(3, "S++", "&e&l"),
    PMOD(4, "PMOD", "&f&l"),
    GM(5, "GM", "&b&l"),
    DEV(6, "DEV", "&b&l");

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
