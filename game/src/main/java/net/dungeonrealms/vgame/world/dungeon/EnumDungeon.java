package net.dungeonrealms.vgame.world.dungeon;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 11/1/2016 at 2:38 PM.
 */
public enum EnumDungeon
{

    VARENGLADE(3, "Varenglade");

    private int tier;
    private String name;

    EnumDungeon(int tier, String name) {
        this.tier = tier;
        this.name = name;
    }

    public int getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }
}
