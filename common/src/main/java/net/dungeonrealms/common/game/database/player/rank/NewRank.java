package net.dungeonrealms.common.game.database.player.rank;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.md_5.bungee.api.ChatColor;

/**
 * Recoded Ranks
 * 
 * Created March 12, 2017.
 * @author Kneesnap
 */
public enum NewRank {
	DEFAULT("Default", ChatColor.GRAY, "Default"),
	SUB("S", ChatColor.GREEN, "Subscriber"),
	SUB_PLUS("S+", ChatColor.GOLD, "Subscriber+"),
	SUB_PLUS_PLUS("S++", ChatColor.YELLOW, "Subscriber++"),
	PARTNER("PARTNER", ChatColor.GOLD, "Server Partner"),
	BUILDER("BUILDER", ChatColor.DARK_GREEN, "Builder"),
	YOUTUBER("YT", ChatColor.RED, "Youtuber"),
	HIDDENMOD("S", ChatColor.GREEN, "Hidden Player Moderator"),
	PMOD("PMOD", ChatColor.WHITE, "Player Moderator"),
	SUPPORT("SUPPORT", ChatColor.AQUA, "Support Agent"),
	TRIALGM("GM", ChatColor.AQUA, "Trial Game Master"),
	GM("GM", ChatColor.AQUA, "Game Master"),
	HEADGM("GM", ChatColor.AQUA, "Head Game Master"),
	DEV("DEV", ChatColor.DARK_AQUA, "Developer");
	
	private final String prefix;
	private final ChatColor colorPrefix;
	private final String longName;
	
	//Contains Rank Data
	volatile static HashMap<UUID, NewRank> PLAYER_RANKS = new HashMap<>();
	
	NewRank(String prefix, ChatColor colorPrefix, String longName) {
		this.prefix = prefix;
		this.colorPrefix = colorPrefix;
		this.longName = longName;
	}
	
	/**
	 * Gets the prefix that gets applied to this rank in chat.
	 * @return
	 */
	public String getChatPrefix() {
		return ChatColor.BOLD + "" + getColorPrefix() + this.prefix + ChatColor.RESET + " ";
	}
	
	/**
	 * Gets the prefix color for this rank.
	 */
	public ChatColor getColorPrefix() {
		return this.colorPrefix;
	}
	
	/**
	 * Gets the full name of this rank.
	 * @return
	 */
	public String getLongName() {
		return getColorPrefix() + this.longName;
	}
	
	/**
	 * Returns the rank of a player. Defaults to "DEFAULT"
	 */
	public static NewRank getRank(Player player) {
		return getRank(player.getUniqueId());
	}
	
	/**
	 * Gets the Rank based on a player UUID.
	 */
	public static NewRank getRank(UUID uuid) {
		if(PLAYER_RANKS.containsKey(uuid))
			return PLAYER_RANKS.get(uuid);
		loadRank(uuid);
		return NewRank.DEFAULT;
	}
	
	/**
	 * Sets the rank of a player.
	 */
	public static void setRank(Player player, NewRank rank) {
		setRank(player.getUniqueId(), rank);
		//Tells the player their rank has been set.
		if(player.isOnline()) {
			player.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + rank.getLongName());
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
		}
	}
	
	/**
	 * Sets the rank of a player.
	 */
	public static void setRank(UUID uuid, NewRank rank) {
		setRank(uuid, rank, 0);
	}
	
	/**
	 * Sets the rank of a UUID, along with expiration.
	 */
	public static void setRank(UUID uuid, NewRank rank, int days) {
		PLAYER_RANKS.put(uuid, rank);
		
		//Update the database
		AsyncUtils.pool.submit(() -> {
			UpdateOptions uo = new UpdateOptions();
            uo.upsert(true);
            
			DatabaseInstance.ranks.updateMany(Filters.eq("uuid", uuid.toString()), new Document(EnumOperators.$SET.getUO(), 
                    new Document("rank", rank.name())).append("expiration_date", new Date().getTime() + (days * 24 * 60 * 60 * 1000)), uo);
			System.out.println("Rank Set.");
		});
	}
	
	private static void loadRank(UUID uuid) {
		System.out.println("Loading Rank?");
		AsyncUtils.pool.submit(() -> {
			System.out.println("Trying to load rank.");
            FindIterable<Document> results = DatabaseInstance.ranks.find(Filters.eq("uuid", uuid.toString()));
            Document rank = results.first();
            if (rank == null) {
            	System.out.println("Found no rank :/");
            	setRank(uuid, NewRank.DEFAULT);
            } else {
            	System.out.println("Got fiel.");
            	NewRank playerRank = null;
            	String rankName = rank.getString("rank");
            	for(NewRank nr : NewRank.values())
            		if(nr.name().equalsIgnoreCase(rankName))
            			playerRank = nr;
            	
            	if(playerRank == null){
            		System.out.println("Could not load rank for " + uuid.toString() + ". Looking for " + rankName);
            		return;
            	}
            	PLAYER_RANKS.put(uuid, playerRank);
            }
		});
	}
	
	/**
	 * Checks if a given PIN is correct.
	 */
	public static boolean isCorrectPIN(Player player, String pin) {
		Document data = DatabaseInstance.ranks.find(Filters.eq("uuid", player.getUniqueId().toString())).first();
		if(data == null){
			System.out.println("Failed to find rank data for " + player.getName());
			return false;
		}
		return pin.equals(data.getString("loginCode"));
	}
	
	public static boolean hasPIN(Player player) {
		Document data = DatabaseInstance.ranks.find(Filters.eq("uuid", player.getUniqueId().toString())).first();
		return data != null && data.getString("loginCode") != null;
	}
	
	/**
	 * Updates the PIN in the database.
	 * TODO: Encryption?
	 */
	public static void setPIN(Player player, String pin) {
		AsyncUtils.pool.submit(() -> {
			UpdateOptions uo = new UpdateOptions();
            uo.upsert(true);
            
			DatabaseInstance.ranks.updateMany(Filters.eq("uuid", player.getUniqueId().toString()), new Document(EnumOperators.$SET.getUO(), 
                    new Document("rank", pin)), uo);
		});
	}
	
	/**
	 * Checks if a rank has expired.
	 * 
	 */
	public static boolean hasRankExpired(UUID uuid) {
		int days = getDaysUntilRankExpiry(uuid);
		return days > 0;
	}
	
	public static int getDaysUntilRankExpiry(UUID uuid) {
		Document data = DatabaseInstance.ranks.find(Filters.eq("uuid", uuid.toString())).first();
		if(data == null)
			return 0;
		long expires = data.getLong("expiration_date") - new Date().getTime();
		expires /= (24 * 60 * 60 * 1000);
		return (int)expires;
	}
}