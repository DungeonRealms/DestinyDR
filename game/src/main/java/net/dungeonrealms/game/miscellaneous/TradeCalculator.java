package net.dungeonrealms.game.miscellaneous;


import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.item.items.functional.ItemEnchantArmor;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.item.items.functional.ItemGemPouch;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.item.items.functional.ItemScrap;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.PouchTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.world.item.Item.ItemTier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the items a merchant will give to a player.
 * 
 * Recoded by Kneesnap on April 9th, 2017.
 */
public class TradeCalculator {

    public static List<ItemStack> calculateMerchantOffer(List<ItemStack> playerOffer) {
        List<PersistentItem> merchantOffer = new ArrayList<>();
        List<ItemStack> remove = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        
        
        List<PotionItem> potions = new ArrayList<>();
        Map<ScrapTier, Integer> scraps = new HashMap<>();
        Map<MiningTier, Integer> ore = new HashMap<>();
        
        for (ScrapTier t : ScrapTier.values())
        	scraps.put(t, 0);
        
        for (MiningTier t : MiningTier.values())
        	ore.put(t, 0);
        
        for (ItemStack is : playerOffer) {
        	if (is == null || is.getType() == Material.AIR || !ItemManager.isItemTradeable(is))
        		continue;
        	
        	boolean removeItem = false; //Whether this item should be removed from the player's inventory.
        	
        	//  POTION  //
        	if (PotionItem.isPotion(is)) {
        		PotionItem potion = new PotionItem(is);
        		if (potion.getTier() == PotionTier.TIER_5)
        			continue;
        		removeItem = true;
        		potions.add(potion);
        	}
        	
        	//  ORE  //
        	MiningTier oreTier = MiningTier.getTierFromOre(is.getType());
        	if (oreTier != null) {
        		removeItem = true;
        		ore.put(oreTier, ore.get(oreTier) + is.getAmount());
        	}
        	
        	//  SCRAP  //
        	if (ItemScrap.isScrap(is)) {
        		removeItem = true;
        		ItemScrap scrap = new ItemScrap(is);
        		scraps.put(scrap.getTier(), scraps.get(scrap.getTier()) + is.getAmount());
        	}
        	
        	//  GEAR  //
        	if (CombatItem.isCombatItem(is)) {
        		CombatItem ci = (CombatItem)PersistentItem.constructItem(is);
        		removeItem = true;
        		int scrap = ci.getGeneratedItemType().getMerchantScraps();
        		ScrapTier scrapTier = ScrapTier.getScrapTier(ci.getTier().getId());
        		
        		// Generate Scrap
        		ItemStack item = new ItemScrap(scrapTier.getNext()).generateItem();
        		item.setAmount(scrapTier == ScrapTier.TIER5 ? 2 * scrap : scrap);
        		items.add(item);
        	}
        	
        	//  ORB OF ALTERATION  //
        	if (ItemOrb.isOrb(is)) {
        		removeItem = true;
        		ItemStack scrap = new ItemScrap(ScrapTier.TIER5).generateItem();
        		scrap.setAmount(20);
        		items.add(scrap);
        	}
        	
        	//  PROFESSION ITEM  //
        	if (ProfessionItem.isProfessionItem(is)) {
        		removeItem = true;
        		ProfessionItem pi = (ProfessionItem)PersistentItem.constructItem(is);
        		merchantOffer.add(pi.getEnchant());
        	}
        	
        	if (removeItem)
        		remove.add(is);
        }
        
        remove.forEach(playerOffer::remove);
        
        //  POTION TRADES  //
        for(boolean splash : new boolean[] {true, false}) {
        	for (PotionTier tier : PotionTier.values()) {
        		if (tier == PotionTier.TIER_5)
        			continue;
        		
        		int potsNeeded = 7 - tier.getId();
        		int totalPots = (int) potions.stream().filter(p -> p.isSplash() == splash && p.getTier() == tier).count();
        		
        		while (totalPots >= potsNeeded) {
        			totalPots -= potsNeeded;
        			merchantOffer.add(new PotionItem(PotionTier.getById(tier.getId() + 1)).setSplash(splash));
        		}
        		
        		for (int i = 0; i < totalPots; i++)
        			merchantOffer.add(new PotionItem(tier).setSplash(splash));
        	}
        }
        
        //  ORE TRADES  //
        for (MiningTier oreTier : MiningTier.values()) {
        	int currentOre = ore.get(oreTier);
        	
        	for (int i = 0; i < oreTier.getPouchCosts().length; i++) {
        		int pouchCost = oreTier.getPouchCosts()[i];
        		PouchTier t = PouchTier.getById(i + 1);
        		
        		while (currentOre >= pouchCost) {
        			currentOre -= pouchCost;
        			merchantOffer.add(new ItemGemPouch(t));
        		}
        		
        		ItemStack scrap = new ItemScrap(ScrapTier.getScrapTier(Math.max(2, t.getId() - 1))).generateItem();
        		int giveScrap = (int) (currentOre * Math.max(0.5D, Math.pow(2, -t.getId() + 2)));
        		while (giveScrap > 0) {
        			int sub = Math.min(currentOre, 64);
        			currentOre -= sub;
        			ItemStack add = scrap.clone();
        			add.setAmount(sub);
        			items.add(add);
        		}
        	}
        }
        
        //  SCRAP TRADES  //
        for (ScrapTier tier : ScrapTier.values()) {
        	int scrapAmt = scraps.get(tier);
        	ItemTier currTier = ItemTier.getByTier(tier.getTier());
        	
        	while (scrapAmt > tier.getWepEnchPrice() && tier.getWepEnchPrice() >= 0) {
        		scrapAmt -= tier.getWepEnchPrice();
        		merchantOffer.add(new ItemEnchantWeapon(currTier));
        	}
        	
        	while (scrapAmt > tier.getArmEnchPrice() && tier.getArmEnchPrice() >= 0) {
        		scrapAmt -= tier.getArmEnchPrice();
        		merchantOffer.add(new ItemEnchantArmor(currTier));
        	}
        	
        	while (scrapAmt > tier.getOrbPrice() && tier.getOrbPrice() >= 0) {
        		scrapAmt -= tier.getOrbPrice();
        		merchantOffer.add(new ItemOrb());
        	}
        	
        	ItemStack scrap = new ItemScrap(tier.downgrade()).generateItem();
        	double mult = (tier == ScrapTier.TIER1 ? 0.5D : (tier == ScrapTier.TIER5 ? 3D : 2D));
        	int leftOver = (int) (scrapAmt * mult);
        	while (leftOver > 0) {
        		int sub = Math.min(leftOver, 64);
        		leftOver -= sub;
        		ItemStack add = scrap.clone();
        		add.setAmount(sub);
        		items.add(add);
        	}
        }
        
        merchantOffer.forEach(pi -> items.add(pi.generateItem()));
        return items;
    }
}