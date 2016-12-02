package net.dungeonrealms.api.event.creature;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.world.entity.generic.IGameEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityDamageCreatureEvent extends Event {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private IGameEntity gameEntity;

    // We'll use a Bukkit entity for this one instead
    @Getter
    private Entity entity;

    @Getter
    private ItemStack itemStack;

    public EntityDamageCreatureEvent(IGameEntity gameEntity, Entity entity, ItemStack itemStack) {
        this.gameEntity = gameEntity;
        this.entity = entity;
        this.itemStack = itemStack;
    }
}