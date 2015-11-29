package net.dungeonrealms.listeners;

import ca.thederpygolems.armorequip.ArmorEquipEvent;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelOffer;
import net.dungeonrealms.duel.DuelingMechanics;
import net.dungeonrealms.enchantments.EnchantmentAPI;
import net.dungeonrealms.handlers.ClickHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.Item.AttributeType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor.ArmorAttributeType;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.stats.PlayerStats;
import net.dungeonrealms.stats.StatsManager;
import net.dungeonrealms.trade.Trade;
import net.dungeonrealms.world.glyph.Glyph;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Nick on 9/18/2015.
 */
public class InventoryListener implements Listener {

    /**
     * Handles important inventories (guilds, etc.)
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onImportantInventoryClick(InventoryClickEvent event) {

        if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR) && event.getCursor() != null && !event.getCursor().getType().equals(Material.AIR)) {
            if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
            Glyph.getInstance().applyGlyph(((Player)event.getWhoClicked()), event, event.getCursor(), event.getCurrentItem());
        }

        ClickHandler.getInstance().doClick(event);
    }

    /**
     * Disables the clicking of items that contain NBTTag `important` in `type`.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getCurrentItem());
        if (nmsItem == null)
            return;
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelOfferClick(InventoryClickEvent e) {
    	if(!e.getInventory().getTitle().contains("VS.")) return;
		if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR){
    		e.setCancelled(true);
    		return;
    	}
    	Player p = (Player) e.getWhoClicked();
    	DuelOffer offer = DuelingMechanics.getOffer(p.getUniqueId());
    	if(offer == null){ p.closeInventory(); return;}
    	if(e.getRawSlot() > offer.sharedInventory.getSize()) return;
    	
    	if(e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BONE){
    		e.setCancelled(true);
    		return;
    	}
		int slot = e.getRawSlot();
		if (slot == 30) {
          e.setCancelled(true);
  		  offer.updateOffer();
          offer.cycleArmor();
          return;
		} else if (slot == 32) {
          e.setCancelled(true);
  		  offer.updateOffer();
          offer.cycleItem();
          return;
      	}
    	
       	if(offer.isLeftSlot(e.getRawSlot())){
    		if(!offer.isLeftPlayer(p)){
    			e.setCancelled(true);
    			return;
    		}
          }else{
        	  if(offer.isLeftPlayer(p)){
    			e.setCancelled(true);
    			return;
        	  }
    	}
       	
    	if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
    		return;
		ItemStack stackClicked = e.getCurrentItem();
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
		if(nms.hasTag() && nms.getTag().hasKey("status")){
			String status = nms.getTag().getString("status");
			e.setCancelled(true);
			if(status.equalsIgnoreCase("ready")){
				offer.updateReady(p.getUniqueId());
				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "NOT READY",
				        null, DyeColor.GRAY.getDyeData());
				nms = CraftItemStack.asNMSCopy(item);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("status", "notready");
				nms.setTag(nbt);
				nms.c(ChatColor.YELLOW + "NOT READY");
				e.getInventory().setItem(e.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
				offer.checkReady();
				return;
			}else{
				offer.updateReady(p.getUniqueId());
				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY",
				        null, DyeColor.LIME.getDyeData());
				nms = CraftItemStack.asNMSCopy(item);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("status", "ready");
				nms.setTag(nbt);
				nms.c(ChatColor.YELLOW + "READY");
				e.getInventory().setItem(e.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
				offer.checkReady();
				return;
			}
		}
		offer.updateOffer();
    }
    

    /**
     * @param event
     * @since 1.0 Dragging is naughty.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragItemInDuelWager(InventoryDragEvent event) {
        if (event.getInventory().getTitle().contains("VS.") || event.getInventory().getTitle().contains("Bank")
                || event.getInventory().getTitle().contains("@") || event.getInventory().getTitle().contains("Trade"))
            event.setCancelled(true);
    }

    /** Called when a player switches item
     *
     * @param event
     * @since 1.0
     */
    /*@EventHandler(priority = EventPriority.LOWEST)
    public void playerSwitchItem(PlayerItemHeldEvent event) {
		if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		int slot = event.getNewSlot();
		if (event.getPlayer().getInventory().getItem(slot) != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getPlayer().getInventory().getItem(slot));
			if (nms.hasTag()) {
				if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon")) {
					ItemTier tier = new Attribute(event.getPlayer().getInventory().getItem(slot)).getItemTier();
					int minLevel = tier.getRangeValues()[0];
					Player p = event.getPlayer();
					int pLevel = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, p.getUniqueId());
					if (pLevel < minLevel) {
						p.sendMessage(ChatColor.RED + "You must be level " + ChatColor.YELLOW.toString() + minLevel
								+ ChatColor.RED.toString() + " to wield this weapon!");
						event.setCancelled(true);
					}
				}
			}
		}
	}*/

    /**
     * Called when a player equips armor
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerEquipArmor(ArmorEquipEvent event) {
        //TODO: Chase do this to prevent all forms of putting on armor if they are not the correct level.
        if (!CombatLog.isInCombat(event.getPlayer())) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 1f);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                HealthHandler.getInstance().setPlayerMaxHPLive(event.getPlayer(), HealthHandler.getInstance().calculateMaxHPFromItems(event.getPlayer()));
                HealthHandler.getInstance().setPlayerHPRegenLive(event.getPlayer(), HealthHandler.getInstance().calculateHealthRegenFromItems(event.getPlayer()));
                if (HealthHandler.getInstance().getPlayerHPLive(event.getPlayer()) > HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer())) {
                    HealthHandler.getInstance().setPlayerHPLive(event.getPlayer(), HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer()));
                }
            }, 10L);
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "Equipping armor while in combat will not change your stats! Please re-equip out of combat!");
			if (!HealthHandler.COMBAT_ARMORSWITCH.contains(event.getPlayer())) {
				HealthHandler.COMBAT_ARMORSWITCH.add(event.getPlayer());
			}
        }
    }

    /**
     * @param event
     * @since 1.0 Closes both players wager inventory.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClosed(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().contains("VS.")) {
            Player p = (Player) event.getPlayer();
            DuelOffer offer = DuelingMechanics.getOffer(p.getUniqueId());
            if(offer == null) return;
            if(!offer.p1Ready || !offer.p2Ready){
            	offer.giveBackItems();
            	DuelingMechanics.removeOffer(offer);
            	Player p1 =Bukkit.getPlayer(offer.player1);
            	if(p1 != null)
            		p1.closeInventory();
            	Player p2 = Bukkit.getPlayer(offer.player2);
            	if(p2 != null)
            		p2.closeInventory();
            }
        } else if (event.getInventory().getTitle().contains("Storage Chest")) {
            Storage storage = BankMechanics.getInstance().getStorage(event.getPlayer().getUniqueId());
            storage.inv.setContents(event.getInventory().getContents());
        } else if (event.getInventory().getTitle().contains("Loot")) {
            Player p = (Player) event.getPlayer();
            Block block = p.getTargetBlock((Set<Material>) null, 100);
            LootManager.LOOT_SPAWNERS.stream().filter(loot -> loot.location.equals(block.getLocation())).forEach(net.dungeonrealms.loot.LootSpawner::update);
        } else if (event.getInventory().getTitle().contains("Trade Window")) {
            Player p = (Player) event.getPlayer();
            Trade t = net.dungeonrealms.trade.TradeManager.getTrade(p.getUniqueId());
            if(t != null)
            if (!t.p1Ready || !t.p2Ready) {
                t.handleClose();
            }
        } else if (event.getInventory().getTitle().contains("Stat Points")) {
            PlayerStats stat = API.getGamePlayer((Player) event.getPlayer()).getStats();
            if (stat.reset) {
                stat.resetTemp();
            }
            stat.reset = true;
        }else if(event.getInventory().getTitle().contains("Collection Bin")){
        	
        }
    }

    /**
     * @param event
     * @since 1.0 handles Trading inventory items.
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTradeInvClicked(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("Trade Window")) {
    		if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR){
    			event.setCancelled(true);
        		return;
        	}
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            Trade trade = net.dungeonrealms.trade.TradeManager.getTrade(event.getWhoClicked().getUniqueId());
            if(trade == null){
            	return;
            }
            int slot = event.getRawSlot();
            if (slot >= 36)
                return;
            
            if(event.getCurrentItem() == null)
            	return;
            if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }
            
            if(trade.isLeftSlot(slot)){
            	if(!trade.isLeftPlayer(event.getWhoClicked().getUniqueId())){
            		event.setCancelled(true);
            		return;
            	}
            }else if(trade.isRightSlot(slot)){
            	if(trade.isLeftPlayer(event.getWhoClicked().getUniqueId())){
            		event.setCancelled(true);
            		return;
            	}
            }
            
        	if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
        		return;
    		ItemStack stackClicked = event.getCurrentItem();
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
    		if(nms.hasTag() && nms.getTag().hasKey("status")){
    			String status = nms.getTag().getString("status");
    			event.setCancelled(true);
    			if(status.equalsIgnoreCase("ready")){
    				trade.updateReady(event.getWhoClicked().getUniqueId());
    				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "NOT READY",
    				        null, DyeColor.GRAY.getDyeData());
    				nms = CraftItemStack.asNMSCopy(item);
    				NBTTagCompound nbt = new NBTTagCompound();
    				nbt.setString("status", "notready");
    				nms.setTag(nbt);
    				nms.c(ChatColor.YELLOW + "NOT READY");
    				event.getInventory().setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
    				trade.checkReady();
    				return;
    			}else{
    				trade.updateReady(event.getWhoClicked().getUniqueId());
    				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY",
    				        null, DyeColor.LIME.getDyeData());
    				nms = CraftItemStack.asNMSCopy(item);
    				NBTTagCompound nbt = new NBTTagCompound();
    				nbt.setString("status", "ready");
    				nms.setTag(nbt);
    				nms.c(ChatColor.YELLOW + "READY");
    				event.getInventory().setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
    				trade.checkReady();
    				return;
    			}
    		}
            trade.changeReady();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseOrbs(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_8_R3.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        if(cursorItem.getType() != Material.MAGMA_CREAM || !nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || nmsCursor.getTag().hasKey("type") && !nmsCursor.getTag().getString("type").equalsIgnoreCase("orb"))return;
        ItemStack slotItem = event.getCurrentItem();
        if(!API.isWeapon(slotItem)  && !API.isArmor(slotItem)) return;
        if(slotItem == null || slotItem.getType() == Material.AIR) return;
        event.setCancelled(true);
        if(cursorItem.getAmount() == 1){
        	event.setCursor(new ItemStack(Material.AIR));
        }else{
        	ItemStack newStack = cursorItem.clone();
        	newStack.setAmount(newStack.getAmount() - 1);
        	event.setCursor(newStack);
        }
        event.getCurrentItem().setType(Material.AIR);
        event.getInventory().addItem(new ItemGenerator().reRoll(slotItem));
    }
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseEnchant(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_8_R3.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        if(cursorItem.getType() != Material.EMPTY_MAP || !nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type"))return;
        ItemStack slotItem = event.getCurrentItem();
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(slotItem);
        if(!API.isWeapon(slotItem)  && !API.isArmor(slotItem)) return;
        event.setCancelled(true);
        
        
        if(nmsCursor.getTag().getString("type").equalsIgnoreCase("protection")){
        	if(!EnchantmentAPI.isItemProtected(slotItem)){
            	int tier = nmsCursor.getTag().getInt("tier");
            	int itemTier = 1;
            	if(nmsItem.getTag().hasKey("armorTier")){
            		itemTier = nmsItem.getTag().getInt("armorTier");
            	}else{
            		itemTier = nmsItem.getTag().getInt("itemTier");
            	}
        		if(tier > itemTier){
        			event.getWhoClicked().sendMessage(ChatColor.RED + "This protection scroll is made for a higher tier!");
        			return;
        		}
        		
        		event.setCurrentItem(EnchantmentAPI.addItemProtection(event.getCurrentItem()));
        		  if(cursorItem.getAmount() == 1){
                  	event.setCursor(new ItemStack(Material.AIR));
                  }else{
                  	ItemStack newStack = cursorItem.clone();
                  	newStack.setAmount(newStack.getAmount() - 1);
                  	event.setCursor(newStack);
                  }
        	}
        	return;
        }
        
        if(API.isWeapon(slotItem)){
        	if(!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("weaponenchant")){
        		return;
        	}
        	
        	int tier = nmsCursor.getTag().getInt("tier");
        	if(tier != new Attribute(slotItem).getItemTier().getTierId()){
        		event.getWhoClicked().sendMessage(ChatColor.RED + "You can not use that enchant scroll on this weapon.");
        		return;
        	}
        	
        int amount = 0;
        if(nmsItem.getTag().hasKey("enchant")){
        	amount = nmsItem.getTag().getInt("enchant");
        }

        boolean failed = false;
        if(amount < 3){
        	failed = false;
        }else{
			if(amount >= 12) {
				event.getWhoClicked().sendMessage(ChatColor.RED + "This item is already enchanted +12, cannot apply more stats.");
				event.setCancelled(true);
				((Player)event.getWhoClicked()).updateInventory();
				return;
			}
			int win_chance = new Random().nextInt(100);
			int fail = 0;
			if(amount >= 3) {
    			switch(amount){
    			case 3:
    				fail = 30;
    				break;
    			case 4:
    				fail = 40;
    				break;
    			case 5:
    				fail = 50;
    				break;
    			case 6:
    				fail = 65;
    				break;
    			case 7:
    				fail = 75;
    				break;
    			case 8:
    				fail = 80;
    				break;
    			case 9:
    				fail = 85;
    				break;
    			case 10:
    				fail = 90;
    				break;
    			case 11:
    				fail = 95;
    				break;
    			}
				if(win_chance < fail) {
					failed = true;
					// Fail.
				} else if(win_chance >= fail) {
					failed = false;
				}
			}
        }
        if(failed){
        	event.setCancelled(true);
        	
        	if(EnchantmentAPI.isItemProtected(slotItem)){
                event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your protection scroll saved your item from vanishing");
                event.setCurrentItem(EnchantmentAPI.removeItemProtection(event.getCurrentItem()));
        		return;
        	}
            if(cursorItem.getAmount() == 1){
            	event.setCursor(new ItemStack(Material.AIR));
            }else{
            	ItemStack newStack = cursorItem.clone();
            	newStack.setAmount(newStack.getAmount() - 1);
            	event.setCursor(newStack);
            }
            event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your item VANISHED");
            event.setCurrentItem(new ItemStack(Material.AIR));
        	return;
        }
     	
    	if(EnchantmentAPI.isItemProtected(slotItem)){
            event.setCurrentItem(EnchantmentAPI.removeItemProtection(event.getCurrentItem()));
    		return;
    	}
        ItemMeta meta2 = slotItem.getItemMeta();
        String itemName = meta2.getDisplayName();
        ArrayList<String> lore = (ArrayList<String>) meta2.getLore();
    	
        String newName = "";
        if(amount == 0){
            newName = itemName;
        }else{
        	newName = itemName.substring((itemName.lastIndexOf("]") + 2), itemName.length());
        }
        
    	String finalName = ChatColor.RED + "[" + "+" + (amount+1) + "] " + newName;
        double  doublenewDamage = nmsItem.getTag().getInt("damage") + ((5 * nmsItem.getTag().getInt("damage")) / 100);
        int finalDmg = (int) Math.round(doublenewDamage);
        Attribute att = new Attribute(slotItem);
        List<String> itemLore = new ArrayList<>();
        itemLore.add(ItemGenerator.setCorrectItemLore(AttributeType.DAMAGE, finalDmg, att.getItemTier().getTierId()));
        
        for(String current : lore){
        	if(!current.startsWith(ChatColor.WHITE + "Damage:"))
        		itemLore.add(current);
        }
        nmsItem.getTag().setInt("enchant", amount + 1);
        nmsItem.getTag().setInt("damage", finalDmg);
        ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);
        
        
        
        ItemMeta meta = newItem.getItemMeta();
        meta.setDisplayName(finalName);
        meta.setLore(itemLore);
        newItem.setItemMeta(meta);
        
        if(cursorItem.getAmount() == 1){
        	event.setCursor(new ItemStack(Material.AIR));
        }else{
        	ItemStack newStack = cursorItem.clone();
        	newStack.setAmount(newStack.getAmount() - 1);
        	event.setCursor(newStack);
        }
        event.getCurrentItem().setType(Material.AIR);
        event.setCurrentItem(new ItemStack(Material.AIR));
        if((amount + 1) > 3)
        	EnchantmentAPI.addGlow(newItem);
        event.getWhoClicked().getInventory().addItem(newItem);
        ((Player)event.getWhoClicked()).updateInventory();
        }else if(API.isArmor(slotItem)){
        	
        	
        	
        	if(!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("armorenchant")){
        		return;
        	}
        	
        	
        	int tier = nmsCursor.getTag().getInt("tier");
        	int armorTier = nmsItem.getTag().getInt("armorTier");
        	if(tier > armorTier){
        		event.getWhoClicked().sendMessage(ChatColor.RED + "You can not use that enchant scroll on this armor.");
        		return;
        	}
        	
        int amount = 0;
        if(nmsItem.getTag().hasKey("enchant")){
        	amount = nmsItem.getTag().getInt("enchant");
        }

        boolean failed = false;
        if(amount < 3){
        	failed = false;
        }else{
			if(amount >= 12) {
				event.getWhoClicked().sendMessage(ChatColor.RED + "This item is already enchanted +12, cannot apply more stats.");
				event.setCancelled(true);
				((Player)event.getWhoClicked()).updateInventory();
				return;
			}
			int win_chance = new Random().nextInt(100);
			int fail = 0;
			if(amount >= 3) {
    			switch(amount){
    			case 3:
    				fail = 30;
    				break;
    			case 4:
    				fail = 40;
    				break;
    			case 5:
    				fail = 50;
    				break;
    			case 6:
    				fail = 65;
    				break;
    			case 7:
    				fail = 75;
    				break;
    			case 8:
    				fail = 80;
    				break;
    			case 9:
    				fail = 85;
    				break;
    			case 10:
    				fail = 90;
    				break;
    			case 11:
    				fail = 95;
    				break;
    			}
				if(win_chance < fail) {
					failed = true;
					// Fail.
				} else if(win_chance >= fail) {
					failed = false;
				}
			}
        }
        if(failed){
        	
        	if(EnchantmentAPI.isItemProtected(slotItem)){
                event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your protection scroll saved your item from vanishing");
                event.setCurrentItem(EnchantmentAPI.removeItemProtection(event.getCurrentItem()));
        		return;
        	}
        	
        	event.setCancelled(true);
            if(cursorItem.getAmount() == 1){
            	event.setCursor(new ItemStack(Material.AIR));
            }else{
            	ItemStack newStack = cursorItem.clone();
            	newStack.setAmount(newStack.getAmount() - 1);
            	event.setCursor(newStack);
            }
            event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your item VANISHED");
            event.setCurrentItem(new ItemStack(Material.AIR));
        	return;
        }

        ItemMeta meta2 = slotItem.getItemMeta();
        String itemName = meta2.getDisplayName();
        String newName = "";
        if(amount == 0){
            newName = itemName;
        }else{
        	newName = itemName.substring((itemName.lastIndexOf("]") + 2), itemName.length());
        }
        
    	String finalName = ChatColor.RED + "[" + "+" + (amount+1) + "] " + newName;
        List<String> itemLore = new ArrayList<>();
        
        double hpDouble = nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_POINTS.getNBTName()) + ((nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_POINTS.getNBTName()) * 5) / 100);
        int newHP = (int) Math.round((hpDouble));
		itemLore.add(ArmorGenerator.setCorrectArmorLore(ArmorAttributeType.HEALTH_POINTS, newHP));
        nmsItem.getTag().setInt(ArmorAttributeType.HEALTH_POINTS.getNBTName(), newHP);

		if(nmsItem.getTag().hasKey(ArmorAttributeType.HEALTH_REGEN.getNBTName())){
            double hpRegenDouble = nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_REGEN.getNBTName()) + ((nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_REGEN.getNBTName()) * 5) / 100);
            int newHPRegen = (int) Math.round((hpRegenDouble));
            nmsItem.getTag().setInt(ArmorAttributeType.HEALTH_REGEN.getNBTName(), newHPRegen);
			itemLore.add(ArmorGenerator.setCorrectArmorLore(ArmorAttributeType.HEALTH_REGEN, newHPRegen));

		}else if(nmsItem.getTag().hasKey(ArmorAttributeType.ENERGY_REGEN.getNBTName())){
            double energyRegen = nmsItem.getTag().getInt(ArmorAttributeType.ENERGY_REGEN.getNBTName());
            int newEnergyRegen = (int) Math.round((energyRegen)) + 1;
            nmsItem.getTag().setInt(ArmorAttributeType.ENERGY_REGEN.getNBTName(), newEnergyRegen);
			itemLore.add(ArmorGenerator.setCorrectArmorLore(ArmorAttributeType.ENERGY_REGEN, newEnergyRegen));
		}
		
        ArrayList<String> lore = (ArrayList<String>) meta2.getLore();
        for(String current : lore){
        	if(current.contains("Health Points") || current.contains("Health Regen") || current.contains("Energy Regen"))
        		continue;
        		itemLore.add(current);
        }
        
    	nmsItem.getTag().setInt("enchant", amount + 1);
        ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);
        
        
        
        ItemMeta meta = newItem.getItemMeta();
        meta.setDisplayName(finalName);
        meta.setLore(itemLore);
        newItem.setItemMeta(meta);
        
        if(cursorItem.getAmount() == 1){
        	event.setCursor(new ItemStack(Material.AIR));
        }else{
        	ItemStack newStack = cursorItem.clone();
        	newStack.setAmount(newStack.getAmount() - 1);
        	event.setCursor(newStack);
        }
        event.getCurrentItem().setType(Material.AIR);
        event.setCurrentItem(new ItemStack(Material.AIR));
        if((amount + 1) >= 3)
        	EnchantmentAPI.addGlow(newItem);
        event.getWhoClicked().getInventory().addItem(newItem);
        ((Player)event.getWhoClicked()).updateInventory();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAddToGemPouch(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        if(event.getCursor().getType() != Material.EMERALD || event.getCurrentItem().getType() != Material.INK_SACK) return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_8_R3.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        ItemStack slotItem = event.getCurrentItem();
        net.minecraft.server.v1_8_R3.ItemStack nmsSlot = CraftItemStack.asNMSCopy(slotItem);
        Player player = (Player) event.getWhoClicked();
        if(!nmsSlot.hasTag() || !nmsCursor.hasTag())return;
        if(!nmsSlot.getTag().hasKey("type") || !nmsSlot.getTag().getString("type").equalsIgnoreCase("money"))return;
        if(!nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("money")) return;
        
        int amount = cursorItem.getAmount();
        int pouchAmount = nmsSlot.getTag().getInt("worth");
        int tier = nmsSlot.getTag().getInt("tier");
        int pouchMax = BankMechanics.getInstance().getPouchMax(tier);
        
        if(pouchAmount < pouchMax){
        	player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
        	if(pouchAmount + amount >= pouchMax){
        		amount = pouchMax - pouchAmount;
        		event.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, pouchMax));
        		event.setCursor(BankMechanics.getInstance().createGems(amount));
        	}else{
        		event.setCursor(null);
        		event.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, pouchAmount + amount));
        	}
        }else{
        	player.sendMessage(ChatColor.RED + "That gem pouch is full!");
        }
        
        
    }
    
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseScrapItem(InventoryClickEvent event) {
        if (event.getCursor() == null) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        if(slotItem == null || slotItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();
        if(!RepairAPI.isItemArmorScrap(cursorItem))return;
        if (!RepairAPI.canItemBeRepaired(slotItem)) return;
        if (!(RepairAPI.isItemArmorOrWeapon(slotItem)) &&
        		!Mining.isDRPickaxe(slotItem) &&
        		!Fishing.isDRFishingPole(slotItem)) return;
                int scrapTier = RepairAPI.getScrapTier(cursorItem);
                int slotTier = 0;
                if(Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)){
            		slotTier = Mining.getPickTier(slotItem);
            		Utils.log.info(slotTier + " != " + scrapTier);
                    if (scrapTier != slotTier) return;
                    if (cursorItem.getAmount() == 1) {
                        event.setCancelled(true);
                        event.setCursor(new ItemStack(Material.AIR));
                    } else if (cursorItem.getAmount() > 1) {
                        event.setCancelled(true);
                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        event.setCursor(cursorItem);
                    }
                    double itemDurability = RepairAPI.getCustomDurability(slotItem);

                    if (itemDurability + 45.0D >= 1500.0D) {
                        RepairAPI.setCustomItemDurability(slotItem, 1500);
                        player.updateInventory();
                    } else if (itemDurability + 45.0D < 1500.0D) {
                        RepairAPI.setCustomItemDurability(slotItem, (itemDurability + 45.0D));
                        player.updateInventory();
                    }
                    player.updateInventory();
                    double newPercent = RepairAPI.getCustomDurability(slotItem);

                    int particleID = 1;
                    switch (scrapTier) {
                        case 1:
                            particleID = 25;
                            break;
                        case 2:
                            particleID = 30;
                            break;
                        case 3:
                            particleID = 42;
                            break;
                        case 4:
                            particleID = 57;
                            break;
                        case 5:
                            particleID = 41;
                            break;
                    }
                    if (slotItem.getType() == Material.BOW) {
                        particleID = 5;
                    }
                    
                    Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
                    ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                        player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + newPercent + "/1500");
                    }
                    return;
                }
                
                if(RepairAPI.isItemArmorOrWeapon(slotItem)){
                slotTier = RepairAPI.getArmorOrWeaponTier(slotItem);
                if (scrapTier != slotTier) return;
                if (slotItem.getDurability() == 0) return;
                if (cursorItem.getAmount() == 1) {
                    event.setCancelled(true);
                    event.setCursor(new ItemStack(Material.AIR));
                } else if (cursorItem.getAmount() > 1) {
                    event.setCancelled(true);
                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    event.setCursor(cursorItem);
                }

                double itemDurability = RepairAPI.getCustomDurability(slotItem);

                if (itemDurability + 45.0D >= 1500.0D) {
                    RepairAPI.setCustomItemDurability(slotItem, 1500);
                    player.updateInventory();
                } else if (itemDurability + 45.0D < 1500.0D) {
                    RepairAPI.setCustomItemDurability(slotItem, (itemDurability + 45.0D));
                    player.updateInventory();
                }
                player.updateInventory();
                double newPercent = RepairAPI.getCustomDurability(slotItem);

                int particleID = 1;
                switch (scrapTier) {
                    case 1:
                        particleID = 25;
                        break;
                    case 2:
                        particleID = 30;
                        break;
                    case 3:
                        particleID = 42;
                        break;
                    case 4:
                        particleID = 57;
                        break;
                    case 5:
                        particleID = 41;
                        break;
                }
                if (slotItem.getType() == Material.BOW) {
                    particleID = 5;
                }
                Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
                ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                    player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + newPercent + "/1500");
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerClickStatsInventory(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("Stat Points")) {
            //Stat Points Inv
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                ItemStack clicked = event.getCurrentItem();
                Player p = (Player) event.getWhoClicked();
                PlayerStats stats = StatsManager.getPlayerStats(p);
                int slot = event.getRawSlot();
                Inventory inv = event.getInventory();
                switch (slot) {
                    case 2:
                        //Strength
                        if (event.isRightClick())
                            stats.removePoint("str", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("str", p, inv);
                        break;
                    case 3:
                        //Dexterity
                        if (event.isRightClick())
                            stats.removePoint("dex", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("dex", p, inv);
                        break;
                    case 4:
                        //Intellect
                        if (event.isRightClick())
                            stats.removePoint("int", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("int", p, inv);
                        break;
                    case 5:
                        //Vitality
                        if (event.isRightClick())
                            stats.removePoint("vit", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("vit", p, inv);
                        break;
                    case 6:
                        stats.dexPoints += stats.tempdexPoints;
                        stats.vitPoints += stats.tempvitPoints;
                        stats.strPoints += stats.tempstrPoints;
                        stats.intPoints += stats.tempintPoints;
                        stats.dexPoints += stats.tempdexPoints;
                        stats.freePoints = stats.tempFreePoints;
                        stats.reset = false;
                        stats.resetTemp();
                        stats.updateDatabase();
                        p.closeInventory();
                        //Confirm
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDragItemInMerchant(InventoryClickEvent event) {
        if (event.getInventory().getName().equals("Merchant")) {
            int slot = event.getRawSlot();
            if (!(slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18 || slot == 19
                    || slot == 20 || slot == 21) && !(slot > 27)) {
                if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Naughty Naughty Naughty!");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Naughty Naughty Naughty!");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "Naughty Naughty Naughty!");
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }
    
    /**
     * Handles the accepting and denying for repairing items.
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerClickRepairInv(InventoryClickEvent event) {
    	if(!event.getInventory().getTitle().contains("Repair your item for")) return;
    	if(event.getRawSlot() < 9){
    		event.setCancelled(true);
    		if(event.getRawSlot() == 3){
    			String string = event.getInventory().getTitle().substring(event.getInventory().getTitle().indexOf(ChatColor.BOLD.toString()) + 2);
    			string = string.replace("g?", "");
    			int cost = Integer.parseInt(string);
    			if(BankMechanics.getInstance().takeGemsFromInventory(cost, (Player) event.getWhoClicked())){
    				ItemStack stack = event.getWhoClicked().getItemInHand();
    				RepairAPI.setCustomItemDurability(stack, 1500);
    				event.getWhoClicked().setItemInHand(stack);
    				event.getWhoClicked().closeInventory();
    			}else{
    				event.getWhoClicked().sendMessage(ChatColor.RED + "You do not have " +cost + " gems!");
    				event.getWhoClicked().closeInventory();
    			}
    		}else if(event.getRawSlot() == 5){
    			event.getWhoClicked().closeInventory();
    		}
    	}
    }
}
