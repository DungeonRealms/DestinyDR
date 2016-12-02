package net.dungeonrealms.frontend.vgame.world.entity.generic.construct;

import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumEntityTier {

    ONE(ChatColor.WHITE, 25), TWO(ChatColor.GREEN, 30), THREE(ChatColor.AQUA, 35), FOUR(ChatColor.LIGHT_PURPLE, 40), FIVE(ChatColor.YELLOW, 45);

    @Getter
    private ChatColor color;

    @Getter
    private int healthSize;

    EnumEntityTier(ChatColor color, int healthSize) {
        this.color = color;
        this.healthSize = healthSize;
    }
}
