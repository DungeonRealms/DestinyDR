package net.dungeonrealms.database;

import java.sql.ResultSet;

public interface LoadableData {

    void extractData(ResultSet resultSet);
}
