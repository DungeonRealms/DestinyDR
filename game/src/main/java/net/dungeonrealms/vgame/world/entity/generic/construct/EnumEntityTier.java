package net.dungeonrealms.vgame.world.entity.generic.construct;

import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumEntityTier
{
    ONE(ChatColor.WHITE), TWO(ChatColor.GREEN), THREE(ChatColor.AQUA), FOUR(ChatColor.LIGHT_PURPLE), FIVE(ChatColor.YELLOW);

    @Getter
    private ChatColor color;

    EnumEntityTier(ChatColor color)
    {
        this.color = color;
    }
}
