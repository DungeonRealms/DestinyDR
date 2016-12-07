package net.dungeonrealms.packet.player;

import lombok.Getter;
import net.dungeonrealms.packet.Packet;

import java.util.UUID;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PacketPlayerDataSend implements Packet {

    @Getter
    private UUID dataOwner;

    public PacketPlayerDataSend(UUID dataOwner) {
        this.dataOwner = dataOwner;
    }
}
