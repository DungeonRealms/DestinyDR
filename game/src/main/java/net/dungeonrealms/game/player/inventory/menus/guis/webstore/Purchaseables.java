package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 5/10/2017.
 */
@Getter
public enum Purchaseables {

    LOOT_BUFF_20("Loot Buff", "\n20% global loot buff across all\nshards for every player!", Material.DIAMOND, WebstoreCategories.GLOBAL_BUFFS, 0, true, true,false, ChatColor.AQUA),
    LOOT_BUFF_40("Loot Buff", "\n40% global loot buff across all\nshards for every player!", Material.DIAMOND, WebstoreCategories.GLOBAL_BUFFS, 9, true, true,false, ChatColor.AQUA),
    PROFESSION_BUFF_20("Profession Buff", "\n20% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, WebstoreCategories.GLOBAL_BUFFS, 4, true, true,false, ChatColor.GOLD),
    PROFESSION_BUFF_40("Profession Buff", "\n40% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, WebstoreCategories.GLOBAL_BUFFS, 13, true, true,false, ChatColor.GOLD),
    LEVEL_BUFF_20("Level Buff", "\n20% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, WebstoreCategories.GLOBAL_BUFFS, 8, true, true,false, ChatColor.GREEN),
    LEVEL_BUFF_40("Level Buff", "\n40% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, WebstoreCategories.GLOBAL_BUFFS, 17, true, true,false, ChatColor.GREEN),

    SUB("Sub Rank", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 0, false, false, false,ChatColor.GREEN),
    SUB_PLUS("Sub+ Rank", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 4, false, false,false, ChatColor.GOLD),
    SUB_PLUS_PLUS("Sub++ Rank", "\nIn-game Subscriber++ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true,true, ChatColor.GOLD),

    SUB_MONTHLY("Sub Rank (Monthly)", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),
    SUB_PLUS_MONTHLY("Sub+ Rank (Monthly)", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),

    SUB_ONE_MONTH("Sub Rank (1 Month)", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),
    SUB_PLUS_ONE_MONTH("Sub+ Rank (1 Month)", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),

    SUB_THREE_MONTH("Sub Rank (3 Month)", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),
    SUB_PLUS_THREE_MONTH("Sub+ Rank (3 Month)", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),

    SUB_SIX_MONTH("Sub Rank (6 Month)", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),
    SUB_PLUS_SIX_MONTH("Sub+ Rank (6 Month)", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),

    SUB_TWELVE_MONTH("Sub Rank (1 Year)", "\nIn-game Subscriber rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),
    SUB_PLUS_TWELVE_MONTH("Sub+ Rank (1 Year)", "\nIn-game Subscriber+ rank!", Material.EMERALD, WebstoreCategories.SUBSCRIPTIONS, 8, false, true, true, ChatColor.GOLD),

    WIZARD_HAT("Wizard Hat", "\nShow off your inner Wizard!", Material.SAPLING, WebstoreCategories.HATS, 0, false, true, false, ChatColor.WHITE, true, 4),
    CROWN("Gold Crown", "\nA shiny Crown fit for a King.", Material.SAPLING, WebstoreCategories.HATS, 1, false, true, false, ChatColor.GOLD, true, 2),
    DRAGON_MASK("Dragon Mask", "\nAn ancient Dragon Skull", Material.SKULL_ITEM, WebstoreCategories.HATS, 2, false, true, false, ChatColor.LIGHT_PURPLE, true, 5),

    COAL_ORE_HAT("Coal Ore Hat", "\nA helmet made of precious ore\n&oOnly obtainable through T1 Mining with Treasure Find!", Material.COAL_ORE, WebstoreCategories.HATS, 9, false, true, false, ChatColor.WHITE, true),
    EMERALD_ORE_HAT("Emerald Ore Hat", "\nA helmet made of precious ore\n&oOnly obtainable through T2 Mining with Treasure Find!", Material.EMERALD_ORE, WebstoreCategories.HATS, 10, false, true, false, ChatColor.GREEN, true),
    IRON_ORE_HAT("Iron Ore Hat", "\nA helmet made of precious ore\n&oOnly obtainable through T3 Mining with Treasure Find!", Material.IRON_ORE, WebstoreCategories.HATS, 11, false, true, false, ChatColor.AQUA, true),
    DIAMOND_ORE_HAT("Diamond Ore Hat", "\nA helmet made of precious ore\n&oOnly obtainable through T4 Mining with Treasure Find!", Material.DIAMOND_ORE, WebstoreCategories.HATS, 12, false, true, false, ChatColor.LIGHT_PURPLE, true),
    GOLD_ORE_HAT("Gold Ore Hat", "\nA helmet made of precious ore\n&oOnly obtainable through T5 Mining with Treasure Find!", Material.GOLD_ORE, WebstoreCategories.HATS, 13, false, true, false, ChatColor.GOLD, true),

    WOLF_PET("Wolf Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 0, false, true, false, ChatColor.GREEN, true, EntityType.WOLF.getTypeId(), EnumPets.WOLF),
    ENDERMITE_PET("Endermite Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 1, false, true, false, ChatColor.GREEN, true, EntityType.ENDERMITE.getTypeId(), EnumPets.ENDERMITE),
    CAVE_SPIDER_PET("Wolf Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 2, false, true, false, ChatColor.GREEN, true, EntityType.CAVE_SPIDER.getTypeId(), EnumPets.CAVE_SPIDER),
    BABY_ZOMBIE_PET("Baby Zombie Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 3, false, true, false, ChatColor.GREEN, true, EntityType.ZOMBIE.getTypeId(), EnumPets.BABY_ZOMBIE),
    BABY_PIG_ZOMBIE_PET("Baby Pig Zombie Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 4, false, true, false, ChatColor.GREEN, true, EntityType.PIG_ZOMBIE.getTypeId(), EnumPets.BABY_PIG_ZOMBIE),
    OCELOT_PET("Ocelot Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 5, false, true, false, ChatColor.GREEN, true, EntityType.OCELOT.getTypeId(), EnumPets.OCELOT),
    RABBIT_PET("Rabbit Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 6, false, true, false, ChatColor.GREEN, true, EntityType.RABBIT.getTypeId(), EnumPets.RABBIT),
    CHICKEN_PET("Chicken Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 7, false, true, false, ChatColor.GREEN, true, EntityType.CHICKEN.getTypeId(), EnumPets.CHICKEN),
    BAT_PET("Bat Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 8, false, true, false, ChatColor.GREEN, true, EntityType.BAT.getTypeId(), EnumPets.BAT),
    SLIME_PET("Slime Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 9, false, true, false, ChatColor.GREEN, true, EntityType.SLIME.getTypeId(), EnumPets.SLIME),
    MAGMA_CUBE_PET("Magma Cube Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 10, false, true, false, ChatColor.GREEN, true, EntityType.MAGMA_CUBE.getTypeId(), EnumPets.MAGMA_CUBE),
    ENDERMAN_PET("Enderman Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 11, false, true, false, ChatColor.GREEN, true, EntityType.ENDERMAN.getTypeId(), EnumPets.ENDERMAN),
    GUARDIAN_PET("Guardian Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 12, false, true, false, ChatColor.GREEN, true, EntityType.GUARDIAN.getTypeId(), EnumPets.GUARDIAN),
    ELDER_GUARDIAN_PET("Elder Guardian Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 13, false, true, false, ChatColor.GREEN, true, EntityType.GUARDIAN.getTypeId(), EnumPets.ELDER_GAURDIAN),
    BABY_SHEEP_PET("Baby Sheep Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 14, false, true, false, ChatColor.GREEN, true, EntityType.SHEEP.getTypeId(), EnumPets.BABY_SHEEP),
    RAINBOW_SHEEP_PET("Rainbow Sheep Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 15, false, true, false, ChatColor.GREEN, true, EntityType.SHEEP.getTypeId(), EnumPets.RAINBOW_SHEEP),
    BETA_ZOMBIE_PET("Beta Zombie Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 16, false, true, false, ChatColor.GREEN, true, EntityType.ZOMBIE.getTypeId(), EnumPets.BETA_ZOMBIE),
    SILVERFISH_PET("Silverfish Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 17, false, true, false, ChatColor.GREEN, true, EntityType.SILVERFISH.getTypeId(), EnumPets.SILVERFISH),
    SNOWMAN_PET("Snowman Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 18, false, true, false, ChatColor.GREEN, true, EntityType.SNOWMAN.getTypeId(), EnumPets.SNOWMAN),
    INDEPENDENCE_CREEPER_PET("Independence Creeper Pet", "", Material.MONSTER_EGG, WebstoreCategories.PETS, 19, false, true, false, ChatColor.GREEN, true, EntityType.CREEPER.getTypeId(), EnumPets.INDEPENDENCE_CREEPER),


    SCRAP_TAB("Scrap Tab", "\nIn-game storage for your scrap!", Material.INK_SACK, WebstoreCategories.MISCELLANEOUS, 0, false, true, true, ChatColor.GOLD),
    JUKEBOX("Mobile Music Box", "\nPlay your favorite tunes where ever you want!", Material.JUKEBOX, WebstoreCategories.MISCELLANEOUS, 5, false, true, false, ChatColor.AQUA),
    ITEM_NAME_TAG("Item Name Tag", "\nRename an item to anything you want!", Material.NAME_TAG, WebstoreCategories.MISCELLANEOUS, 3, true, true, false, ChatColor.GREEN),
    GOLDEN_CURSE("Golden Curse", "\nEverything you touch shall\nturn to gold for all.", Material.GOLD_BLOCK, WebstoreCategories.MISCELLANEOUS, 8, false, true, false, ChatColor.GOLD, true),
    DPS_DUMMY("DPS Dummy", "A squishy dummy made to take a hit!", Material.ARMOR_STAND, WebstoreCategories.MISCELLANEOUS, 4, false, true, false, ChatColor.GREEN);

    private String name;
    private String description;
    private Material itemType;
    private WebstoreCategories category;
    private int guiSlot;
    private boolean canHaveMultiple, shouldStore, isSpecialCaseClaim;
    private ChatColor displayNameColor;
    private boolean enabled;
    private int meta;
    private Object[] specialArgs;

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, boolean isSpecialCaseClaim) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore,isSpecialCaseClaim, ChatColor.WHITE);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, boolean isSpecialCaseClaim, ChatColor displayNameColor) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore,isSpecialCaseClaim, displayNameColor, true);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, boolean isSpecialCaseClaim, ChatColor displayNameColor, boolean enabled) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore,isSpecialCaseClaim, displayNameColor, enabled, 0);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, boolean isSpecialCaseClaim, ChatColor displayNameColor, boolean enabled, int meta) {
        this(name, description, itemType, category, guiSlot, hasMultiples, shouldStore,isSpecialCaseClaim, displayNameColor, enabled, meta,null);
    }

    Purchaseables(String name, String description, Material itemType, WebstoreCategories category, int guiSlot, boolean hasMultiples, boolean shouldStore, boolean isSpecialCaseClaim, ChatColor displayNameColor, boolean enabled, int meta, Object... specialArgs) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.category = category;
        this.guiSlot = guiSlot;
        this.canHaveMultiple = hasMultiples;
        this.shouldStore = shouldStore;
        this.isSpecialCaseClaim = isSpecialCaseClaim;
        this.displayNameColor = displayNameColor;
        this.enabled = enabled;
        this.meta = meta;
        this.specialArgs = specialArgs;
    }

    public List<String> getDescription(boolean showColors) {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        if (showColors) {
            for (int index = 0; index < toReturn.size(); index++) {
                String line = toReturn.get(index);
                toReturn.set(index, getDescriptionColor() + ChatColor.translateAlternateColorCodes('&', line));
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

        if (autoSave)
            SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
    }

    public static final int NOT_STOREABLE = -1, NO_MULTIPLES = 0, SUCCESS = 1, NONE_OWNED = 2, SUCESS_REMOVED_ALL = 3, FAILED = 4;


    public int addNumberPending(PlayerWrapper wrapper, int amount, String whoPurchased,String whoPurchasedEnjinID, String datePurchased, String transactionId, boolean autoSave, Consumer<Integer> callback) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!

        PendingPurchaseable pending = new PendingPurchaseable(this, whoPurchased,whoPurchasedEnjinID, datePurchased, amount, transactionId);
        wrapper.getPendingPurchaseablesUnlocked().add(pending);
        if (autoSave) {
            SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
        } else if (callback != null) {
            callback.accept(0);
        }
        return SUCCESS;

    }

    public int removeNumberPending(PlayerWrapper wrapper, int amount, boolean autoSave, Consumer<Integer> callback) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!

        List<PendingPurchaseable> pendingList = wrapper.getPendingPurchaseablesUnlocked();
        int currentNumberTracking = amount;
        for (int index = 0; index < pendingList.size(); index++) {
            if (currentNumberTracking <= 0) break;
            PendingPurchaseable pending = pendingList.get(index);
            if (!pending.getPurchaseables().equals(this)) continue;
            if (pending.getNumberPurchased() > currentNumberTracking) {
                pending.setNumberPurchased(pending.getNumberPurchased() - currentNumberTracking);
                currentNumberTracking = 0;
                break;
            }
            pendingList.remove(index);
            currentNumberTracking -= pending.getNumberPurchased();
        }

        if (autoSave) {
            SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
        } else if (callback != null) callback.accept(0);
        if (currentNumberTracking == amount) return NONE_OWNED;
        if (currentNumberTracking > 0) return SUCESS_REMOVED_ALL;
        return SUCCESS;

    }

    public static boolean removePending(PlayerWrapper wrapper, String transactionID, boolean autoSave, Consumer<Integer> callback) {
        List<PendingPurchaseable> pendingList = wrapper.getPendingPurchaseablesUnlocked();
        for (int index = 0; index < pendingList.size(); index++) {
            PendingPurchaseable pending = pendingList.get(index);
            if (!pending.getTransactionId().equals(transactionID)) continue;
            pendingList.remove(index);
            if (autoSave)
                SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
            else if (callback != null) callback.accept(0);
            return true;
        }

        return false;
    }

    public int addNumberUnlocked(PlayerWrapper wrapper, int amount, Consumer<Integer> callback) {
        return this.addNumberUnlocked(wrapper, amount, true, callback);
    }

    public int addNumberUnlocked(PlayerWrapper wrapper, int amount, boolean autoSave, Consumer<Integer> callback) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!
        Integer currentNumberUnlocked = wrapper.getPurchaseablesUnlocked().get(this);
        if (currentNumberUnlocked == null) currentNumberUnlocked = 0;
        if (!this.isCanHaveMultiple() && currentNumberUnlocked >= 1) return NO_MULTIPLES;


        wrapper.getPurchaseablesUnlocked().put(this, amount + currentNumberUnlocked);
        if (autoSave)
            SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
        else if (callback != null) callback.accept(0);
        return SUCCESS;
    }

    public int removeNumberUnlocked(PlayerWrapper wrapper, int amount, Consumer<Integer> callback) {
        return this.removeNumberUnlocked(wrapper, amount, true, callback);
    }

    public int removeNumberUnlocked(PlayerWrapper wrapper, int amount, boolean autoSave, Consumer<Integer> callback) {
        if (!this.isShouldStore()) return NOT_STOREABLE;//This item is stored and handled seperately!
        Integer currentNumberUnlocked = wrapper.getPurchaseablesUnlocked().get(this);
        if (currentNumberUnlocked == null) return NONE_OWNED;

        if (currentNumberUnlocked - amount <= 0) {
            wrapper.getPurchaseablesUnlocked().remove(this);
            if (autoSave)
                SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
            else if (callback != null) callback.accept(0);
            return SUCESS_REMOVED_ALL;
        }

        wrapper.getPurchaseablesUnlocked().put(this, currentNumberUnlocked - amount);
        if (autoSave)
            SQLDatabaseAPI.getInstance().executeUpdate(callback, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
        else if(callback != null) callback.accept(0);
        return SUCCESS;
    }
}
