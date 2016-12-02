package net.dungeonrealms.common.awt.frame;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameShard {

    @Getter
    private ServerCore serverCore;

    @Getter
    @Setter
    private boolean enabled;

    public GameShard(ServerCore serverCore) {
        this.serverCore = serverCore;
    }
}
