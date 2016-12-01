package net.dungeonrealms.vgame.world.entity.generic;

import net.dungeonrealms.vgame.world.entity.generic.construct.EntityData;
import net.dungeonrealms.vgame.world.entity.generic.construct.element.EnumEntityElement;
import net.dungeonrealms.vgame.world.entity.generic.construct.health.EntityHealthBar;
import net.dungeonrealms.vgame.world.location.GameLocation;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IGameEntity {

    EntityData getEntityData();

    EntityInsentient getEntity();

    /**
     * Gets the game location of the game entity
     *
     * @return The game location
     */
    default GameLocation getGameLocation() {
        double axisValues[] = {this.getEntity().getBukkitEntity().getLocation().getX(),
                this.getEntity().getBukkitEntity().getLocation().getY(),
                this.getEntity().getBukkitEntity().getLocation().getZ()};
        return new GameLocation(axisValues);
    }

    /**
     * Checks whether the game entity is elemental or not
     *
     * @return is elemental
     */
    default boolean isElemental() {
        return this.getEntityData().getEntityElement() != EnumEntityElement.EMPTY; // Not an empty element?
    }

    /**
     * Displays the EntityHealthBar for the game entity
     *
     * @see EntityHealthBar
     */
    default void displayHealth() {
        if (this.getEntity().getHealth() > 0 && this.getEntity().getHealth() < this.getEntity().getMaxHealth()) {
            this.getEntity().setCustomName(new EntityHealthBar(this).getAsString());
            this.getEntity().setCustomNameVisible(true);
        }
    }

    /**
     * Displays the name stored in the entity data of the game entity
     */
    default void displayName() {
        this.getEntity().setCustomName(this.getEntityData().getName());
        this.getEntity().setCustomNameVisible(true);
    }

    /**
     * Checks whether the game entity has other entities nearby
     *
     * @param axisValues The x y z
     * @return has entities nearby
     */
    default boolean hasEntitiesNearby(double[] axisValues) {
        return !this.getEntity().getBukkitEntity().getNearbyEntities(axisValues[0], axisValues[1], axisValues[2]).isEmpty();
    }

    /**
     * Checks whether the game entity has players nearby
     *
     * @param axisValues The x y z
     * @return has players nearby
     */
    default boolean hasPlayersNearby(double[] axisValues) {
        for (Entity entity : this.getEntity().getBukkitEntity().getNearbyEntities(axisValues[0], axisValues[1], axisValues[2])) {
            return entity != null && entity instanceof Player;
        }
        return false;
    }
}
