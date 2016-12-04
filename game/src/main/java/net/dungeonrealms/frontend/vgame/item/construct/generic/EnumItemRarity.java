package net.dungeonrealms.frontend.vgame.item.construct.generic;

import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 4-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemRarity {

    COMMON(80, ChatColor.GRAY.toString() + ChatColor.ITALIC + "Common"),
    UNCOMMON(40, ChatColor.GREEN.toString() + ChatColor.ITALIC + "Uncommon"),
    RARE(15, ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare"),
    UNIQUE(3, ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique");

    @Getter
    private String name;

    @Getter
    private double weight;

    EnumItemRarity(double weight, String name) {
        this.weight = weight;
        this.name = name;
    }

    public static EnumItemRarity random() {
        AtomicCollection<EnumItemRarity> atomicCollection = new AtomicCollection<>();
        atomicCollection.add(COMMON.getWeight(), COMMON);
        atomicCollection.add(UNCOMMON.getWeight(), UNCOMMON);
        atomicCollection.add(RARE.getWeight(), RARE);
        atomicCollection.add(UNIQUE.getWeight(), UNIQUE);
        return atomicCollection.next();
    }
}
