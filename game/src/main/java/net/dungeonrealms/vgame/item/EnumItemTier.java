package net.dungeonrealms.vgame.item;

import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;

import java.util.HashMap;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemTier
{
    ONE(2), TWO(3), THREE(4), FOUR(5), FIVE(6);

    @Getter
    private int maxAttributes;

    private static AtomicCollection<EnumItemTier> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumItemTier(int maxAttributes)
    {
        this.maxAttributes = maxAttributes;
    }

    public static EnumItemTier random()
    {
        if (loaded)
        {
            return atomicCollection.next();
        } else
        {
            // Weight is not final
            atomicCollection.getMap().get().put(0.8, ONE);
            atomicCollection.getMap().get().put(0.6, TWO);
            atomicCollection.getMap().get().put(0.4, THREE);
            atomicCollection.getMap().get().put(0.2, FOUR);
            atomicCollection.getMap().get().put(0.05, FIVE);
            loaded = true;
            return atomicCollection.next();
        }
    }
}
