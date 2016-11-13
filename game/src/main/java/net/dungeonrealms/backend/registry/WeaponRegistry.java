package net.dungeonrealms.backend.registry;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.Getter;
import net.dungeonrealms.backend.packet.mono.EnumMonoType;
import net.dungeonrealms.backend.packet.mono.MonoPacket;
import net.dungeonrealms.common.game.database.sql.SQLBoolean;
import net.dungeonrealms.common.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.EnumItemRarity;
import net.dungeonrealms.vgame.item.EnumItemTier;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttribute;
import org.bukkit.ChatColor;
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
import java.util.stream.Collectors;

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
        // Save all existant weapon items into the database
        Gson gson = new Gson();
        for (WeaponItem weaponItem : getMap().values())
        {
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put("UUID", weaponItem.getUniqueId());
            map.put("material", weaponItem.getItemStack().getType().name());
            map.put("type", weaponItem.getItemType().name());
            map.put("rarity", weaponItem.getItemRarity().name());
            map.put("tier", weaponItem.getItemTier().name());
            map.put("attributeTier", weaponItem.getAttributeTier().name());
            map.put("durability", weaponItem.getDurability());
            map.put("name", weaponItem.getName());
            map.put("soulbound", weaponItem.isSoulbound() ? SQLBoolean.TRUE.name() : SQLBoolean.FALSE.name());
            map.put("tradeable", weaponItem.isTradeable() ? SQLBoolean.TRUE.name() : SQLBoolean.FALSE.name());
            map.put("attributes", gson.toJson(weaponItem.getWeaponAttributes()));

            // Set into the database
            Game.getGame().getGameShard().getSqlDatabase().set(table, map, "UUID", weaponItem.getUniqueId().toString());
        }
    }

    // Ran upon preparation
    @Override
    public void collect()
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
        {
            Gson gson = new Gson();
            try (ResultSet set = Game.getGame().getGameShard().getSqlDatabase().getSet("SELECT * FROM " + table))
            {
                while (set.next())
                {
                    UUID uuid = UUID.fromString(set.getString("UUID"));
                    Material material = Material.valueOf(set.getString("material"));
                    EnumItemRarity rarity = EnumItemRarity.valueOf(set.getString("rarity"));
                    EnumItemType itemType = EnumItemType.valueOf(set.getString("type"));
                    EnumItemTier itemTier = EnumItemTier.valueOf(set.getString("tier"));
                    EnumItemTier attributeTier = EnumItemTier.valueOf(set.getString("attributeTier"));
                    int durability = set.getInt("durability");
                    String name = set.getString("name");
                    SQLBoolean soulbound = SQLBoolean.valueOf(set.getString("soulbound"));
                    SQLBoolean tradeable = SQLBoolean.valueOf(set.getString("tradeable"));
                    int minDmg = set.getInt("minDmg");
                    int maxDmg = set.getInt("maxDmg");

                    // JSON conversion
                    List<String> attributeStrings = gson.fromJson(set.getString("attributes"), List.class);

                    // Convert the attribute names to the actual attribute
                    List<EnumWeaponAttribute> attributeList = Lists.newArrayList();
                    attributeList.addAll(attributeStrings.stream().map(EnumWeaponAttribute::valueOf).collect(Collectors.toList()));
                    WeaponItem weaponItem = new WeaponItem(uuid, material, rarity, itemTier, attributeTier, itemType, durability, name, attributeList,
                            soulbound == SQLBoolean.TRUE ? true : false, tradeable == SQLBoolean.TRUE ? true : false, minDmg, maxDmg, false);
                    this.getMap().put(weaponItem.getItemStack(), weaponItem);
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
        Game.getGame().getGameShard().getSqlDatabase().createTable(table,
                Arrays.asList("UUID;VARCHAR(50)",
                        "material;TEXT",
                        "type;TEXT",
                        "rarity;TEXT",
                        "tier;TEXT",
                        "attributeTier;TEXT",
                        "durability;INT(11)",
                        "name;TEXT",
                        "soulbound;TEXT",
                        "tradeable;TEXT",
                        "attributes;TEXT"));
        this.connected = true;
    }

    // Ran if a new weaponItem has been generated
    public void store(WeaponItem weaponItem)
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
        {
            this.itemMap.get().put(weaponItem.getItemStack(), weaponItem);

            Gson gson = new Gson();
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put("UUID", weaponItem.getUniqueId());
            map.put("material", weaponItem.getItemStack().getType().name());
            map.put("type", weaponItem.getItemType().name());
            map.put("rarity", weaponItem.getItemRarity().name());
            map.put("tier", weaponItem.getItemTier().name());
            map.put("attributeTier", weaponItem.getAttributeTier().name());
            map.put("durability", weaponItem.getDurability());
            map.put("name", weaponItem.getName());
            map.put("soulbound", weaponItem.isSoulbound() ? SQLBoolean.TRUE.name() : SQLBoolean.FALSE.name());
            map.put("tradeable", weaponItem.isTradeable() ? SQLBoolean.TRUE.name() : SQLBoolean.FALSE.name());
            map.put("attributes", gson.toJson(weaponItem.getWeaponAttributes()));

            // Set into the database
            Game.getGame().getGameShard().getSqlDatabase().set(table, map, "UUID", weaponItem.getUniqueId().toString());

            // Send to all shards
            new MonoPacket(weaponItem.getUniqueId(), EnumMonoType.SEND_WEAPON).send();
        }, 20 * 2);
    }

    // Ran if a MonoPacket for a weaponItem is being received
    public void receive(UUID uuid)
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () ->
        {
            Gson gson = new Gson();
            try (ResultSet set = Game.getGame().getGameShard().getSqlDatabase().getResultSetByUUID(this.table, uuid.toString()))
            {
                while (set.next())
                {
                    Material material = Material.valueOf(set.getString("material"));
                    EnumItemRarity rarity = EnumItemRarity.valueOf(set.getString("rarity"));
                    EnumItemType itemType = EnumItemType.valueOf(set.getString("type"));
                    EnumItemTier itemTier = EnumItemTier.valueOf(set.getString("tier"));
                    EnumItemTier attributeTier = EnumItemTier.valueOf(set.getString("attributeTier"));
                    int durability = set.getInt("durability");
                    String name = set.getString("name");
                    SQLBoolean soulbound = SQLBoolean.valueOf(set.getString("soulbound"));
                    SQLBoolean tradeable = SQLBoolean.valueOf(set.getString("tradeable"));
                    int minDmg = set.getInt("minDmg");
                    int maxDmg = set.getInt("maxDmg");

                    // JSON conversion
                    List<String> attributeStrings = gson.fromJson(set.getString("attributes"), List.class);

                    // Convert the attribute names to the actual attribute
                    List<EnumWeaponAttribute> attributeList = Lists.newArrayList();
                    attributeList.addAll(attributeStrings.stream().map(EnumWeaponAttribute::valueOf).collect(Collectors.toList()));
                    WeaponItem weaponItem = new WeaponItem(uuid, material, rarity, itemTier, attributeTier, itemType, durability, name, attributeList,
                            soulbound == SQLBoolean.TRUE ? true : false, tradeable == SQLBoolean.TRUE ? true : false, minDmg, maxDmg, false);
                    this.getMap().put(weaponItem.getItemStack(), weaponItem);
                }
            } catch (SQLException e)
            {
                Game.getGame().getInstanceLogger().sendMessage(ChatColor.RED + "Mono Packet[WEAPON], failed to read raw byte data");
            }
        }, 20 * 5); // Delay it, lets make sure it actually exists..
    }
}
