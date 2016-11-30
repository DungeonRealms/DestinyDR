package net.dungeonrealms.api.event.creature;

import lombok.Getter;
import net.dungeonrealms.api.creature.EnumCreatureState;
import net.dungeonrealms.vgame.world.entity.generic.IGameEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CreatureStateChangeEvent extends Event {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private IGameEntity gameEntity;

    @Getter
    private EnumCreatureState newState;

    @Getter
    private EnumCreatureState oldState;

    public CreatureStateChangeEvent(IGameEntity gameEntity, EnumCreatureState newState, EnumCreatureState oldState) {
        this.gameEntity = gameEntity;
        this.newState = newState;
        this.oldState = oldState;
    }
}
