package net.dungeonrealms.vgame.world.entity.generic;

import lombok.Getter;
import net.dungeonrealms.vgame.world.entity.generic.type.EntityBandit;
import net.dungeonrealms.vgame.world.entity.generic.type.EntitySkeleton;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumGameEntity {
    BANDIT(EntityBandit.class, Arrays.asList("Lazy", "Old", "Starving", "Clumsy")),
    SKELETON(EntitySkeleton.class, Arrays.asList("Broken", "Rotting"));

    @Getter
    private Class entityClass;

    @Getter
    private List<String> simpleNames;

    EnumGameEntity(Class clazz, List<String> simpleNames) {
        this.entityClass = clazz;
        this.simpleNames = simpleNames;
    }
}
