package net.dungeonrealms.vgame.stash.item;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IStack
{
    UUID getUniqueId();

    ItemStack getItemStack();

    EnumItemType getItemType();

    boolean isSoulbound();

    boolean isTradeable();
}
