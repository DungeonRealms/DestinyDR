package net.dungeonrealms.game.listener.mechanic;

import com.google.common.collect.Lists;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemGemPouch;
import net.dungeonrealms.game.item.items.functional.ItemGemNote;
import net.dungeonrealms.game.item.items.functional.ItemMoney;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.EnumUpgrade;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chase, by fixed by Proxying and under inspection of xFinityPro. Later it was changed by Kneesnap. Probably it will be changed sometime in the future.
 */
public class BankListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestRightClick(PlayerInteractEvent e) {
    	if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.ENDER_CHEST)
    		return;
    	//  No banks in realms  //
    	e.setCancelled(true);
    	if(!GameAPI.isMainWorld(e.getClickedBlock().getLocation()))
            return;
    	
        if (e.getPlayer().isSneaking()) {
            promptUpgradeBank(e.getPlayer());
        } else {
        	//  OPEN BANK  //
        	e.getPlayer().openInventory(getBank(e.getPlayer().getUniqueId()));
        	e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
        }
    }


    //GEM PICKUP CODE.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickUp(PlayerPickupItemEvent event) {

        if (event.getItem().hasMetadata("whitelist") && event.getItem().getTicksLived() < 60 * 20 * 2) {
            //Whitelisted item, dont let them pick it up.
            String allowed = event.getItem().getMetadata("whitelist").get(0).asString();
            if (!allowed.equals(event.getPlayer().getName())) {
                event.setCancelled(true);
                return;
            }
        }
        
        ItemStack item = event.getItem().getItemStack();
        if (ItemGem.isGem(item)) {
        	ItemGem gem = (ItemGem) PersistentItem.constructItem(item);
            Player player = event.getPlayer();
            if (player.getOpenInventory() != null && GameAPI.isShop(player.getOpenInventory())) {
                // Player is browsing a shop
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            event.getItem().remove();
            
            Player p = event.getPlayer();
            PlayerWrapper pw = PlayerWrapper.getWrapper(p);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            pw.sendDebug("                      " + ChatColor.GREEN + "+" + event.getItem().getItemStack().getAmount() + ChatColor.BOLD + "G");
            
            pw.getPlayerGameStats().addStat(StatColumn.GEMS_EARNED, gem.getGemValue());
            
            int giveGems = event.getRemaining();
            for (int i = 0; i < p.getInventory().getContents().length; i++) {
            	ItemStack pch = p.getInventory().getContents()[i];
            	if (giveGems <= 0)
            		break;
            	if (!ItemGemPouch.isPouch(pch))
            		continue;
            	
            	ItemGemPouch pouch = (ItemGemPouch)PersistentItem.constructItem(pch);
            	int oldGemValue = pouch.getGemValue();
            	pouch.setGemValue(Math.min(oldGemValue + giveGems, pouch.getMaxStorage()));
            	giveGems -= (pouch.getGemValue() - oldGemValue);
            	p.getInventory().setItem(i, pouch.generateItem());
            }
            
            if (giveGems > 0)
            	GameAPI.giveOrDropItem(player, new ItemGem(giveGems).generateItem());
        }
    }
    
    private boolean canItemBeStored(ItemStack item) {
    	return !(ItemMoney.isMoney(item) || (!ItemManager.isItemTradeable(item) && !ItemManager.isItemSoulbound(item)));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleBankClick(InventoryClickEvent evt) {
    	if (!evt.getInventory().getTitle().equalsIgnoreCase("Bank Chest"))
    		return;
    	Player player = (Player) evt.getWhoClicked();
    	evt.setCancelled(true);
    	
    	if (evt.getRawSlot() < 9) {
    		if (evt.getCursor() == null) {
    			if (evt.getRawSlot() == 0) {
    				//  OPEN STORAGE  //
    				Storage storage = BankMechanics.getStorage(player.getUniqueId());
                    if (storage == null) {
                        player.sendMessage(ChatColor.RED + "Please wait while your bank is being loaded...");
                        return;
                    }

                    if (evt.isLeftClick()) {
                        storage.openBank(player);
                    } else if (evt.getClick() == ClickType.MIDDLE || evt.getClick() == ClickType.RIGHT) {
                        promptUpgradeBank(player);
                    }
    			} else if (evt.getRawSlot() == 1) {
    				//  SCRAP TAB  //
                    CurrencyTab tab = BankMechanics.getCurrencyTab(player.getUniqueId());
                    if (tab == null || !(tab.hasAccess || Rank.isTrialGM(player))) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                        player.sendMessage(ChatColor.RED + "You have not unlocked the Scrap Tab!");
                        player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + "http://dungeonrealms.net/store" + ChatColor.GRAY + "!");
                        player.closeInventory();
                        return;
                    }
                    
                    player.openInventory(tab.createCurrencyInventory());
    			} else if (evt.getRawSlot() == 4 && evt.getCurrentItem() != null && evt.getCurrentItem().getType() == Material.CHEST) {
    				//  COLLECTION BIN  //
    				Storage storage = BankMechanics.getStorage(player.getUniqueId());
    				
    				if (storage == null || storage.collection_bin == null) {
    					player.sendMessage(ChatColor.RED + "Collection Bin is empty.");
    					return;
    				}
    				
    				player.openInventory(storage.collection_bin);
    			} else if (evt.getRawSlot() == 8) {
    				//  ATM  //
    				if (evt.getClick() == ClickType.LEFT) {
    					promptWithdrawGems(player);
    				} else if (evt.getClick() == ClickType.RIGHT) {
    					promptWithdrawNote(player);
    				}
    			}
    		} else {
    			// Check that we're holding an item, also that we're depositing it onto an empty slot.
    			if (evt.getCursor() == null || evt.getCursor().getType() == Material.AIR
    					|| evt.getCurrentItem() != null || evt.getCurrentItem().getType() != Material.AIR)
    				return;
    			
    			if (!evt.isLeftClick() && !evt.isRightClick())
    				return;
    			
    			if(ItemMoney.isMoney(evt.getCursor())) {
    				// Deposit money.
    				handleMoneyDeposit(evt);
    				updateBank(evt.getInventory(), player.getUniqueId());
    			} else {
    				// Attempt deposit a regular item.
    				Storage storage = BankMechanics.getStorage(player.getUniqueId());
    				ItemStack cursor = evt.getCursor();
                    if (storage == null) {
                        player.sendMessage(ChatColor.RED + "Please wait while your storage is being loaded..");
                        return;
                    }
                    
                    if (!canItemBeStored(cursor)) {
                    	player.sendMessage(ChatColor.RED + "You can't store this item.");
                    	return;
                    }
                    
                    if (!storage.hasSpace()) {
                    	player.sendMessage(ChatColor.RED + "You do not have the space required to add this item.");
                    	return;
                    }
                    
                    int storeSize = evt.isLeftClick() ? cursor.getAmount() : 1;
                    
                    //  ADD ITEM  //
                    ItemStack add = cursor.clone();
                    add.setAmount(storeSize);
                    storage.inv.addItem(add);
                    
                    //  REMOVE ITEM FROM CURSOR  //
                    ItemStack replacement = cursor.clone();
                    replacement.setAmount(replacement.getAmount() - storeSize);
                    player.setItemOnCursor(replacement.getAmount() > 0 ? replacement : null);
                    player.sendMessage(ChatColor.GREEN + "Item added to storage!");
                    
    			}
    		}
    	} else if (evt.isShiftClick()) {
    		ItemStack item = evt.getCurrentItem();
    		if (ItemMoney.isMoney(item)) {
    			handleMoneyDeposit(evt);
    			updateBank(evt.getInventory(), player.getUniqueId());
    			return;
    		}
    		
    		if (!canItemBeStored(item)) {
				player.sendMessage(ChatColor.RED + "This item cannot be stored.");
				return;
			}
			
			Storage storage = BankMechanics.getStorage(player.getUniqueId());
			
			if (!storage.hasSpace()) {
				player.sendMessage(ChatColor.RED + "You do not have the space required to add this item.");
				return;
			}
			
			storage.inv.addItem(item);
            evt.setCurrentItem(null);
            player.sendMessage(ChatColor.GREEN + "Item added to storage!");
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleStorageClick(InventoryClickEvent evt) {
    	if (!evt.getInventory().getTitle().equalsIgnoreCase("Storage Chest"))
    		return;
    	
    	Inventory inv = evt.getInventory();
    	int slot = evt.getRawSlot();
    	ItemStack attemptAdd = null;
    	InventoryAction action = evt.getAction();
    	
    	if (evt.isShiftClick() && slot >= inv.getSize()) {
    		attemptAdd = evt.getCurrentItem();
    	} else if(slot < inv.getSize()) {
    		attemptAdd = evt.getCursor();
    		if (action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP)
    			attemptAdd = evt.getView().getBottomInventory().getItem(evt.getHotbarButton());
    	}
    	
    	if (attemptAdd == null || !canItemBeStored(attemptAdd)) {
    		evt.setCancelled(true);
    		return;
    	}
    	
    	handleMoneyDeposit(evt);
    }
    
    /**
     * Handle a deposit click. Is not registered, should be called by another registered listener.
     */
    public void handleMoneyDeposit(InventoryClickEvent evt) {
    	Inventory inv = evt.getInventory();
    	int slot = evt.getRawSlot();
    	InventoryAction action = evt.getAction();
    	Player p = (Player)evt.getWhoClicked();
    	
    	if (evt.isShiftClick() && slot >= inv.getSize()) {
    		evt.setCurrentItem(attemptDeposit(p, evt.getCurrentItem()));
    	} else if(slot < inv.getSize()) {
    		if (action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP) {
    			ItemStack item = evt.getView().getBottomInventory().getItem(evt.getHotbarButton());
    			evt.getView().getBottomInventory().setItem(evt.getHotbarButton(), attemptDeposit(p, item));
    		} else {
    			p.setItemOnCursor(attemptDeposit(p, evt.getCursor()));
    		}
    	}
    }
    
    private ItemStack attemptDeposit(Player player, ItemStack item) {
    	if (!ItemMoney.isMoney(item))
    		return item;
    	ItemMoney money = (ItemMoney)PersistentItem.constructItem(item);
    	PlayerWrapper.getWrapper(player).addGems(money.getGemValue());
        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "+" + ChatColor.GREEN + money.getGemValue() + ChatColor.BOLD + "G, New Balance: " + ChatColor.GREEN + getPlayerGems(player.getUniqueId()) + " GEM(s)");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    	money.setGemValue(0);
    	return money.generateItem();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleCollectionBinClick(InventoryClickEvent evt) {
    	if (!evt.getInventory().getTitle().equalsIgnoreCase("Collection Bin"))
    		return;
    	
    	evt.setCancelled(true);
    	if(evt.isShiftClick()) //No shift clicks.
    		return;
    	
    	if (evt.getRawSlot() > evt.getInventory().getSize()) //Only handle a click if it was in the collection bin.
    		return;
    	
    	// Item has to exist.
    	if (evt.getCurrentItem() == null && evt.getCurrentItem().getType() == Material.AIR)
    		return;
    	
    	// Return the item.
        if (evt.getWhoClicked().getInventory().firstEmpty() >= 0) {
        	VanillaItem item = new VanillaItem(evt.getCurrentItem());
        	item.removePrice();
            evt.setCurrentItem(new ItemStack(Material.AIR));
            evt.getWhoClicked().getInventory().addItem(item.generateItem());
        }
    }
    
    /**
     * Prompts a player how much they would like to withdraw as a bank note.
     * @param player
     */
    public void promptWithdrawNote(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " + ChatColor.GREEN + getPlayerGems(player.getUniqueId()) + " GEM(s)");
        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "How much would you like to CONVERT today, " + player.getDisplayName() + "?");
        player.sendMessage(ChatColor.GRAY + "Please enter the amount you'd like To CONVERT into a gem note. Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
        
        Chat.listenForNumber(player, 1, Integer.MAX_VALUE, num -> {
        	int currentGems = getPlayerGems(player.getUniqueId());
        	
        	if (num > ItemGemNote.MAX_SIZE) {
        		player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but I cannot create bank notes that large.");
        	} else if (num > currentGems) {
        		player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but you only have " + currentGems + " GEM(s) stored in our bank.");
                player.sendMessage(ChatColor.GRAY + "You cannot withdraw more GEM(s) than you have stored.");
        	} else {
        		player.getInventory().addItem(new ItemGemNote(player.getName(), num).generateItem());
        		PlayerWrapper.getWrapper(player).subtractGems(num);
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "New Balance: " + ChatColor.GREEN + (currentGems - num) + " GEM(s)");
                player.sendMessage(ChatColor.GRAY + "You have converted " + num + " GEM(s) from your bank account into a " + ChatColor.BOLD.toString() + "GEM NOTE.");
                player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Here are your Gems, thank you for your business!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        	}
        }, () -> player.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED"));
    }
    
    /**
     * Prompts a player how much they would like to withdraw in raw gems.
     * @param player
     */
    public void promptWithdrawGems(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " + ChatColor.GREEN + getPlayerGems(player.getUniqueId()) + " GEM(s)");
        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "How much would you like to WITHDRAW today, " + player.getDisplayName() + "?");
        player.sendMessage(ChatColor.GRAY + "Please enter the amount you'd like To WITHDRAW. Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
        
        // We want to have a custom message show if they enter too much, so we use max integer.
        Chat.listenForNumber(player, 1, Integer.MAX_VALUE, num -> {
        	int currentGems = getPlayerGems(player.getUniqueId());
        	if (num > getPlayerGems(player.getUniqueId())) {
        		player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but you only have " + currentGems + " GEM(s) stored in our bank.");
                player.sendMessage(ChatColor.GRAY + "You cannot withdraw more GEM(s) than you have stored.");
        		return;
        	} else if (hasSpaceInInventory(player, num)) {
        		PlayerWrapper.getWrapper(player).subtractGems(num);
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "New Balance: " + ChatColor.GREEN + (currentGems - num) + " GEM(s)");
                player.sendMessage(ChatColor.GRAY + "You have withdrawn " + num + " GEM(s) from your bank account.");
                player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Here are your Gems, thank you for your business!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        		BankMechanics.givePlayerRawGems(player, num);
        	}
        }, () -> player.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED"));
    }
    
    public void promptUpgradeBank(Player p) {
    	int storageLevel = PlayerWrapper.getWrapper(p).getBankLevel();
        if (storageLevel >= 6) {
            p.sendMessage(ChatColor.RED + "You've reached the current Storage lvl cap!");
            return;
        }
        EnumUpgrade nextLevel = EnumUpgrade.getTier(storageLevel).getNextUpgrade();
        int newLevel = storageLevel + 1;

        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Bank Upgrade Confirmation" + ChatColor.DARK_GRAY + " ***");
        p.sendMessage(ChatColor.DARK_GRAY + "           CURRENT Slots: " + ChatColor.GREEN + storageLevel * 9 + ChatColor.DARK_GRAY + "          NEW Slots: " + ChatColor.GREEN + newLevel * 9);
        p.sendMessage(ChatColor.DARK_GRAY + "                  Upgrade Cost: " + ChatColor.GREEN + "" + nextLevel.getBankCost() + " Gem(s)");
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "Enter '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your upgrade.");
        p.sendMessage("");
        p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Bank upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
        p.sendMessage("");
        
        Chat.promptPlayerConfirmation(p, () -> {
        	p.closeInventory();
        	boolean success = BankMechanics.takeGemsFromInventory(p, nextLevel.getBankCost());
        	if (success) {
        		PlayerWrapper.getWrapper(p).setBankLevel(newLevel);
                p.sendMessage("");
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** BANK UPGRADE TO LEVEL " + newLevel + " COMPLETE ***");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.25F);
                BankMechanics.upgradeStorage(p.getUniqueId());
        	} else {
        		p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
                p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + nextLevel.getBankCost());
        	}
        }, () -> p.sendMessage(ChatColor.RED + "Bank Upgrade Cancelled"));
    }
    
    @EventHandler
    public void onItemDrag(InventoryDragEvent event) {
    	ItemStack check = event.getOldCursor();
    	if(event.getInventory().getTitle().equalsIgnoreCase("Storage Chest"))
    		if(ItemMoney.isMoney(check) || !ItemManager.isItemTradeable(check))
    			event.setCancelled(true);
    }

    /**
     * Checks if player has room in inventory for amount of gems to withdraw.
     *
     * @param player
     * @param Gems being added
     * @since 1.0
     */
    private boolean hasSpaceInInventory(Player player, int itemCount) {
        if (itemCount > 64) {
            int slotsNeeded = Math.round(itemCount / 64) + 1;
            int emptySlots = 0;
            
            for (ItemStack content : player.getInventory().getContents())
                if (content == null || content.getType() == Material.AIR)
                	emptySlots++;

            if (slotsNeeded > emptySlots) {
            	player.sendMessage(ChatColor.RED
                        + "You do not have enough space in your inventory to withdraw " + itemCount + " GEM(s).");
            	player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + slotsNeeded + " slots");
            } 
            return emptySlots >= slotsNeeded;
        }
        return player.getInventory().firstEmpty() != -1;
    }

    private void updateBank(Inventory inv, UUID uuid) {
    	ItemStack bankItem = new ItemStack(Material.EMERALD);
    	ItemMeta meta = bankItem.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + String.valueOf(getPlayerGems(uuid)) + ChatColor.GREEN + ChatColor.BOLD.toString() + " GEM(s)");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to withdraw " + ChatColor.GREEN.toString() + ChatColor.BOLD + "RAW GEMS");
        lore.add(ChatColor.GREEN + "Right Click " + ChatColor.GRAY + "to create " + ChatColor.GREEN.toString() + ChatColor.BOLD + "A GEM NOTE");

        meta.setLore(lore);
        bankItem.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(bankItem);
        nms.getTag().setString("type", "bank");
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
    }
    
    /**
     * Gets an Inventory specific for player.
     *
     * @param uuid
     * @since 1.0
     */
    @SuppressWarnings("deprecation")
	private Inventory getBank(UUID uuid) {
        Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
        ItemStack storage = new ItemStack(Material.CHEST, 1);
        ItemMeta storagetMeta = storage.getItemMeta();
        storagetMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD + "STORAGE");
        ArrayList<String> storelore = new ArrayList<>();
        storelore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.GREEN.toString() + ChatColor.BOLD + "STORAGE");
        storelore.add(ChatColor.GREEN + "Middle Click " + ChatColor.GRAY + "to " + ChatColor.GREEN.toString() + ChatColor.BOLD + "UPGRADE BANK");
        storagetMeta.setLore(storelore);
        storage.setItemMeta(storagetMeta);
        net.minecraft.server.v1_9_R2.ItemStack storagenms = CraftItemStack.asNMSCopy(storage);
        storagenms.getTag().setString("type", "storage");
        inv.setItem(0, CraftItemStack.asBukkitCopy(storagenms));


        if (CurrencyTab.isEnabled()) {
            List<String> currencyLore = Lists.newArrayList();

            CurrencyTab tab = BankMechanics.getCurrencyTab(uuid);
            if (tab != null && tab.hasAccess) {
                currencyLore.add(ChatColor.GRAY + "Hold up to 500 additional scrap.");
                currencyLore.add(ChatColor.GRAY + "Max of 250 can be stored of each type.");
                currencyLore.add("");
                currencyLore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "Scrap Stored");
                currencyLore.add(ChatColor.GREEN.toString() + tab.getTotalScrapStored() + ChatColor.BOLD + " / " + ChatColor.GREEN + "500");
                currencyLore.add("");
                currencyLore.add(ChatColor.GRAY + "Click to view your Scrap Tab.");
            } else {
                currencyLore.add(ChatColor.GRAY + "Hold up to 500 additional scrap.");
                currencyLore.add("");
                currencyLore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LOCKED");
                currencyLore.add("");
                currencyLore.add(ChatColor.GRAY + "You can unlock this Scrap Tab");
                currencyLore.add(ChatColor.GRAY + "at " + ChatColor.UNDERLINE + "http://dungeonrealms.net/store" + ChatColor.GRAY + "!");
            }

            ItemStack currencyTab = new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, DyeColor.YELLOW.getDyeData()))
                    .setName(ChatColor.GREEN.toString() + ChatColor.BOLD + "Scrap Tab").setLore(currencyLore).build();

            NBTWrapper wrapper = new NBTWrapper(currencyTab);
            wrapper.setString("scrapTab", "true");
            wrapper.setString("ench", "");
            inv.setItem(1, wrapper.build());

        }


        ItemStack item = new ItemBuilder().setItem(new ItemStack(Material.CHEST)).setName(ChatColor.RED.toString() + ChatColor.BOLD + "COLLECTION BIN")
                .setLore(Lists.newArrayList(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.GREEN.toString() +
                        ChatColor.BOLD + "COLLECTION BIN")).setNBTString("type", "collection").build();

        Storage stor = BankMechanics.getStorage(uuid);
        updateBank(inv, uuid);
        if (stor != null && stor.collection_bin != null)
            inv.setItem(4, item);
        return inv;
    }

    /**
     * Get Player Gems.
     *
     * @param uuid
     * @since 1.0
     */
    private int getPlayerGems(UUID uuid) {
        return PlayerWrapper.getPlayerWrapper(uuid).getGems();
    }

}