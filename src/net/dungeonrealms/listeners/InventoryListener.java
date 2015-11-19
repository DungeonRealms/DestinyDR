package net.dungeonrealms.listeners;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ca.thederpygolems.armorequip.ArmorEquipEvent;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelOffer;
import net.dungeonrealms.duel.DuelingMechanics;
import net.dungeonrealms.handlers.ClickHandler;
import net.dungeonrealms.handlers.HealthHandler;
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
		Utils.log.info("Updated");
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
    public void onPlayerUseOrbsAndEnchant(InventoryClickEvent event) {
        if (event.getCursor() == null) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        if(slotItem == null || slotItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();

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
        if ((RepairAPI.isItemArmorOrWeapon(slotItem)) || Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)) {
            if (RepairAPI.canItemBeRepaired(slotItem)) {
                int scrapTier = RepairAPI.getScrapTier(cursorItem);
                int slotTier = 0;
                if(Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)){
            		slotTier = CraftItemStack.asNMSCopy(slotItem).getTag().getInt("itemTier");
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
            int slot = event.getSlot();
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
                }
            }
        }
    }
}
