package net.dungeonrealms.game.item.items.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * ItemGeneric - A GearItem that can be applied to any item.
 * Contains generic information that can be applied to any item such as Soulbound, untradeable, etc.
 * 
 * Created March 28th, 2017.
 * @author Kneesnap
 */
public abstract class ItemGeneric extends PersistentItem {

	private Map<ItemData, Boolean> dataMap = new HashMap<>();
	
	private List<String> lore;
	
	@Getter @Setter
	private boolean antiDupe;
	
	@Getter
	private ItemType itemType;
	
	@Getter @Setter //Whether or not this item should be removed.
	private boolean destroyed;
	
	private boolean resetLore; //This marks whether lore should be reset. This is used so lore isn't added from a previous item update.
	
	@Setter @Getter
	private int price; //The price of this item. 0 marks no price.
	
	@Setter @Getter
	private boolean showPrice; //Whether or not lore should be created for this price.
	
	private long soulboundTrade = 0;
	private List<String> soulboundAllowedTraders;
	
	//Tier should share the same name for consistency.
	protected static final String TIER = "itemTier";
	
	public ItemGeneric(ItemType type) {
		this(null, type);
	}
	
	public ItemGeneric(ItemStack item) {
		this(item, getType(item));
	}
	
	public ItemGeneric(ItemStack item, ItemType type) {
		super(item);
		this.itemType = type;
		this.lore = new ArrayList<>();
		
		//Alert us if anything goes awry. Attempts to delete the item. (It likely won't)
		if (isEventItem() && !(DungeonRealms.getInstance().isMasterShard || DungeonRealms.getInstance().isEventShard)) {
			GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[WARNING] " + ChatColor.WHITE + "Found event item on non-event shard! Found "
					+ item.getAmount() + "x" + (item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name()));
			setDestroyed(true);
		}
	}
	
	public boolean isPermanentUntradeable() {
		return getSData(ItemData.PUNTRADEABLE);
	}
	
	public boolean isSoulbound() {
		return getSData(ItemData.SOULBOUND);
	}
	
	public boolean isUndroppable() {
		return getSData(ItemData.UNDROPPABLE);
	}
	
	public boolean isUntradeable() {
		return getSData(ItemData.UNTRADEABLE);
	}
	
	public boolean isEventItem() {
		return getSData(ItemData.EVENT);
	}
	
	public ItemGeneric setPermUntradeable(boolean perm) {
		dataMap.put(ItemData.PUNTRADEABLE, perm);
		return this;
	}
	
	public ItemGeneric setSoulbound(boolean soulbound) {
		dataMap.put(ItemData.SOULBOUND, soulbound);
		return this;
	}
	
	public ItemGeneric setUndroppable(boolean undroppable) {
		dataMap.put(ItemData.UNDROPPABLE, undroppable);
		return this;
	}
	
	public ItemGeneric setUntradeable(boolean untradeable) {
		dataMap.put(ItemData.UNTRADEABLE, untradeable);
		return this;
	}
	
	public ItemGeneric setEventItem(boolean event) {
		dataMap.put(ItemData.EVENT, event);
		return this;
	}

	@Override
	protected void loadItem() {
		for(ItemData data : ItemData.values())
			dataMap.put(data, getData(data));
		
		if (isSoulbound() && hasTag("soulboundTrade")) {
			long time = getTag().getLong("soulboundTrade");
			if (time > System.currentTimeMillis()) {
				this.soulboundTrade = time;
				this.soulboundAllowedTraders = Arrays.asList(getTagString("soulboundBypass").split(","));
			}
		}
		
		setPrice(getTagInt("price"));
		setShowPrice(getTagBool("showPrice"));
	}
	
	@Override
	public ItemStack getItem() {
		return isDestroyed() ? new ItemStack(Material.AIR) : super.getItem();
	}

	@Override
	public void updateItem() {
		if (isDestroyed())
			return;
		
		for (ItemData data : ItemData.values()) {
			boolean enabled = getSData(data);
			setData(data, enabled);
			//Update the lore.
			if(data.getDisplay() != null)
				addLore(data.getDisplay());
		}
		
		// Don't save this data if it has expired.
		if (isSoulbound() && this.soulboundTrade > 0) {
			getTag().setLong("soulboundTrade", this.soulboundTrade);
			setTagString("soulboundBypass", String.join(",", this.soulboundAllowedTraders));
		}
		
		if (getItemType() != null)
			setTagString("type", getItemType().getNBT());

		//  APPLY ANTI DUPE  //
		if (isAntiDupe() && !hasTag("u"))
			setTagString("u", AntiDuplication.createEpoch(getItem())); //TODO: Make sure this is never lost.
		
		if (getPrice() > 0) {
			setTagInt("price", getPrice());
			if(isShowPrice()) {
				setTagBool("showPrice", true);
				addLore(ChatColor.GREEN + "Price: " + ChatColor.WHITE + getPrice() + "g" + ChatColor.GREEN + " each");
			}
		}
		
		// Only update the lore if this is being generated. (TODO Wait, why did I add this?)
		if (isGenerating())
			getMeta().setLore(this.lore);
		getItem().setItemMeta(getMeta());
		resetLore = true;
	}
	
	/**
	 * Remove the price from this item.
	 */
	public void removePrice() {
		setPrice(0);
	}
	
	/**
	 * Allows this item to be traded to a specified player for X minutes.
	 * @param Player
	 * @param time (In Seconds)
	 */
	public void addSoulboundBypass(Player p, int time) {
		this.soulboundTrade = System.currentTimeMillis() + time * 1000;
		this.soulboundAllowedTraders.add(p.getName());
	}
	
	/**
	 * Can this item be traded to a specified player?
	 */
	public boolean isSoulboundBypass(Player p) {
		return !isSoulbound() || (this.soulboundTrade > System.currentTimeMillis() && this.soulboundAllowedTraders.contains(p.getName()));
	}
	
	protected void addLore(String s) {
		if(resetLore) {
			this.lore.clear();
			resetLore = false;
		}
		this.lore.add(ChatColor.GRAY + s);
	}
	
	protected void removeLore(String startsWith) {
		for(int i = 0; i < this.lore.size(); i++) {
			if (this.lore.get(i).startsWith(startsWith)) {
				this.lore.remove(i);
				return;
			}
		}
	}
	
	protected void clearLore() {
		this.lore.clear();
	}
	
	private boolean getSData(ItemData data) {
		return dataMap.containsKey(data) && dataMap.get(data);
	}
	
	private boolean getData(ItemData data) {
		return getTagBool(data.getNBTTag());
	}
	
	private void setData(ItemData data, boolean enabled) {
		if(!hasTag(data.getNBTTag()) && !enabled) //Don't set tags to their default value, waste of memory.
			return;
		setTagBool(data.getNBTTag(), enabled);
	}
	
	private enum ItemData { 
		SOULBOUND(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Soulbound"),
		UNTRADEABLE(ChatColor.GRAY + "Untradeable"),
		PUNTRADEABLE(ChatColor.GRAY + "Permanent Untradeable"),
		EVENT(ChatColor.RED + "Event Item"),
		UNDROPPABLE(null);
		
		@Getter
		private final String display;
		
		ItemData(String display) {
			this.display = display;
		}
		
		public String getNBTTag() {
			return this.name().toLowerCase();
		}
	}
}
