package net.dungeonrealms.vgame.stash.item;

import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemRarity
{
    COMMON(0, ChatColor.GRAY, ChatColor.ITALIC + "Common" + ChatColor.RESET),
    UNCOMMON(1, ChatColor.GREEN, ChatColor.ITALIC + "Uncommon" + ChatColor.RESET),
    RARE(2, ChatColor.AQUA, ChatColor.ITALIC + "Rare" + ChatColor.RESET),
    UNIQUE(3, ChatColor.YELLOW, ChatColor.ITALIC + "Unique" + ChatColor.RESET);

    @Getter
    private int id;

    @Getter
    private String name;

    @Getter
    private ChatColor color;

    private static AtomicCollection<EnumItemRarity> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumItemRarity(int id, ChatColor color, String identifierName)
    {
        this.id = id;
        this.color = color;
        this.name = identifierName;
    }

    public static EnumItemRarity random()
    {
        if (loaded)
        {
            return atomicCollection.next();
        } else
        {
            // Weight is not final
            atomicCollection.add(1, COMMON);
            atomicCollection.add(0.4, UNCOMMON);
            atomicCollection.add(0.1, RARE);
            atomicCollection.add(0.06, UNIQUE);
            loaded = true;
            return atomicCollection.next();
        }
    }
}
