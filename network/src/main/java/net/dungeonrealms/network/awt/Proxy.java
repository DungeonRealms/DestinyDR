package net.dungeonrealms.network.awt;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface Proxy
{
    EnumProxyHolder getProxyHolder();

    void sendGlobalPacket(String par1, String... contents);
}
