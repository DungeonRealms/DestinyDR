package net.dungeonrealms.common.awt.database.sql.request.result;

import lombok.Getter;
import net.dungeonrealms.common.awt.database.sql.request.enumeration.EnumRequestType;

/**
 * Created by Giovanni on 15-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class RawResult {
    @Getter
    private EnumRequestType requestType;

    private Object result;

    public RawResult(Object object, EnumRequestType requestType) {
        this.result = object;
        this.requestType = requestType;
    }

    public String toString() {
        if (this.requestType == EnumRequestType.STRING) {
            return String.valueOf(result);
        }
        return null;
    }

    public int toInteger() {
        if (this.requestType == EnumRequestType.INTEGER) {
            return Integer.valueOf(String.valueOf(result));
        }
        return 0;
    }

    public Object toObject() {
        return result;
    }
}
