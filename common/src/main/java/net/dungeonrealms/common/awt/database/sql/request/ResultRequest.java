package net.dungeonrealms.common.awt.database.sql.request;

import net.dungeonrealms.common.awt.database.connection.EnumConnectionResult;
import net.dungeonrealms.common.awt.database.sql.MySQL;
import net.dungeonrealms.common.awt.database.sql.request.enumeration.EnumClauseType;
import net.dungeonrealms.common.awt.database.sql.request.enumeration.EnumRequestType;
import net.dungeonrealms.common.awt.database.sql.request.result.RawResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Giovanni on 15-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ResultRequest {
    /**
     * RawResult result = new ResultRequest(INTEGER, UUID).fromTable("eggs").fromColumn("yolkLevel"). fromValue(5).send(MySQL).get();
     * int level = result.toInteger();
     */

    private EnumRequestType requestType;

    private EnumClauseType clauseType;

    private String column, table;

    private EnumConnectionResult result;

    private RawResult rawResult;

    private Object clause;

    public ResultRequest(EnumRequestType requestType, EnumClauseType clauseType) {
        this.requestType = requestType;
        this.clauseType = clauseType;
    }

    public ResultRequest fromTable(String table) {
        this.table = table;
        return this;
    }

    public ResultRequest fromColumn(String column) {
        this.column = column;
        return this;
    }

    public ResultRequest fromValue(Object object) {
        this.clause = object;
        return this;
    }

    // TODO on a different thread 4 safety
    public ResultRequest send(MySQL from) throws SQLException, ClassNotFoundException {
        switch (this.clauseType) {
            case UUID:
                UUID uuid = UUID.fromString(String.valueOf(this.clause));
                ResultSet resultSet = from.query("SELECT * FROM " + this.table + " WHERE UUID = '" + uuid + "';");
                this.rawResult = new RawResult(resultSet.getObject(column), this.requestType);
                break;
            default:
                break;
        }
        return null;
    }

    public RawResult get() {
        if (this.result == EnumConnectionResult.SUCCESS) {
            return rawResult;
        }
        return null;
    }
}
