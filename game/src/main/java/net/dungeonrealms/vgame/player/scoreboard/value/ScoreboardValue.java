package net.dungeonrealms.vgame.player.scoreboard.value;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.vgame.player.scoreboard.EnumScoreboardType;

import java.util.Map;

/**
 * Created by Giovanni on 25-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ScoreboardValue {
    @Getter
    @Setter
    private Map<Integer, String> dataMap;

    public ScoreboardValue(EnumScoreboardType scoreboardType) {
        this.dataMap = Maps.newHashMap();
    }

    public ScoreboardValue plus(int key, String value) {
        this.dataMap.put(key, value);
        return this;
    }
}
