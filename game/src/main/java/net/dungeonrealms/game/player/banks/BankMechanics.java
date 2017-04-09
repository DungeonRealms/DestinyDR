package net.dungeonrealms.game.player.banks;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievementMoney;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemMoney;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Chase on Sep 18, 2015
 * Gutted by Kneesnap on April 4th, 2017.
 */
public class BankMechanics {
	
    public static HashMap<UUID, Storage> storage = new HashMap<>();

    public static HashMap<String, ItemStack> shopPricing = new HashMap<>();

    private static Map<UUID, CurrencyTab> currencyTab = new HashMap<>();
    
    /**
     * Gets a player's scrap tab.
     */
    public static CurrencyTab getCurrencyTab(UUID uuid) {
    	return currencyTab.get(uuid);
    }
    
    /**
     * Gets the amount of gems the player has accumulated in their inventory.
     */
    public static int getGemsInInventory(Player p) {
    	int gemCount = 0;
    	for (ItemStack i : p.getInventory().getContents())
    		if (ItemMoney.isMoney(i))
    			gemCount += ((ItemMoney)PersistentItem.constructItem(i)).getGemValue();
    	return gemCount;
    }
    
    /**
     * Adds a given amount of raw gems to a player's inventory.
     */
    public static void givePlayerRawGems(Player player, int gems) {
    	while (gems > 0) {
    		int give = Math.min(64, gems);
    		ItemGem gem = new ItemGem(give);
    		GameAPI.giveOrDropItem(player, gem.generateItem());
    		gems -= give;
        }
    }
    
    /**
     * Removes a given amount of gems from a player's inventory.
     * If there are not enough gems, it does not remove any and returns false.
     * Otherwise it returns true.
     */
    public static boolean takeGemsFromInventory(Player p, int cost) {
    	// Don't take any if we don't have enough.
    	if (getGemsInInventory(p) < cost)
    		return false;
    	
    	Inventory inv = p.getInventory();
    	int payLeft = cost;
    	
    	for(int i = 0; i < inv.getContents().length; i++) {
    		if (payLeft <= 0)
    			break;
    		
    		ItemStack item = inv.getItem(i);
    		if (!ItemMoney.isMoney(item))
    			continue;
    		ItemMoney money = (ItemMoney)PersistentItem.constructItem(item);
    		
    		//  REMOVE GEMS  //
    		int removeGems = Math.min(money.getGemValue(), payLeft);
    		money.setGemValue(money.getGemValue() - removeGems);
    		inv.setItem(i, money.generateItem());
    		payLeft -= removeGems;
    	}
    	
    	return true;
    }

    /**
     * Add gems to player database
     */
    public static void addGemsToPlayerBank(UUID uuid, int num) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.GEMS, num, true);
        checkBankAchievements(uuid);
    }

    public static Storage getStorage(UUID uniqueId) {
        return storage.get(uniqueId);
    }

    public static void checkBankAchievements(UUID uuid) {
    	int bankGemAmount = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
    	for (EnumAchievementMoney ach : EnumAchievementMoney.values())
    		if (bankGemAmount >= ach.getMoneyRequirement())
    			Achievements.getInstance().giveAchievement(uuid, ach.getAchievement());
    }

    public static void upgradeStorage(UUID uniqueId) {
        storage.get(uniqueId).upgrade();
    }

	public static void setCurrencyTab(UUID uniqueId, CurrencyTab tab) {
		currencyTab.put(uniqueId, tab);
	}
}
