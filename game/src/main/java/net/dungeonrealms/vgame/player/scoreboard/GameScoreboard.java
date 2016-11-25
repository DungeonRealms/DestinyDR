package net.dungeonrealms.vgame.player.scoreboard;

import lombok.Getter;
import net.dungeonrealms.common.frontend.lib.scoreboard.ScoreboardBuilder;
import net.dungeonrealms.packet.party.PacketPartyInfo;
import net.dungeonrealms.vgame.player.GamePlayer;
import net.dungeonrealms.vgame.player.scoreboard.value.ScoreboardValue;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.List;


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

    public GameScoreboard(GamePlayer gamePlayer, EnumScoreboardType scoreboardType)
    {
        this.scoreboardType = scoreboardType;
        switch (scoreboardType)
        {
            case PARTY:
                if (gamePlayer.isInParty())
                {
                    this.scoreboardHolder = new ScoreboardBuilder("&b&lPARTY")
                            .setDisplaySlot(DisplaySlot.SIDEBAR);
                } else gamePlayer.updateScoreboard(EnumScoreboardType.DEFAULT);
                break;
            case OBJECTIVE:
                if (gamePlayer.getCurrentObjective() != null)
                {
                    this.scoreboardHolder = new ScoreboardBuilder("&5&lOBJECTIVE")
                            .setDisplaySlot(DisplaySlot.SIDEBAR);
                    new PacketPartyInfo(gamePlayer.getPlayer().getName(), null, true);
                } else gamePlayer.updateScoreboard(EnumScoreboardType.DEFAULT);
                break;
            case DEFAULT:
                break;
        }
    }
}
