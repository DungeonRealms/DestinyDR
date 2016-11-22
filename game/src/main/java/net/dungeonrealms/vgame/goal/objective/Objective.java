package net.dungeonrealms.vgame.goal.objective;

import lombok.Getter;
import net.dungeonrealms.vgame.player.GamePlayer;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Objective
{
    @Getter
    private String name;

    @Getter
    private boolean achievement;

    @Getter
    private EnumObjectiveType objectiveType;

    public Objective(String name, EnumObjectiveType objectiveType, boolean achievement)
    {
        this.name = name;
        this.objectiveType = objectiveType;
        this.achievement = achievement;
    }

    public void send(GamePlayer gamePlayer)
    {
        if(!achievement)
        {
            gamePlayer.setCurrentObjective(this);
        }
    }
}
