package net.dungeonrealms.game.player.banks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievementMoney;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemMoney;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Sep 18, 2015
 * Gutted by Kneesnap on April 4th, 2017.
 */
public class BankMechanics {
	
    public static HashMap<UUID, Storage> storage = new HashMap<>();
    public static HashMap<String, ItemStack> shopPricing = new HashMap<>();

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
    
    public static Storage getStorage(Player player) {
    	return getStorage(player.getUniqueId());
    }
    
    public static Storage getStorage(UUID uniqueId) {
        return storage.get(uniqueId);
    }

    public static void checkBankAchievements(Player player) {
    	int bankGemAmount = PlayerWrapper.getPlayerWrapper(player).getGems();
    	for (EnumAchievementMoney ach : EnumAchievementMoney.values())
    		if (bankGemAmount >= ach.getMoneyRequirement())
    			Achievements.getInstance().giveAchievement(player.getUniqueId(), ach.getAchievement());
    }
    
    public static void upgradeStorage(UUID uniqueId) {
        storage.get(uniqueId).upgrade();
    }
}
