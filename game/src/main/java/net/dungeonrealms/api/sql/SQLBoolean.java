package net.dungeonrealms.api.sql;

import lombok.Getter;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum SQLBoolean
{
    TRUE(0), FALSE(1);

    @Getter
    private int value;

    SQLBoolean(int value)
    {
        this.value = value;
    }
}
