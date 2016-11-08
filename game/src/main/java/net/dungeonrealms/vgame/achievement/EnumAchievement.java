package net.dungeonrealms.vgame.achievement;

import lombok.Getter;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumAchievement
{
    ANDALUCIA(0, "&a&lWELCOME TO ANDALUCIA", new String[]{"&7Welcome to the province of Andalucia.."}, "world.achievements.start", 0);

    @Getter
    private String name;

    @Getter
    private String[] description;

    @Getter
    private int id;

    @Getter
    private String databaseIdentifier;

    @Getter
    private int expReward;

    EnumAchievement(int id, String name, String[] description, String databaseIdentifier, int expReward)
    {
        this.name = name;
        this.id = id;
        this.description = description;
        this.databaseIdentifier = databaseIdentifier;
        this.expReward = expReward;
    }
}
