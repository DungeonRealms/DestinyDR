package net.dungeonrealms.vgame.item.weapon;

import com.google.gson.Gson;
import lombok.Getter;
import net.dungeonrealms.common.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.EnumItemRarity;
import net.dungeonrealms.vgame.item.EnumItemTier;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttibute;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
            this.collect();
        }
    }

    // Must be ran async!
    @Override
    public void save()
    {
        // Save all existant weapon items into the database
        Gson gson = new Gson();
        for (WeaponItem weaponItem : getMap().values())
        {
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put("UUID", weaponItem.getUniqueID());
            map.put("material", weaponItem.getItemStack().getType().name());
            map.put("type", weaponItem.getType().name());
            map.put("rarity", weaponItem.getItemRarity().name());
            map.put("tier", weaponItem.getItemTier().name());
            map.put("durability", weaponItem.getDurability());
            map.put("name", weaponItem.getName());
            map.put("attributes", gson.toJson(weaponItem.getWeaponAttibutes()));

            // Set into the database
            Game.getGame().getSQLDatabase().set(table, map, "UUID", weaponItem.getUniqueID().toString());
        }
    }

    // Must be ran async!
    @Override
    public void collect()
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () -> {
            Gson gson = new Gson();
            ResultSet set = Game.getGame().getSQLDatabase().getSet("SELECT * FROM " + table);
            try
            {
                while (set.next())
                {
                    UUID uuid = UUID.fromString(set.getString("UUID"));
                    Material material = Material.valueOf(set.getString("material"));
                    EnumItemRarity rarity = EnumItemRarity.valueOf(set.getString("rarity"));
                    EnumItemType itemType = EnumItemType.valueOf(set.getString("type"));
                    EnumItemTier itemTier = EnumItemTier.valueOf(set.getString("tier"));
                    int durability = set.getInt("durability");
                    String name = set.getString("name");
                    List<EnumWeaponAttibute> attibuteList = gson.fromJson(set.getString("attributes"), List.class);
                    // TODO construct weapon
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
                Game.getGame().getServer().shutdown(); // No weapons, no purpose.
            }
        }, 20L);
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
                        "type;TEXT",
                        "rarity;TEXT",
                        "tier;TEXT",
                        "durability;INT(11)",
                        "name;TEXT",
                        "attributes;TEXT"));
        this.connected = true;
    }
}
