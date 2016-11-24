package net.dungeonrealms.api.creature;

import net.dungeonrealms.api.creature.intelligence.EnumIntelligenceType;
import net.dungeonrealms.api.creature.meta.LivingMeta;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface ICreature extends IEntity
{
    EnumEntityType getEntityType();

    EnumIntelligenceType getIntelligenceType();

    LivingMeta getLivingMeta();

    default void setMovementSpeed(double value)
    {
        this.getEntityInsentient().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(value / 10);
    }

    default void setFollowingRange(double value)
    {
        this.getEntityInsentient().getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(value);
    }

    default void addTarget(Entity entity)
    {

    }

    default void spawn(Location location)
    {
        this.getEntity().setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this.getEntity());
    }
}
