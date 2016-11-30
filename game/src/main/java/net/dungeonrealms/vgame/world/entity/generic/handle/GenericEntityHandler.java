package net.dungeonrealms.vgame.world.entity.generic.handle;

import net.dungeonrealms.api.event.creature.CreatureDamageEntityEvent;
import net.dungeonrealms.api.event.creature.CreatureStateChangeEvent;
import net.dungeonrealms.api.event.creature.EntityDamageCreatureEvent;
import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.vgame.Game;
import org.bukkit.event.EventHandler;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GenericEntityHandler implements SuperHandler.ListeningHandler {
    @Override
    public void prepare() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onCreatureDamageEntity(CreatureDamageEntityEvent event) {
        event.getEntity().getBukkitEntity().sendMessage("");
    }

    @EventHandler
    public void onEntityDamageCreature(EntityDamageCreatureEvent event) {
        event.getGameEntity().displayHealth();
    }

    @EventHandler
    public void onCreatureStateChange(CreatureStateChangeEvent event) {
        event.getGameEntity().getEntityData().setCreatureState(event.getNewState());
    }
}
