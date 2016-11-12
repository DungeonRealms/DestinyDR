package net.dungeonrealms.backend.packet.mono;

import lombok.Getter;
import net.dungeonrealms.vgame.Game;

import java.util.UUID;

/**
 * Created by Giovanni on 12-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MonoPacket
{
    @Getter
    private UUID uniqueId;

    private EnumMonoType monoType;

    public MonoPacket(UUID uuid, EnumMonoType monoType)
    {
        this.uniqueId = uuid;
        this.monoType = monoType;
    }

    public void send()
    {
        Game.getGame().getGameShard().getGameClient().sendNetworkMessage(this.monoType.name(), this.uniqueId.toString(), true);
    }
}
