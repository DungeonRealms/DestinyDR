package net.dungeonrealms.frontend.vgame.player.goal.achievement;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumAchievementGroup {

    COMBAT("Combat", Material.STONE_SWORD, 14, 12), EXPLORER("Exploration", Material.MAP, 21, 14);

    @Getter
    private Material material;

    @Getter
    private String name;

    @Getter
    private int size;

    @Getter
    private int slot;

    EnumAchievementGroup(String name, Material material, int size, int slot) {
        this.material = material;
        this.name = name;
        this.size = size;
        this.slot = slot;
    }
}
