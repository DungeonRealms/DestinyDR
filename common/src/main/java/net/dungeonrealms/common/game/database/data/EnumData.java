package net.dungeonrealms.common.game.database.data;

/**
 * Created by Nick on 8/29/2015.
 */
public enum EnumData {

//    UUID("info.uuid"), //Loaded
//    USERNAME("info.username"), //Loaded
//    HEALTH("info.health"), // Loaded
//    FIRST_LOGIN("info.firstLogin"), //Loaded
//    LAST_LOGIN("info.lastLogin"), //Loaded
//    LAST_LOGOUT("info.lastLogout"), //Loaded
//    FREE_ECASH("info.freeEcash"), //Loaded
//    LAST_SHARD_TRANSFER("info.lastShardTransfer"), //Loaded
//    LEVEL("info.netLevel"), //Loaded
//    IP_ADDRESS("info.ipAddress"), //Loaded
//    IS_PLAYING("info.isPlaying"), //Loaded
//    IS_COMBAT_LOGGED("info.isCombatLogged"), //Loaded
//    EXPERIENCE("info.experience"),//Loaded
//    GEMS("info.gems"),//Loaded
//    HEARTHSTONE("info.hearthstone"),//Loaded
//    ECASH("info.ecash"),//Loaded
//    FRIENDS("info.friends"),//Loaded
//    IGNORED("info.ignored"),//Loaded
//    ALIGNMENT("info.alignment"),//Loaded
//    ALIGNMENT_TIME("info.alignmentTime"),//Loaded
//    CURRENT_LOCATION("info.currentLocation"),//Loaded
//    HASSHOP("info.shopOpen"),//Loaded
//    SHOPLEVEL("info.shopLevel"),//Loaded
//    MULELEVEL("info.muleLevel"),//Loaded
//    CURRENT_FOOD("info.foodLevel"),//Loaded
//    LOGGERDIED("info.loggerdied"),//Loaded
//    ENTERINGREALM("info.enteringrealm"),//Loaded
//    CURRENTSERVER("info.current"),//Loaded
//    ACTIVE_MOUNT("info.activemount"),//Loaded
//    ACTIVE_PET("info.activepet"),//Loaded
//    ACTIVE_TRAIL("info.activetrail"),//Loaded
//    ACTIVE_MOUNT_SKIN("info.activemountskin"),//Loaded
//    QUEST_DATA("info.questData"),//Loaded

//    GEMS_COUNT("info.gemsCount"),

//    ACHIEVEMENTS("collectibles.achievements"),//No idea how we are going to do this one. Started to look into it but didn't really see

    LOGIN_PIN("rank.loginCode"), //Not taking this over. This will be replaced with 2FA.
//    RANK("rank.rank"), // Loaded
//    RANK_SUB_EXPIRATION("rank.expiration_date"), // Loaded

    //Converted to CurrencyTab in PlayerWrapper.
//    CURRENCY_TAB_ACCESS("currencytab.access"),//Loaded
//    CURRENCY_TAB_T1("currencytab.t1"),//Loaded
//    CURRENCY_TAB_T2("currencytab.t2"),//Loaded
//    CURRENCY_TAB_T3("currencytab.t3"),//Loaded
//    CURRENCY_TAB_T4("currencytab.t4"),//Loaded
//    CURRENCY_TAB_T5("currencytab.t5"),//Loaded

//    INVENTORY_COLLECTION_BIN("inventory.collection_bin"), //Loaded
//    INVENTORY_MULE("inventory.mule"), //Loaded
//    INVENTORY_STORAGE("inventory.storage"), //Loaded (this is the bank)
//    INVENTORY("inventory.player"), //Loaded
//    INVENTORY_LEVEL("inventory.level"), //Loaded (this is bank size)
//    ARMOR("inventory.armor"), //Loaded

    GUILD("info.guild"),

    GUILD_INVITATION("notices.guildInvitation");
//    FRIEND_REQUESTS("notices.friendRequest"),
//    MAILBOX("notices.mailbox"),
//    LAST_BUILD("notices.lastBuild"),
//    LAST_NOTES_SIZE("notices.lastBuildNotesSize"),
//    LAST_VOTE("notices.lastVote"),

//    BANNED_TIME("punishments.banned"), //Loaded
//    MUTE_TIME("punishments.muted"), //Loaded
//    BANNED_REASON("punishments.bannedReason"),//Loaded
//    MUTE_REASON("punishments.muteReason"),//Loaded

//    REALM_UPLOAD("realm.uploading"), //Loaded
//    REALM_UPGRADE("realm.upgrading"),//Loaded
//    REALM_TITLE("realm.title"),//Loaded
//    REALM_LAST_RESET("realm.lastReset"),//Loaded
//    REALM_TIER("realm.tier"),//Loaded

//    MOUNTS("collectibles.mounts"),// Loaded
//    PETS("collectibles.pets"),// Loaded
//    BUFFS("collectibles.buffs"), //never used/
//    PARTICLES("collectibles.particles"),// Loaded
//    MOUNT_SKINS("collectibles.mountskins"),//Loaded

//    TOGGLE_DEBUG("toggles.debug"),//Loaded
//    TOGGLE_TRADE("toggles.trade"),//Loaded
//    TOGGLE_TRADE_CHAT("toggles.tradeChat"),//Loaded
//    TOGGLE_GLOBAL_CHAT("toggles.globalChat"),//Loaded
//    TOGGLE_RECEIVE_MESSAGE("toggles.receiveMessage"),//Loaded
//    TOGGLE_PVP("toggles.pvp"),//Loaded
//    TOGGLE_DUEL("toggles.duel"),//Loaded
//    TOGGLE_CHAOTIC_PREVENTION("toggles.chaoticPrevention"),//Loaded
//    TOGGLE_SOUNDTRACK("toggles.soundtrack"),//Loaded
//    TOGGLE_TIPS("toggles.tips"),//Loaded
//    TOGGLE_GLOW("toggles.glow"),//Loaded
//    TOGGLE_DAMAGE_INDICATORS("toggles.damageIndicators"),//Loaded
//    TOGGLE_VANISH("toggles.vanish"), // Loaded

    //Gotta go cut grass rip
//    PORTAL_SHARDS_T1("portalKeyShards.tier1"),//Loaded
//    PORTAL_SHARDS_T2("portalKeyShards.tier2"),//Loaded
//    PORTAL_SHARDS_T3("portalKeyShards.tier3"),//Loaded
//    PORTAL_SHARDS_T4("portalKeyShards.tier4"),//Loaded
//    PORTAL_SHARDS_T5("portalKeyShards.tier5"),//Loaded

//    PLAYER_KILLS("stats.player_kills"),//Loaded
//    LAWFUL_KILLS("stats.lawful_kills"),//Loaded
//    UNLAWFUL_KILLS("stats.unlawful_kills"),//Loaded
//    DEATHS("stats.deaths"),//Loaded
//    T1_MOB_KILLS("stats.monster_kills_t1"),//Loaded
//    T2_MOB_KILLS("stats.monster_kills_t2"),//Loaded
//    T3_MOB_KILLS("stats.monster_kills_t3"),//Loaded
//    T4_MOB_KILLS("stats.monster_kills_t4"),//Loaded
//    T5_MOB_KILLS("stats.monster_kills_t5"),//Loaded
//    BOSS_KILLS_MAYEL("stats.boss_kills_mayel"),//Loaded
//    BOSS_KILLS_BURICK("stats.boss_kills_burick"),//Loaded
//    BOSS_KILLS_INFERNALABYSS("stats.boss_kills_infernalAbyss"),//Loaded
//    LOOT_OPENED("stats.loot_opened"),//Loaded
//    DUELS_WON("stats.duels_won"),//Loaded
//    DUELS_LOST("stats.duels_lost"),//Loaded
//    ORE_MINED("stats.ore_mined"),//Loaded
//    FISH_CAUGHT("stats.fish_caught"),//Loaded
//    ORBS_USED("stats.orbs_used"),//Loaded
//    TIME_PLAYED("stats.time_played"),//Loaded
//    SUCCESSFUL_ENCHANTS("stats.successful_enchants"),//Loaded
//    FAILED_ENCHANTS("stats.failed_enchants"),//Loaded
//    ECASH_SPENT("stats.ecash_spent"),//Loaded
//    GEMS_EARNED("stats.gems_earned"),//Loaded
//    GEMS_SPENT("stats.gems_spent"),//Loaded


    /*
    Player Attributes
     */
    //Adds Armor, Block Chance, Axe Damage and Polearm Damage
//    STRENGTH("attributes.strength"), //Loaded
    //Add DPS%, Dodge Chance, Armor Penetration and Bow Damage
//    DEXTERITY("attributes.dexterity"),//Loaded
    //Adds Energy Regeneration, elemental damage, critical hit chance and staff damamge.
//    INTELLECT("attributes.intellect"),//Loaded
    //Adds Health, hp regen, elemental resistance, and sword damage.
//    VITALITY("attributes.vitality"),//Loaded

//    RESETS("attributes.resets"),//Loaded

//    FREERESETS("attributes.freeresets"),//Loaded

//    BUFFER_POINTS("attributes.bufferPoints");//Loaded


    private String key;

    EnumData(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
