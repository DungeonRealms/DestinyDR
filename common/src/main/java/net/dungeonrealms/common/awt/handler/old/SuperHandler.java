package net.dungeonrealms.common.awt.handler.old;

import org.bukkit.event.Listener;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface SuperHandler {

    // Old, to prevent push errors

    void prepare();

    interface Handler extends SuperHandler {

    }

    interface ListeningHandler extends SuperHandler, Listener {

    }
}
