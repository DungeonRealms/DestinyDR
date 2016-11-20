package net.dungeonrealms.vgame.core.stash.registry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dungeonrealms.common.old.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.core.stash.item.weapon.WeaponItem;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.JSONParser;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    private String table;

    @Getter
    private boolean connected;

    @Override
    public void prepare()
    {
        this.table = "atomicRWeapon";

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
        // Save all existent weapon items into the database
        Gson gson = new Gson();
        for (WeaponItem weaponItem : getMap().values())
        {
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put("atomic", gson.toJson(weaponItem));

            // Set into the database
            Game.getGame().getGameShard().getSqlDatabase().set(table, map, "UUID", weaponItem.getUniqueId().toString());

            map.clear();
        }
    }

    // Ran upon preparation
    @Override
    public void collect()
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
        {
            Gson gson = new Gson();
            try (ResultSet set = Game.getGame().getGameShard().getSqlDatabase().getSet("SELECT * FROM " + this.table))
            {
                while (set.next())
                {
                    try
                    {
                        JsonObject jsonObject = (JsonObject) new JSONParser().parse(set.getString("atomic"));
                        WeaponItem weaponItem = gson.fromJson(jsonObject, WeaponItem.class);
                        this.getMap().put(weaponItem.getItemStack(), weaponItem);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
                Game.getGame().getServer().shutdown(); // No weapons, no purpose.
            }
        }, 60L); // Start collecting after 3 sec
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
        Game.getGame().getGameShard().getSqlDatabase().createTable(this.table,
                Arrays.asList("UUID;VARCHAR(50)", "atomic;TEXT"));
        this.connected = true;
    }

    // Ran if a new weaponItem has been generated
    /** public void store(WeaponItem weaponItem)
     {
     Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
     {
     this.itemMap.get().put(weaponItem.getItemStack(), weaponItem);

     Gson gson = new Gson();
     ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
     map.put("atomic", gson.toJson(weaponItem));

     // Set into the database
     Game.getGame().getGameShard().getSqlDatabase().set(this.table, map, "UUID", weaponItem.getUniqueId().toString());

     // Send to all shards
     new MonoPacket(weaponItem.getUniqueId(), EnumMonoType.SEND_WEAPON).send();
     }, 20 * 2);
     }

     public void receive(UUID uuid)
     {
     Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
     {
     Gson gson = new Gson();
     try (ResultSet set = Game.getGame().getGameShard().getSqlDatabase().getResultSetByUUID(this.table, uuid.toString()))
     {
     while (set.next())
     {
     try
     {
     JsonObject jsonObject = (JsonObject) new JSONParser().parse(set.getString("atomic"));
     WeaponItem weaponItem = gson.fromJson(jsonObject, WeaponItem.class);
     this.getMap().put(weaponItem.getItemStack(), weaponItem);
     } catch (Exception e)
     {
     e.printStackTrace();
     }
     }
     } catch (SQLException e)
     {
     Game.getGame().getInstanceLogger().sendMessage(ChatColor.RED + "Mono Packet[WEAPON], failed to read raw byte data");
     }
     }, 20 * 5); // Delay it, lets make sure it actually exists..
     }
     **/
}
