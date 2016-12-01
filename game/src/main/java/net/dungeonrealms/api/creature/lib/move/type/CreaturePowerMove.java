package net.dungeonrealms.api.creature.lib.move.type;

import lombok.Getter;
import net.dungeonrealms.api.creature.lib.move.EnumPowerMove;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class CreaturePowerMove {

    @Getter
    private EnumPowerMove powerMove;

    public CreaturePowerMove(EnumPowerMove powerMove) {
        this.powerMove = powerMove;
    }

    public abstract void perform();
}
