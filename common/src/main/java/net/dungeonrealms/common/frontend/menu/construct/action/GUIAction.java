package net.dungeonrealms.common.frontend.menu.construct.action;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class GUIAction {
    @Getter
    private ItemStack itemStack;

    public GUIAction(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public abstract void perform(Player player, InventoryClickEvent clickEvent);
}
