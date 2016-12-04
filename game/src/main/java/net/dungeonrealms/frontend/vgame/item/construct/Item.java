package net.dungeonrealms.frontend.vgame.item.construct;

import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemRarity;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemTier;
import net.dungeonrealms.frontend.vgame.item.security.UAI;
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

    /**
     * Gets the item rarity of the game item
     *
     * @return The rarity
     */
    EnumItemRarity getRarity();

    /**
     * Gets the item tier of the game item
     *
     * @return The item tier
     */
    EnumItemTier getTier();
}
