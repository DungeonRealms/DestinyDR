package net.dungeonrealms.network.packet.type;

import lombok.Getter;
import net.dungeonrealms.network.packet.Packet;

import java.util.UUID;

/**
 * Created by Giovanni on 12-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MonoPacket extends Packet {
    @Getter
    public UUID uniqueId;

    @Getter
    public byte[] data;
}
