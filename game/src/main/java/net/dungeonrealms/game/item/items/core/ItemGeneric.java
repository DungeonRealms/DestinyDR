package net.dungeonrealms.game.item.items.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * ItemGeneric - A GearItem that can be applied to any item.
 * Contains generic information that can be applied to any item such as Soulbound, untradeable, etc.
 * <p>
 * Created March 28th, 2017.
 *
 * @author Kneesnap
 */
public abstract class ItemGeneric extends PersistentItem {
	
	@Getter
	private Map<ItemFlag, Boolean> dataMap;
	
	private List<String> lore;
	
	@Getter
	private boolean resetLore; // Should this lore be reset?
	
	@Getter @Setter
	private boolean antiDupe;
	
	@Getter
	private ItemType itemType;
	
	@Getter @Setter //Whether or not this item should be removed.
	private boolean destroyed;
	
	@Setter @Getter
    private int price; //The price of this item. 0 marks no price.
	
	@Setter @Getter
	private boolean showPrice = true; //Whether or not lore should be created for this price.
	
	private long soulboundTrade = 0;
	private List<String> soulboundAllowedTraders;
	
	@Getter
	private boolean glowing;
	
	@Getter
	private ItemMeta meta;
	
	@Setter @Getter // EC - Name
	private String customName;
	
	@Getter @Setter
	private String customLore;
	
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
	}
	
	public boolean isPermanentUntradeable() {
		return getFlag(ItemFlag.PUNTRADEABLE);
	}
	
	public boolean isDisplay() {
		return getFlag(ItemFlag.MENU);
    }
	
	public boolean isSoulbound() {
		return getFlag(ItemFlag.SOULBOUND);
	}
	
	public boolean isUndroppable() {
		return getFlag(ItemFlag.UNDROPPABLE);
	}
	
	public boolean isUntradeable() {
		return getFlag(ItemFlag.UNTRADEABLE);
	}
	
	public boolean isEventItem() {
		return getFlag(ItemFlag.EVENT);
	}
	
	public boolean isDungeon() {
		return getFlag(ItemFlag.DUNGEON);
	}
	
	public ItemGeneric setDisplay(boolean display) {
		setFlag(ItemFlag.MENU, display);
		return this;
	}
	
	public ItemGeneric setDungeon(boolean dungeon) {
		setFlag(ItemFlag.DUNGEON, dungeon);
		return this;
	}
	
    public ItemGeneric setPermUntradeable(boolean perm) {
        setFlag(ItemFlag.PUNTRADEABLE, perm);
        return this;
    }

    public ItemGeneric setSoulbound(boolean soulbound) {
        setFlag(ItemFlag.SOULBOUND, soulbound);
        return this;
    }

    public ItemGeneric setUndroppable(boolean undroppable) {
        setFlag(ItemFlag.UNDROPPABLE, undroppable);
        return this;
    }

    public ItemGeneric setUntradeable(boolean untradeable) {
        setFlag(ItemFlag.UNTRADEABLE, untradeable);
        return this;
    }

    public ItemGeneric setEventItem(boolean event) {
        setFlag(ItemFlag.EVENT, event);
        return this;
    }

    protected ItemGeneric setType(ItemType type) {
        if (type != null)
            this.itemType = type;
        return this;
    }

    public ItemGeneric setGlowing(boolean b) {
        this.glowing = b;
        return this;
    }

    @Override
    protected void loadItem() {
    	this.meta = new ItemStack(Material.DIRT).getItemMeta();
    	
        for (ItemFlag data : ItemFlag.values())
            setFlag(data, getTagBool(data.getNBTTag()));

        if (isSoulbound() && hasTag("soulboundTrade")) {
            long time = getTag().getLong("soulboundTrade");
            if (time > System.currentTimeMillis()) {
                this.soulboundTrade = time;
                this.soulboundAllowedTraders = Arrays.asList(getTagString("soulboundBypass").split(","));
            }
        }

        setPrice(getTagInt("price"));
        setShowPrice(getTagBool("showPrice"));
        setGlowing(EnchantmentAPI.isGlowing(getItem()));
        setType(ItemType.getType(getTagString("type")));

        if (hasTag("ecName"))
            setCustomName(getTagString("ecName"));

        if (hasTag("ecLore"))
            setCustomLore(getTagString("ecLore"));
    }

    /**
     * Remove the item unique id. Used by Loot Chests to not produce duplicate items.
     */
    public void removeEpoch() {
        removeTag("u");
    }

    @Override
    public ItemStack getItem() {
        return isDestroyed() ? new ItemStack(Material.AIR) : super.getItem();
    }

    @Override
    public ItemStack generateItem() {
        this.meta = getStack().getItemMeta();
        ItemStack ret = super.generateItem().clone();
        return ret;
    }

    @Override
    public void updateItem() {
        if (isDestroyed())
            return;

        for (ItemFlag data : ItemFlag.values()) {
            boolean enabled = getFlag(data);
            setTagBool(data.getNBTTag(), enabled);

            if (data.getDisplay() != null && enabled && (lore == null || !lore.contains(ChatColor.GRAY + data.getDisplay())))
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
            setTagString("u", AntiDuplication.createEpoch(getItem()));

        if (getPrice() > 0) {
            setTagInt("price", getPrice());
            if (isShowPrice()) {
                setTagBool("showPrice", true);
                addLore(ChatColor.GREEN + "Price: " + ChatColor.WHITE + getPrice() + "g" + ChatColor.GREEN + " each");
            }
        }

        if (isGlowing())
            EnchantmentAPI.addGlow(getItem());

        // Ecash Name - Overrides custom names by other items.
        // getCustomName() should ONLY be overridden to modify the EC display name, ie append [+3] to the name.
        if (this.customName != null) {
        	setTagString("ecName", this.customName);
        	getMeta().setDisplayName(getCustomName());
        }
        
        // Ecash Lore - Overrides custom names given by other items.
        // getCustomLore() should ONLY be overriden to modify the EC display lore. NOT any other lore.
        if (this.customLore != null) {
        	setTagString("ecLore", this.customLore);
        	addLore(getCustomLore());
        }
        
        saveMeta();
        resetLore = true; // Reset the lore if we generate again.
    }

    /**
     * Saves data in meta to NBT.
     * Just using setItemMeta will override NBT tags, so we set them manually.
     */
    private void saveMeta() {
        ItemStack withMeta = getItem().clone();
        getMeta().setLore(this.lore);
        withMeta.setItemMeta(getMeta());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(withMeta);
        if (!nms.hasTag())
            return;
        NBTTagCompound merge = nms.getTag();
        for (String key : merge.c())
            getTag().set(key, merge.get(key));
        getItem().setItemMeta(getMeta());
    }

    /**
     * Remove the price from this item.
     */
    public void removePrice() {
        setPrice(0);
    }

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

    /**
     * Adds lore to this item. * SHOULD ONLY BE CALLED DURING updateItem() *
     * Automatically applies gray at the start of the string.
     * @param s
     */
    public void addLore(String s) {
        if (this.lore == null) // Can't put above constructor as it will override any values set in loadItem
            this.lore = new ArrayList<>();

        if (resetLore) {
            this.lore.clear();
            resetLore = false;
        }
        
        this.lore.add(ChatColor.GRAY + s);
    }

    protected void removeLore(String startsWith) {
        for (int i = 0; i < this.lore.size(); i++) {
            if (this.lore.get(i).startsWith(startsWith)) {
                this.lore.remove(i);
                return;
            }
        }
    }

    public boolean getFlag(ItemFlag data) {
        return dataMap != null && dataMap.containsKey(data) && dataMap.get(data);
    }

    public void setFlag(ItemFlag data, boolean enabled) {
        if (dataMap == null)
            dataMap = new HashMap<>();
        dataMap.put(data, enabled);
    }

    @AllArgsConstructor
    @Getter
    public enum ItemFlag {
        SOULBOUND(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Soulbound"),
        UNTRADEABLE(ChatColor.GRAY + "Untradeable"),
        PUNTRADEABLE(ChatColor.GRAY + "Permanent Untradeable"),
        EVENT(ChatColor.RED + "Event Item"),
        MENU(ChatColor.GRAY + "Display Item", false),
        DUNGEON(ChatColor.RED + "Dungeon Item"),
        UNDROPPABLE(null, false);

        private final String display;
        private boolean showInGUI;

        ItemFlag(String s) {
            this(s, true);
        }

        public String getNBTTag() {
            return this.name().toLowerCase();
        }
    }
}
