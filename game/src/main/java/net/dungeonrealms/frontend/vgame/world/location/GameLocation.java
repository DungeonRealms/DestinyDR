package net.dungeonrealms.frontend.vgame.world.location;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.frontend.Game;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameLocation extends Location {

    @Getter
    private double[] axisValues;

    public GameLocation(double[] axisValues) {
        super(Game.getGame().getGameWorld().getBukkitWorld(), axisValues[0], axisValues[1], axisValues[2]);
        this.axisValues = axisValues;
    }

    /**
     * Checks if there are nearby entities
     *
     * @param range The axis{x,y,z} values to check in
     * @return boolean
     */
    public boolean hasEntitiesNearby(int range) {
        return arrayNearbyEntities(Entity.class, range).length != 0;
    }

    /**
     * Gets the nearby entities and adds them to an array
     *
     * @param entityClass The target entity
     * @param range       The axis{x,y,z} values to check in
     * @return The entity array
     */
    private <E extends Entity> Entity[] arrayNearbyEntities(Class<E> entityClass, int range) {
        List<Entity> entityList = Lists.newArrayList();
        this.getWorld().getNearbyEntities(this, range, range, range).stream().filter(entityClass::isInstance).forEach(entityList::add);
        Entity[] entities = new Entity[entityList.size()];
        for (int i = 0; i < entities.length; i++) {
            entities[i] = entityList.get(i);
        }
        return entities;
    }

    /**
     * Gets the nearby entities and adds them to a list
     *
     * @param entityClass The target entity
     * @param range       The axis{x,y,z} values to check in
     * @return The entity list
     */
    private <E extends Entity> List<Entity> listNearbyEntities(Class<E> entityClass, int range) {
        List<Entity> entityList = Lists.newArrayList();
        for (Entity entity : this.arrayNearbyEntities(entityClass, range)) entityList.add(entity);
        return entityList;
    }
}
