package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum QueryType {
    FIX_ONLINE_USERS("UPDATE users SET currentShard = null, isPlaying = 0 WHERE currentShard = '%s';"),
    FIX_WHOLE_SHARD("UPDATE users SET is_online = 0 WHERE currentShard = '%s';"),
    SET_ONLINE_STATUS("UPDATE users SET is_online = %s, currentShard = '%s' WHERE `users`.`uuid` = '%s';"),
    SET_ONLINE_USER("UPDATE users SET is_online = %s WHERE account_id = '%s';"),
    INCREMENT_GEMS("UPDATE characters SET gems = gems + %s WHERE character_id = '%s';"),
    INCREMENT_GEMS_EARNED("UPDATE statistics SET gems_earned = gems_earned + '%s' WHERE character_id = '%s';"),
    SET_ECASH("UPDATE users SET ecash = '%s' WHERE account_id = '%s';"),
    UPDATE_HEARTH_STONE("UPDATE characters SET currentHearthStone = '%s' WHERE characters.character_id = '%s';"),
    UPDATE_RANK("UPDATE ranks SET rank = '%s', expiration = '%s' WHERE account_id = '%s';"),
    SELECT_COLLECTION_BIN("SELECT collection_storage FROM characters WHERE character_id = '%s';");

    @Getter
    private String query;


    public String getQuery(Object... object) {
        return String.format(getQuery(), object);
    }
}
