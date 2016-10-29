package net.dungeonrealms.api.collection;

import lombok.Getter;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AtomicCollection<E>
{
    @Getter
    private final ThreadLocal<NavigableMap<Double, E>> map;

    // Using atomic references to prevent the duplication of objects within an atomic collection.
    {
        map = new ThreadLocal<NavigableMap<Double, E>>()
        {
            @Override
            protected NavigableMap<Double, E> initialValue()
            {
                return new TreeMap<>();
            }
        };
    }

    private final Random random;
    private double total = 0;

    public AtomicCollection()
    {
        this(new Random());
    }

    public AtomicCollection(Random random)
    {
        this.random = random;
    }

    public void add(double weight, E result)
    {
        if (weight <= 0) return;
        total += weight;
        map.get().put(total, result);
    }

    public E next()
    {
        double value = random.nextDouble() * total;
        return map.get().ceilingEntry(value).getValue();
    }

    public class Entry
    {
        private Enum enumeration;

        Entry(Enum enumeration)
        {
            this.enumeration = enumeration;
        }

        public Enum getEnumeration()
        {
            return enumeration;
        }
    }
}
