package net.dungeonrealms.vgame.item.construct;

import net.dungeonrealms.vgame.item.EnumGameItem;
import net.dungeonrealms.vgame.security.UAI;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface Item {
    /**
     * Gets the game item type from the game item
     *
     * @return A game item type
     */
    EnumGameItem getGameItem();

    /**
     * Gets the atomic id of the game item
     *
     * @return An atomic id
     */
    UAI getAtomicId();

    /**
     * Gets the Bukkit itemstack from the game item
     *
     * @return An itemstack
     */
    ItemStack getItemStack();
}
