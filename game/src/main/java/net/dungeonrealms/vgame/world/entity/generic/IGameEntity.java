package net.dungeonrealms.vgame.world.entity.generic;

import net.dungeonrealms.vgame.world.entity.generic.construct.EntityData;
import net.dungeonrealms.vgame.world.entity.generic.construct.element.EnumEntityElement;
import net.dungeonrealms.vgame.world.entity.generic.construct.health.EntityHealthBar;
import net.minecraft.server.v1_9_R2.EntityInsentient;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IGameEntity {
    EntityData getEntityData();

    EntityInsentient getEntity();

    default boolean isElemental() {
        return this.getEntityData().getEntityElement() != EnumEntityElement.EMPTY; // Not an empty element?
    }

    default void displayHealth() {
        if (this.getEntity().getHealth() > 0 && this.getEntity().getHealth() < this.getEntity().getMaxHealth()) {
            this.getEntity().setCustomName(new EntityHealthBar(this).getAsString());
            this.getEntity().setCustomNameVisible(true);
        }
    }

    default void displayName() {
        this.getEntity().setCustomName(this.getEntityData().getName());
        this.getEntity().setCustomNameVisible(true);
    }
}
