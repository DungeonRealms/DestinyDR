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
    US0("US-0", "us0", new ServerAddress("127.0.0.1", 40001), ShardType.DEVELOPMENT),

    // NORMAL US SHARDS //
    US1("US-1", "us1", new ServerAddress("142.4.215.161", 40001), ShardType.DEFAULT),
    US2("US-2", "us2", new ServerAddress("142.4.215.119", 40001), ShardType.DEFAULT),
    US3("US-3", "us3", new ServerAddress("198.27.81.165", 40001), ShardType.DEFAULT),

    // SUB SHARDS //
    SUB1("SUB-1", "sub1", new ServerAddress("198.27.81.165", 40002), ShardType.SUBSCRIBER),

    // CUSTOMER SUPPORT AND SALES SHARD //
    CS1("CS-1", "cs1", new ServerAddress("127.0.0.1", 40002), ShardType.SUPPORT);

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
    	DEVELOPMENT("", ChatColor.AQUA, PlayerRank.BUILDER, "DIAMOND", "Please be aware your data is seperate from the live servers."),
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
