package net.dungeonrealms.lobby.player;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TemporaryPlayer {

    @Getter
    private UUID uniqueId;

    @Getter
    @Setter
    private boolean dataVerified;

    public TemporaryPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
}
