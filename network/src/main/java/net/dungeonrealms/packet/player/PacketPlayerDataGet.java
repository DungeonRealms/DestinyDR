package net.dungeonrealms.packet.player;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.verify.VerificationResult;
import net.dungeonrealms.packet.Packet;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PacketPlayerDataGet implements Packet {

    @Getter
    private VerificationResult verificationResult;

    public PacketPlayerDataGet(VerificationResult verificationResult) {
        this.verificationResult = verificationResult;
    }
}
