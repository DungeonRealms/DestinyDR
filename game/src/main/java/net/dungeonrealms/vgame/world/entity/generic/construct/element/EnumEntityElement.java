package net.dungeonrealms.vgame.world.entity.generic.construct.element;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 27-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumEntityElement
{
    POISON(ChatColor.DARK_GREEN, Arrays.asList("Poison", "Poisonous")),
    FIRE(ChatColor.RED, Arrays.asList("Pyromaniac", "Fire", "Inferno")),
    ICE(ChatColor.BLUE, Arrays.asList("Ice")),
    HOLY(ChatColor.GOLD, Arrays.asList("Strong", "Holy")),
    EMPTY(null, null);

    @Getter
    private ChatColor color;

    @Getter
    private List<String> simpleNames;

    EnumEntityElement(ChatColor chatColor, List<String> simpleNames)
    {
        this.color = chatColor;
        this.simpleNames = simpleNames;
    }
}
