package net.dungeonrealms.control.party.type;

import lombok.Getter;

/**
 * Created by Giovanni on 21-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumPartyType {
    DEFAULT(5, ""), SUB(8, "&a&lSUB"), SUB_2(9, "&6&lSUB+"), SUB_3(10, "&e&lSUB++"), DEV(20, "&b&lDEV");

    @Getter
    private int partySlots;

    @Getter
    private String name;

    EnumPartyType(int partySlots, String name) {
        this.partySlots = partySlots;
        this.name = name;
    }
}
