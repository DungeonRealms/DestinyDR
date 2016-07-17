package net.dungeonrealms.game.listener.inventory;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.commands.CommandModeration;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handlers.ClickHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelOfferClick(InventoryClickEvent e) {
        if (!e.getInventory().getTitle().contains("VS.")) return;
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            e.setCancelled(true);
            return;
        }
        Player p = (Player) e.getWhoClicked();
        DuelOffer offer = DuelingMechanics.getOffer(p.getUniqueId());
        if (offer == null) {
            p.closeInventory();
            return;
        }
        if (e.getRawSlot() > offer.sharedInventory.getSize()) return;

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BONE) {
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

        if (offer.isLeftSlot(e.getRawSlot())) {
            if (!offer.isLeftPlayer(p)) {
                e.setCancelled(true);
                return;
            }
        } else {
            if (offer.isLeftPlayer(p)) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        ItemStack stackClicked = e.getCurrentItem();
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
        if (nms.hasTag() && nms.getTag().hasKey("status")) {
            String status = nms.getTag().getString("status");
            e.setCancelled(true);
            if (status.equalsIgnoreCase("ready")) {
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
            } else {
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


    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!CommandModeration.offline_inv_watchers.containsKey(event.getPlayer().getUniqueId())) return;

        UUID target = CommandModeration.offline_inv_watchers.get(event.getPlayer().getUniqueId());

        String inventory = ItemSerialization.toString(event.getInventory());
        DatabaseAPI.getInstance().update(target, EnumOperators.$SET, EnumData.INVENTORY, inventory, false);
        GameAPI.updatePlayerData(target);

        CommandModeration.offline_inv_watchers.remove(event.getPlayer().getUniqueId());
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void editPlayerAmor(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().contains("Armor")) return;
        String playerArmor = event.getInventory().getTitle().split(" ")[0];
        Player player = Bukkit.getPlayer(playerArmor);
        if (player != null) {
            ItemStack[] contents = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                if (event.getInventory().getItem(i) != null &&
                        event.getInventory().getItem(i).getType() != Material.AIR &&
                        GameAPI.isArmor(event.getInventory().getItem(i))) {
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
        if (!GameAPI.isArmor(event.getNewArmorPiece()) && !GameAPI.isArmor(event.getOldArmorPiece())) return;
        if (event.getNewArmorPiece() != null && event.getNewArmorPiece().getType() != Material.AIR) {
            Attribute a = new Attribute(event.getNewArmorPiece());
            int playerLevel = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, player.getUniqueId());
            if (playerLevel < a.getItemTier().getRangeValues()[0]) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + "cannot" + ChatColor.RED + " equip this " +
                        "item! You must be level: " + ChatColor.BOLD + a.getItemTier().getRangeValues()[0]);
                player.updateInventory();
                return;
            }
        }
        if (!CombatLog.isInCombat(player)) {
            if (GameAPI.getGamePlayer(player) == null) {
                return;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            // KEEP THIS DELAY IT PREVENTS ARMOR STACKING
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                handleArmorDifferences(event.getOldArmorPiece(), event.getNewArmorPiece(), player);
                HealthHandler.getInstance().setPlayerMaxHPLive(player, GameAPI.getStaticAttributeVal(ArmorAttributeType.HEALTH_POINTS, player) + 50);
                HealthHandler.getInstance().setPlayerHPRegenLive(player, GameAPI.getStaticAttributeVal(ArmorAttributeType.HEALTH_REGEN, player) + 5);
                if (HealthHandler.getInstance().getPlayerHPLive(player) > HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    HealthHandler.getInstance().setPlayerHPLive(player, HealthHandler.getInstance().getPlayerMaxHPLive(player));
                }
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
        if (!GameAPI.isArmor(newArmor) && !GameAPI.isArmor(oldArmor)) return;

        // recalculate attributes
        GameAPI.calculateAllAttributes(p);

        String oldArmorName = (oldArmor == null || oldArmor.getType() == Material.AIR) ? "NOTHING" : oldArmor.getItemMeta().getDisplayName();
        String newArmorName = (newArmor == null || newArmor.getType() == Material.AIR) ? "NOTHING" : newArmor.getItemMeta().getDisplayName();
        GamePlayer gp = GameAPI.getGamePlayer(p);

        // display differences to player
        p.sendMessage(ChatColor.GRAY + "" + oldArmorName + "" + ChatColor.WHITE +
                ChatColor.BOLD + " -> " + ChatColor.GRAY + "" + newArmorName + "");
        if (!GameAPI.isArmor(newArmor)) { // unequipping armor
            List<String> oldModifiers = GameAPI.getModifiers(oldArmor);
            assert oldModifiers != null;
            net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldArmor).getTag();
            // iterate through to get decreases from stats not in the new armor
            for (String modifier : oldModifiers) {
                // get the tag name (in case the stat is a range, in which case compare max values)
                String tagName = oldTag.hasKey(modifier) ? modifier : modifier + "Max";
                int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                // calculate new values
//                Integer[] newTotalVal = type.isRange()
//                        ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
//                        gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
//                        : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
//                gp.setAttributeVal(type, newTotalVal);
                Integer[] newTotalVal = gp.getAttributes().get(type.getNBTName());
                if (oldArmorVal != 0) { // note the decrease to the p
                    p.sendMessage(ChatColor.RED + "-" + oldArmorVal
                            + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                            + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                }
            }
        } else { // equipping armor
            List<String> newModifiers = GameAPI.getModifiers(newArmor);
            assert newModifiers != null;
            net.minecraft.server.v1_9_R2.NBTTagCompound newTag = CraftItemStack.asNMSCopy(newArmor).getTag();

            if (GameAPI.isArmor(oldArmor)) { // switching armor
                List<String> oldModifiers = GameAPI.getModifiers(oldArmor);
                net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldArmor).getTag();
                // get differences
                for (String modifier : newModifiers) {
                    // get the attribute type to determine if we need a percentage or not and to get the
                    // correct display name
                    ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                    // get the tag name (in case the stat is a range, in which case compare max values)
                    String tagName = type.isRange() ? modifier + "Max" : modifier;
                    // get the tag values (if the armor piece doesn't have the modifier, set equal to 0)
                    int newArmorVal = newTag.hasKey(tagName) ? newTag.getInt(tagName) : 0;
                    int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                    // calculate new values
//                    Integer[] newTotalVal;
//                    if (type.isRange()) {
//                        newTotalVal = gp.changeAttributeVal(type, new Integer[]{newTag.getInt(modifier + "Min") -
//                                oldTag.getInt(modifier + "Min"), newTag.getInt(modifier + "Max") - oldTag.getInt
//                                (modifier + "Max")});
//                    } else {
//                        newTotalVal = gp.changeAttributeVal(type, new Integer[]{0, newTag.getInt(modifier) -
//                                oldTag.getInt(modifier)});
//                    }
                    Integer[] newTotalVal = gp.getAttributes().get(type.getNBTName());
                    if (newArmorVal >= oldArmorVal) { // increase in the stat
                        p.sendMessage(ChatColor.GREEN + "+" + (newArmorVal - oldArmorVal)
                                + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                    } else { // decrease in the stat
                        p.sendMessage(ChatColor.RED + "-" + (oldArmorVal - newArmorVal)
                                + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                    }
                }
                // iterate through to get decreases from stats not in the new armor
                oldModifiers.removeAll(newModifiers);
                for (String modifier : oldModifiers) {
                    ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                    String tagName = type.isRange() ? modifier + "Max" : modifier;
                    int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                    Integer[] newTotalVal = gp.getAttributes().get(type.getNBTName());
//                    Integer[] newTotalVal = type.isRange()
//                            ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
//                            gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
//                            : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
//                    gp.setAttributeVal(type, newTotalVal);
                    if (oldArmorVal != 0) { // note the decrease to the player
                        p.sendMessage(ChatColor.RED + "-" + oldArmorVal
                                + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                    }
                }
            } else { // only equipping
                for (String modifier : newModifiers) {
                    // get the attribute type to determine if we need a percentage or not and to get the
                    // correct display name
                    ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                    // get the tag name (in case the stat is a range, in which case compare max values)
                    String tagName = type.isRange() ? modifier + "Max" : modifier;
                    // calculate new values
//                    Integer[] newTotalVal = type.isRange()
//                            ? new Integer[]{gp.getRangedAttributeVal(type)[0] + newTag.getInt(modifier + "Min"),
//                            gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier + "Max")}
//                            : new Integer[]{0, gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier)};
                    // get the tag values (if the armor piece doesn't have the modifier, set equal to 0)
                    int newArmorVal = newTag.hasKey(tagName) ? newTag.getInt(tagName) : 0;
                    Integer[] newTotalVal = gp.getAttributes().get(type.getNBTName());
//                    gp.setAttributeVal(type, newTotalVal);
                    p.sendMessage(ChatColor.GREEN + "+" + newArmorVal
                            + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                            + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                }
            }
        }
//        GameAPI.recalculateStatBonuses(gp.getAttributes(), gp.getAttributeBonusesFromStats(), gp);
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
            if (offer == null) return;
            if (!offer.p1Ready || !offer.p2Ready) {
                offer.giveBackItems();
                DuelingMechanics.removeOffer(offer);
                Player p1 = Bukkit.getPlayer(offer.player1);
                if (p1 != null)
                    p1.closeInventory();
                Player p2 = Bukkit.getPlayer(offer.player2);
                if (p2 != null)
                    p2.closeInventory();
            }
        } else if (event.getInventory().getTitle().contains("Storage Chest")) {
            Storage storage = BankMechanics.getInstance().getStorage(event.getPlayer().getUniqueId());
            storage.inv.setContents(event.getInventory().getContents());
        } else if (event.getInventory().getTitle().contains("Trade Window")) {
            Player p = (Player) event.getPlayer();
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
        } else if (event.getInventory().getTitle().contains("Collection Bin")) {
            Storage storage = BankMechanics.getInstance().getStorage(event.getPlayer().getUniqueId());
            Inventory bin = storage.collection_bin;
            if (bin == null)
                return;
            int i = 0;
            for (ItemStack stack : bin.getContents()) {
                if (stack == null || stack.getType() == Material.AIR)
                    continue;
                i++;
            }
            if (i == 0) {
                DatabaseAPI.getInstance().update(storage.ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
                storage.collection_bin = null;
            }
        }
    }

    /**
     * @param event
     * @since 1.0 handles Trading inventory items.
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTradeInvClicked(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("Trade Window")) {
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            Trade trade = TradeManager.getTrade(event.getWhoClicked().getUniqueId());
            if (trade == null) {
                return;
            }

            if (event.getCurrentItem() == null)
                return;

            if (!GameAPI.isItemTradeable(event.getCurrentItem()) || !GameAPI.isItemDroppable(event.getCurrentItem())) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can't trade this item.");
                event.setCancelled(true);
                return;
            }


            int slot = event.getRawSlot();
            if (slot >= 36)
                return;

            if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }

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
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseOrbs(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_9_R2.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        if (cursorItem.getType() != Material.MAGMA_CREAM || !nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || nmsCursor.getTag().hasKey("type") && !nmsCursor.getTag().getString("type").equalsIgnoreCase("orb"))
            return;
        ItemStack slotItem = event.getCurrentItem();
        if (!GameAPI.isWeapon(slotItem) && !GameAPI.isArmor(slotItem)) return;
        if (slotItem == null || slotItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) {
            return;
        }
        if (gp.getLevel() < 30) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must be level 30 to use Orbs of Alteration");
            return;
        }
        if (player.hasMetadata("last_orb_use")) {
            if ((System.currentTimeMillis() - player.getMetadata("last_orb_use").get(0).asLong()) < (500)) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }
        player.setMetadata("last_orb_use", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        gp.getPlayerStatistics().setOrbsUsed(gp.getPlayerStatistics().getOrbsUsed() + 1);
        event.setCancelled(true);
        if (cursorItem.getAmount() == 1) {
            event.setCursor(new ItemStack(Material.AIR));
        } else {
            ItemStack newStack = cursorItem.clone();
            newStack.setAmount(newStack.getAmount() - 1);
            event.setCursor(newStack);
        }

        ItemStack oldItem = CraftItemStack.asCraftCopy(slotItem);

        ItemStack item = new ItemGenerator().setReroll(true).setSoulbound(GameAPI.isItemSoulbound(slotItem)).setUntradeable(GameAPI.isItemUntradeable(slotItem))
                .setPermanentlyUntradeable(GameAPI.isItemPermanentlyUntradeable(slotItem)).setOrigItem(slotItem).generateItem().getItem();
        event.setCurrentItem(item);

        ItemStack newItem = event.getCurrentItem();

        if (oldItem.hasItemMeta() && oldItem.getItemMeta().hasDisplayName()) {
            // Copy name if name is custom
            String name = oldItem.getItemMeta().getDisplayName();
            if (name.contains(ChatColor.ITALIC.toString()) && name.contains("EC")) { // Custom
                // E-CASH
                // name.
                ItemMeta im = newItem.getItemMeta();
                im.setDisplayName(name); // Set it to the old name.
                newItem.setItemMeta(im);
            }
        }

        player.updateInventory();

        if (oldItem.getItemMeta().getLore().size() < newItem.getItemMeta().getLore().size()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FIREWORKS_SPARK, player.getLocation().add(0, 2.5, 0), new Random().nextFloat(), new Random().nextFloat(),
                        new Random().nextFloat(), 0.75F, 100);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } else {
            // FAIL. Same or worse.
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0F, 1.25F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LAVA, player.getLocation().add(0, 2.5, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(),
                        1F, 75);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        if (EnchantmentAPI.getEnchantLvl(newItem) >= 4) {
            // Glowing effect.
            EnchantmentAPI.addGlow(newItem);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftingInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            player.getOpenInventory().getTopInventory().setItem(1, null);
            player.getOpenInventory().getTopInventory().setItem(2, null);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseEnchant(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_9_R2.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        if (cursorItem.getType() != Material.EMPTY_MAP || !nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type"))
            return;
        ItemStack slotItem = event.getCurrentItem();
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(slotItem);
        if (!GameAPI.isWeapon(slotItem) && !GameAPI.isArmor(slotItem) && !Fishing.isDRFishingPole(slotItem) && !Mining.isDRPickaxe(slotItem))
            return;
        event.setCancelled(true);
        GamePlayer gamePlayer = GameAPI.getGamePlayer((Player) event.getWhoClicked());
        if (gamePlayer == null) return;


        if (nmsCursor.getTag().getString("type").equalsIgnoreCase("protection")) {
            if (!EnchantmentAPI.isItemProtected(slotItem)) {
                int tier = nmsCursor.getTag().getInt("tier");
                int itemTier;
                itemTier = nmsItem.getTag().getInt("itemTier");
                if (tier != itemTier) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This protection scroll is made for a higher tier!");
                    return;
                }
                if (EnchantmentAPI.getEnchantLvl(slotItem) > 8) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This item can no longer be protected!");
                    return;
                }

                event.setCurrentItem(EnchantmentAPI.addItemProtection(event.getCurrentItem()));
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your " + event.getCurrentItem().getItemMeta().getDisplayName() + ChatColor.GREEN + " is now protected -- even if an enchant scroll fails, it will " + ChatColor.UNDERLINE + "NOT" + ChatColor.GREEN + " be destroyed up to +8 status.");
                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
                Firework fw = (Firework) event.getWhoClicked().getWorld().spawnEntity(event.getWhoClicked().getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();
                FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.GREEN).withFade(Color.GREEN).with(FireworkEffect.Type.STAR).trail(true).build();
                fwm.addEffect(effect);
                fwm.setPower(0);
                fw.setFireworkMeta(fwm);
                if (cursorItem.getAmount() == 1) {
                    event.setCursor(new ItemStack(Material.AIR));
                } else {
                    ItemStack newStack = cursorItem.clone();
                    newStack.setAmount(newStack.getAmount() - 1);
                    event.setCursor(newStack);
                }
            } else {
                event.getWhoClicked().sendMessage(ChatColor.RED + "Item already protected.");
            }
            return;
        }

        if (GameAPI.isWeapon(slotItem)) {
            if (!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("weaponenchant")) {
                return;
            }

            int tier = nmsCursor.getTag().getInt("tier");
            if (tier != new Attribute(slotItem).getItemTier().getTierId()) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can not use that enchant scroll on this weapon.");
                return;
            }

            int amount = 0;
            if (nmsItem.getTag().hasKey("enchant")) {
                amount = nmsItem.getTag().getInt("enchant");
            }

            boolean failed = false;
            if (amount < 4) {
                failed = false;
            } else {
                if (amount >= 12) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This item is already enchanted +12, cannot apply more stats.");
                    event.setCancelled(true);
                    ((Player) event.getWhoClicked()).updateInventory();
                    return;
                }
                int win_chance = new Random().nextInt(100);
                int fail = 0;
                if (amount >= 3) {
                    switch (amount) {
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
                    if (win_chance < fail) {
                        failed = true;
                        // Fail.
                    } else if (win_chance >= fail) {
                        failed = false;
                    }
                }
            }
            if (failed) {
                event.setCancelled(true);
                if (cursorItem.getAmount() == 1) {
                    event.setCursor(new ItemStack(Material.AIR));
                } else {
                    ItemStack newStack = cursorItem.clone();
                    newStack.setAmount(newStack.getAmount() - 1);
                    event.setCursor(newStack);
                }
                if (amount <= 8) {
                    if (EnchantmentAPI.isItemProtected(slotItem)) {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Your enchantment scroll " + ChatColor.UNDERLINE + "FAILED" + ChatColor.RED + " but since you had white scroll protection, your item did not vanish.");
                        ItemStack item = slotItem.clone();
                        ItemStack stack = EnchantmentAPI.removeItemProtection(item);
                        event.setCurrentItem(stack);
                        return;
                    }
                }
                event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0F, 1.25F);

                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LAVA, event.getWhoClicked().getLocation().add(0, 2.5, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 75);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your item VANISHED");
                event.setCurrentItem(new ItemStack(Material.AIR));
                gamePlayer.getPlayerStatistics().setFailedEnchants(gamePlayer.getPlayerStatistics().getFailedEnchants() + 1);
                return;
            }

            ItemMeta meta2 = slotItem.getItemMeta();
            String itemName = meta2.getDisplayName();
            ArrayList<String> lore = (ArrayList<String>) meta2.getLore();

            String newName;
            if (amount == 0) {
                newName = itemName;
            } else {
                newName = itemName.substring((itemName.lastIndexOf("]") + 2), itemName.length());
            }

            String finalName = ChatColor.RED + "[" + "+" + (amount + 1) + "] " + newName;
            double doublenewDamageMin = nmsItem.getTag().getInt("damageMin") + ((5 * nmsItem.getTag().getInt("damageMin")) / 100);
            double doublenewDamageMax = nmsItem.getTag().getInt("damageMax") + ((5 * nmsItem.getTag().getInt("damageMax")) / 100);
            int finalDmgMin = (int) Math.round(doublenewDamageMin) + 1;
            int finalDmgMax = (int) Math.round(doublenewDamageMax) + 1;

            // update the item lore
            lore.set(0, ChatColor.RED + "DMG: " + finalDmgMin + " - " + finalDmgMax);

            // update the NMS tags
            nmsItem.getTag().setInt("enchant", amount + 1);
            nmsItem.getTag().setInt("damageMin", finalDmgMin);
            nmsItem.getTag().setInt("damageMax", finalDmgMax);
            ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);


            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(finalName);
            meta.setLore(lore);
            newItem.setItemMeta(meta);
            if (EnchantmentAPI.isItemProtected(slotItem)) {
                newItem = EnchantmentAPI.removeItemProtection(newItem);
            }
            if (cursorItem.getAmount() == 1) {
                event.setCursor(new ItemStack(Material.AIR));
            } else {
                ItemStack newStack = cursorItem.clone();
                newStack.setAmount(newStack.getAmount() - 1);
                event.setCursor(newStack);
            }
            event.getCurrentItem().setType(Material.AIR);
            event.setCurrentItem(new ItemStack(Material.AIR));
            if ((amount + 1) >= 4)
                EnchantmentAPI.addGlow(newItem);
            event.getWhoClicked().getInventory().addItem(newItem);
            ((Player) event.getWhoClicked()).updateInventory();
            event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            Firework fw = (Firework) event.getWhoClicked().getWorld().spawnEntity(event.getWhoClicked().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
            gamePlayer.getPlayerStatistics().setSuccessfulEnchants(gamePlayer.getPlayerStatistics().getSuccessfulEnchants() + 1);
        } else if (GameAPI.isArmor(slotItem)) {
            if (!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("armorenchant")) {
                return;
            }
            int tier = nmsCursor.getTag().getInt("tier");
            int armorTier = nmsItem.getTag().getInt("itemTier");
            if (tier != armorTier) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can not use that enchant scroll on this armor.");
                return;
            }

            int amount = 0;
            if (nmsItem.getTag().hasKey("enchant")) {
                amount = nmsItem.getTag().getInt("enchant");
            }

            boolean failed = false;
            if (amount < 4) {
                failed = false;
            } else {
                if (amount >= 12) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "This item is already enchanted +12, cannot apply more stats.");
                    event.setCancelled(true);
                    ((Player) event.getWhoClicked()).updateInventory();
                    return;
                }
                int win_chance = new Random().nextInt(100);
                int fail = 0;
                if (amount >= 3) {
                    switch (amount) {
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
                    if (win_chance < fail) {
                        failed = true;
                        // Fail.
                    } else if (win_chance >= fail) {
                        failed = false;
                    }
                }
            }
            if (failed) {

                event.setCancelled(true);
                if (cursorItem.getAmount() == 1) {
                    event.setCursor(new ItemStack(Material.AIR));
                } else {
                    ItemStack newStack = cursorItem.clone();
                    newStack.setAmount(newStack.getAmount() - 1);
                    event.setCursor(newStack);
                }

                if (amount <= 8) {
                    if (EnchantmentAPI.isItemProtected(slotItem)) {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Your enchantment scroll " + ChatColor.UNDERLINE + "FAILED" + ChatColor.RED + " but since you had white scroll protection, your item did not vanish.");
                        ItemStack item = slotItem.clone();
                        ItemStack stack = EnchantmentAPI.removeItemProtection(item);
                        event.setCurrentItem(stack);
                        return;
                    }
                }

                event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0F, 1.25F);

                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LAVA, event.getWhoClicked().getLocation().add(0, 2.5, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 75);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your item VANISHED");
                event.setCurrentItem(new ItemStack(Material.AIR));
                gamePlayer.getPlayerStatistics().setFailedEnchants(gamePlayer.getPlayerStatistics().getFailedEnchants() + 1);
                return;
            }

            ItemMeta meta2 = slotItem.getItemMeta();
            String itemName = meta2.getDisplayName();
            String newName;
            if (amount == 0) {
                newName = itemName;
            } else {
                newName = itemName.substring((itemName.lastIndexOf("]") + 2), itemName.length());
            }

            String finalName = ChatColor.RED + "[" + "+" + (amount + 1) + "] " + newName;
            List<String> itemLore = slotItem.getItemMeta().getLore();

            double hpDouble = nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_POINTS.getNBTName()) + ((nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_POINTS.getNBTName()) * 5) / 100);
            int newHP = (int) Math.round((hpDouble));
            itemLore.set(1, ChatColor.RED + "HP: +" + newHP);
            nmsItem.getTag().setInt(ArmorAttributeType.HEALTH_POINTS.getNBTName(), newHP);

            if (nmsItem.getTag().hasKey(ArmorAttributeType.HEALTH_REGEN.getNBTName())) {
                double hpRegenDouble = nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_REGEN.getNBTName()) + ((nmsItem.getTag().getInt(ArmorAttributeType.HEALTH_REGEN.getNBTName()) * 5) / 100);
                int newHPRegen = (int) Math.round((hpRegenDouble));
                nmsItem.getTag().setInt(ArmorAttributeType.HEALTH_REGEN.getNBTName(), newHPRegen);
                itemLore.set(2, ChatColor.RED + "HP REGEN: +" + newHPRegen + " HP/s");
            } else if (nmsItem.getTag().hasKey(ArmorAttributeType.ENERGY_REGEN.getNBTName())) {
                double energyRegen = nmsItem.getTag().getInt(ArmorAttributeType.ENERGY_REGEN.getNBTName());
                int newEnergyRegen = (int) Math.round((energyRegen)) + 1;
                nmsItem.getTag().setInt(ArmorAttributeType.ENERGY_REGEN.getNBTName(), newEnergyRegen);
                itemLore.set(2, ChatColor.RED + "ENERGY REGEN: +" + newEnergyRegen + "%");
            }

            ArrayList<String> lore = (ArrayList<String>) meta2.getLore();
            boolean toAdd;
            for (String current : lore) {
                toAdd = true;
                if (current.contains("HP:") || current.contains("HP REGEN:") || current.contains("ENERGY REGEN:")) {
                    continue;
                }
                for (String oldLore : itemLore) {
                    if (ChatColor.stripColor(oldLore).toLowerCase().equals(ChatColor.stripColor(current.toLowerCase()))) {
                        toAdd = false;
                    }
                }
                if (toAdd) {
                    itemLore.add(current);
                }
            }

            nmsItem.getTag().setInt("enchant", amount + 1);
            ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);


            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(finalName);
            meta.setLore(itemLore);
            newItem.setItemMeta(meta);
            if (EnchantmentAPI.isItemProtected(slotItem)) {
                newItem = EnchantmentAPI.removeItemProtection(newItem);
            }
            if (cursorItem.getAmount() == 1) {
                event.setCursor(new ItemStack(Material.AIR));
            } else {
                ItemStack newStack = cursorItem.clone();
                newStack.setAmount(newStack.getAmount() - 1);
                event.setCursor(newStack);
            }
            event.getCurrentItem().setType(Material.AIR);
            event.setCurrentItem(new ItemStack(Material.AIR));
            if ((amount + 1) >= 4)
                EnchantmentAPI.addGlow(newItem);
            event.getWhoClicked().getInventory().addItem(newItem);
            ((Player) event.getWhoClicked()).updateInventory();
            event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            Firework fw = (Firework) event.getWhoClicked().getWorld().spawnEntity(event.getWhoClicked().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
            gamePlayer.getPlayerStatistics().setSuccessfulEnchants(gamePlayer.getPlayerStatistics().getSuccessfulEnchants() + 1);
        } else if (Fishing.isDRFishingPole(slotItem)) {
            if (!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("fishingenchant")) {
                return;
            }

            Fishing.FishingRodEnchant enchant = null;
            int value = 1;
            for (Fishing.FishingRodEnchant tempEnchant : Fishing.FishingRodEnchant.values()) {
                if (nmsCursor.getTag().hasKey(tempEnchant.name())) {
                    enchant = tempEnchant;
                    value = nmsCursor.getTag().getInt(tempEnchant.name());
                    break;
                }
            }

            ItemMeta meta = slotItem.getItemMeta();
            List<String> lore = meta.getLore();

            Iterator<String> i = lore.iterator();

            while (i.hasNext()) {
                String line = i.next();
                if (line.contains(enchant.name))
                    i.remove();
            }


            String clone = lore.get(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.add(ChatColor.RED + enchant.name + " +" + value + "%");
            lore.add(clone);
            meta.setLore(lore);
            slotItem.setItemMeta(meta);


            ItemStack newItem = slotItem.clone();
            event.getCurrentItem().setType(Material.AIR);
            event.setCurrentItem(new ItemStack(Material.AIR));


            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(newItem);
            nms.getTag().setInt(enchant.name(), value);
            event.getWhoClicked().getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
            ((Player) event.getWhoClicked()).updateInventory();


            if (cursorItem.getAmount() == 1) {
                event.setCursor(new ItemStack(Material.AIR));
            } else {
                ItemStack newStack = cursorItem.clone();
                newStack.setAmount(newStack.getAmount() - 1);
                event.setCursor(newStack);
            }


            event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            Firework fw = (Firework) event.getWhoClicked().getWorld().spawnEntity(event.getWhoClicked().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
            gamePlayer.getPlayerStatistics().setSuccessfulEnchants(gamePlayer.getPlayerStatistics().getSuccessfulEnchants() + 1);

        } else if (Mining.isDRPickaxe(slotItem)) {
            if (!nmsCursor.hasTag() || !nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("pickaxeenchant")) {
                return;
            }

            Mining.EnumMiningEnchant enchant = null;
            int value = 1;
            for (Mining.EnumMiningEnchant tempEnchant : Mining.EnumMiningEnchant.values()) {
                if (nmsCursor.getTag().hasKey(tempEnchant.name())) {
                    enchant = tempEnchant;
                    value = nmsCursor.getTag().getInt(tempEnchant.name());
                    break;
                }
            }

            ItemMeta meta = slotItem.getItemMeta();
            List<String> lore = meta.getLore();

            Iterator<String> i = lore.iterator();

            while (i.hasNext()) {
                String line = i.next();
                if (line.contains(enchant.display))
                    i.remove();
            }


            String clone = lore.get(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.add(ChatColor.RED + enchant.display + " +" + value + "%");
            lore.add(clone);
            meta.setLore(lore);
            slotItem.setItemMeta(meta);


            ItemStack newItem = slotItem.clone();
            event.getCurrentItem().setType(Material.AIR);
            event.setCurrentItem(new ItemStack(Material.AIR));


            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(newItem);
            nms.getTag().setInt(enchant.name(), value);
            event.getWhoClicked().getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
            ((Player) event.getWhoClicked()).updateInventory();


            if (cursorItem.getAmount() == 1) {
                event.setCursor(new ItemStack(Material.AIR));
            } else {
                ItemStack newStack = cursorItem.clone();
                newStack.setAmount(newStack.getAmount() - 1);
                event.setCursor(newStack);
            }


            event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            Firework fw = (Firework) event.getWhoClicked().getWorld().spawnEntity(event.getWhoClicked().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
            gamePlayer.getPlayerStatistics().setSuccessfulEnchants(gamePlayer.getPlayerStatistics().getSuccessfulEnchants() + 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAddToGemPouch(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        if (event.getCursor().getType() != Material.EMERALD || event.getCurrentItem().getType() != Material.INK_SACK)
            return;
        ItemStack cursorItem = event.getCursor();
        net.minecraft.server.v1_9_R2.ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursorItem);
        ItemStack slotItem = event.getCurrentItem();
        net.minecraft.server.v1_9_R2.ItemStack nmsSlot = CraftItemStack.asNMSCopy(slotItem);
        Player player = (Player) event.getWhoClicked();
        if (!nmsSlot.hasTag() || !nmsCursor.hasTag()) return;
        if (!nmsSlot.getTag().hasKey("type") || !nmsSlot.getTag().getString("type").equalsIgnoreCase("money"))
            return;
        if (!nmsCursor.getTag().hasKey("type") || !nmsCursor.getTag().getString("type").equalsIgnoreCase("money"))
            return;

        int amount = cursorItem.getAmount();
        int pouchAmount = nmsSlot.getTag().getInt("worth");
        int tier = nmsSlot.getTag().getInt("tier");
        int pouchMax = BankMechanics.getInstance().getPouchMax(tier);

        if (pouchAmount < pouchMax) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            if (pouchAmount + amount > pouchMax) {
                amount = (pouchMax - (pouchAmount + amount)) * -1;
                event.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, pouchMax));
                event.setCursor(BankMechanics.getInstance().createGems(amount));
            } else {
                event.setCursor(null);
                event.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, pouchAmount + amount));
            }
        } else {
            player.sendMessage(ChatColor.RED + "That gem pouch is full!");
        }


    }


    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseScrapItem(InventoryClickEvent event) {
        if (event.getCursor() == null) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        if (slotItem == null || slotItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();
        if (!RepairAPI.isItemArmorScrap(cursorItem)) return;
        if (!RepairAPI.canItemBeRepaired(slotItem)) return;
        if (!(RepairAPI.isItemArmorOrWeapon(slotItem)) && !Mining.isDRPickaxe(slotItem) && !Fishing.isDRFishingPole(slotItem))
            return;
        int scrapTier = RepairAPI.getScrapTier(cursorItem);
        int slotTier = 0;
        if (Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)) {
            if (Mining.isDRPickaxe(slotItem))
                slotTier = Mining.getPickTier(slotItem);
            else {
                slotTier = Fishing.getRodTier(slotItem);
            }
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
            int repairPercent = (int) ((newPercent / 1500.D) * 100);

            for (int i = 0; i < 6; i++) {
                player.getWorld().playEffect(player.getLocation().add(i, 1.3, i), Effect.TILE_BREAK, particleID, 12);
                player.getWorld().playEffect(player.getLocation().add(i, 1.15, i), Effect.TILE_BREAK, particleID, 12);
                player.getWorld().playEffect(player.getLocation().add(i, 1, i), Effect.TILE_BREAK, particleID, 12);
            }
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + repairPercent + "%");
            }
            return;
        }

        if (RepairAPI.isItemArmorOrWeapon(slotItem)) {
            slotTier = RepairAPI.getArmorOrWeaponTier(slotItem);
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
            int repairPercent = (int) ((newPercent / 1500.D) * 100);
            for (int i = 0; i < 6; i++) {
                player.getWorld().playEffect(player.getLocation().add(i, 1.3, i), Effect.TILE_BREAK, particleID, 12);
                player.getWorld().playEffect(player.getLocation().add(i, 1.15, i), Effect.TILE_BREAK, particleID, 12);
                player.getWorld().playEffect(player.getLocation().add(i, 1, i), Effect.TILE_BREAK, particleID, 12);
            }
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + repairPercent + "%");
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
                final Inventory inv = event.getInventory();
                int amount = 1;
                if (event.isShiftClick())
                    amount = 3;
                if (event.getClick() == ClickType.MIDDLE) {
                    p.sendMessage(ChatColor.GREEN + "Type a custom allocated amount.");
                    stats.reset = false;
                    p.closeInventory();
                }
                switch (slot) {
                    case 2:
                        //Strength

                        if (event.getClick() == ClickType.MIDDLE) {

                            Chat.listenForMessage(p, e -> {
                                if (e.getMessage().equalsIgnoreCase("cancel") || e.getMessage().equalsIgnoreCase("c")) {
                                    p.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                                    return;
                                }
                                int number = 0;
                                int currentFreePoints = GameAPI.getGamePlayer(p).getStats().tempFreePoints;
                                try {
                                    number = Integer.parseInt(e.getMessage());
                                } catch (Exception exc) {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number");
                                    stats.resetTemp();
                                    return;
                                }
                                if (number <= 0) {
                                    p.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    stats.resetTemp();

                                } else if (number > currentFreePoints) {
                                    p.sendMessage(ChatColor.GRAY + "You cannot allocate more points than you have stored.");
                                    stats.resetTemp();

                                } else {
                                    for (int i = 0; i < number; i++)
                                        stats.allocatePoint("str", p, inv);
                                    p.openInventory(inv);

                                }
                            }, thing -> {
                                thing.sendMessage(ChatColor.RED + "CUSTOM STAT - CANCELLED");
                                stats.resetTemp();
                            });


                            break;
                        }
                        for (int i = 0; i < amount; i++) {
                            if (event.isRightClick())
                                stats.removePoint("str", p, inv);
                            if (event.isLeftClick())
                                stats.allocatePoint("str", p, inv);
                        }
                        break;
                    case 3:
                        if (event.getClick() == ClickType.MIDDLE) {

                            Chat.listenForMessage(p, e -> {
                                if (e.getMessage().equalsIgnoreCase("cancel") || e.getMessage().equalsIgnoreCase("c")) {
                                    p.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                                    return;
                                }
                                int number = 0;
                                int currentFreePoints = GameAPI.getGamePlayer(p).getStats().tempFreePoints;
                                try {
                                    number = Integer.parseInt(e.getMessage());
                                } catch (Exception exc) {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number");
                                    stats.resetTemp();
                                    return;
                                }
                                if (number <= 0) {
                                    p.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    stats.resetTemp();

                                } else if (number > currentFreePoints) {
                                    p.sendMessage(ChatColor.GRAY + "You cannot allocate more points than you have stored.");
                                    stats.resetTemp();

                                } else {
                                    for (int i = 0; i < number; i++)
                                        stats.allocatePoint("dex", p, inv);
                                    p.openInventory(inv);

                                }
                            }, thing -> {
                                thing.sendMessage(ChatColor.RED + "CUSTOM STAT - CANCELLED");
                                stats.resetTemp();
                            });

                            break;
                        }

                        //Dexterity
                        for (int i = 0; i < amount; i++) {
                            if (event.isRightClick())
                                stats.removePoint("dex", p, inv);
                            if (event.isLeftClick())
                                stats.allocatePoint("dex", p, inv);
                        }
                        break;
                    case 4:

                        if (event.getClick() == ClickType.MIDDLE) {

                            Chat.listenForMessage(p, e -> {
                                if (e.getMessage().equalsIgnoreCase("cancel") || e.getMessage().equalsIgnoreCase("c")) {
                                    p.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                                    return;
                                }
                                int number = 0;
                                int currentFreePoints = GameAPI.getGamePlayer(p).getStats().tempFreePoints;
                                try {
                                    number = Integer.parseInt(e.getMessage());
                                } catch (Exception exc) {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number");
                                    stats.resetTemp();
                                    return;
                                }
                                if (number <= 0) {
                                    p.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    stats.resetTemp();

                                } else if (number > currentFreePoints) {
                                    p.sendMessage(ChatColor.GRAY + "You cannot allocate more points than you have stored.");
                                    stats.resetTemp();

                                } else {
                                    for (int i = 0; i < number; i++)
                                        stats.allocatePoint("int", p, inv);
                                    p.openInventory(inv);

                                }
                            }, thing -> {
                                thing.sendMessage(ChatColor.RED + "CUSTOM STAT - CANCELLED");
                                stats.resetTemp();
                            });

                            break;
                        }

                        //Intellect

                        for (int i = 0; i < amount; i++) {
                            if (event.isRightClick())
                                stats.removePoint("int", p, inv);
                            if (event.isLeftClick())
                                stats.allocatePoint("int", p, inv);
                        }
                        break;
                    case 5:


                        if (event.getClick() == ClickType.MIDDLE) {

                            Chat.listenForMessage(p, e -> {
                                if (e.getMessage().equalsIgnoreCase("cancel") || e.getMessage().equalsIgnoreCase("c")) {
                                    p.sendMessage(ChatColor.RED + "CUSTOM STAT - " + ChatColor.BOLD + "CANCELLED");
                                    return;
                                }
                                int number = 0;
                                int currentFreePoints = GameAPI.getGamePlayer(p).getStats().tempFreePoints;
                                try {
                                    number = Integer.parseInt(e.getMessage());
                                } catch (Exception exc) {
                                    p.sendMessage(ChatColor.RED + "Please enter a valid number");
                                    stats.resetTemp();
                                    return;
                                }
                                if (number <= 0) {
                                    p.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    stats.resetTemp();

                                } else if (number > currentFreePoints) {
                                    p.sendMessage(ChatColor.GRAY + "You cannot allocate more points than you have stored.");
                                    stats.resetTemp();

                                } else {
                                    for (int i = 0; i < number; i++)
                                        stats.allocatePoint("vit", p, inv);
                                    p.openInventory(inv);

                                }
                            }, thing -> {
                                thing.sendMessage(ChatColor.RED + "CUSTOM STAT - CANCELLED");
                                stats.resetTemp();
                            });

                            break;
                        }

                        //Vitality
                        for (int i = 0; i < amount; i++) {
                            if (event.isRightClick())
                                stats.removePoint("vit", p, inv);
                            if (event.isLeftClick())
                                stats.allocatePoint("vit", p, inv);
                        }
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
                        stats.updateDatabase(false);
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

    /**
     * Handles the accepting and denying for repairing items.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerClickRepairInv(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().contains("Repair your item for")) return;
        event.setCancelled(true);
        if (event.getRawSlot() == 3) {
            String string = event.getInventory().getTitle().substring(event.getInventory().getTitle().indexOf(ChatColor.BOLD.toString()) + 2);
            string = string.replace("g?", "");
            int cost = Integer.parseInt(string);
            if (BankMechanics.getInstance().takeGemsFromInventory(cost, (Player) event.getWhoClicked())) {
                ItemStack stack = event.getWhoClicked().getEquipment().getItemInMainHand();
                RepairAPI.setCustomItemDurability(stack, 1500);
                event.getWhoClicked().getEquipment().setItemInMainHand(stack);
                event.getWhoClicked().closeInventory();
            } else {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You do not have " + cost + " gems!");
                event.getWhoClicked().closeInventory();
            }
        } else if (event.getRawSlot() == 5) {
            event.getWhoClicked().closeInventory();
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

                            DatabaseAPI.getInstance().update(pl.getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, newTier.getTier(), false);

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

                            ItemStack newMule = ItemManager.getPlayerMuleItem(newTier);

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
