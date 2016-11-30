package net.dungeonrealms.api.event.creature;

import lombok.Getter;
import net.dungeonrealms.vgame.world.entity.generic.IGameEntity;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CreatureDamageEntityEvent extends Event {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private IGameEntity gameEntity;

    @Getter
    private Entity entity;

    @Getter
    private double damage;

    public CreatureDamageEntityEvent(IGameEntity gameEntity, Entity entity, double damage) {
        this.gameEntity = gameEntity;
        this.entity = entity;
        this.damage = damage;
    }
}
