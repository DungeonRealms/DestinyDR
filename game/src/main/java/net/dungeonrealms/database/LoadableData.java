package net.dungeonrealms.database;

import java.sql.ResultSet;

public abstract class LoadableData {
    public abstract void extractData(ResultSet resultSet);
}
