/**
 * 
 */
package net.dungeonrealms.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 20, 2015
 */
public class DuelMechanics {
	public static HashMap<UUID, UUID> PENDING_DUELS = new HashMap<UUID, UUID>();
	public static HashMap<UUID, UUID> DUELS = new HashMap<UUID, UUID>();
	public static ArrayList<UUID> cooldown = new ArrayList<UUID>();

	// ALL PLAYERS IN A DUEL
	/**
	 * @param p1
	 * @param p2
	 */
	public static void sendDuelRequest(Player p1, Player p2) {
		PENDING_DUELS.put(p1.getUniqueId(), p2.getUniqueId());
		PENDING_DUELS.put(p2.getUniqueId(), p1.getUniqueId());
		cooldown.add(p1.getUniqueId());
		// REMOVE PLAYER FROM COOLDOWN AFTER 10 SECONDS
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
			cooldown.remove(p1.getUniqueId());
		} , 10L * 20L);

		p1.sendMessage(ChatColor.YELLOW.toString() + "Duel Request has been sent to " + p2.getDisplayName()
			+ " they have 10 seconds to respond to your duel request!");
		p2.sendMessage(ChatColor.YELLOW.toString() + "Duel request received from " + p1.getDisplayName()
			+ " hit them back to accept");
	}

	public static boolean isPendingDuel(Player p) {
		return PENDING_DUELS.containsKey(p.getUniqueId());
	}

	public static boolean isDueling(Player p) {
		return DUELS.containsKey(p.getUniqueId());
	}

	public static void cancelRequestedDuel(Player p) {
		UUID uuid1 = PENDING_DUELS.get(p.getUniqueId());
		UUID uuid2 = PENDING_DUELS.get(uuid1);
		PENDING_DUELS.remove(uuid1);
		PENDING_DUELS.remove(uuid2);
	}

	/**
	 * @param p1
	 * @param p2
	 */
	public static void setupDuel(Player p1, Player p2) {
		cancelRequestedDuel(p1);
		DUELS.put(p1.getUniqueId(), p2.getUniqueId());
		DUELS.put(p2.getUniqueId(), p1.getUniqueId());
		p1.sendMessage(ChatColor.GREEN  + "Duel started with " + p2.getDisplayName());
		p2.sendMessage(ChatColor.GREEN  + "Duel started with " + p1.getDisplayName());
	}

	/**
	 * Player2 is the loser.
	 * 
	 * @param p1
	 * @param p2
	 */
	public static void endDuel(Player p1, Player p2) {
		Bukkit.broadcastMessage(p1.getDisplayName() + " has defeated " + p2.getDisplayName() + " in a duel.");
		DuelMechanics.DUELS.remove(p1.getUniqueId());
		DuelMechanics.DUELS.remove(p2.getUniqueId());
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static boolean isDuelPartner(Player p1, Player p2) {
		return DUELS.get(p1.getUniqueId()) == p2.getUniqueId();
	}

	/**
	 * @return
	 */
	public static boolean isOnCooldown(Player p1) {
		return cooldown.contains(p1.getUniqueId());
	}
}
