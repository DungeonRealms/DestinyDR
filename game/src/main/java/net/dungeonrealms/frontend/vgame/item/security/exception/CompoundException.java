package net.dungeonrealms.frontend.vgame.item.security.exception;

import net.minecraft.server.v1_9_R2.ItemStack;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CompoundException extends Exception {
    public CompoundException(ItemStack itemStack) {
        super("Error whilst reading the compound of " + itemStack.getName());
    }
}
