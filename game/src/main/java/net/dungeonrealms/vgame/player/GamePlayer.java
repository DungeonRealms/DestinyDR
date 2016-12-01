package net.dungeonrealms.vgame.player;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.api.creature.lib.damage.EnumDamageSource;
import net.dungeonrealms.common.backend.player.DataPlayer;
import net.dungeonrealms.vgame.player.goal.achievement.EnumAchievement;
import net.dungeonrealms.vgame.player.goal.objective.Objective;
import net.dungeonrealms.vgame.player.scoreboard.EnumScoreboardType;
import net.dungeonrealms.vgame.player.scoreboard.GameScoreboard;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GamePlayer implements IPlayer {

    @Getter
    private DataPlayer data; // All raw data

    @Getter
    private Player player;

    @Setter
    @Getter
    private Objective currentObjective;

    @Getter
    @Setter
    private GameScoreboard gameScoreboard;

    @Getter
    private EnumDamageSource damageSource = EnumDamageSource.PLAYER;

    @Setter
    @Getter
    private boolean inParty;

    @Getter
    @Setter
    private boolean teleporting;

    public GamePlayer(DataPlayer dataPlayer) {
        this.data = dataPlayer;
        this.player = dataPlayer.getPlayer();
    }

    public boolean hasAchievement(EnumAchievement achievement) {
        return this.data.getCollectionData().getAchievements().contains(achievement.name());
    }

    public void updateScoreboard(EnumScoreboardType to) {
        this.gameScoreboard = new GameScoreboard(this, to);
        this.gameScoreboard.getScoreboardHolder().send(this.player);
    }
}
