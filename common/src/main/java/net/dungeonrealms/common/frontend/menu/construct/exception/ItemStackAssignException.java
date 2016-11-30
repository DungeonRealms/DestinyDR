package net.dungeonrealms.common.frontend.menu.construct.exception;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemStackAssignException extends Exception {
    public ItemStackAssignException() {
        super("No itemstack assigned, can't assign to slot");
    }
}
