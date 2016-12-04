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
public enum EnumItemTier {

    ONE(90, ChatColor.WHITE),
    TWO(55, ChatColor.GREEN),
    THREE(30, ChatColor.AQUA),
    FOUR(15, ChatColor.LIGHT_PURPLE),
    FIVE(5, ChatColor.YELLOW);

    @Getter
    private ChatColor color;

    @Getter
    private double weight;

    EnumItemTier(double weight, ChatColor chatColor) {
        this.weight = weight;
        this.color = chatColor;
    }

    public static EnumItemTier random() {
        AtomicCollection<EnumItemTier> atomicCollection = new AtomicCollection<>();
        atomicCollection.add(ONE.getWeight(), ONE);
        atomicCollection.add(TWO.getWeight(), TWO);
        atomicCollection.add(THREE.getWeight(), THREE);
        atomicCollection.add(FOUR.getWeight(), FOUR);
        atomicCollection.add(FIVE.getWeight(), FIVE);
        return atomicCollection.next();
    }
}
