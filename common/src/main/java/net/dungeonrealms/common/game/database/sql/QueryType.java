package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum QueryType {
    SET_ONLINE_USER("UPDATE users SET online = %s WHERE account_id = '%s';");

    @Getter
    String query;


    public String getQuery(Object... object) {
        return String.format(getQuery(), object);
    }
}
