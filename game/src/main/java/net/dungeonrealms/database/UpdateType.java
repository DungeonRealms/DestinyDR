package net.dungeonrealms.database;

import lombok.Getter;

import java.util.Arrays;

/**
 * Update statements for the playerUpdate so we can fine tune updates for data..
 */
public enum UpdateType {
    GEMS("gems", "gems", "SELECT gems FROM characters WHERE character_id = '%s';"),
    HEARTHSTONE("hearthstone", "currentHearthStone", "SELECT currentHearthStone FROM characters WHERE character_id = '%s';"),
    REALM("upgradingRealm", "upgrading", "SELECT upgrading FROM realms WHERE account_id = '%s';"),
    ECASH("ecash", "ecash", "SELECT ecash FROM users WHERE account_id = '%s';"),
    EXP("experience", "experience", "SELECT experience FROM characters WHERE account_id = '%s';"),
    RANK("rank", "rank", "SELECT rank FROM ranks WHERE account_id = '%s';"),
    LEVEL("level", "level", "SELECT level FROM characters WHERE character_id = '%s';");

    @Getter
    String fieldName;
    @Getter
    String columnName;
    @Getter
    String selectStatement;

    UpdateType(String field, String columnName, String query) {
        this.fieldName = field;
        this.selectStatement = query;
        this.columnName = columnName;
    }

    public String getQuery(PlayerWrapper wrapper) {
        String query = this.selectStatement;
        if (query.contains("character_id")) {
            return String.format(query, wrapper.getCharacterID());
        } else if (query.contains("account_id")) {
            return String.format(query, wrapper.getAccountID());
        } else if (query.contains("uuid = ")) {
            return String.format(query, wrapper.getUuid().toString());
        }
        return query;
    }

    public static UpdateType getFromName(String name) {
        return Arrays.stream(values()).filter(type -> type.getFieldName().equals(name)).findFirst().orElse(null);
    }
}
