package net.dungeonrealms.api.event;

import net.dungeonrealms.api.creature.EnumCreatureState;
import net.dungeonrealms.api.event.creature.CreatureDamageEntityEvent;
import net.dungeonrealms.api.event.creature.CreatureStateChangeEvent;
import net.dungeonrealms.api.event.creature.EntityDamageCreatureEvent;
import net.dungeonrealms.vgame.old.Game;
import net.dungeonrealms.vgame.world.entity.generic.IGameEntity;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CreatureCaller implements ICaller {

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = ((CraftEntity) event.getEntity()).getHandle();
        if (entity instanceof IGameEntity) {
            Game.getGame().getServer().getPluginManager()
                    .callEvent(new net.dungeonrealms.api.event.creature.CreatureSpawnEvent((IGameEntity) entity));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = ((CraftEntity) event.getEntity()).getHandle();
        Entity damager = ((CraftEntity) event.getEntity()).getHandle();
        if (damager instanceof IGameEntity) {
            IGameEntity gameEntity = (IGameEntity) damager;
            // When a game entity hits a bukkit entity - TODO support for non living entities
            Game.getGame().getServer().getPluginManager().callEvent(new CreatureDamageEntityEvent(gameEntity, damaged, ((LivingEntity) damager).getEquipment().getItemInMainHand()));
        } else if (damaged instanceof IGameEntity) {
            IGameEntity gameEntity = (IGameEntity) damaged;
            // When a bukkit entity hits a game entity
            Game.getGame().getServer().getPluginManager().callEvent(new EntityDamageCreatureEvent(gameEntity, damager.getBukkitEntity(),
                    ((LivingEntity) damager).getEquipment().getItemInMainHand()));
            // Update the creature it's state
            Game.getGame().getServer().getPluginManager().callEvent(new CreatureStateChangeEvent(gameEntity, EnumCreatureState.DAMAGED, gameEntity.getEntityData().getCreatureState()));
        }
    }
}
