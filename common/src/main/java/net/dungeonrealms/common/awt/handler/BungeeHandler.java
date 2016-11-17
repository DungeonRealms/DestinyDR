package net.dungeonrealms.common.awt.handler;

import net.md_5.bungee.api.plugin.Listener;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface BungeeHandler extends SuperHandler.Handler
{
    interface ListeningHandler extends BungeeHandler, Listener
    {
    }
}
