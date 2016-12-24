package net.dungeonrealms.common.game.database.data;

/**
 * Created by Nick on 8/29/2015.
 */
public enum EnumData {

    UUID("info.uuid"),
    USERNAME("info.username"),
    HEALTH("info.health"),
    FIRST_LOGIN("info.firstLogin"),
    LAST_LOGIN("info.lastLogin"),
    LAST_LOGOUT("info.lastLogout"),
    FREE_ECASH("info.freeEcash"),
    LAST_SHARD_TRANSFER("info.lastShardTransfer"),
    LEVEL("info.netLevel"),
    IP_ADDRESS("info.ipAddress"),
    IS_PLAYING("info.isPlaying"),
    IS_COMBAT_LOGGED("info.isCombatLogged"),
    EXPERIENCE("info.experience"),
    GEMS("info.gems"),
    HEARTHSTONE("info.hearthstone"),
    ECASH("info.ecash"),
    FRIENDS("info.friends"),
    ALIGNMENT("info.alignment"),
    ALIGNMENT_TIME("info.alignmentTime"),
    CURRENT_LOCATION("info.currentLocation"),
    HASSHOP("info.shopOpen"),
    SHOPLEVEL("info.shopLevel"),
    MULELEVEL("info.muleLevel"),
    CURRENT_FOOD("info.foodLevel"),
    LOGGERDIED("info.loggerdied"),
    ENTERINGREALM("info.enteringrealm"),
    CURRENTSERVER("info.current"),
    ACTIVE_MOUNT("info.activemount"),
    ACTIVE_PET("info.activepet"),
    ACTIVE_TRAIL("info.activetrail"),
    ACTIVE_MOUNT_SKIN("info.activemountskin"),

    GEMS_COUNT("info.gemsCount"),

    ACHIEVEMENTS("collectibles.achievements"),

    RANK("rank.rank"),
    RANK_SUB_EXPIRATION("rank.expiration_date"),
    PURCHASE_HISTORY("rank.purchaseHistory"),

    INVENTORY_COLLECTION_BIN("inventory.collection_bin"),
    INVENTORY_MULE("inventory.mule"),
    INVENTORY_STORAGE("inventory.storage"),
    INVENTORY("inventory.player"),
    INVENTORY_LEVEL("inventory.level"),
    ARMOR("inventory.armor"),
    ITEMUIDS("inventory.itemuids"),

    GUILD("info.guild"),

    GUILD_INVITATION("notices.guildInvitation"),
    FRIEND_REQUESTS("notices.friendRequest"),
    MAILBOX("notices.mailbox"),
    LAST_BUILD("notices.lastBuild"),
    LAST_VOTE("notices.lastVote"),

    BANNED_TIME("punishments.banned"),
    MUTE_TIME("punishments.muted"),
    BANNED_REASON("punishments.bannedReason"),
    MUTE_REASON("punishments.muteReason"),

    REALM_UPLOAD("realm.uploading"),
    REALM_UPGRADE("realm.upgrading"),
    REALM_TITLE("realm.title"),
    REALM_LAST_RESET("realm.lastReset"),
    REALM_TIER("realm.tier"),

    MOUNTS("collectibles.mounts"),
    PETS("collectibles.pets"),
    BUFFS("collectibles.buffs"),
    PARTICLES("collectibles.particles"),
    MOUNT_SKINS("collectibles.mountskins"),

    TOGGLE_DEBUG("toggles.debug"),
    TOGGLE_TRADE("toggles.trade"),
    TOGGLE_TRADE_CHAT("toggles.tradeChat"),
    TOGGLE_GLOBAL_CHAT("toggles.globalChat"),
    TOGGLE_RECEIVE_MESSAGE("toggles.receiveMessage"),
    TOGGLE_PVP("toggles.pvp"),
    TOGGLE_DUEL("toggles.duel"),
    TOGGLE_CHAOTIC_PREVENTION("toggles.chaoticPrevention"),
    TOGGLE_SOUNDTRACK("toggles.soundtrack"),
    TOGGLE_TIPS("toggles.tips"),
    TOGGLE_VANISH("toggles.vanish"), // for GMs only

    PORTAL_SHARDS_T1("portalKeyShards.tier1"),
    PORTAL_SHARDS_T2("portalKeyShards.tier2"),
    PORTAL_SHARDS_T3("portalKeyShards.tier3"),
    PORTAL_SHARDS_T4("portalKeyShards.tier4"),
    PORTAL_SHARDS_T5("portalKeyShards.tier5"),

    PLAYER_KILLS("stats.player_kills"),
    LAWFUL_KILLS("stats.lawful_kills"),
    UNLAWFUL_KILLS("stats.unlawful_kills"),
    DEATHS("stats.deaths"),
    T1_MOB_KILLS("stats.monster_kills_t1"),
    T2_MOB_KILLS("stats.monster_kills_t2"),
    T3_MOB_KILLS("stats.monster_kills_t3"),
    T4_MOB_KILLS("stats.monster_kills_t4"),
    T5_MOB_KILLS("stats.monster_kills_t5"),
    BOSS_KILLS_MAYEL("stats.boss_kills_mayel"),
    BOSS_KILLS_BURICK("stats.boss_kills_burick"),
    BOSS_KILLS_INFERNALABYSS("stats.boss_kills_infernalAbyss"),
    LOOT_OPENED("stats.loot_opened"),
    DUELS_WON("stats.duels_won"),
    DUELS_LOST("stats.duels_lost"),
    ORE_MINED("stats.ore_mined"),
    FISH_CAUGHT("stats.fish_caught"),
    ORBS_USED("stats.orbs_used"),
    TIME_PLAYED("stats.time_played"),
    SUCCESSFUL_ENCHANTS("stats.successful_enchants"),
    FAILED_ENCHANTS("stats.failed_enchants"),
    ECASH_SPENT("stats.ecash_spent"),
    GEMS_EARNED("stats.gems_earned"),
    GEMS_SPENT("stats.gems_spent"),


    /*
    Player Attributes
     */
    //Adds Armor, Block Chance, Axe Damage and Polearm Damage
    STRENGTH("attributes.strength"),
    //Add DPS%, Dodge Chance, Armor Penetration and Bow Damage
    DEXTERITY("attributes.dexterity"),
    //Adds Energy Regeneration, elemental damage, critical hit chance and staff damamge.
    INTELLECT("attributes.intellect"),
    //Adds Health, hp regen, elemental resistance, and sword damage.
    VITALITY("attributes.vitality"),

    RESETS("attributes.resets"),

    FREERESETS("attributes.freeresets"),

    BUFFER_POINTS("attributes.bufferPoints");


    private String key;

    EnumData(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
