package net.dungeonrealms.vgame.world.entity.generic.construct;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.vgame.world.entity.generic.construct.element.EnumEntityElement;
import net.dungeonrealms.vgame.world.entity.generic.construct.message.EntityMessageList;

import java.util.UUID;

/**
 * Created by Giovanni on 27-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityData
{
    @Getter
    private UUID uniqueId;

    @Getter
    @Setter
    private EnumEntityTier entityTier;

    @Getter
    @Setter
    private EntityMessageList messageList;

    @Getter
    @Setter
    private EnumEntityElement entityElement;

    @Getter
    @Setter
    private int level;

    public EntityData(UUID uuid)
    {
        this.uniqueId = uuid;
    }

    public boolean isNull()
    {
        return entityElement == null || entityTier == null;
    }
}
