package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DataType {

    CHARACTER_ID("characters", "character_id");
    @Getter String tableName;
    @Getter String columnName;
}
