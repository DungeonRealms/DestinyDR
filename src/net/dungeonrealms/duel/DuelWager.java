/**
 * 
 */
package net.dungeonrealms.duel;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 20, 2015
 */
public class DuelWager {
	public Player p1;
	public Player p2;
	public ItemTier armorTier;
	public ItemTier weaponTier;
	public ArrayList<ItemStack> winningItems = new ArrayList<>();

	public DuelWager(Player p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
		armorTier = ItemTier.TIER_5;
		weaponTier = ItemTier.TIER_5;
	}

	public void setItemSlot(int slot, ItemStack stack) {
		p1.getOpenInventory().setItem(slot, stack);
		p2.getOpenInventory().setItem(slot, stack);
	}

	public boolean isLeft(Player p) {
		return (p.getUniqueId() == p1.getUniqueId());
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
			if (j > list.length) {
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
			if (j > list.length) {
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
		DuelMechanics.cancelRequestedDuel(p1);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			int time = 10;

			@Override
			public void run() {
			time--;
			p1.sendMessage(ChatColor.GREEN.toString() + time + ChatColor.YELLOW.toString()
					+ " seconds until the battle begins!");
			if (time == 0) {
				p1.sendMessage(ChatColor.GREEN + "Duel started with " + p2.getDisplayName());
				p2.sendMessage(ChatColor.GREEN + "Duel started with " + p1.getDisplayName());
				DuelMechanics.DUELS.put(p1.getUniqueId(), p2.getUniqueId());
				DuelMechanics.DUELS.put(p2.getUniqueId(), p1.getUniqueId());
				this.cancel();
			}
			}

		}, 0, 20l);

	}

}
