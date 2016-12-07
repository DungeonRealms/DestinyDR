package net.dungeonrealms.control.packet;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Giovanni on 7-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class PacketHandler {

    public abstract void handlePacket(Packet packet);
}
