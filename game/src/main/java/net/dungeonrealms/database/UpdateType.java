package net.dungeonrealms.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Update statements for the playerUpdate so we can fine tune updates for data..
 */
@AllArgsConstructor @Getter
public enum UpdateType {
    GEMS("gems", "SELECT gems FROM characters WHERE character_id = '%s';"),
    HEARTHSTONE("hearthstone", "currentHearthStone", "SELECT currentHearthStone FROM characters WHERE character_id = '%s';"),
    REALM("upgradingRealm", "upgrading", "SELECT upgrading FROM realms WHERE account_id = '%s';"),
    ECASH("ecash", "SELECT ecash FROM users WHERE account_id = '%s';"),
    EXP("experience", "SELECT experience FROM characters WHERE account_id = '%s';"),
    RANK("rank", "SELECT rank FROM ranks WHERE account_id = '%s';"),
    LEVEL("level", "SELECT level FROM characters WHERE character_id = '%s';"),
    GUILD("guildID", "guild_id", "SELECT guild_id FROM guilds WHERE 0 = 0"), //TODO: FINISH.
    MUTE("mute"),
    UNLOCKABLES("unlockables");

    private String fieldName;
    private String columnName;
    private String selectStatement;

    UpdateType(String field) {
    	this(field, null, null); // Only used for special cases.
    }

    UpdateType(String field, String sql) {
    	this(field, field, sql);
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
