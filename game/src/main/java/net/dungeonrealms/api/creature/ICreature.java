package net.dungeonrealms.api.creature;

import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface ICreature
{
    Entity getEntity();

    CreatureType getCreatureType();

    default void kill()
    {
        this.getEntity().getWorld().removeEntity(this.getEntity());
    }

    default void spawn(Location location)
    {
        getEntity().setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this.getEntity());
    }
}
