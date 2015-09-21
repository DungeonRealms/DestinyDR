/**
 * 
 */
package net.dungeonrealms.duel;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
	 * @param p1UUID
	 * @param p2UUID
	 */
	public static void sendDuelRequest(UUID p1UUID, UUID p2UUID) {
		PENDING_DUELS.put(p1UUID, p2UUID);
		PENDING_DUELS.put(p2UUID, p1UUID);
		cooldown.add(p1UUID);
		// REMOVE PLAYER FROM COOLDOWN AFTER 10 SECONDS
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> cooldown.remove(p1UUID), 5 * 20L);

		Bukkit.getPlayer(p1UUID).sendMessage(ChatColor.YELLOW.toString() + "Duel Request has been sent to " + Bukkit.getPlayer(p2UUID).getDisplayName()
				+ " they have 10 seconds to respond to your duel request!");
		Bukkit.getPlayer(p2UUID).sendMessage(ChatColor.YELLOW.toString() + "Duel request received from " + Bukkit.getPlayer(p1UUID).getDisplayName()
				+ " hit them back to accept");
	}

	public static boolean isPendingDuel(UUID uuid) {
		return PENDING_DUELS.containsKey(uuid);
	}

	public static boolean isDueling(UUID uuid) {
		return DUELS.containsKey(uuid);
	}

	public static void cancelRequestedDuel(UUID uuid) {
		UUID uuid1 = PENDING_DUELS.get(uuid);
		UUID uuid2 = PENDING_DUELS.get(uuid1);
		PENDING_DUELS.remove(uuid1);
		PENDING_DUELS.remove(uuid2);
	}

	/**
	 * Player2 is the loser.
	 * 
	 * @param p1UUID
	 * @param p2UUID
	 */
	public static void endDuel(UUID p1UUID, UUID p2UUID) {
		Bukkit.broadcastMessage(Bukkit.getPlayer(p1UUID).getDisplayName() + " has defeated " + Bukkit.getPlayer(p2UUID).getDisplayName() + " in a duel.");
		DuelMechanics.DUELS.remove(p1UUID);
		DuelMechanics.DUELS.remove(p2UUID);
	}

	/**
	 * @param p1UUID
	 * @param p2UUID
	 * @return
	 */
	public static boolean isDuelPartner(UUID p1UUID, UUID p2UUID) {
		return DUELS.get(p1UUID) == p2UUID;
	}

	public static boolean isPendingDuelPartner(UUID p1UUID, UUID p2UUID) {
		return PENDING_DUELS.get(p1UUID) == p2UUID;
	}

	/**
	 * @return
	 */
	public static boolean isOnCooldown(UUID uuid) {
		return cooldown.contains(uuid);
	}

	/**
	 * @param p1UUID
	 * @param p2UUID
	 */
	// 0, 8 Confirm
	// 4, 13, 22, 27,28,29, 31,33,34,35 Seperator
	// 30 Armor Tier, 32 weapon tier
	// LEFT ITEMS 1,2,3 9, 10, 11, 12, 18, 19, 20, 21
	// RIGHT ITEMS 23,24,25,26 , 5,6,7, 14,15,16,17
	public static void launchWager(UUID p1UUID, UUID p2UUID) {
		DuelWager wager = new DuelWager(p1UUID, p2UUID);
		WAGERS.add(wager);
		Inventory inv = Bukkit.createInventory(null, 36, Bukkit.getPlayer(p1UUID).getName() + "  vs. " + Bukkit.getPlayer(p2UUID).getName());
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
		Bukkit.getPlayer(p1UUID).openInventory(inv);
		Bukkit.getPlayer(p2UUID).openInventory(inv);

	}

	/**
	 * @param uuid
	 * @return
	 */
	public static DuelWager getWager(UUID uuid) {
		for (int i = 0; i < WAGERS.size(); i++) {
			DuelWager current = WAGERS.get(i);
			if (current.p1UUID == uuid || current.p2UUID == uuid)
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
