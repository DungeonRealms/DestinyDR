package net.dungeonrealms.vgame.item.weapon.handle;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class BowHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }
}
