/**
 * 
 */
package net.dungeonrealms.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 20, 2015
 */
public class DuelMechanics {
	public static HashMap<UUID, UUID> PENDING_DUELS = new HashMap<>();
	public static HashMap<UUID, UUID> DUELS = new HashMap<>();
	public static ArrayList<UUID> cooldown = new ArrayList<>();
	public static ArrayList<DuelWager> WAGERS = new ArrayList<>();

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

	/**
	 * @param p1
	 * @param p2
	 */
	// 0, 8 Confirm
	// 4, 13, 22, 27,28,29, 31,33,34,35 Seperator
	// 30 Armor Tier, 32 weapon tier
	// LEFT ITEMS 1,2,3 9, 10, 11, 12, 18, 19, 20, 21
	// RIGHT ITEMS 23,24,25,26 , 5,6,7, 14,15,16,17
	public static void launchWager(Player p1, Player p2) {
		Inventory inv = Bukkit.createInventory(null, 36, p1.getName() + "  vs. " + p2.getName());
		ItemStack seperator = ItemManager.createItem(Material.BONE, "", null);
		ItemStack armorTier = ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
		ItemStack weaponTier = ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
		ItemStack confirm = ItemManager.createItemWithData(Material.INK_SACK, "Confirm", null,
			DyeColor.GRAY.getDyeData());
		inv.setItem(4, seperator);
		inv.setItem(13, seperator);
		inv.setItem(22, seperator);
		inv.setItem(27, seperator);
		inv.setItem(28, seperator);
		inv.setItem(29, seperator);
		inv.setItem(31, seperator);
		inv.setItem(33, seperator);
		inv.setItem(34, seperator);
		inv.setItem(35, seperator);
		inv.setItem(4, seperator);
		inv.setItem(30, armorTier);
		inv.setItem(32, weaponTier);
		inv.setItem(0, confirm);
		inv.setItem(8, confirm);
		p1.openInventory(inv);
		p2.openInventory(inv);

	}

	/**
	 * @param player
	 * @return
	 */
	public static DuelWager getWager(Player player) {
		for (int i = 0; i < WAGERS.size(); i++) {
			DuelWager current = WAGERS.get(i);
			if (current.p1.getUniqueId() == player.getUniqueId() || current.p2.getUniqueId() == player.getUniqueId())
			return current;
		}
		return null;
	}

	/**
	 * @param wager
	 */
	public static void removeWager(DuelWager wager) {
		WAGERS.remove(wager);
	}
}
