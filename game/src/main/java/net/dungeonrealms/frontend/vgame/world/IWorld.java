package net.dungeonrealms.frontend.vgame.world;

import org.bukkit.World;
import org.bukkit.event.Listener;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IWorld extends Listener {

    World getBukkitWorld();

    String getIdentifier();
}
