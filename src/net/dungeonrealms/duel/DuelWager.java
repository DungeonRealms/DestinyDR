/**
 * 
 */
package net.dungeonrealms.duel;

import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Chase on Sep 20, 2015
 */
public class DuelWager {
	public UUID p1UUID;
	public UUID p2UUID;
	public ItemTier armorTier;
	public ItemTier weaponTier;
	public ArrayList<ItemStack> winningItems = new ArrayList<>();
	public boolean completed = false;
	
	public DuelWager(UUID p1UUID, UUID p2UUID) {
		this.p1UUID = p1UUID;
		this.p2UUID = p2UUID;
		armorTier = ItemTier.TIER_5;
		weaponTier = ItemTier.TIER_5;
	}

	public void setItemSlot(int slot, ItemStack stack) {
		Bukkit.getPlayer(p1UUID).getOpenInventory().setItem(slot, stack);
		Bukkit.getPlayer(p2UUID).getOpenInventory().setItem(slot, stack);
	}

	public boolean isLeft(UUID uuid) {
		return (uuid == p1UUID);
	}

	/**
	 * Go to next Tier
	 */
	public void cycleWeapon() {
		ItemTier[] list = ItemTier.values();
		int j = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i] == weaponTier) {
			j = i + 1;
			if (j >= list.length) {
				j = 0;
				break;
			}
			}
		}
		weaponTier = list[j];
		setItemSlot(32, getWeaponItem());
	}

	/**
	 * @return
	 */
	private ItemStack getWeaponItem() {
		switch (weaponTier) {
		case TIER_1:
			return ItemManager.createItem(Material.WOOD_SWORD, "Weapon Tier Limit", null);
		case TIER_2:
			return ItemManager.createItem(Material.STONE_SWORD, "Weapon Tier Limit", null);
		case TIER_3:
			return ItemManager.createItem(Material.IRON_SWORD, "Weapon Tier Limit", null);
		case TIER_4:
			return ItemManager.createItem(Material.DIAMOND_SWORD, "Weapon Tier Limit", null);
		case TIER_5:
			return ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
		}
		return null;
	}

	/**
	 * Go to next Tier
	 */
	public void cycleArmor() {
		ItemTier[] list = ItemTier.values();
		int j = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i] == armorTier) {
			j = i + 1;
			if (j >= list.length) {
				j = 0;
				break;
			}
			}
		}
		armorTier = list[j];
		setItemSlot(30, getArmorItem());
	}

	/**
	 * @return
	 */
	private ItemStack getArmorItem() {
		switch (armorTier) {
		case TIER_1:
			return ItemManager.createItem(Material.LEATHER_CHESTPLATE, "Armor Tier Limit", null);
		case TIER_2:
			return ItemManager.createItem(Material.CHAINMAIL_CHESTPLATE, "Armor Tier Limit", null);
		case TIER_3:
			return ItemManager.createItem(Material.IRON_CHESTPLATE, "Armor Tier Limit", null);
		case TIER_4:
			return ItemManager.createItem(Material.DIAMOND_CHESTPLATE, "Armor Tier Limit", null);
		case TIER_5:
			return ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
		}
		return null;
	}

	/**
	 * 
	 */
	public void startDuel() {
		completed = true;
		Bukkit.getPlayer(p1UUID).closeInventory();
		Bukkit.getPlayer(p2UUID).closeInventory();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			int time = 10;

			@Override
			public void run() {
			time--;
				Bukkit.getPlayer(p2UUID).sendMessage(ChatColor.GREEN.toString() + time + ChatColor.YELLOW.toString()
					+ " seconds until the battle begins!");
			if (time == 0) {
				Bukkit.getPlayer(p1UUID).sendMessage(ChatColor.GREEN + "Duel started with " + Bukkit.getPlayer(p2UUID).getDisplayName());
				Bukkit.getPlayer(p2UUID).sendMessage(ChatColor.GREEN + "Duel started with " + Bukkit.getPlayer(p1UUID).getDisplayName());
				DuelMechanics.DUELS.put(p1UUID, p2UUID);
				DuelMechanics.DUELS.put(p2UUID, p1UUID);
				this.cancel();
			}
			}

		}, 0, 10 * 1000l);

	}

}
