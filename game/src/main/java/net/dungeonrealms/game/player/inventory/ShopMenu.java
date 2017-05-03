package net.dungeonrealms.game.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.mastery.Utils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

/**
 * A menu that is used to purchase items from the server using portal shards or eCash.
 * 
 * Created April 10th, 2017.
 * @author Kneesnap
 */
public abstract class ShopMenu {

	@Getter @Setter
	private Inventory inventory;
	
	@Getter
	private Map<Integer, ShopItem> items = new HashMap<>();
	
	private int index = 0;
	
	@Getter 
	private Player player;
	
	protected static ShopItem BACK;
	
	static {
		createStaticItems();
	}
	
	public ShopMenu(Player player, String title, int rows) {
		this.inventory = Bukkit.createInventory(null, rows * 9, title);
		open(player);
	}
	
	protected NPCMenu getLastMenu() {
		return null; //NPCMenu.ECASH_VENDOR;
	}
	
	protected void skipSlot() {
		skipSlot(1);
	}
	
	protected void skipSlot(int i) {
		this.index += i;
	}
	
	protected ShopItem addItem(ShopItem item) {
		if (this.index >= getInventory().getSize()) {
			GameAPI.sendDevMessage("Attempted to place more items than " + getInventory().getTitle() + " can hold.");
			Utils.printTrace();
			return item;
		}
		getItems().put(this.index, item);
		this.index += 1;
		return item;
	}
	
	protected ShopItem addItem(ItemStack item) {
		return addItem(new VanillaItem(item));
	}
	
	protected ShopItem addItem(ItemGeneric item) {
		return addItem(new ShopItem(item));
	}
	
	protected ShopItem addItem(ItemGeneric item, ShopItemClick cb) {
		return addItem(new ShopItem(item, cb));
	}
	
	/**
	 * Fills empty items with glass.
	 */
	protected void bloat() {
		ItemStack glass = new ItemStack(Material.THIN_GLASS);
		ItemMeta meta = glass.getItemMeta();
		meta.setDisplayName(" ");
		glass.setItemMeta(meta);
		
		for (int i = 0; i < getInventory().getContents().length; i++) {
            ItemStack item = getInventory().getContents()[i];
            if (item == null || item.getType().equals(Material.AIR))
                getInventory().setItem(i, glass.clone());
        }
	}
	
	protected void setIndex(int i) {
		this.index = i;
	}
	
	/**
	 * Places the items in the shop.
	 */
	protected abstract void setItems();
	
	/**
	 * Closes this inventory safely and removes all items.
	 */
	public void destroy() {
		Lists.newArrayList(getInventory().getViewers()).forEach(HumanEntity::closeInventory);
		getInventory().clear();
	}
	
	/**
	 * Opens this gui for the specified player.
	 */
	public void open(Player player) {
		if (player == null)
			return;
		this.player = player;
		this.index = 0;
		this.inventory.clear();
		setItems();
		for (int i : getItems().keySet())
			getInventory().setItem(i, getItems().get(i).generateItem());
		player.openInventory(getInventory());
		ShopMenuListener.getMenus().put(player, this);
	}
	
	protected static ItemStack createItem(Material mat, String name, String... lore) {
		ItemStack stack = new ItemStack(mat);
		ItemMeta meta = stack.getItemMeta();
		
		// Name
		if (name != null)
			meta.setDisplayName(name);
		
		// Lore
		List<String> l = new ArrayList<>();
		if (lore != null)
			for(String s : lore)
				l.add(ChatColor.GRAY + s);
		meta.setLore(l);
		
		
		stack.setItemMeta(meta);
		return stack;
	}
	
	private static void createStaticItems() {
		BACK = new ShopItem(new VanillaItem(createItem(Material.BARRIER, ChatColor.GREEN + "Return")), (player, item) -> {
			ShopMenu menu = ShopMenuListener.getMenu(player);
			if (menu.getLastMenu() != null)
				menu.getLastMenu().open(player);
			return false;
		});
	}
}
