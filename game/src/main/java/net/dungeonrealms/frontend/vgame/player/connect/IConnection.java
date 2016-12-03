package net.dungeonrealms.frontend.vgame.player.connect;

import net.dungeonrealms.common.awt.frame.handler.Handler;

/**
 * Created by Giovanni on 3-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IConnection extends Handler.ListeningHandler {

    boolean allowSynchronized();
}
