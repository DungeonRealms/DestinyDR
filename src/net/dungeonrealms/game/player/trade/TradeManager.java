package net.dungeonrealms.game.player.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Nov 16, 2015
 */
public class TradeManager {

	public static ArrayList<Trade> trades = new ArrayList<>();
	public static ArrayList<UUID> cooldown = new ArrayList<>();
	public static HashMap<UUID, UUID> pending = new HashMap<>();

	/**
	 * sender, receiver
	 * 
	 * @param p1
	 * @param p2
	 */
	public static void sendTradeRequest(UUID p1, UUID p2) {
		Player sender = Bukkit.getPlayer(p1);
		Player requested = Bukkit.getPlayer(p2);
		if(sender  == null|| requested == null){
			return;
		}
		if (isOnCooldown(p1)) {
			sender.sendMessage(ChatColor.RED + "You're currently on cooldown for sending duel requests!");
			return;
		}
		if (isPending(requested.getUniqueId()) && getPendingPartner(requested.getUniqueId()) != null && getPendingPartner(requested.getUniqueId()).toString()
		        .equalsIgnoreCase(sender.getUniqueId().toString())) {
			startTrade(sender, requested);
			return;
		}

		cooldown.add(p1);
		sender.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Trade request sent.");
		Bukkit.getPlayer(p2).sendMessage(ChatColor.AQUA + Bukkit.getPlayer(p1).getName() +ChatColor.YELLOW +  " Would like to trade!");
		Bukkit.getPlayer(p2).sendMessage(ChatColor.AQUA + Bukkit.getPlayer(p1).getName()
		        + ChatColor.YELLOW + " Shift right click them and choose trade to accept!");
		pending.put(p1, p2);

		Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
			if (pending.containsKey(p1))
				pending.remove(p1);
			cooldown.remove(p1);
		} , 200l);// Remove Pending Request after 10 seconds.

	}

	/**
	 * @param uniqueId
	 * @return UUID
	 */
	public static UUID getPendingPartner(UUID uuid) {
		if (pending.containsKey(uuid)) {
			for (UUID id : pending.keySet()) {
				if (id.toString().equalsIgnoreCase(uuid.toString()))
					return pending.get(id);
			}
		}

		if (pending.containsValue(uuid)) {
			for (UUID id : pending.values()) {
				if (id.toString().equalsIgnoreCase(uuid.toString())) {
					for (UUID uniqueId : pending.keySet()) {
						if (uniqueId.toString().equalsIgnoreCase(id.toString()))
							return uniqueId;
					}
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param uuid
	 * @return boolean
	 */
	public static boolean isPending(UUID uuid) {
		return pending.containsKey(uuid) || pending.containsValue(uuid);
	}

	/**
	 * @param p1
	 * @return
	 */
	private static boolean isOnCooldown(UUID p1) {
		return false;
	}

	public static void startTrade(Player p1, Player p2) {
		trades.add(new Trade(p1, p2));
	}

	public static Trade getTrade(UUID uuid) {
		for (Trade trade : trades) {
			if (trade.p1.getUniqueId().toString().equalsIgnoreCase(uuid.toString())
			        || trade.p2.getUniqueId().toString().equalsIgnoreCase(uuid.toString()))
				return trade;
		}
		return null;
	}

}
