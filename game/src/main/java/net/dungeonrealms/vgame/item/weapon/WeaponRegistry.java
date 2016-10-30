package net.dungeonrealms.vgame.item.weapon;

import com.google.gson.Gson;
import lombok.Getter;
import net.dungeonrealms.common.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.Game;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
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

    @Getter
    private YamlConfiguration yamlConfiguration; // Registry configuration

    @Getter
    private String table;

    @Getter
    private boolean connected;

    @Override
    public void prepare()
    {
        this.table = this.yamlConfiguration.getString("registry.table");

        createData();

        if (connected)
        {
            this.itemMap = new AtomicReference<ConcurrentHashMap<ItemStack, WeaponItem>>();
            this.itemMap.set(new ConcurrentHashMap<>());
        }
    }


    // Must be ran async!
    @Override
    public void save()
    {
        // Save all existant weapon items into the database
        for (WeaponItem weaponItem : getMap().values())
        {
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put("UUID", weaponItem.getUniqueID());
            map.put("material", weaponItem.getItemStack().getType().name());
            map.put("rarity", weaponItem.getItemRarity().name());
            map.put("tier", weaponItem.getItemTier().name());
            map.put("durability", weaponItem.getDurability());
            map.put("name", weaponItem.getName());
            map.put("attributes", new Gson().toJson(weaponItem.getWeaponAttibutes()));

            // Set into the database
            Game.getGame().getSQLDatabase().set(table, map, "UUID", weaponItem.getUniqueID().toString());
        }
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

    @Override
    public void createData()
    {
        Game.getGame().getSQLDatabase().createTable(table,
                Arrays.asList("UUID;VARCHAR(50)",
                        "material;TEXT",
                        "rarity;TEXT",
                        "tier;TEXT",
                        "durability;INT(11)",
                        "name;TEXT",
                        "attributes;TEXT"));
        this.connected = true;
    }
}
