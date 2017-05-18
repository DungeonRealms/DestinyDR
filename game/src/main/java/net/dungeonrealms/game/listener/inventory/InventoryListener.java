package net.dungeonrealms.game.listener.inventory;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.command.moderation.*;
import net.dungeonrealms.game.handler.ClickHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
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

import java.util.UUID;

/**
 * Created by Nick on 9/18/2015.
 * TODO: Modularize the admin /*see commands.
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

        if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR) && event.getCursor() != null && !event.getCursor().getType().equals(Material.AIR))
            if (event.getSlotType() == InventoryType.SlotType.ARMOR)
                return;

        ClickHandler.getInstance().doClick(event);
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
        if (title.contains("VS.") || title.contains("Bank")
                || GameAPI.isShop(event.getInventory()) || title.contains("Trade")
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

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        // Delay allows calculating stats after the new armor is set.
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> handleArmorDifferences(event.getOldArmorPiece(), event.getNewArmorPiece(), player));
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
        
        wp.calculateAllAttributes(); // To prevent armor stacking, don't use the values we just grabbed
        
        for (AttributeType t : armorChanges.keySet()) {
            ModifierRange armorVal = armorChanges.getAttribute(t);
            ModifierRange newVal = wp.getAttributes().getAttribute(t);

            p.sendMessage((armorVal.getValue() > 0 ? ChatColor.GREEN + "+" : ChatColor.RED + "")
                    + armorVal.getValue() + t.getSuffix()
                    + " " + ChatColor.stripColor(t.getPrefix().split(":")[0]) + " ["
                    + newVal.getValue() + t.getSuffix() + "]");
        }
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

        if (GUIMenu.alwaysCancelInventories.contains(event.getInventory().getName()) && !event.isCancelled()) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Cancelling " + event.getInventory().getName() + " Click for " + event.getWhoClicked().getName() + ", hasnt been cancelled in menu.");
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

                boolean ready = status.equalsIgnoreCase("ready");
                trade.updateReady(event.getWhoClicked().getUniqueId());
                ItemStack item = new ItemStack(Material.INK_SACK);
                item.setDurability(ready ? DyeColor.GRAY.getDyeData() : DyeColor.GREEN.getDyeData());
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
            PlayerWrapper wp = PlayerWrapper.getWrapper(p);
            PlayerStats stats = wp.getPlayerStats();

            if (event.getCurrentItem() != null && slot >= 2 && slot < 6) {
                final Inventory inv = event.getInventory();
                int amount = event.isShiftClick() ? 3 : 1;
                Stats stat = Stats.values()[slot - 2];

                if (event.getClick() == ClickType.MIDDLE) {

                    p.sendMessage(ChatColor.GREEN + "Type a custom allocated amount.");
                    stats.reset = false;
                    int currentFreePoints = stats.getFreePoints();

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
        }
    }
}
