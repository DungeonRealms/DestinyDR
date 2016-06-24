package net.dungeonrealms.game.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.common.collect.Lists;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handlers.ClickHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldEvent;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                        API.isArmor(event.getInventory().getItem(i))) {
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
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (API.getGamePlayer(player) == null) {
                    return;
                }
                HealthHandler.getInstance().setPlayerMaxHPLive(player, API.getGamePlayer(player).getPlayerMaxHP());
                HealthHandler.getInstance().setPlayerHPRegenLive(player, HealthHandler.getInstance().calculateHealthRegenFromItems(player));
                if (HealthHandler.getInstance().getPlayerHPLive(player) > HealthHandler.getInstance().getPlayerMaxHPLive(player)) {
                    HealthHandler.getInstance().setPlayerHPLive(player, HealthHandler.getInstance().getPlayerMaxHPLive(player));
                }
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
        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId()).toString())) {
            String oldArmorName = (oldArmor == null || oldArmor.getType() == Material.AIR) ? "NOTHING" : oldArmor.getItemMeta().getDisplayName();
            String newArmorName = (newArmor == null || newArmor.getType() == Material.AIR) ? "NOTHING" : newArmor.getItemMeta().getDisplayName();
            GamePlayer gp = API.getGamePlayer(p);

            p.sendMessage(ChatColor.GRAY + "" + oldArmorName + "" + ChatColor.WHITE +
                    ChatColor.BOLD + " -> " + ChatColor.GRAY + "" + newArmorName + "");
            if (newArmor == null || newArmor.getType() == Material.AIR) { // unequipping armor
                List<String> oldModifiers = API.getModifiers(oldArmor);
                net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldArmor).getTag();
                // iterate through to get decreases from stats not in the new armor
                oldModifiers.stream().forEach(modifier -> {
                    // get the tag name (in case the stat is a range, in which case compare max values)
                    String tagName = oldTag.hasKey(modifier) ? modifier : modifier + "Max";
                    int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                    ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                    // calculate new values
                    Integer[] newTotalVal = type.isRange()
                            ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                            gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                            : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                    gp.setAttributeVal(type, newTotalVal);
                    if (oldArmorVal != 0) { // note the decrease to the p
                        p.sendMessage(ChatColor.RED + "-" + oldArmorVal
                                + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                    }
                });
            } else { // equipping armor
                List<String> newModifiers = API.getModifiers(newArmor);
                net.minecraft.server.v1_9_R2.NBTTagCompound newTag = CraftItemStack.asNMSCopy(newArmor).getTag();

                if (oldArmor != null && oldArmor.getType() != Material.AIR) { // switching armor
                    List<String> oldModifiers = API.getModifiers(oldArmor);
                    net.minecraft.server.v1_9_R2.NBTTagCompound oldTag = CraftItemStack.asNMSCopy(oldArmor).getTag();
                    // get differences
                    newModifiers.stream().forEach(modifier -> {
                        // get the attribute type to determine if we need a percentage or not and to get the
                        // correct display name
                        ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                        // get the tag name (in case the stat is a range, in which case compare max values)
                        String tagName = type.isRange() ? modifier + "Max" : modifier;
                        // get the tag values (if the armor piece doesn't have the modifier, set equal to 0)
                        int newArmorVal = newTag.hasKey(tagName) ? newTag.getInt(tagName) : 0;
                        int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                        // calculate new values
                        Integer[] newTotalVal;

                        if (type.isRange()) {
                            newTotalVal = gp.changeAttributeVal(type, new Integer[]{newTag.getInt(modifier + "Min") -
                                    oldTag.getInt(modifier + "Min"), newTag.getInt(modifier + "Max") - oldTag.getInt
                                    (modifier + "Max")});
                        }
                        else {
                            newTotalVal = gp.changeAttributeVal(type, new Integer[]{0, newTag.getInt(modifier) -
                                    oldTag.getInt(modifier)});
                        }

                        if (newArmorVal >= oldArmorVal) { // increase in the stat
                            p.sendMessage(ChatColor.GREEN + "+" + (newArmorVal - oldArmorVal)
                                    + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                    + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                        }
                        else { // decrease in the stat
                            p.sendMessage(ChatColor.RED + "-" + (oldArmorVal - newArmorVal)
                                    + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                    + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                        }
                    });
                    // iterate through to get decreases from stats not in the new armor
                    oldModifiers.removeAll(newModifiers);
                    oldModifiers.stream().forEach(modifier -> {
                        ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                        String tagName = type.isRange() ? modifier + "Max" : modifier;
                        int oldArmorVal = oldTag.hasKey(tagName) ? oldTag.getInt(tagName) : 0;
                        Integer[] newTotalVal = type.isRange()
                                ? new Integer[]{gp.getRangedAttributeVal(type)[0] - oldTag.getInt(modifier + "Min"),
                                gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier + "Max")}
                                : new Integer[]{0, gp.getRangedAttributeVal(type)[1] - oldTag.getInt(modifier)};
                        gp.setAttributeVal(type, newTotalVal);
                        if (oldArmorVal != 0) { // note the decrease to the player
                            p.sendMessage(ChatColor.RED + "-" + oldArmorVal
                                    + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                    + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                        }
                    });
                }
                else { // only equipping
                    newModifiers.stream().forEach(modifier -> {
                        // get the attribute type to determine if we need a percentage or not and to get the
                        // correct display name
                        ArmorAttributeType type = ArmorAttributeType.getByNBTName(modifier);
                        // get the tag name (in case the stat is a range, in which case compare max values)
                        String tagName = type.isRange() ? modifier + "Max" : modifier;
                        // calculate new values
                        Integer[] newTotalVal = type.isRange()
                                ? new Integer[] { gp.getRangedAttributeVal(type)[0] + newTag.getInt(modifier + "Min"),
                                gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier + "Max") }
                                : new Integer[] { 0, gp.getRangedAttributeVal(type)[1] + newTag.getInt(modifier) };
                        // get the tag values (if the armor piece doesn't have the modifier, set equal to 0)
                        int newArmorVal = newTag.hasKey(tagName) ? newTag.getInt(tagName) : 0;
                        gp.setAttributeVal(type, newTotalVal);
                        p.sendMessage(ChatColor.GREEN + "+" + newArmorVal
                                + (type.isPercentage() ? "%" : "") + " " + type.getName() + " ["
                                + newTotalVal[1] + (type.isPercentage() ? "%" : "") + "]");
                    });
                }
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
            Trade t = net.dungeonrealms.game.player.trade.TradeManager.getTrade(p.getUniqueId());
            if (t != null)
                if (!t.p1Ready || !t.p2Ready) {
                    t.handleClose();
                }
        } else if (event.getInventory().getTitle().contains("Stat Points")) {
            PlayerStats stat = API.getGamePlayer((Player) event.getPlayer()).getStats();
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
            Trade trade = net.dungeonrealms.game.player.trade.TradeManager.getTrade(event.getWhoClicked().getUniqueId());
            if (trade == null) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot >= 36)
                return;

            if (event.getCurrentItem() == null)
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
        if (API.getGamePlayer((Player) event.getWhoClicked()).getLevel() < 30) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "You must be level 30 to use Orbs of Alteration");
            return;
        }
        ItemStack slotItem = event.getCurrentItem();
        if (!API.isWeapon(slotItem) && !API.isArmor(slotItem)) return;
        if (slotItem == null || slotItem.getType() == Material.AIR) return;
        event.setCancelled(true);
        if (cursorItem.getAmount() == 1) {
            event.setCursor(new ItemStack(Material.AIR));
        } else {
            ItemStack newStack = cursorItem.clone();
            newStack.setAmount(newStack.getAmount() - 1);
            event.setCursor(newStack);
        }
        ItemStack item = new ItemGenerator().setItem(slotItem).reroll().getItem();
        event.setCurrentItem(new ItemStack(Material.AIR));
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getWhoClicked().getInventory().addItem(item));
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
        if (!API.isWeapon(slotItem) && !API.isArmor(slotItem)) return;
        event.setCancelled(true);


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

        if (API.isWeapon(slotItem)) {
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

                if (EnchantmentAPI.isItemProtected(slotItem)) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your protection scroll saved your item from vanishing");
                    event.setCurrentItem(EnchantmentAPI.removeItemProtection(event.getCurrentItem()));
                    return;
                }
                if (cursorItem.getAmount() == 1) {
                    event.setCursor(new ItemStack(Material.AIR));
                } else {
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
            ArrayList<String> lore = (ArrayList<String>) meta2.getLore();

            String newName = "";
            if (amount == 0) {
                newName = itemName;
            } else {
                newName = itemName.substring((itemName.lastIndexOf("]") + 2), itemName.length());
            }

            String finalName = ChatColor.RED + "[" + "+" + (amount + 1) + "] " + newName;
            double doublenewDamageMin = nmsItem.getTag().getInt("damageMin") + ((5 * nmsItem.getTag().getInt("damageMin")) / 100);
            double doublenewDamageMax = nmsItem.getTag().getInt("damageMax") + ((5 * nmsItem.getTag().getInt("damageMax")) / 100);
            if (tier == 1) {
                doublenewDamageMin += 1;
                doublenewDamageMax += 1;
            }
            int finalDmgMin = (int) Math.round(doublenewDamageMin);
            int finalDmgMax = (int) Math.round(doublenewDamageMax);
            Attribute att = new Attribute(slotItem);

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
            if ((amount + 1) > 3)
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
        } else if (API.isArmor(slotItem)) {
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

                if (EnchantmentAPI.isItemProtected(slotItem)) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your protection scroll saved your item from vanishing");
                    event.setCurrentItem(EnchantmentAPI.removeItemProtection(event.getCurrentItem()));
                    return;
                }

                event.getWhoClicked().sendMessage(ChatColor.RED + "While dealing with magical enchants. Your item VANISHED");
                event.setCurrentItem(new ItemStack(Material.AIR));
                return;
            }

            ItemMeta meta2 = slotItem.getItemMeta();
            String itemName = meta2.getDisplayName();
            String newName = "";
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
        if (!nmsSlot.getTag().hasKey("type") || !nmsSlot.getTag().getString("type").equalsIgnoreCase("money")) return;
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
        if (!(RepairAPI.isItemArmorOrWeapon(slotItem)) && !Mining.isDRPickaxe(slotItem) && !Fishing.isDRFishingPole(slotItem)) return;
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
            int repairPercent = (int) ((newPercent / 1500) * 100);

            Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
            ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(((CraftPlayer) player).getHandle(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
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
            int repairPercent = (int) ((newPercent / 1500) * 100);
            Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
            ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(((CraftPlayer) player).getHandle(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
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
            /*if (event.getCurrentItem() != null && !(API.isItemTradeable(event.getCurrentItem()))) {
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
                if (onCursor.getType() == Material.SADDLE || onCursor.getType() == Material.EYE_OF_ENDER || onCursor.getType() == Material.NAME_TAG) {
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
            if (dragged.getType() == Material.SADDLE || dragged.getType() == Material.EYE_OF_ENDER || dragged.getType() == Material.NAME_TAG) {
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
//                            event.getClickedInventory().setItem(event.getRawSlot(), newMule);
//                            pl.updateInventory();
                            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);
                        }
                    }
                }
            }
        }
    }
}
