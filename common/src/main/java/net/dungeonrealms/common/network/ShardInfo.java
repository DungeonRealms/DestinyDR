package net.dungeonrealms.common.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

import net.dungeonrealms.common.game.database.player.rank.Rank.PlayerRank;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * ShardInfo - Connection data for each shard.
 * 
 * Redone May 4th, 2017.
 * @author Kneesnap
 */
@AllArgsConstructor @Getter
public enum ShardInfo implements Serializable {
	
    // DEVELOPMENT SHARD //
    US0("US-0", new ServerAddress("158.69.121.40", 40012), ShardType.DEVELOPMENT),
    TEST("TEST", new ServerAddress("158.69.121.40", 40013), ShardType.DEVELOPMENT),

    // US 1 SHARD //
    US1("US-1", new ServerAddress("158.69.23.118", 42000), ShardType.DEFAULT),

    // US 2 SHARD //
    US2("US-2", new ServerAddress("158.69.121.38", 42000), ShardType.DEFAULT),

    // US 3 SHARD //
    US3("US-3", new ServerAddress("158.69.121.67", 42000), ShardType.DEFAULT),

    // SUB 1 SHARD //
    SUB1("SUB-1", new ServerAddress("158.69.121.67", 42001), ShardType.SUBSCRIBER),

    // CS 1 SHARD //
    CS1("CS-1", new ServerAddress("158.69.121.40", 22965), ShardType.SUPPORT);
//    CS1("CS-1", "cs1", new ServerAddress("158.69.121.48", 45521));

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
    
    @AllArgsConstructor
    public enum ShardType {
    	
    	DEFAULT("", ChatColor.YELLOW, Material.END_CRYSTAL),
    	YOUTUBE("YouTubers Only", ChatColor.RED, PlayerRank.YOUTUBER, Material.REDSTONE),
    	SUPPORT("Support Agents Only", ChatColor.BLUE, PlayerRank.SUPPORT, Material.PRISMARINE_SHARD),
    	DEVELOPMENT("", ChatColor.AQUA, PlayerRank.GM, Material.DIAMOND, "Please be aware your data is seperate from the live servers."),
    	BETA("Test content early.", ChatColor.DARK_RED, Material.TNT, "You will be testing " + ChatColor.UNDERLINE + "new" + ChatColor.GRAY + " and " + ChatColor.UNDERLINE + "unfinished" + ChatColor.GRAY + " versions of Dungeon Realms.", "Please report bugs to a GM or Developer."),
    	SUBSCRIBER("Subscribers Only", ChatColor.GREEN, PlayerRank.SUB, Material.EMERALD),
    	BRAZILLIAN("Brazilian Shard", ChatColor.YELLOW, PlayerRank.DEFAULT, Material.SAPLING, 3, new String[] {"The official language of this server is " + ChatColor.UNDERLINE + "Portuguese" + ChatColor.GRAY + "."}),
    	ROLEPLAY("Role-playing Shard", ChatColor.YELLOW, Material.BOOK),
    	EVENT("", ChatColor.YELLOW, Material.GOLD_INGOT, "Please be aware that data is not synchronized with the live game. ", "This shard is only accessible for a short amount of time.");
    	
    	@Getter private String description;
    	@Getter private ChatColor color;
    	@Getter private PlayerRank minRank;
    	private Material icon;
    	private int meta;
    	@Getter private String[] info;
    	
    	ShardType(String d, ChatColor c, Material m, String... info) {
    		this(d, c, PlayerRank.DEFAULT, m, info);
    	}
    	
    	ShardType(String d, ChatColor c, PlayerRank rank, Material i, String... info) {
    		this(d, c, rank, i, 0, info);
    	}
    	
    	public ItemStack getIcon(){
    		return new ItemStack(icon, (short)meta);
    	}
    	
    }
}
