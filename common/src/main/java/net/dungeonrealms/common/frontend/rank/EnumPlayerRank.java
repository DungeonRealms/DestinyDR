package net.dungeonrealms.common.frontend.rank;

/**
 * Created by Evoltr on 11/15/2016.
 */
public enum EnumPlayerRank
{
    DEFAULT(0, "", "&7"),
    SUB(1, "S", "&a&l"),
    SUB_2(2, "S+", "&6&"),
    SUB_3(3, "S++", "&e&l"),
    PMOD(4, "PMOD", "&f&l"),
    GM(5, "GM", "&b&l"),
    DEV(6, "DEV", "&b&l");

    private int id;
    private String name;
    private String color;

    EnumPlayerRank(int id, String name, String color)
    {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public static EnumPlayerRank getRank(String name)
    {
        for (EnumPlayerRank rank : values())
        {
            if (rank.getName().equalsIgnoreCase(name))
            {
                return rank;
            }
        }
        return null;
    }

    public int getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getColor()
    {
        return color;
    }

    public boolean hasRank(EnumPlayerRank rank)
    {
        return compareTo(rank) >= 0;
    }
}
