package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum QueryType {
    FIX_ONLINE_USERS("UPDATE users SET currentShard = null, isPlaying = 0 WHERE currentShard = '%s';"),
    FIX_WHOLE_SHARD("UPDATE users SET is_online = 0 WHERE currentShard = '%s';"),
    SET_ONLINE_STATUS("UPDATE users SET is_online = %s, currentShard = '%s' WHERE `users`.`uuid` = '%s';"),
    SET_ONLINE_USER("UPDATE users SET is_online = %s WHERE account_id = '%s';"),
    SET_HASSHOP("UPDATE characters SET shopOpened = '%s' WHERE character_id = '%s';"),
    SET_ECASH("UPDATE users SET ecash = '%s' WHERE account_id = '%s';"),
    SET_MULELEVEL("UPDATE characters SET mule_level = '%s' WHERE character_id = '%s';"),
    //Realms
    SET_REALM_UPGRADE("UPDATE realms SET upgrading = '%s' WHERE account_id = '%s';"),
    SET_REALM_UPLOADING("UPDATE realms SET uploading = '%s' WHERE account_id = '%s';"),
//    SET_REALM_UPGRADE_AND_UPLOAD("UPDATE realms SET upgrading = '%s', uploading = '%s' WHERE account_id = '%s';"),
    SET_REALM_INFO("UPDATE realms SET upgrading = '%s', uploading = '%s', tier = '%s' WHERE account_id = '%s';"),

    INCREMENT_GEMS("UPDATE characters SET gems = gems + %s WHERE character_id = '%s';"),
    INCREMENT_GEMS_EARNED("UPDATE statistics SET gems_earned = gems_earned + '%s' WHERE character_id = '%s';"),
    UPDATE_COLLECTION_BIN("UPDATE characters SET collection_storage = '%s' WHERE character_id = '%s';"),
    UPDATE_HEARTH_STONE("UPDATE characters SET currentHearthStone = '%s' WHERE characters.character_id = '%s';"),
    UPDATE_RANK("UPDATE ranks SET rank = '%s', expiration = '%s' WHERE account_id = '%s';"),
    SELECT_COLLECTION_BIN("SELECT collection_storage FROM characters WHERE character_id = '%s';"),
    SELECT_UNLOCKABLES("SELECT mounts, pets, particles, mountSkin, trails FROM users WHERE account_id = '%s';"),
    INSERT_FRIENDS("INSERT IGNORE INTO friends(account_id, friend_id, status) VALUES ('%s', '%s', '%s') ON DUPLICATE KEY UPDATE status = '%s';"),
    SELECT_BANNED_IPS("SELECT * FROM punishments WHERE ip_address = '%s' ORDER BY expiration DESC LIMIT 1;");

    @Getter
    private String query;


    public String getQuery(Object... object) {
        return String.format(getQuery(), object);
    }
}
