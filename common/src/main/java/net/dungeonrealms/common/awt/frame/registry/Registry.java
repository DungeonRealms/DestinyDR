package net.dungeonrealms.common.awt.frame.registry;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface Registry {

    boolean isConnected();

    boolean ignoreEnabled();

    UUID getUniqueId();

    void prepare();

    void disable();
}
