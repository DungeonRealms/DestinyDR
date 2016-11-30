package net.dungeonrealms.common.frontend.lib.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Created by Giovanni on 30-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ScoreboardBuilder {
    @Getter
    private String displayName = "HELLO";

    @Getter
    private ScoreboardManager scoreboardManager;

    @Getter
    private Scoreboard board;

    @Getter
    private Objective objective;

    public ScoreboardBuilder(String displayName) {
        this.displayName = displayName;
        this.scoreboardManager = Bukkit.getServer().getScoreboardManager();
        this.board = getScoreboardManager().getNewScoreboard();
        this.objective = getBoard().registerNewObjective("ronaldo", "bestPlayer"); // None cares

        setDisplaySlot(DisplaySlot.SIDEBAR);
        setDisplayName(displayName);
    }

    public ScoreboardBuilder setDisplaySlot(DisplaySlot displaySlot) {
        getObjective().setDisplaySlot(displaySlot);
        return this;
    }

    public ScoreboardBuilder setDisplayName(String name) {
        getObjective().setDisplayName(name);
        return this;
    }

    public ScoreboardBuilder setLine(int line, String value) {
        getObjective().getScore(ChatColor.translateAlternateColorCodes('&', value)).setScore(line);
        return this;
    }

    public ScoreboardBuilder setScore(String key, int value) {
        getObjective().getScore(key).setScore(value);
        return this;
    }

    public ScoreboardBuilder send(Player player) {
        player.setScoreboard(getBoard());
        return this;
    }
}
