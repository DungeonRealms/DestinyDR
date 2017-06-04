package net.dungeonrealms.common.game.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum QueryType {
    FIX_ONLINE_USERS("UPDATE users SET currentShard = null, is_online = 0 WHERE currentShard = '%s';"),
    FIX_WHOLE_SHARD("UPDATE users SET is_online = 0 WHERE currentShard = '%s';"),
    SET_ONLINE_STATUS("UPDATE users SET is_online = %s, currentShard = %s WHERE `users`.`account_id` = '%s';"),
    SET_ONLINE_USER("UPDATE users SET is_online = %s WHERE account_id = '%s';"),
    SET_HASSHOP("UPDATE characters SET shopOpened = '%s' WHERE character_id = '%s';"),
    SET_ECASH("UPDATE users SET ecash = '%s' WHERE account_id = '%s';"),
    SET_MULELEVEL("UPDATE characters SET mule_level = '%s' WHERE character_id = '%s';"),
    SET_ACHIEVEMENTS("UPDATE characters SET achievements = %s WHERE character_id = %s;"),
    SET_GUILD_RANK("UPDATE guild_members SET rank = '%s' WHERE account_id = '%s';"),
    //Realms
    SET_REALM_UPGRADE("UPDATE realm SET upgrading = '%s' WHERE character_id = '%s';"),
    SET_REALM_UPLOADING("UPDATE realm SET uploading = '%s' WHERE character_id = '%s';"),
    SET_REALM_INFO("UPDATE realm SET upgrading = '%s', uploading = '%s', tier = '%s' WHERE character_id = '%s';"),
    SET_RANK("UPDATE ranks SET rank = '%s' WHERE account_id = '%s';"),
    SET_GEMS("UPDATE characters SET gems = %s WHERE character_id = '%s';"),
    SET_PETS("UPDATE users SET pets = %s WHERE account_id = %s;"),
    SET_MOUNTS("UPDATE users SET mounts = %s WHERE account_id = %s;"),

    GUILD_INVITE("REPLACE INTO guild_members(account_id, guild_id, rank, joined, accepted) VALUES ('%s', '%s', '%s', '%s', '%s');"),
    INCREMENT_GEMS("UPDATE characters SET gems = gems + %s WHERE character_id = '%s';"),
    INCREMENT_GEMS_EARNED("UPDATE statistics SET gems_earned = gems_earned + '%s' WHERE character_id = '%s';"),
    UNBAN_PLAYER("UPDATE punishments SET quashed = 1 WHERE account_id = '%s' AND type = 'ban';"),
    UNMUTE_PLAYER("UPDATE punishments SET quashed = 1 WHERE account_id = '%s' AND type = 'mute';"),
    UPDATE_COLLECTION_BIN("UPDATE characters SET collection_storage = '%s' WHERE character_id = '%s';"),
    UPDATE_HEARTH_STONE("UPDATE characters SET currentHearthStone = %s WHERE characters.character_id = '%s';"),
    UPDATE_RANK("UPDATE ranks SET rank = '%s', expiration = '%s' WHERE account_id = '%s';"),
    INSERT_FRIENDS("INSERT IGNORE INTO friends(account_id, friend_id, status) VALUES ('%s', '%s', '%s') ON DUPLICATE KEY UPDATE status = '%s';"),
    INSERT_BAN("INSERT IGNORE INTO punishments(account_id, type, issued, expiration, punisher_id, reason, quashed) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s');"),
    INSERT_MUTE("INSERT INTO punishments(account_id, type, issued, expiration, punisher_id, reason) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');"),
    DELETE_FRIENDS("DELETE FROM friends WHERE account_id = '%s';"),
    DELETE_FRIEND("DELETE FROM friends WHERE account_id = '%s' AND friend_id = '%s';"),
    DELETE_GUILD("DELETE FROM guilds WHERE guild_id = '%s';"),
    SELECT_COLLECTION_BIN("SELECT collection_storage FROM characters WHERE character_id = '%s';"),
    SELECT_UNLOCKABLES("SELECT mounts, pets, particles, mountSkin, trails, currencyTab FROM users WHERE account_id = '%s';"),
    SELECT_VALID_PUNISHMENTS("SELECT expiration, punisher_id, reason, type FROM `punishments` LEFT JOIN users ON `punishments`.`account_id` = `users`.`account_id` WHERE `users`.`uuid` = '%s' AND quashed = 0 AND (expiration > UNIX_TIMESTAMP() OR expiration = 0) ORDER BY issued DESC LIMIT 1;"),
    SELECT_ALL_PUNISHMENTS("SELECT expiration, punisher_id, reason, type, issued, quashed FROM `punishments` LEFT JOIN users ON `punishments`.`account_id` = `users`.`account_id` WHERE `users`.`uuid` = '%s' ORDER BY issued DESC;"),
    SELECT_IP_BANS("SELECT expiration FROM punishments LEFT JOIN `ip_addresses` ON `punishments`.`account_id` = `ip_addresses`.`account_id` WHERE `ip_addresses`.`ip_address` = '%s' AND `punishments`.`quashed` <> 1 AND (`punishments`.`expiration` > UNIX_TIMESTAMP() OR `punishments`.`expiration` <= -1) AND `punishments`.`type` = 'ban' LIMIT 1;"),
    SELECT_BANS("SELECT issued, expiration, punisher_id, reason, quashed FROM punishments LEFT JOIN users ON punishments.account_id = users.account_id WHERE type = 'ban' AND users.uuid = '%s' AND quashed = 0 AND (expiration > UNIX_TIMESTAMP() OR expiration = 0) ORDER BY issued DESC LIMIT 1;"),
    //PLAYER WRAPPER QUERIES
    UPDATE_REALM("UPDATE realm SET title = %s, description = %s, uploading = %s, upgrading = %s, tier = %s, enteringRealm = %s, lastReset = %s WHERE character_id = %s;"),
    CHARACTER_UPDATE("UPDATE characters SET created = %s, level = %s, experience = %s, alignment = %s, inventory_storage = %s, armour_storage = %s, gems = %s, bank_storage = %s, bank_level = %s, " +
            "shop_level = %s, mule_storage = %s, mule_level = %s, health = %s, location = %s, " +
            "activeMount = %s, activePet = %s, activeTrail = %s, activeMountSkin = %s, activeHatOverride = %s, questData = %s, collection_storage = %s, " +
            "foodLevel = %s, combatLogged = %s, shopOpened = %s, loggerDied = %s, currentHearthStone = %s, alignmentTime = %s, portalShardsT1 = %s, portalShardsT2 = %s, portalShardsT3 = %s, portalShardsT4 = %s, portalShardsT5 = %s WHERE `character_id` = %s;"),
    USER_UPDATE("UPDATE users SET username = %s, selected_character_id = %s, ecash = %s, joined = %s, last_login = %s, last_logout = %s, last_free_ecash = %s, last_shard_transfer = %s, is_online = %s, currentShard = %s, currencyTab = %s, firstLogin = %s, lastViewedBuild = %s, lastNoteSize = %s, lastVote = %s, " +
            "mounts = %s, pets = %s, particles = %s, mountSkin = %s, purchaseables = %s, pending_purchaseables = %s WHERE account_id = %s"),
    DELETE_GUILD_MEMBER("DELETE FROM guild_members WHERE account_id = %s"),
    BACKUP_CHARACTER("UPDATE characters SET level = %s, experience = %s, location = %s, inventory_storage = %s, armour_storage = %s, gems = %s, bank_storage = %s, mule_storage = %s, mule_level = %s WHERE character_id = %s;"),
    SELECT_ALTS("SELECT `ip_addresses`.`account_id`, `ip_addresses`.`last_used`,`users`.`username`, users.last_login FROM `ip_addresses` LEFT JOIN users ON `ip_addresses`.`account_id` = `users`.`account_id` WHERE `ip_addresses`.`ip_address` = '%s';"),
    SELECT_ALTS_FROM_ACCOUNT_ID("SELECT `ip_addresses`.`ip_address`, `ip_addresses`.`last_used` FROM `ip_addresses` WHERE `ip_addresses`.`account_id` = %s;"),
    SELECT_LOG("SELECT * FROM `purchase_history` WHERE `uuid` = %s AND `transaction_id` = %s AND `action` = %s;"),
    UPDATE_PURCHASES("UPDATE users SET purchaseables = %s, pending_purchaseables = %s WHERE account_id = %s"),
    INSERT_PURCHASE_LOG("INSERT IGNORE INTO purchase_history (action, transaction_id, date, uuid) VALUES(%s,%s,%s,%s)"),
    SELECT_PURCHASES("SELECT purchaseables, pending_purchaseables FROM users WHERE account_id = %s");

    @Getter private String rawQuery;

    public String getQuery(Object... object) {
        return String.format(this.rawQuery, object);
    }
}
