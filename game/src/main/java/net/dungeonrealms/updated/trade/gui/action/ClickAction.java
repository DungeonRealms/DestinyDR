package net.dungeonrealms.updated.trade.gui.action;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ClickAction {

    @Getter
    private EnumClicker clicker;

    @Getter @Nullable
    private ItemStack itemStack;

    @Getter
    private EnumClickAction clickAction;

    @Getter
    private int slot;

    public ClickAction(EnumClicker clicker, EnumClickAction clickAction, ItemStack itemStack, int slot) {
        this.clicker = clicker;
        this.clickAction = clickAction;
        this.itemStack = itemStack;
        this.slot = slot;
    }
}
