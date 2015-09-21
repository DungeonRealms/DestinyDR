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
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

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
	public static void sendDuelRequest(UUID p1, UUID p2) {
		PENDING_DUELS.put(p1, p2);
		PENDING_DUELS.put(p2, p1);
		cooldown.add(p1);
		// REMOVE PLAYER FROM COOLDOWN AFTER 10 SECONDS
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
			cooldown.remove(p1);
		} , 5 * 20L);
		Player player1 = Bukkit.getPlayer(p1);
		Player player2 = Bukkit.getPlayer(p2);
		player1.sendMessage(ChatColor.YELLOW.toString() + "Duel Request has been sent to " + player2.getDisplayName()
			+ " they have 10 seconds to respond to your duel request!");
		player2.sendMessage(ChatColor.YELLOW.toString() + "Duel request received from " + player1.getDisplayName()
			+ " hit them back to accept");
	}

	public static boolean isPendingDuel(UUID p) {
		return PENDING_DUELS.containsKey(p);
	}

	public static boolean isDueling(UUID p) {
		return DUELS.containsKey(p);
	}

	public static void cancelRequestedDuel(UUID p) {
		UUID uuid1 = PENDING_DUELS.get(p);
		UUID uuid2 = PENDING_DUELS.get(uuid1);
		PENDING_DUELS.remove(uuid1);
		PENDING_DUELS.remove(uuid2);
	}



	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static boolean isDuelPartner(UUID p1, UUID p2) {
		return DUELS.get(p1) == p2;
	}

	public static boolean isPendingDuelPartner(UUID p1, UUID p2) {
		return PENDING_DUELS.get(p1) == p2;
	}

	/**
	 * @return
	 */
	public static boolean isOnCooldown(UUID p1) {
		return cooldown.contains(p1);
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
		DuelWager wager = new DuelWager(p1, p2);
		WAGERS.add(wager);
		Inventory inv = Bukkit.createInventory(null, 36, p1.getName() + "  vs. " + p2.getName());
		ItemStack seperator = ItemManager.createItem(Material.BONE, " ", null);
		ItemStack armorTier = ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
		ItemStack weaponTier = ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
		ItemStack confirm = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "Ready",
			null, DyeColor.GRAY.getDyeData());
		ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "Ready", null,
			DyeColor.GRAY.getDyeData());
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("state", "notready");
		nms.setTag(nbt);
		nms.c(ChatColor.YELLOW + "READY");
		inv.setItem(0, CraftItemStack.asBukkitCopy(nms));
		inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
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
		p1.openInventory(inv);
		p2.openInventory(inv);

	}

	/**
	 * @param player
	 * @return
	 */
	public static DuelWager getWager(UUID uuid) {
		for (int i = 0; i < WAGERS.size(); i++) {
			DuelWager current = WAGERS.get(i);
			if (current.p1.getUniqueId() ==uuid || current.p2.getUniqueId() == uuid)
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
