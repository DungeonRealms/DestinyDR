package net.dungeonrealms.game.quests.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import net.dungeonrealms.DungeonRealms;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
/**
 * This is the GuiBase for the quest editor. (Maybe in the future we'll use it for all of DR)
 * 
 * Created February 5th, 2017.
 * @author Kneesnap
 */
public class GuiBase implements Listener {
	
	private Inventory inventory;
	protected Player player;
	private HashMap<Integer, Consumer<InventoryClickEvent>> callbacks = new HashMap<Integer, Consumer<InventoryClickEvent>>();

	protected static Consumer<InventoryClickEvent> UNCANCEL = (evt) -> {
		evt.setCancelled(false);
		evt.setResult(Result.ALLOW);
	};
	
	protected static ItemStack GO_BACK = createItem(Material.WATCH, 1, (short)0, ChatColor.YELLOW + "Go Back", new String[] {"Click here to return to the previous screen."});
	
	public GuiBase(Player player, String inventoryName, List<?> list, int extra){
		this(player, inventoryName, 
				( list.size() - 
				( list.size() % 9))
				/ 9 + (list.size() % 9 > 0 ? 1 : 0) + extra );
	}
	
	public GuiBase(Player player, String inventoryName, InventoryType type){
		this(player, inventoryName, 1, type);
	}
	
	public GuiBase(Player player, String inventoryName, int rows){
		this(player, inventoryName, rows, null);
	}
	
	public GuiBase(Player player, String inventoryName, int rows, InventoryType type){
		this.player = player;
		if(type == null){
			this.inventory = Bukkit.createInventory(player, rows * 9, inventoryName);
		}else{
			this.inventory = Bukkit.createInventory(player, type, inventoryName);
		}
		
		// The delay lets everything initialize after super() first  //
		Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
			this.createGUI();
			player.openInventory(this.inventory);
			Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
		});
	}
	
	/**
	 * This should be overwritten by any GUI
	 * 
	 */
	protected void createGUI(){
		
	}
	
	protected void onClose(){
		
	}
	
	protected static ItemStack createItem(Material mat, int amt, short meta, String displayName, String[] lore){
		ItemStack item = new ItemStack(mat, amt);
		item.setDurability(meta);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColor.RESET + displayName);
		List<String> loreList = Arrays.asList(lore);
		loreList.stream().forEach((line) -> loreList.set(loreList.indexOf(line), ChatColor.GRAY + line));
		itemMeta.setLore(loreList);
		item.setItemMeta(itemMeta);
		return item;
	}
	
	protected ItemStack setSlot(int slot, Material mat, String displayName, String[] lore){
		return setSlot(slot, mat, 1, displayName, lore, null);
	}
	
	protected ItemStack setSlot(int slot, Material mat, String displayName, String[] lore, Consumer<InventoryClickEvent> callback){
		return setSlot(slot, mat, 1, displayName, lore, callback);
	}
	
	protected ItemStack setSlot(int slot, Material mat, short meta, String displayName, String[] lore, Consumer<InventoryClickEvent> callback){
		return setSlot(slot, mat, 1, meta, displayName, lore, callback);
	}
	
	protected ItemStack setSlot(int slot, Material material, int amount, String displayName, String[] lore, Consumer<InventoryClickEvent> callback){
		return setSlot(slot, material, amount, (short)0, displayName, lore, callback);
	}
	
	protected ItemStack setSlot(int slot, Material material, int amount, short meta, String displayName, String[] lore, Consumer<InventoryClickEvent> callback){
		return setSlot(slot, createItem(material, amount, meta, displayName, lore), callback);
	}
	
	protected ItemStack setSlot(int slot, ItemStack item){
		this.inventory.setItem(slot,  item);
		return item;
	}
	
	protected ItemStack setSlot(int slot, ItemStack item, Consumer<InventoryClickEvent> callback){
		if(callback != null)
			this.setCallback(slot, callback);
		return setSlot(slot, item);
	}
	
	protected void setCallback(int slot, Consumer<InventoryClickEvent> callback){
		this.callbacks.put(slot, callback);
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public Inventory getInventory(){
		return this.inventory;
	}
	
	protected int getSize(){
		return this.inventory.getSize();
	}
	
	private boolean isCorrectInventory(Inventory inv, HumanEntity player){
		return inv.getTitle().equals(this.inventory.getTitle()) && player == this.player;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent evt){
		if(!isCorrectInventory(evt.getInventory(), evt.getWhoClicked()))
			return;
		if(evt.getRawSlot() >= evt.getInventory().getSize() && !evt.isShiftClick()){
			return;
		}
			
		evt.setCancelled(true);
		
		if(this.callbacks.containsKey(evt.getRawSlot()))
			this.callbacks.get(evt.getRawSlot()).accept(evt);
	}
	
	@EventHandler
	public void onInventoryClosed(InventoryCloseEvent evt){
		if(!isCorrectInventory(evt.getInventory(), evt.getPlayer()))
			return;
		evt.getPlayer().setItemOnCursor(null);
		InventoryClickEvent.getHandlerList().unregister(this);
		InventoryCloseEvent.getHandlerList().unregister(this);
		this.onClose();
	}
}
