package net.dungeonrealms.api.creature;

import net.dungeonrealms.api.creature.damage.IDamageSource;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
interface IEntity extends IDamageSource
{
    Entity getEntity();

    EntityInsentient getEntityInsentient();

    default void kill()
    {
        this.getEntity().getWorld().removeEntity(this.getEntity());
    }

    default void clearIntelligence()
    {

    }
}
