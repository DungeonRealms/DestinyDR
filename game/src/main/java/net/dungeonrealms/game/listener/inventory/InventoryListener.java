package net.dungeonrealms.game.listener.inventory;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.command.moderation.*;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.ClickHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

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
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        }

        ClickHandler.getInstance().doClick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!CommandInvsee.offline_inv_watchers.containsKey(event.getPlayer().getUniqueId())) return;

        if (event.getInventory().getName().contains("'s Offline Inventory View")) {
            UUID target = CommandInvsee.offline_inv_watchers.get(event.getPlayer().getUniqueId());

            String inventory = ItemSerialization.toString(event.getInventory());
            DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.INVENTORY, inventory, true, true, null);
        }

        CommandInvsee.offline_inv_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorSeeClose(InventoryCloseEvent event) {
        if (!CommandArmorsee.offline_armor_watchers.containsKey(event.getPlayer().getUniqueId())) return;

        UUID target = CommandArmorsee.offline_armor_watchers.get(event.getPlayer().getUniqueId());
        if (event.getInventory().getTitle().contains("'s Offline Armor View (Last slot is offhand")) {
            ArrayList<String> armor = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                ItemStack stack = event.getInventory().getContents()[i];
                if (stack == null || stack.getType() == Material.AIR) {
                    armor.add("");
                } else {
                    armor.add(ItemSerialization.itemStackToBase64(stack));
                }
            }
            ItemStack offHand = event.getInventory().getContents()[4];
            if (offHand == null || offHand.getType() == Material.AIR) {
                armor.add("");
            } else {
                armor.add(ItemSerialization.itemStackToBase64(offHand));
            }

            DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.ARMOR, armor, true, true, null);
        }
        CommandArmorsee.offline_armor_watchers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBankSeeClose(InventoryCloseEvent event) {
        if (!(CommandBanksee.offline_bank_watchers.containsKey(event.getPlayer().getUniqueId()))) return;

        UUID target = CommandBanksee.offline_bank_watchers.get(event.getPlayer().getUniqueId());

        if (event.getInventory().getTitle().contains("Storage Chest")) {
            Inventory inv = event.getInventory();
            if (inv == null) return;

            String serializedInv = ItemSerialization.toString(inv);
            DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv, true, true, null);
        }
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
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {

            //Check again incase this data isnt accurate.
            boolean isPlaying = (Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, target);

            if (isPlaying) {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.RED + (onlineNow != null ? onlineNow.getName() : target.toString()) + " has sinced logged into DungeonRealms and your modified inventory would not been saved properly.");
                    player.sendMessage(ChatColor.RED + "Please /mulesee them on their shard to see their live mule inventory.");
                }
            } else {

                DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.INVENTORY_MULE, serializedInv, true, true, null);
                if (player.isOnline())
                    player.sendMessage(ChatColor.RED + "Saved offline mule inventory to our database.");
            }
        });

        CommandMuleSee.getOfflineMuleSee().remove(player.getUniqueId());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBinSeeClose(InventoryCloseEvent event) {
        if (!(CommandBinsee.offline_bin_watchers.containsKey(event.getPlayer().getUniqueId()))) return;

        UUID target = CommandBinsee.offline_bin_watchers.get(event.getPlayer().getUniqueId());
        if (event.getInventory().getTitle().contains("Collection Bin")) {
            Inventory inv = event.getInventory();
            if (inv == null) return;

            String serializedInv = ItemSerialization.toString(inv);
            DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, serializedInv, true, true, null);
        }
        CommandBinsee.offline_bin_watchers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * @param event
     * @since 1.0 Dragging is naughty.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragItemInDuelWager(InventoryDragEvent event) {
        if (event.getInventory().getTitle().contains("VS.") || event.getInventory().getTitle().contains("Bank")
                || GameAPI.isShop(event.getInventory()) || event.getInventory().getTitle().contains("Trade"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        StringBuilder msg = new StringBuilder();
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
        if (!ItemArmor.isArmor(event.getNewArmorPiece()) && !ItemArmor.isArmor(event.getOldArmorPiece())) return;
        // Level restrictions on equipment removed on 7/18/16 Build#131
        // Level restrictions added back on 2/2/2017
        
        if (!RestrictionListener.canPlayerUseItem(event.getPlayer(), event.getNewArmorPiece())) {
        	event.setCancelled(true);
        	player.updateInventory();
        	return;
        }
        
        if (!CombatLog.isInCombat(player)) {
            if (GameAPI.getGamePlayer(player) == null) {
                return;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            // KEEP THIS DELAY IT PREVENTS ARMOR STACKING
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
            	handleArmorDifferences(event.getOldArmorPiece(), event.getNewArmorPiece(), player);
            }, 10L);
        } else if (!event.getMethod().equals(ArmorEquipEvent.EquipMethod.DEATH) && !event.getMethod().equals(ArmorEquipEvent.EquipMethod.BROKE)) {
            player.sendMessage(ChatColor.RED + "You are in the middle of combat! You " + ChatColor.UNDERLINE +
                    "cannot" + ChatColor.RED + " switch armor right now.");
            event.setCancelled(true);
            player.updateInventory();
        }
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
        // recalculate attributes
    	GamePlayer gp = GameAPI.getGamePlayer(p);
    	gp.calculateAllAttributes();
    	
    	boolean hasOldArmor = ItemArmor.isArmor(oldArmor);
    	boolean hasNewArmor = ItemArmor.isArmor(newArmor);
    	
        String oldArmorName = hasOldArmor ? "NOTHING" : oldArmor.getItemMeta().getDisplayName();
        String newArmorName = hasNewArmor ? "NOTHING" : newArmor.getItemMeta().getDisplayName();
        
        
        // display differences to player
        p.sendMessage(ChatColor.GRAY + oldArmorName + ChatColor.WHITE +
                ChatColor.BOLD + " -> " + ChatColor.GRAY + newArmorName);
        
        //TODO: Don't show the same stat twice, combine the messages.
        AttributeList armorChanges = new AttributeList();
        
        // Show stats for the armor being removed.
        if (hasOldArmor) {
        	ItemArmor removedArmor = (ItemArmor)PersistentItem.constructItem(oldArmor);
        	armorChanges.addStats(removedArmor.getAttributes());
        }
        
        // Show stats for the armor being equipped.
        if (hasNewArmor) {
        	ItemArmor addedArmor = (ItemArmor)PersistentItem.constructItem(newArmor);
        	armorChanges.addStats(addedArmor.getAttributes());
        }
        
        for (AttributeType t : armorChanges.keySet()) {
        	ModifierRange armorVal = armorChanges.getAttribute(t);
        	ModifierRange newVal = gp.getAttributes().getAttribute(t);
        	
        	if (armorVal.getValue() != 0)
        		continue;
        	
        	p.sendMessage((armorVal.getValue() > 0 ? ChatColor.GREEN + "+": ChatColor.RED + "-")
        			+ armorVal.getValue() + t.getSuffix()
        			+ " " + t.getDisplayPrefix() + " ["
        			+ newVal.getValue() + t.getSuffix() + "]");
        }
        
        HealthHandler.updatePlayerHP(p);
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
//        } else if (event.getInventory().getTitle().startsWith(p.getName()) || event.getInventory().getTitle().endsWith(p.getName())) {
            Trade t = TradeManager.getTrade(p.getUniqueId());
            if (t != null)
                if (!t.p1Ready || !t.p2Ready) {
                    t.handleClose();
                }
        } else if (event.getInventory().getTitle().contains("Stat Points")) {
            PlayerStats stat = GameAPI.getGamePlayer((Player) event.getPlayer()).getStats();
            if (stat.reset) {
                stat.resetTemp();
            }
            stat.reset = true;
        } else if (event.getInventory().getTitle().contains("Collection Bin") && !CommandBinsee.offline_bin_watchers.containsKey(event.getPlayer().getUniqueId())) {
            Storage storage = BankMechanics.getStorage(event.getPlayer().getUniqueId());
            if (storage == null) {
                return; //Not possible.
            }
            Inventory bin = storage.collection_bin;
            if (bin == null)
                return;
            
            int i = 0;
            for (ItemStack stack : bin.getContents())
                if (stack != null && stack.getType() != Material.AIR)
                	i++;
            if (i == 0) {
                //storage.clearCollectionBin();
            	DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
            	storage.collection_bin = null;
            }
        }
    }


    @EventHandler
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTradeInvClicked(InventoryClickEvent event) {
        if (GameAPI.isShop(event.getInventory())) return;
        Player player = (Player) event.getWhoClicked();

//        System.out.println("---------");
//        System.out.println("First trade click: " + event.getSlot() + " From " + event.getInventory().getName() + " Slot: " + event.getRawSlot() + " Current: " + event.getCurrentItem() + " Cursor: " + event.getCursor());
//        if (event.getInventory().getTitle().contains(player.getName())) {
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
            if (!vi.isSoulboundBypass(trade.getOppositePlayer(player)) || vi.isUntradeable()) {
            	player.sendMessage(ChatColor.RED + "You can't trade this item.");
                event.setCancelled(true);
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
                if (status.equalsIgnoreCase("ready")) {
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
                } else {
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
            Player clicker = (Player) event.getWhoClicked();
            trade.p1.sendMessage(ChatColor.RED + "Trade modified by " + ChatColor.BOLD.toString() + clicker.getName());
            trade.p2.sendMessage(ChatColor.RED + "Trade modified by " + ChatColor.BOLD.toString() + clicker.getName());
            trade.changeReady();
            trade.setDividerColor(DyeColor.RED);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (trade != null && trade.inv != null && trade.inv.getViewers().size() > 0)
                    trade.setDividerColor(DyeColor.WHITE);
            }, 20L);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerClickStatsInventory(InventoryClickEvent event) {
        if (GameAPI.isShop(event.getInventory())) return;
        if (event.getInventory().getTitle().contains("Stat Points")) {
            //Stat Points Inv
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Player p = (Player) event.getWhoClicked();
            PlayerStats stats = StatsManager.getPlayerStats(p);

            if (event.getCurrentItem() != null && slot >= 2 && slot < 6) {
                final Inventory inv = event.getInventory();
                int amount = event.isShiftClick() ? 3 : 1;
                Stats stat = Stats.values()[slot - 2];

                if (event.getClick() == ClickType.MIDDLE) {

                    p.sendMessage(ChatColor.GREEN + "Type a custom allocated amount.");
                    stats.reset = false;

                    int currentFreePoints = GameAPI.getGamePlayer(p).getStats().getFreePoints();

                    Chat.listenForNumber(p, 0, currentFreePoints, num -> {
                        for (int i = 0; i < num; i++)
                            stats.allocatePoint(stat, inv);
                        p.openInventory(inv);
                    }, () -> {
                        p.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                        stats.resetTemp();
                    });

                } else {
                    for (int i = 0; i < amount; i++) {
                        if (event.isRightClick())
                            stats.removePoint(stat, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint(stat, inv);
                    }
                }
            }

            if (slot == 6) {
                stats.confirmStats();
                p.closeInventory();
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
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
            /*if (event.getCurrentItem() != null && !(GameAPI.isItemTradeable(event.getCurrentItem()))) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }*/
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDoWeirdArmorThing(InventoryClickEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (!(event.getAction() == InventoryAction.HOTBAR_SWAP)) return;
        if (!(event.getSlotType() == InventoryType.SlotType.ARMOR)) return;
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        event.getWhoClicked().sendMessage(ChatColor.RED + "Please do not try to equip armor this way!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerShiftClickWithImportantItem(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getClick().isShiftClick()) {
            Inventory clicked = event.getInventory();
            if (clicked == event.getWhoClicked().getInventory()) {
                ItemStack clickedOn = event.getCurrentItem();
                if (clickedOn != null) {
                    if (clickedOn.getType() == Material.SADDLE || clickedOn.getType() == Material.EYE_OF_ENDER || clickedOn.getType() == Material.NAME_TAG) {
                        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(clickedOn);
                        NBTTagCompound tag = nmsStack.getTag();
                        if (tag == null) return;
                        if (!(tag.getString("type").equalsIgnoreCase("important"))) return;
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerClickWithImportantItem(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getInventory().getName().equals("Party Loot Selection")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            return;
        }

        Inventory clicked = event.getInventory();
        if (clicked != event.getWhoClicked().getInventory()) {
            ItemStack onCursor = event.getCursor();
            if (onCursor != null) {
                if (onCursor.getType() == Material.SADDLE || onCursor.getType() == Material.EYE_OF_ENDER || onCursor.getType() == Material.NAME_TAG || onCursor.getType() == Material.LEASH) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(onCursor);
                    NBTTagCompound tag = nmsStack.getTag();
                    if (tag == null) return;
                    if (!(tag.getString("type").equalsIgnoreCase("important"))) return;
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDragImportantItem(InventoryDragEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        ItemStack dragged = event.getOldCursor();
        if (dragged != null) {
            if (dragged.getType() == Material.SADDLE || dragged.getType() == Material.EYE_OF_ENDER || dragged.getType() == Material.NAME_TAG || dragged.getType() == Material.LEASH) {
                net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(dragged);
                NBTTagCompound tag = nmsStack.getTag();
                if (tag == null) return;
                if (!(tag.getString("type").equalsIgnoreCase("important"))) return;
                int inventorySize = event.getInventory().getSize();
                for (int i : event.getRawSlots()) {
                    if (i < inventorySize) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerApplyMuleUpgrade(InventoryClickEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        if (event.getCurrentItem() != null && event.getCursor() != null &&
                event.getCursor().getType() != Material.AIR && event.getCurrentItem().getType() != Material.AIR) {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();

            Player pl = (Player) event.getWhoClicked();
            if (current.getType() == Material.LEASH && cursor.getType() == Material.CHEST) {
                //Check for mule upgrade?
                net.minecraft.server.v1_9_R2.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursor);
                net.minecraft.server.v1_9_R2.ItemStack nmsCurrent = CraftItemStack.asNMSCopy(current);
                if (nmsCursor.hasTag() && nmsCurrent.hasTag()) {
                    NBTTagCompound tag = nmsCursor.getTag();
                    //Mule upgrade item.
                    if (tag.hasKey("usage") && tag.hasKey("muleLevel") && tag.getString("usage").equals("muleUpgrade")) {
                        NBTTagCompound currentTag = nmsCurrent.getTag();
                        if (currentTag.hasKey("usage") && currentTag.hasKey("muleTier") && currentTag.getString("usage").equals("mule")) {
                            event.setCancelled(true);
                            event.setResult(Event.Result.DENY);
                            //Upgrading mule.
                            //Check if its the right upgrade.
                            int upgradeLevel = tag.getInt("muleLevel");
                            int currentTier = currentTag.getInt("muleTier");

                            if (currentTier + 1 < upgradeLevel || currentTier == upgradeLevel) {
                                //Cant upgrade.
                                pl.sendMessage(ChatColor.RED + "You cannot apply this upgrade to this mule!");
                                return;
                            }

                            if (event.getCursor().getAmount() > 1) {
                                cursor.setAmount(cursor.getAmount() - 1);
                                pl.setItemOnCursor(cursor);
                            } else {
                                event.setCursor(null);
                                pl.setItemOnCursor(null);
                            }

                            MuleTier newTier = MuleTier.getByTier(upgradeLevel);
                            if (newTier == null) {
                                pl.sendMessage(ChatColor.RED + "Unable to find proper upgrade level.");
                                return;
                            }
                            pl.sendMessage(ChatColor.GREEN + "Mule upgraded to " + newTier.getName() + "!");

                            DatabaseAPI.getInstance().update(pl.getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, newTier.getTier(), true, true, null);

                            if (MountUtils.inventories.containsKey(pl.getUniqueId())) {
                                Inventory inv = MountUtils.inventories.get(pl.getUniqueId());
                                //Close all people viewing this inventory.
                                Lists.newArrayList(inv.getViewers()).forEach(HumanEntity::closeInventory);

                                if (newTier.getSize() != inv.getSize()) {
                                    Inventory upgradeInventory = Bukkit.createInventory(null, newTier.getSize(), "Mule Storage");
                                    //Upgrade that shit.
                                    for (int i = 0; i < inv.getSize(); i++) {
                                        //Set that inventory of the items.
                                        if (upgradeInventory.getSize() > i)
                                            upgradeInventory.setItem(i, inv.getItem(i));
                                    }

                                    //Clear the old inventory.
                                    inv.clear();
                                    MountUtils.inventories.put(pl.getUniqueId(), upgradeInventory);
                                }
                            }

                            ItemStack newMule = new ItemMuleMount().generateItem();

                            ItemStack[] contents = pl.getInventory().getContents();
                            contents[event.getSlot()] = newMule;
                            pl.getInventory().setContents(contents);
                            pl.updateInventory();
                            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void craftingInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            player.getOpenInventory().getTopInventory().setItem(1, null);
            player.getOpenInventory().getTopInventory().setItem(2, null);
        }
    }
}
