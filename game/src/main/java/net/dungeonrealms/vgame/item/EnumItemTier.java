package net.dungeonrealms.vgame.item;

import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemTier
{
    COMMON(0, ChatColor.GRAY, ChatColor.ITALIC + "Common" + ChatColor.RESET),
    UNCOMMON(1, ChatColor.GREEN, ChatColor.ITALIC + "Uncommon" + ChatColor.RESET),
    RARE(2, ChatColor.AQUA, ChatColor.ITALIC + "Rare" + ChatColor.RESET),
    UNIQUE(3, ChatColor.YELLOW, ChatColor.ITALIC + "Unique" + ChatColor.RESET),
    LEGENDARY(4, ChatColor.GOLD, ChatColor.ITALIC + "Legendary" + ChatColor.RESET);

    @Getter
    private int id;

    @Getter
    private String name;

    @Getter
    private ChatColor color;

    private static AtomicCollection<EnumItemTier> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumItemTier(int id, ChatColor color, String identifierName)
    {
        this.id = id;
        this.color = color;
        this.name = identifierName;
    }

    public static EnumItemTier random()
    {
        if (loaded)
        {
            return atomicCollection.next();
        } else
        {
            // Weight is not final
            atomicCollection.getMap().get().put(0.8, COMMON);
            atomicCollection.getMap().get().put(0.6, UNCOMMON);
            atomicCollection.getMap().get().put(0.4, RARE);
            atomicCollection.getMap().get().put(0.2, UNIQUE);
            atomicCollection.getMap().get().put(0.05, LEGENDARY);
            loaded = true;
            return atomicCollection.next();
        }
    }
}
