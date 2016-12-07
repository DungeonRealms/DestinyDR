package net.dungeonrealms.packet.player.in;

import lombok.Getter;
import net.dungeonrealms.packet.Packet;

import java.util.UUID;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PacketPlayerDataRequest implements Packet {

    @Getter
    private UUID owner;

    @Getter
    private String serverId;

    public PacketPlayerDataRequest(UUID owner, String fromServer) {
        this.owner = owner;
        this.serverId = fromServer;
    }
}
