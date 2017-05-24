package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/10/2017.
 */
@AllArgsConstructor @Getter
public enum Purchaseables {

    LOOT_BUFF_20("Loot Buff", "\n20% global loot buff across all\nshards for every player!", Material.DIAMOND, WebstoreCategories.GLOBAL_BUFFS, 0, true, true, ChatColor.AQUA),
    LOOT_BUFF_40("Loot Buff", "\n40% global loot buff across all\nshards for every player!", Material.DIAMOND, WebstoreCategories.GLOBAL_BUFFS, 9, true, true, ChatColor.AQUA),
    PROFESSION_BUFF_20("Profession Buff", "\n20% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, WebstoreCategories.GLOBAL_BUFFS, 4, true, true, ChatColor.GOLD),
    PROFESSION_BUFF_40("Profession Buff", "\n40% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, WebstoreCategories.GLOBAL_BUFFS, 13, true, true, ChatColor.GOLD),
    LEVEL_BUFF_20("Level Buff", "\n20% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, WebstoreCategories.GLOBAL_BUFFS, 8, true, true, ChatColor.GREEN),
    LEVEL_BUFF_40("Level Buff", "\n40% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, WebstoreCategories.GLOBAL_BUFFS, 17, true, true, ChatColor.GREEN),

    SUB("Sub Rank", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 0, false, false, ChatColor.GREEN),
    SUB_PLUS("Sub+ Rank", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 4, false, false, ChatColor.GOLD),
    SUB_PLUS_PLUS("Sub++ Rank", "\nIn-game Subscriber++ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, false, ChatColor.GOLD),

    WIZARD_HAT("Wizard Hat", "\nEvery helmet you wear will look like\na wizard hat!", Material.SAPLING, WebstoreCategories.HATS, 0, false, true, ChatColor.WHITE, true, 4),
    CROWN("Gold Crown", "\nEvery helmet you wear will look like\na kings crown!", Material.SAPLING, WebstoreCategories.HATS, 4, false, true, ChatColor.GOLD, true, 2),

    SCRAP_TAB("Scrap Tab", "\nIn-game storage for your scrap!", Material.INK_SACK, WebstoreCategories.MISCELLANEOUS, 0, false, false, ChatColor.GOLD),
    JUKEBOX("Mobile Music Box", "\nPlay your favorite tunes where ever you want!", Material.JUKEBOX, WebstoreCategories.MISCELLANEOUS, 5, false, true, ChatColor.AQUA),
    ITEM_NAME_TAG("Item Name Tag", "\nRename an item to anything you want!", Material.NAME_TAG, WebstoreCategories.MISCELLANEOUS, 3, false, false, ChatColor.GREEN),
    GOLDEN_CURSE("Golden Curse", "\nEverything you touch shall\nturn to gold for all.", Material.GOLD_BLOCK, WebstoreCategories.MISCELLANEOUS, 8, false, true, ChatColor.GOLD, true),
    DPS_DUMMY("DPS Dummy", "A squishy dummy made to take a hit!", Material.ARMOR_STAND, WebstoreCategories.MISCELLANEOUS, 4, false, true, ChatColor.GREEN);

    private String name;
    private String description;
    private Material itemType;
    private WebstoreCategories category;
    private int guiSlot;
    private boolean canHaveMultiple, shouldStore;
    private ChatColor displayNameColor;
    private boolean enabled;
    private int meta;

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore, ChatColor.WHITE);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, ChatColor displayNameColor) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore, displayNameColor, true);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, ChatColor displayNameColor, boolean enabled) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore, displayNameColor, enabled, 0);
    }

    public List<String> getDescription(boolean showColors) {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        if (showColors) {
            for (int index = 0; index < toReturn.size(); index++) {
                String line = toReturn.get(index);
                toReturn.set(index, getDescriptionColor() + line);
            }
        }
        return toReturn;
    }

    public String getName() {
        return this.getName(true);
    }
    
    public ChatColor getDescriptionColor() {
    	return ChatColor.GRAY;
    }

    public String getName(boolean showColor) {
        return displayNameColor.toString() + ChatColor.BOLD.toString() + this.name;
    }

    public List<String> getDescription() {
        return this.getDescription(true);
    }

    public static int getNumberOfItems(WebstoreCategories category) {
        int num = 0;
        for (Purchaseables item : Purchaseables.values())
            if (item.getCategory() == category)
            	num++;

        return num;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public int getNumberOwned(PlayerWrapper wrapper) {
        if (wrapper == null)
        	return -1;
        
        if (!isShouldStore())
        	return this == SCRAP_TAB ? (wrapper.getCurrencyTab() != null && wrapper.getCurrencyTab().hasAccess ? 1 : 0) : -1;
        
        Integer number = wrapper.getPurchaseablesUnlocked().get(this);
        return number != null ? number : 0;
    }

    public boolean isUnlocked(PlayerWrapper wrapper) {
        return getNumberOwned(wrapper) > 0;
    }

    public String getOwnedDisplayString(PlayerWrapper wrapper) {
        int numOwned = getNumberOwned(wrapper);
        return numOwned > 0 ? ChatColor.GREEN + ChatColor.BOLD.toString() + (isCanHaveMultiple() ? "OWNED: " + numOwned : "UNLOCKED")
        		: ChatColor.RED + ChatColor.BOLD.toString() + (isCanHaveMultiple() ? "NONE OWNED" : "LOCKED");
    }

    public void setNumberOwned(PlayerWrapper wrapper, int owned) {
        this.setNumberOwned(wrapper, owned, true);
    }

    @SneakyThrows
    public void setNumberOwned(PlayerWrapper wrapper, int owned, boolean autoSave) {
        if (!shouldStore)
            throw new Exception("We can not store this purchase! It might be handled differently!");
        wrapper.getPurchaseablesUnlocked().put(this, owned);

        if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
    }

    public static final int NOT_STOREABLE = -1, NO_MULTIPLES = 0, SUCCESS = 1, NONE_OWNED = 2, SUCESS_REMOVED_ALL = 3, FAILED = 4;


    public int addNumberPending(PlayerWrapper wrapper, int amount, String whoPurchased, String datePurchased, String transactionId,boolean autoSave) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!

        wrapper.getPendingPurchaseablesUnlocked().add(new PendingPurchaseable(this,whoPurchased,datePurchased,amount, transactionId));
        if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
        return SUCCESS;

    }

    public int removeNumberPending(PlayerWrapper wrapper,int amount, boolean autoSave) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!

        List<PendingPurchaseable> pendingList = wrapper.getPendingPurchaseablesUnlocked();
        int currentNumberTracking = amount;
        for(int index = 0; index < pendingList.size(); index++) {
            if(currentNumberTracking <= 0) break;
            PendingPurchaseable pending = pendingList.get(index);
            if (!pending.getPurchaseables().equals(this)) continue;
            if(pending.getNumberPurchased() > currentNumberTracking) {
                pending.setNumberPurchased(pending.getNumberPurchased() - currentNumberTracking);
                currentNumberTracking = 0;
                break;
            }
                pendingList.remove(index);
                currentNumberTracking -= pending.getNumberPurchased();
        }

        if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
        if(currentNumberTracking == amount) return NONE_OWNED;
        if(currentNumberTracking > 0) return SUCESS_REMOVED_ALL;
        return SUCCESS;

    }

    public static boolean removePending(PlayerWrapper wrapper, String transactionID, boolean autoSave) {
        List<PendingPurchaseable> pendingList = wrapper.getPendingPurchaseablesUnlocked();
        for(int index = 0; index < pendingList.size(); index++) {
            PendingPurchaseable pending = pendingList.get(index);
            if(!pending.getTransactionId().equals(transactionID)) continue;
            pendingList.remove(index);
            if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
            return true;
        }

        return false;
    }

    public int addNumberUnlocked(PlayerWrapper wrapper, int amount) {
        return this.addNumberUnlocked(wrapper,amount,true);
    }

    public int addNumberUnlocked(PlayerWrapper wrapper, int amount, boolean autoSave) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!
        Integer currentNumberUnlocked = wrapper.getPurchaseablesUnlocked().get(this);
        if (currentNumberUnlocked == null) currentNumberUnlocked = 0;
        if (!this.isCanHaveMultiple() && currentNumberUnlocked >= 1) return NO_MULTIPLES;


        wrapper.getPurchaseablesUnlocked().put(this, amount + currentNumberUnlocked);
        if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
        return SUCCESS;
    }

    public int removeNumberUnlocked(PlayerWrapper wrapper, int amount) {
        return this.removeNumberUnlocked(wrapper,amount,true);
    }

    public int removeNumberUnlocked(PlayerWrapper wrapper, int amount, boolean autoSave) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!
        Integer currentNumberUnlocked = wrapper.getPurchaseablesUnlocked().get(this);
        if (currentNumberUnlocked == null) return NONE_OWNED;

        if(currentNumberUnlocked - amount <= 0) {
            wrapper.getPurchaseablesUnlocked().remove(this);
            if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
            return SUCESS_REMOVED_ALL;
        }

        wrapper.getPurchaseablesUnlocked().put(this, currentNumberUnlocked - amount);
        if(autoSave)SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
        return SUCCESS;
    }
}
