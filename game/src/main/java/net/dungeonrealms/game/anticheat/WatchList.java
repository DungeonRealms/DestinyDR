package net.dungeonrealms.game.anticheat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.concurrent.MongoAccessThread;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.md_5.bungee.api.ChatColor;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.mongodb.client.model.Filters;

public class WatchList {
	
	/**
	 * Removes a player from the watchlist
	 */
	public static void removePlayer(UUID uuid) {
		System.out.println("Attempting to remove " + uuid.toString());
		DatabaseInstance.playerData.updateOne(Filters.exists("watchlist"), new Document(EnumOperators.$PULL.getUO(), new Document("watchlist", uuid.toString())), MongoAccessThread.uo);
	}
	
	/**
	 * Adds a player to the watchlist
	 */
	public static void addPlayer(UUID uuid) {
		System.out.println("Attempting to add " + uuid.toString());
		DatabaseInstance.playerData.updateOne(Filters.exists("watchlist"), new Document(EnumOperators.$PUSH.getUO(), new Document("watchlist", uuid.toString())), MongoAccessThread.uo);
	}
	
	/**
	 * Gets a list of all watched players.
	 */
	@SuppressWarnings("unchecked")
	public static List<UUID> getWatched() {
		List<UUID> uuids = new ArrayList<UUID>();
		ArrayList<String> watchlist = DatabaseInstance.misc.find(Filters.exists("watchlist"), ArrayList.class).first();
		for(String s : watchlist)
			uuids.add(UUID.fromString(s));
		return uuids;
	}
	
	/**
	 * Opens the watchbook interface for a specific user.
	 */
	public static void openBook(Player player) {
		int PLAYERS_PER_PAGE = 14;
		Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bm = (BookMeta) book.getItemMeta();
			List<String> pages = new ArrayList<String>();
			
			String currentPage = ChatColor.BOLD + "" + ChatColor.UNDERLINE + "   Watch List   \n";
			for(UUID uniqueId : getWatched()) {
				
				String name = GameAPI.getNameFromUUID(uniqueId);
				
				boolean isPlaying = (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uniqueId);
				String server = DatabaseAPI.getInstance().getFormattedShardName(uniqueId);
				
				if(currentPage.split("\n").length > PLAYERS_PER_PAGE) {
					pages.add(currentPage);
					currentPage = ChatColor.BOLD + "" + ChatColor.UNDERLINE + "   Watch List   \n";
				}
				currentPage += name + " [" + (isPlaying ? server : "Offline") + "]\n";
				pages.add(currentPage);
			}
			
			bm.setPages(pages);
			book.setItemMeta(bm);
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> GameAPI.openBook(player, book));
		});
	}
}
