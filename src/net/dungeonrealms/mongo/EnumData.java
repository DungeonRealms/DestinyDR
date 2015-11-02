package net.dungeonrealms.mongo;

/**
 * Created by Nick on 8/29/2015.
 */
public enum EnumData {

    HEALTH("info.health"),
    FIRST_LOGIN("info.firstLogin"),
    LAST_LOGIN("info.lastLogin"),
    LEVEL("info.isPlaying"),
    IS_PLAYING("info.netLevel"),
    EXPERIENCE("info.experience"),
    GEMS("info.gems"),
    HEARTHSTONE("info.hearthstone"),
    ECASH("info.ecash"),
    FRIENDS("info.friends"),
    ALIGNMENT("info.alignment"),
    CURRENT_LOCATION("info.currentLocation"),

    ACHIEVEMENTS("collectibles.achievements"),

    RANK("rank.rank"),
    RANK_EXISTENCE("rank.lastPurchase"),
    PURCHASE_HISTORY("rank.purchaseHistory"),

    INVENTORY_COLLECTION_BIN("inventory.collection_bin"),
    INVENTORY_MULE("inventory.mule"),
    INVENTORY_STORAGE("inventory.storage"),
    INVENTORY("inventory.player"),
    INVENTORY_LEVEL("inventory.level"),

    GUILD("info.guild"),

    GUILD_INVITES("notices.guildInvites"),
    FRIEND_REQUSTS("notices.friendRequest"),

    MOUNTS("collectibles.mounts"),
    PETS("collectibles.pets"),
    PARTICLES("collectibles.particles"),

    TOGGLE_DEBUG("toggles.debug"),
    TOGGLE_TRADE("toggles.trade"),
    TOGGLE_TRADE_CHAT("toggles.tradeChat"),
    TOGGLE_GLOBAL_CHAT("toggles.globalChat"),
    TOGGLE_RECEIVE_MESSAGE("toggles.receiveMessage"),
    TOGGLE_PVP("toggles.pvp"),
    TOGGLE_DUEL("toggles.duel"),
    TOGGLE_CHAOTIC_PREVENTION("toggles.chaoticPrevention"),


    MAILBOX("notices.mailbox"),


    /*
    Player Attributes
     */
    //Adds Armor, Block Chance, Axe Damage and Polearm Damage
    STRENGTH("info.attributes.strength"),
    //Add DPS%, Dodge Chance, Armor Penetration and Bow Damage
    DEXTERITY("info.attributes.dexterity"),
    //Adds Energy Regeneration, elemental damage, critical hit chance and staff damamge.
    INTELLECT("info.attributes.intellect"),
    //Adds Health, hp regen, elemental resistance, and sword damage.
    VITALITY("info.attributes.vitality"),

    BUFFER_POINTS("info.attributes.bufferPoints");
	
	private String key;
	EnumData(String key){
		this.key = key;
	}
	
	public String getKey(){
		return key;
	}

}
