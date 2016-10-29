package net.dungeonrealms.vgame.item.weapon;

import net.dungeonrealms.common.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.item.IStack;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class WeaponRegistry implements DataRegistry
{
    private AtomicReference<ConcurrentHashMap<ItemStack, WeaponItem>> itemMap;
    // Time to hold up to thousands of entries

    @Override
    public void prepare()
    {
        this.itemMap = new AtomicReference<ConcurrentHashMap<ItemStack, WeaponItem>>();
        this.itemMap.set(new ConcurrentHashMap<>());
    }

    @Override
    public AtomicBoolean atomicPreference()
    {
        return new AtomicBoolean(true);
    }

    @Override
    public ConcurrentHashMap<ItemStack, WeaponItem> getMap()
    {
        return itemMap.get();
    }
}
