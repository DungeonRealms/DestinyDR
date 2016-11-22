package net.dungeonrealms.vgame.player.scoreboard;

import lombok.Getter;
import net.dungeonrealms.common.frontend.lib.scoreboard.ScoreboardBuilder;
import org.bukkit.scoreboard.DisplaySlot;


/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameScoreboard
{
    @Getter
    private ScoreboardBuilder scoreboardHolder;

    @Getter
    private EnumScoreboardType scoreboardType;

    public GameScoreboard(EnumScoreboardType scoreboardType)
    {
        this.scoreboardType = scoreboardType;
        this.scoreboardHolder = new ScoreboardBuilder("&6&lDUNGEON REALMS").setDisplaySlot(DisplaySlot.SIDEBAR);
    }
}
