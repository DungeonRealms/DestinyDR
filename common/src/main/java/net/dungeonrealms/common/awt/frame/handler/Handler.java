package net.dungeonrealms.common.awt.frame.handler;

import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface Handler {

    boolean isPrepared();

    UUID getUniqueId();

    void prepare();

    void disable();

    interface ListeningHandler extends Handler, Listener {

    }
}
