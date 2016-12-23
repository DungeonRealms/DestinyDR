package net.dungeonrealms.updated;

import net.dungeonrealms.updated.connection.ConnectionHandler;
import net.dungeonrealms.updated.entity.EntityHandler;
import net.dungeonrealms.updated.entity.pig.handle.PigMountHandler;
import net.dungeonrealms.updated.trade.handle.TradeHandler;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Collector {

    /**
     * Prepare all new handlers
     */
    public void init() {
        new EntityHandler().prepare();
        new PigMountHandler().prepare();
        new ConnectionHandler().prepare();
        new TradeHandler().prepare();
    }
}
