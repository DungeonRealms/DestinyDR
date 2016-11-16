package net.dungeonrealms.control.database.sql.connection;

import lombok.Getter;
import net.dungeonrealms.control.database.EnumDatabase;
import net.dungeonrealms.control.database.IConnection;
import net.dungeonrealms.control.database.sql.MySQL;
import net.dungeonrealms.control.database.sql.exception.ConnectionRunningException;
import net.dungeonrealms.control.database.sql.request.enumeration.EnumClauseType;
import net.dungeonrealms.control.database.sql.request.enumeration.EnumRequestType;
import net.dungeonrealms.control.database.sql.request.ResultRequest;

import java.sql.SQLException;

/**
 * Created by Giovanni on 15-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SQLConnection implements IConnection
{
    private MySQL mySQL;

    @Getter
    private EnumDatabase databaseType = EnumDatabase.SQL;

    @Getter
    private boolean running = false;

    public void runOn(String ip, String port, String dbName, String pass, String user) throws ConnectionRunningException, ClassNotFoundException, SQLException
    {
        if (!running)
        {
            this.mySQL = new MySQL(ip, port, dbName, pass, user);
            this.mySQL.openConnection();
        } else
            throw new ConnectionRunningException();
    }

    public EnumConnectionResult test()
    {
        try
        {
            if (this.mySQL.checkConnection())
                return EnumConnectionResult.SUCCESS;
            else return EnumConnectionResult.FAIL;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return EnumConnectionResult.FAIL;
    }

    public EnumConnectionResult close()
    {
        try
        {
            this.mySQL.closeConnection();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return EnumConnectionResult.FAIL;
        }
        return EnumConnectionResult.SUCCESS;
    }

    public ResultRequest createRequest(EnumRequestType type, EnumClauseType clauseType)
    {
        return new ResultRequest(type, clauseType);
    }
}
