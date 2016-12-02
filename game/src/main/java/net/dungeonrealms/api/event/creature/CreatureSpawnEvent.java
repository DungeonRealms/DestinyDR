package net.dungeonrealms.api.event.creature;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.world.entity.generic.IGameEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CreatureSpawnEvent extends Event {

    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private IGameEntity gameEntity;

    public CreatureSpawnEvent(IGameEntity gameEntity)
    {
        this.gameEntity = gameEntity;
    }
}
