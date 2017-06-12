package net.dungeonrealms.common.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.util.ChatColor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
/**
 * ShardInfo - Connection data for each shard.
 *
 * Redone May 4th, 2017.
 * @author Kneesnap
 */
@AllArgsConstructor @Getter
public enum ShardInfo implements Serializable {
	
    // DEVELOPMENT SHARD //
    US0("US0", new ServerAddress("158.69.121.40", 40011), ShardType.DEVELOPMENT),
    TESTUS0("TEST-US0", new ServerAddress("158.69.121.40", 40012), ShardType.DEVELOPMENT),
    TEST("TEST", new ServerAddress("158.69.121.40", 40013), ShardType.DEVELOPMENT),

    // US 1 SHARD //
    US1("US-1", new ServerAddress("158.69.23.118", 42000), ShardType.DEFAULT),

    // US 2 SHARD //
    US2("US-2", new ServerAddress("158.69.121.38", 42000), ShardType.DEFAULT),

    // US 3 SHARD //
    US3("US-3", new ServerAddress("158.69.121.67", 42000), ShardType.DEFAULT),

    // SUB 1 SHARD //
    SUB1("SUB-1", new ServerAddress("158.69.121.67", 42001), ShardType.SUBSCRIBER),

    // CUSTOMER SUPPORT AND SALES SHARD //
    CS1("CS-1", "cs1", new ServerAddress("158.69.121.48", 45521), ShardType.SUPPORT);

    private String shardID;
    private String pseudoName;
    private ServerAddress address;
    private ShardType type;

    ShardInfo(String shardId, ServerAddress address, ShardType type) {
    	this(shardId, shardId.toLowerCase().replaceAll("-", ""), address, type);
    }


    public static ShardInfo getByPseudoName(String pseudoName) {
        Optional<ShardInfo> query = Arrays.stream(ShardInfo.values()).
                filter(info -> info.getPseudoName().equals(pseudoName)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByShardID(String shardID) {
        Optional<ShardInfo> query = Arrays.stream(ShardInfo.values()).
                filter(info -> info.getShardID().equals(shardID)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByAddress(ServerAddress address) {
        Optional<ShardInfo> query = Arrays.stream(ShardInfo.values()).
                filter(info -> info.getAddress().toString().equals(address.toString())).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    @AllArgsConstructor @Getter
    public enum ShardType {

    	DEFAULT("", ChatColor.YELLOW, "END_CRYSTAL"),
    	YOUTUBE("YouTubers Only", ChatColor.RED, PlayerRank.YOUTUBER, "REDSTONE"),
    	SUPPORT("Support Agents Only", ChatColor.BLUE, PlayerRank.SUPPORT,"PRISMARINE_SHARD"),
    	DEVELOPMENT("", ChatColor.AQUA, PlayerRank.GM, "DIAMOND", "Please be aware your data is seperate from the live servers."),
    	BETA("Test new and experimental content early.", ChatColor.DARK_RED, "TNT", "You will be testing a " + ChatColor.UNDERLINE + "new" + ChatColor.GRAY + " version of Dungeon Realms with " + ChatColor.UNDERLINE + "experimental" + ChatColor.GRAY + " content. The point of this Shard is to find any remaining issues within this massive code overhaul.", "", "Please report bugs to a GM or Developer on DISCORD or through the forums."),
    	SUBSCRIBER("Subscribers Only", ChatColor.GREEN, PlayerRank.SUB, "EMERALD"),
    	BRAZILLIAN("Brazilian Shard", ChatColor.YELLOW, PlayerRank.DEFAULT, "SAPLING", 3, new String[] {"The official language of this server is " + ChatColor.UNDERLINE + "Portuguese" + ChatColor.GRAY + "."}),
    	ROLEPLAY("Role-playing Shard", ChatColor.YELLOW, "BOOK"),
    	EVENT("", ChatColor.YELLOW, "GOLD_INGOT", "Please be aware that data is not synchronized with the live game. ", "This shard is only accessible for a short amount of time.");

    	private String description;
    	private ChatColor color;
    	private PlayerRank minRank;
    	private String icon; // This is a string because the proxy doesn't have the Material class.
    	private int meta;
    	private String[] info;

    	ShardType(String d, ChatColor c, String m, String... info) {
    		this(d, c, PlayerRank.DEFAULT, m, info);
    	}

    	ShardType(String d, ChatColor c, PlayerRank rank, String i, String... info) {
    		this(d, c, rank, i, 0, info);
    	}
    }
}
