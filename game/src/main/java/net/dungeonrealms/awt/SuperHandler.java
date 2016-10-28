package net.dungeonrealms.awt;

import org.bukkit.event.Listener;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface SuperHandler
{
    SuperHandler prepare();

    interface Handler
    {
        void prepare();
    }

    interface ListeningHandler extends Handler, Listener
    {
    }
}
