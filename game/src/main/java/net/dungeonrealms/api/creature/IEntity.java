package net.dungeonrealms.api.creature;

import net.dungeonrealms.api.creature.lib.damage.IDamageSource;
import net.dungeonrealms.common.awt.reflect.BasicReflection;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

import java.util.List;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
interface IEntity extends IDamageSource {

    Entity getEntity();

    EntityCreature getEntityCreature();

    EntityInsentient getEntityInsentient();

    default void kill() {
        this.getEntity().getWorld().removeEntity(this.getEntity());
    }

    default void respawn(Location location) {
        kill();
        spawn(location);
    }

    default void spawn(Location location) {
        this.getEntity().setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this.getEntity());
    }

    default void clearIntelligence() {
        ((List) BasicReflection.accessFieldObject("c", this.getEntityInsentient().goalSelector, PathfinderGoalSelector.class)).clear();
        ((List) BasicReflection.accessFieldObject("b", this.getEntityInsentient().goalSelector, PathfinderGoalSelector.class)).clear();
        ((List) BasicReflection.accessFieldObject("c", this.getEntityInsentient().targetSelector, PathfinderGoalSelector.class)).clear();
        ((List) BasicReflection.accessFieldObject("b", this.getEntityInsentient().targetSelector, PathfinderGoalSelector.class)).clear();

        // Keep always
        this.getEntityInsentient().goalSelector.a(7, new PathfinderGoalRandomStroll(this.getEntityCreature(), 1.0D));
    }

    default void register() {

    }
}
