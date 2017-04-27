package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum QueryType {
    FIX_ONLINE_USERS("UPDATE users SET currentShard = null, is_online = 0 WHERE currentShard = '%s';"),
    FIX_WHOLE_SHARD("UPDATE users SET is_online = 0 WHERE currentShard = '%s';"),
    SET_ONLINE_STATUS("UPDATE users SET is_online = %s, currentShard = '%s' WHERE `users`.`account_id` = '%s';"),
    SET_ONLINE_USER("UPDATE users SET is_online = %s WHERE account_id = '%s';"),
    SET_HASSHOP("UPDATE characters SET shopOpened = '%s' WHERE character_id = '%s';"),
    SET_ECASH("UPDATE users SET ecash = '%s' WHERE account_id = '%s';"),
    SET_MULELEVEL("UPDATE characters SET mule_level = '%s' WHERE character_id = '%s';"),
    SET_ACHIEVEMENTS("UPDATE characters SET achievements = '%s' WHERE character_id = '%s';"),
    //Realms
    SET_REALM_UPGRADE("UPDATE realm SET upgrading = '%s' WHERE character_id = '%s';"),
    SET_REALM_UPLOADING("UPDATE realm SET uploading = '%s' WHERE character_id = '%s';"),
    SET_REALM_INFO("UPDATE realm SET upgrading = '%s', uploading = '%s', tier = '%s' WHERE character_id = '%s';"),
    SET_RANK("UPDATE ranks SET rank = '%s' WHERE account_id = '%s';"),
    GUILD_INVITE("INSERT INTO guild_members(account_id, guild_id, rank, joined, accepted) VALUES ('%s', '%s', '%s', '%s', '%s');"),
    INCREMENT_GEMS("UPDATE characters SET gems = gems + %s WHERE character_id = '%s';"),
    SET_GEMS("UPDATE characters SET gems = %s WHERE character_id = '%s';"),
    INCREMENT_GEMS_EARNED("UPDATE statistics SET gems_earned = gems_earned + '%s' WHERE character_id = '%s';"),
    UNBAN_PLAYER("UPDATE punishments SET quashed = 1 WHERE account_id = '%s' AND type = 'ban';"),
    UNMUTE_PLAYER("UPDATE punishments SET quashed = 1 WHERE account_id = '%s' AND type = 'mute';"),
    UPDATE_COLLECTION_BIN("UPDATE characters SET collection_storage = '%s' WHERE character_id = '%s';"),
    UPDATE_HEARTH_STONE("UPDATE characters SET currentHearthStone = '%s' WHERE characters.character_id = '%s';"),
    UPDATE_RANK("UPDATE ranks SET rank = '%s', expiration = '%s' WHERE account_id = '%s';"),
    INSERT_FRIENDS("INSERT IGNORE INTO friends(account_id, friend_id, status) VALUES ('%s', '%s', '%s') ON DUPLICATE KEY UPDATE status = '%s';"),
    INSERT_BAN("INSERT IGNORE INTO punishments(account_id, type, issued, expiration, punisher_id, reason, quashed) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s';"),
    SELECT_COLLECTION_BIN("SELECT collection_storage FROM characters WHERE character_id = '%s';"),
    SELECT_UNLOCKABLES("SELECT mounts, pets, particles, mountSkin, trails FROM users WHERE account_id = '%s';"),
    SELECT_ALL_PUNISHMENTS("SELECT expiration, punisher_id, reason FROM `punishments` WHERE `account_id` = '%s' AND quashed = 0 AND (expiration > UNIX_TIMESTAMP() OR expiration = 0) ORDER BY expiration DESC LIMIT 1;"),
    SELECT_IP_BANS("SELECT expiration FROM punishments LEFT JOIN `ip_addresses` ON `punishments`.`account_id` = `ip_addresses`.`account_id` WHERE `ip_addresses`.`ip_address` = '%s' AND `punishments`.`quashed` <> 1 AND (`punishments`.`expiration` > UNIX_TIMESTAMP() OR `punishments`.`expiration` <= -1) AND `punishments`.`type` = 'ban' LIMIT 1;"),
    DELETE_FRIENDS(""),
    //Used in lobby to get their bans..
    SELECT_BANS("SELECT users.account_id, issued, expiration, punisher_id, reason FROM punishments LEFT JOIN users ON punishments.account_id = users.account_id WHERE type = 'ban' AND users.uuid = '%s' ORDER BY expiration DESC LIMIT 1;");

    @Getter
    private String query;


    public String getQuery(Object... object) {
        return String.format(this.query, object);
    }
}
