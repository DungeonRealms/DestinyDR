package net.dungeonrealms.api.exception;

import net.dungeonrealms.frontend.vgame.world.entity.generic.construct.EntityData;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class InvalidEntityDataException extends Exception {
    public InvalidEntityDataException(EntityData entityData) {
        super("Entity data is null for [" + entityData.getUniqueId() + "] -> entity generation skipped");
    }
}
