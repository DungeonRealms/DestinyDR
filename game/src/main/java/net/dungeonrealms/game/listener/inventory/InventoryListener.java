package net.dungeonrealms.game.listener.inventory;

import com.codingforcookies.armorequip.ArmorEquipEvent;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.command.moderation.*;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.TradeCalculator;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 9/18/2015.
 * TODO: Modularize the admin /*see commands.
 */
public class InventoryListener implements Listener {

	@EventHandler
    public void handleMechantClose(InventoryCloseEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("Merchant"))
            return;

        Player player = (Player) event.getPlayer();
        getPlayerOffer(event.getInventory()).forEach(i -> GameAPI.giveOrDropItem(player, i));
        player.getOpenInventory().getTopInventory().clear();
        player.updateInventory();
    }

	@EventHandler
	public void handleMerchantClick(InventoryClickEvent event) {
    	Player player = (Player) event.getWhoClicked();
    	int slot = event.getRawSlot();
    	Inventory window = event.getInventory();
    	if (slot < 0 || !event.getInventory().getTitle().equalsIgnoreCase("Merchant"))
    		return; // Ignore dropping cursor or if this isn't a merchant.

    	if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
    		event.setCancelled(true); // Don't allow this, it could allow taking items from the other side of the menu.
    		return;
    	}

    	if (slot < window.getSize()) {
    		// Clicking inside the trade window.

    		if (!isLeft(slot)) {
    			event.setCancelled(true);
    			player.updateInventory();
    			return;
    		}

    		if (slot == 0) { // Accept trade.
    			event.setCancelled(true);

    			List<ItemStack> merchant = TradeCalculator.calculateMerchantOffer(getPlayerOffer(window));
    			int freeSlots = (int) Arrays.stream(player.getInventory().getContents()).filter(i -> i == null
    					|| i.getType() == Material.AIR).count();

    			if (freeSlots < merchant.size()) {
    				player.sendMessage(ChatColor.RED + "Please free " + (merchant.size() - freeSlots) + " inventory slots.");
    				return;
    			}

    			window.clear(); // Clear the items out.
    			merchant.forEach(player.getInventory()::addItem);
    			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);

    			player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 2F);
    			player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 1F);
    			player.sendMessage(ChatColor.GREEN + "Trade accepted.");

    			return;
    		}
    	} else {
    		// In player's inventory.
    		if (event.isShiftClick()) {
    			event.setCancelled(true);
    			int newSlot = firstAllowed(window); // Find the right slot to shift-click this into.
    			if (newSlot > -1) {
    				// Attempt to find the right place to shift click this to.
    				window.setItem(newSlot, event.getCurrentItem());
    				event.setCurrentItem(null);
    			}
    		}
    	}

    	// Run next tick so the items from this action get applied.
    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
    		// Update offer.
    		List<ItemStack> merchantOffer = TradeCalculator.calculateMerchantOffer(getPlayerOffer(window));
    		List<Integer> slots = getMerchantSlots(window);
    		slots.forEach(s -> window.setItem(s, null));

    		int mSlot = 0;
    		for (ItemStack i : merchantOffer) {
    			window.setItem(slots.get(mSlot), i);
    			mSlot++;
    		}

    		player.updateInventory();
    	});
    }

    private List<ItemStack> getPlayerOffer(Inventory window) {
    	List<ItemStack> playerOffer = new ArrayList<>();
    	for (int i : getPlayerSlots(window)) {
    		ItemStack item = window.getItem(i);
    		if (item != null && item.getType() != Material.AIR)
    			playerOffer.add(item);
    	}
    	return playerOffer;
    }

    private int firstAllowed(Inventory i) {
    	return getPlayerSlots(i).stream().filter(s -> i.getItem(s) == null
    			|| i.getItem(s).getType() == Material.AIR).findFirst().orElse(-1);
    }

    private boolean isLeft(int slot) {
    	return slot % 9 < 4;
    }

    private List<Integer> getPlayerSlots(Inventory i) {
    	List<Integer> slots = new ArrayList<>();
    	for (int y = 0; y < i.getSize() / 9; y++)
    		for (int x = 0; x < 4; x++)
    			if (x + y > 0) // Don't add the first slot, since it has the accept button
    				slots.add(x + (y * 9));
    	return slots;
    }

    private List<Integer> getMerchantSlots(Inventory i) {
    	List<Integer> slots = new ArrayList<>();
    	for (int y = 0; y < i.getSize() / 9; y++)
    		for (int x = 0; x < 4; x++)
    			slots.add(x + (y * 9) + 5);
    	return slots;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!CommandInvsee.offline_inv_watchers.containsKey(event.getPlayer().getUniqueId())) return;

        if (event.getInventory().getName().contains("'s Offline Inventory View")) {
            UUID target = CommandInvsee.offline_inv_watchers.get(event.getPlayer().getUniqueId());

            Player viewer = (Player) event.getPlayer();
            String inventory = ItemSerialization.toString(event.getInventory());
            PlayerWrapper.getPlayerWrapper(target, false, true, (wrapper) -> {
                if (wrapper == null) {
                    viewer.sendMessage(ChatColor.RED + "Something went wrong while loading the data!");
                    return;
                }

                if (wrapper.isPlaying()) {
                    viewer.sendMessage(ChatColor.RED + "This player is currently logged in! Your change has not been made!");
                    return;
                }

                wrapper.setPendingInventoryString(inventory);
                wrapper.saveData(true, null, (wrappa) -> {
                    if (wrappa != null) {
                        viewer.sendMessage(ChatColor.GREEN + "Sucessfully saved " + ChatColor.YELLOW + wrappa.getUsername() + "'s " + ChatColor.GREEN + " inventory!");
                    } else {
                        viewer.sendMessage(ChatColor.RED + "Could not save your changes! An error occurred");
                    }
                });
            });
        }

        CommandInvsee.offline_inv_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorSeeClose(InventoryCloseEvent event) {
        if (!CommandArmorsee.offline_armor_watchers.containsKey(event.getPlayer().getUniqueId())) return;

        UUID target = CommandArmorsee.offline_armor_watchers.get(event.getPlayer().getUniqueId());
        if (!(event.getPlayer() instanceof Player)) return;
        Player viewer = (Player) event.getPlayer();
        PlayerWrapper.getPlayerWrapper(target, false, true, (wrapper) -> {
            if (wrapper.isPlaying()) {
                viewer.sendMessage(ChatColor.RED + "This player is currently logged in! We could not save your changes!");
                return;
            }

            String toSave = wrapper.getEquipmentString(event.getInventory());
            wrapper.setPendingArmorString(toSave);
            wrapper.saveData(true, null, null);
        });

        CommandArmorsee.offline_armor_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBankSeeClose(InventoryCloseEvent event) {
        UUID target = CommandBanksee.offline_bank_watchers.get(event.getPlayer().getUniqueId());

        if (target == null) return;

        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getInventory().getName().contains("Storage Chest")) return;
        Player viewer = (Player) event.getPlayer();
        PlayerWrapper.getPlayerWrapper(target, false, true, (wrapper) -> {
            if (wrapper.isPlaying()) {
                viewer.sendMessage(ChatColor.RED + "This player has since logged into shard " + wrapper.getFormattedShardName() + "!");
                return;
            }

            SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET bank_storage = '" + ItemSerialization.toString(event.getInventory()) + "' WHERE character_id = '" + wrapper.getCharacterID() + "';");
        });
        CommandBanksee.offline_bank_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMuleInventoryClose(InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();
        UUID target = CommandMuleSee.getOfflineMuleSee().get(player.getUniqueId());

        //No inventory open.
        if (target == null) return;

        Inventory inv = event.getInventory();
        if (inv == null) return;

        Player onlineNow = Bukkit.getPlayer(target);

        String serializedInv = ItemSerialization.toString(inv);

        PlayerWrapper.getPlayerWrapper(target, false, true, (wrapper) -> {

            if (wrapper.isPlaying()) {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.RED + (onlineNow != null ? onlineNow.getName() : target.toString()) + " has sinced logged into DungeonRealms and your modified inventory would not been saved properly.");
                    player.sendMessage(ChatColor.RED + "Please /mulesee them on their shard to see their live mule inventory.");
                }
            } else {
                //Send this update to be processed..
                SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET mule_storage = '" + serializedInv + "' WHERE character_id = '" + wrapper.getCharacterID() + "';");
                if (player.isOnline())
                    player.sendMessage(ChatColor.RED + "Saved offline mule inventory to our database.");
            }
        });

        CommandMuleSee.getOfflineMuleSee().remove(player.getUniqueId());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBinSeeClose(InventoryCloseEvent event) {
//        if (!(CommandBinsee.offline_bin_watchers.containsKey(event.getPlayer().getUniqueId()))) return;

        UUID target = CommandBinsee.offline_bin_watchers.get(event.getPlayer().getUniqueId());
        if (target == null) return;
        if (event.getInventory().getTitle().contains("Collection Bin")) {
            Inventory inv = event.getInventory();
            if (inv == null) return;

            PlayerWrapper.getPlayerWrapper(target, false, true, (wrapper) -> {
                if (wrapper.isPlaying()) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Player has sinced logged into shard " + wrapper.getFormattedShardName() + ", Please /binsee them on that shard.");
                    return;
                }

                String serializedInv = ItemSerialization.toString(inv);
                SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET collection_storage = '" + serializedInv + "' WHERE character_id = '" + wrapper.getCharacterID() + "';");
            });
        }
        CommandBinsee.offline_bin_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        String title = event.getInventory().getTitle();
        if (title.contains("Bank")
                || GameAPI.isShop(event.getInventory())
                || title.contains("Trade")
                || title.contains("Merchant"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        StringBuilder msg = new StringBuilder();
        if (DungeonRealms.getInstance().isAlmostRestarting()) {
            player.sendMessage(ChatColor.RED + "You cannot interact with inventories while the shard is rebooting.");
            event.setCancelled(true);
            return;
        }
        GameAPI.runAsSpectators(player, (spectator) -> {

            if (msg.length() == 0) {
                if (event.getClickedInventory() == null) {
                    msg.append(ChatColor.YELLOW + player.getName() + " clicked on the outside of the inventory, top: " + getInventoryName(event.getInventory().getName()));
                } else {

                    String name = getInventoryName(event.getClickedInventory().getName());

                    msg.append(ChatColor.YELLOW + player.getName() + " clicked slot " + ChatColor.GOLD + event.getRawSlot() + ChatColor.YELLOW + " in " + ChatColor.GOLD + name + ChatColor.YELLOW
                            + " with action " + ChatColor.GOLD + event.getAction().name());
                    if (name.equals("their inventory")) {
                        msg.append(ChatColor.YELLOW + " top inventory: " + getInventoryName(event.getInventory().getName()));
                    }
                }

                if (event.getCursor() != null) {
                    msg.append(" cursor: ").append(ChatColor.GRAY.toString()).append(event.getCursor().getAmount()).append("x ").append(event.getCursor().hasItemMeta() && event.getCursor().getItemMeta().hasDisplayName() ? event.getCursor().getItemMeta().getDisplayName() : event.getCursor().getType().name());
                }

                if (event.getCurrentItem() != null) {
                    msg.append(ChatColor.YELLOW).append(" clicked item: ").append(ChatColor.GRAY).append(event.getCurrentItem().getAmount()).append("x ").append((event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() ? event.getCurrentItem().getItemMeta().getDisplayName() : event.getCurrentItem().getType().name()));
                }
            }

            spectator.sendMessage(msg.toString());
        });
    }

    private String getInventoryName(String name) {
        if (name.equals("container.inventory")) return "their inventory";
        return name;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void editPlayerArmor(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().contains("Armor") || GameAPI.isShop(event.getInventory())) return;
        String playerArmor = event.getInventory().getTitle().split(" ")[0];
        Player player = Bukkit.getPlayer(playerArmor);
        if (player != null) {
            ItemStack[] contents = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                if (event.getInventory().getItem(i) != null &&
                        event.getInventory().getItem(i).getType() != Material.AIR &&
                        ItemArmor.isArmor(event.getInventory().getItem(i))) {
                    contents[i] = event.getInventory().getItem(i);
                }
            }
            player.getInventory().setArmorContents(contents);
            player.updateInventory();
        }
    }

    //Armor

    /**
     * Called when a player equips armor
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerEquipArmor(ArmorEquipEvent event) {
        Player player = event.getPlayer();

        if ((!ItemArmor.isArmor(event.getNewArmorPiece()) && !ItemArmor.isArmor(event.getOldArmorPiece())) || event.isCancelled())
            return;

        if (!RestrictionListener.canPlayerUseItem(event.getPlayer(), event.getNewArmorPiece())) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }
        
        if (CombatLog.isInCombat(player) && (!event.getMethod().equals(ArmorEquipEvent.EquipMethod.DEATH) && !event.getMethod().equals(ArmorEquipEvent.EquipMethod.BROKE))) {
			player.sendMessage(ChatColor.RED + "You are in the middle of combat! You " + ChatColor.UNDERLINE +
					"cannot" + ChatColor.RED + " switch armor right now.");
			event.setCancelled(true);
			player.updateInventory();
			return;
		}
        
        handleArmorDifferences(event.getOldArmorPiece(), event.getNewArmorPiece(), player);
    }

    /**
     * Calculates the differences between two armor pieces' modifiers and updates the player's
     * stats accordingly. Also sends the difference message to the player. Called on armor
     * equip.
     *
     * @param oldArmor
     * @param newArmor
     * @param p
     */

    private static void handleArmorDifferences(ItemStack oldArmor, ItemStack newArmor, Player p) {
    	p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
    	
    	// Don't remove this delay, it prevents armor stacking. (Something with ArmorEquipEvent.)
    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
    		// recalculate attributes
    		PlayerWrapper wp = PlayerWrapper.getWrapper(p);
    		
    		boolean hasOldArmor = ItemArmor.isArmor(oldArmor);
    		boolean hasNewArmor = ItemArmor.isArmor(newArmor);
    		
    		String oldArmorName = Utils.getItemName(oldArmor);
    		String newArmorName = Utils.getItemName(newArmor);
    		
    		
    		// display differences to player
    		p.sendMessage(ChatColor.GRAY + oldArmorName + ChatColor.WHITE +
    				ChatColor.BOLD + " -> " + ChatColor.GRAY + newArmorName);
    		
    		AttributeList armorChanges = new AttributeList();
    		
    		// Show stats for the armor being removed.
    		if (hasOldArmor) {
    			ItemArmor removedArmor = (ItemArmor) PersistentItem.constructItem(oldArmor);
    			armorChanges.removeStats(removedArmor.getAttributes());
    		}
    		
    		// Show stats for the armor being equipped.
    		if (hasNewArmor) {
    			ItemArmor addedArmor = (ItemArmor) PersistentItem.constructItem(newArmor);
    			armorChanges.addStats(addedArmor.getAttributes());
    		}
    		
    		wp.calculateAllAttributes();
    		
    		for (AttributeType t : armorChanges.keySet()) {
    			ModifierRange armorVal = armorChanges.getAttribute(t);
    			ModifierRange newVal = wp.getAttributes().getAttribute(t);
    			
    			p.sendMessage((armorVal.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "")
    					+ armorVal.getValue() + t.getSuffix()
    					+ " " + ChatColor.stripColor(t.getPrefix().split(":")[0]) + " ["
    					+ newVal.getValue() + t.getSuffix() + "]");
    		}
    	});	
    }


    /**
     * @param event
     * @since 1.0 Closes both players wager inventory.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClosed(InventoryCloseEvent event) {
        if (GameAPI.isShop(event.getInventory())) return;
        Player p = (Player) event.getPlayer();

        if (event.getInventory().getTitle().contains("Storage Chest") && !CommandBanksee.offline_bank_watchers.containsKey(event.getPlayer().getUniqueId())) {
            Storage storage = BankMechanics.getStorage(event.getPlayer().getUniqueId());
            //Not loaded yet?
            if (storage == null) {
                Bukkit.getLogger().info("Closing " + p.getName() + " with no storage in memory.");
                return;
            }
            storage.inv.setContents(event.getInventory().getContents());
        } else if (event.getInventory().getTitle().contains("Trade Window")) {
            Trade t = TradeManager.getTrade(p.getUniqueId());
            if (t != null)
                if (!t.p1Ready || !t.p2Ready) {
                    t.handleClose();
                }
        } else if (event.getInventory().getTitle().contains("Stat Points")) {
            PlayerStats stat = PlayerWrapper.getPlayerWrapper(p).getPlayerStats();
            if (stat.reset) {
                stat.resetTemp();
            }
            stat.reset = true;
        } else if (event.getInventory().getTitle().contains("Collection Bin") && !CommandBinsee.offline_bin_watchers.containsKey(event.getPlayer().getUniqueId())) {
            Storage storage = BankMechanics.getStorage(event.getPlayer().getUniqueId());
            Inventory bin = storage.collection_bin;
            if (bin == null)
                return;

            int i = 0;
            for (ItemStack stack : bin.getContents())
                if (stack != null && stack.getType() != Material.AIR)
                    i++;

            if (i == 0)
                storage.clearCollectionBin();
        }
    }


    @EventHandler()
    public void onInventoryClick(InventoryClickEvent event) {

        //Basically any inventory.
        if (event.getInventory().getName().contains("container.chest") || event.getInventory().getName().contains("Realm Chest")
                || event.getInventory().getName().equalsIgnoreCase("container.chestDouble")
                || (event.getInventory().getName().equalsIgnoreCase("container.minecart"))
                || (event.getInventory().getName().equalsIgnoreCase("container.dispenser"))
                || (event.getInventory().getName().equalsIgnoreCase("container.hopper"))
                || (event.getInventory().getName().equalsIgnoreCase("container.dropper"))
                || event.getInventory().getName().equalsIgnoreCase("Loot")) {

            //Check for soulbound item?
            ItemStack item = event.getClick() == ClickType.NUMBER_KEY ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem();

            if (item == null || item.getType() == Material.AIR)
                return;

            if (ItemManager.isItemSoulbound(item)) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * @param event
     * @since 1.0 handles Trading inventory items.
     */

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTradeInvClicked(InventoryClickEvent event) {
        if (GameAPI.isShop(event.getInventory())) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().contains("Trade Window")) {
//            System.out.println("Second Trade click: " + player.getName() + " Time: " + System.currentTimeMillis() + " Type: " + event.getClick() + " Slot : " + event.getRawSlot());

            //Dont allow these click types.
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR || event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN) {
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            Trade trade = TradeManager.getTrade(event.getWhoClicked().getUniqueId());
            if (trade == null)
                return;

            int slot = event.getRawSlot();

            if (trade.isLeftSlot(slot)) {
                if (!trade.isLeftPlayer(event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            } else if (trade.isRightSlot(slot)) {

                if (trade.isLeftPlayer(event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getCurrentItem() == null)
                return;

            VanillaItem vi = new VanillaItem(event.getCurrentItem());
            if (!vi.isSoulboundBypass(trade.getOppositePlayer(player)) && !ItemManager.isItemTradeable(event.getCurrentItem())) {
                player.sendMessage(ChatColor.RED + "You can't trade this item.");
                event.setCancelled(true);
                return;
            }


            if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }

            if (slot >= 36)
                return;

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;

            ItemStack stackClicked = event.getCurrentItem();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
            if (nms.hasTag() && nms.getTag().hasKey("status")) {
                String status = nms.getTag().getString("status");
                event.setCancelled(true);

                boolean ready = status.equalsIgnoreCase("ready");
                trade.updateReady(event.getWhoClicked().getUniqueId());
                ItemStack item = new ItemStack(Material.INK_SACK);
                item.setDurability(ready ? DyeColor.GRAY.getDyeData() : DyeColor.LIME.getDyeData());
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + (ready ? "NOT READY" : "READY"));
                item.setItemMeta(meta);
                nms = CraftItemStack.asNMSCopy(item);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("status", ready ? "notready" : "ready");
                nms.setTag(nbt);
                nms.c(ChatColor.YELLOW + (ready ? "NOT READY" : "READY"));
                event.getInventory().setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
                trade.checkReady();
                return;
            }
            Player clicker = (Player) event.getWhoClicked();
            if(trade.p1Ready || trade.p2Ready) {
                trade.p1.sendMessage(ChatColor.RED + "Trade modified by " + ChatColor.BOLD.toString() + clicker.getName());
                trade.p2.sendMessage(ChatColor.RED + "Trade modified by " + ChatColor.BOLD.toString() + clicker.getName());
                trade.changeReady();
                trade.setDividerColor(DyeColor.RED);
                clicker.updateInventory();
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (trade != null && trade.inv != null && trade.inv.getViewers().size() > 0)
                    trade.setDividerColor(DyeColor.WHITE);
            }, 20L);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST) // This case is not caught by ArmorEquipEvent.
    public void playerDoWeirdArmorThing(InventoryClickEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")
        		|| event.getAction() != InventoryAction.HOTBAR_SWAP
        		|| event.getSlotType() != InventoryType.SlotType.ARMOR)
        	return;
        event.setCancelled(true);
    }
}