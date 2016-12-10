package net.dungeonrealms.database.api.player.verify;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class VerificationResult {

    @Getter
    private DataPlayer dataPlayer;

    @Getter
    private EnumVerificationResult verificationResult;

    public VerificationResult(EnumVerificationResult result, DataPlayer dataPlayer) {
        this.verificationResult = result;
        this.dataPlayer = dataPlayer;
    }
}
