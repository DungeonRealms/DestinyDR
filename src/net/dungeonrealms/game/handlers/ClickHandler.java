package net.dungeonrealms.game.handlers;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.TradeCalculator;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.entities.utils.PetUtils;
import net.dungeonrealms.game.world.items.EnumItem;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

    static ClickHandler instance = null;

    public static ClickHandler getInstance() {
        if (instance == null) {
            instance = new ClickHandler();
        }
        return instance;
    }

    public void doClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -999) return;

        /*
        Animal Tamer NPC
         */
        switch (name) {
            case "Animal Vendor":
                event.setCancelled(true);
                if (slot > 18) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                    if (playerMounts.contains(nmsStack.getTag().getString("mountType"))) {
                        player.sendMessage(ChatColor.RED + "You already own this mount!");
                        return;
                    } else {
                        if (BankMechanics.getInstance().takeGemsFromInventory(nmsStack.getTag().getInt("mountCost"), player)) {
                            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNTS, nmsStack.getTag().getString("mountType").toUpperCase(), true);
                            player.sendMessage(ChatColor.GREEN + "You have purchased the " + nmsStack.getTag().getString("mountType") + " mount.");
                            player.closeInventory();
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "You cannot afford this mount, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("mountCost") + ChatColor.RED + " Gems.");
                            return;
                        }
                    }
                }
                return;


        /*
        Skill Trainer NPC
         */
            case "Profession Vendor":
                event.setCancelled(true);
                if (slot > 9) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    if (BankMechanics.getInstance().takeGemsFromInventory(100, player)) {
                        switch (slot) {
                            case 0:
                                player.getInventory().addItem(ItemManager.createPickaxe(1));
                                player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                                player.closeInventory();
                                break;
                            case 1:
                                player.getInventory().addItem(ItemManager.createFishingPole(1));
                                player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                                player.closeInventory();
                                break;
                            default:
                                break;
                        }
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this item, you require " + ChatColor.BOLD + "100" + ChatColor.RED + " Gems");
                    }
                    return;
                }
                return;


        /*
        E-Cash Vendor NPC
         */
            case "E-Cash Vendor":
                event.setCancelled(true);
                if (slot > 25) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    if (nmsStack.getTag().hasKey("playerTrailType")) {
                        List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, player.getUniqueId());
                        if (playerTrails.contains(nmsStack.getTag().getString("playerTrailType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this trail!");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PARTICLES, nmsStack.getTag().getString("playerTrailType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + nmsStack.getTag().getString("playerTrailType") + " trail.");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this trail, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    if (nmsStack.getTag().hasKey("mountType")) {
                        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                        if (playerMounts.contains(nmsStack.getTag().getString("mountType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this mount!");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNTS, nmsStack.getTag().getString("mountType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + nmsStack.getTag().getString("mountType") + " mount");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this mount, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    if (nmsStack.getTag().hasKey("petType")) {
                        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, player.getUniqueId());
                        if (playerPets.contains(nmsStack.getTag().getString("petType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this pet.");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, nmsStack.getTag().getString("petType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + nmsStack.getTag().getString("petType") + " pet.");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this pet, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    if (nmsStack.getTag().hasKey("donationStore")) {
                        player.closeInventory();
                        TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
                        bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://shop.dungeonrealms.net"));
                        bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Dungeon Realms Shop").create()));
                        TextComponent test = new TextComponent(ChatColor.RED + "To Purchase E-Cash from our Shop, Click ");
                        test.addExtra(bungeeMessage);
                        player.spigot().sendMessage(test);
                        return;
                    }
                    if (nmsStack.getTag().hasKey("storageExpansion")) {
                        if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                            player.getInventory().addItem(ItemManager.createItem(EnumItem.StorageExpansion));
                            player.sendMessage(ChatColor.GREEN + "You have purchased a Storage Expansion.");
                            player.closeInventory();
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                            return;
                        }
                    }
                    if (nmsStack.getTag().hasKey("repairHammer")) {
                        player.sendMessage(ChatColor.RED + "This is currently not implemented");
                        player.closeInventory();
                    /*if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                        player.getInventory().addItem(ItemManager.createItem(EnumItem.RepairHammer));
                        player.sendMessage(ChatColor.GREEN + "You have purchased Five Repair Hammers.");
                        player.closeInventory();
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                        return;
                    }*/
                    }
                    if (nmsStack.getTag().hasKey("retrainingBook")) {
                        if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                            player.getInventory().addItem(ItemManager.createItem(EnumItem.RetrainingBook));
                            player.sendMessage(ChatColor.GREEN + "You have purchased a Retraining Book.");
                            player.closeInventory();
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                            return;
                        }
                    }
                    if (nmsStack.getTag().hasKey("medalOfGathering")) {
                        player.sendMessage(ChatColor.RED + "This is currently not implemented!");
                        player.closeInventory();
                    /*if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                        player.getInventory().addItem(ItemManager.createItem(EnumItem.MedalOfGathering));
                        player.sendMessage(ChatColor.GREEN + "You have purchased a Medal Of Gathering.");
                        player.closeInventory();
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash!");
                        return;
                    }*/
                    }
                }
                return;


        /*
        Inn Keeper NPC
         */
            case "Hearthstone Re-Location":
                event.setCancelled(true);
                if (slot > 9) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    if (nmsStack.getTag().hasKey("hearthstoneLocation")) {
                        String hearthstoneLocation = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, player.getUniqueId()));
                        if (hearthstoneLocation.equalsIgnoreCase(nmsStack.getTag().getString("hearthstoneLocation"))) {
                            player.sendMessage(ChatColor.RED + "Your Hearthstone is already at this location!");
                            return;
                        } else {
                            if (TeleportAPI.canSetHearthstoneLocation(player, nmsStack.getTag().getString("hearthstoneLocation"))) {
                                if (BankMechanics.getInstance().takeGemsFromInventory(nmsStack.getTag().getInt("gemCost"), player)) {
                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEARTHSTONE, nmsStack.getTag().getString("hearthstoneLocation"), true);
                                    player.sendMessage(ChatColor.GREEN + "Hearthstone set to " + nmsStack.getTag().getString("hearthstoneLocation") + ".");
                                    player.closeInventory();
                                    return;
                                } else {
                                    player.sendMessage(ChatColor.RED + "You do NOT have enough gems for this location, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("gemCost") + ChatColor.RED + " Gems.");
                                    return;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You have not explored the surrounding area of this Hearthstone Location yet");
                                return;
                            }
                        }
                    }
                }
                return;


        /*
        Merchant
         */
            case "Merchant":
                Inventory tradeWindow = event.getInventory();
                if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                    event.setCancelled(true);
                    return;
                }
                if (!(event.isShiftClick()) || (event.isShiftClick() && slot < 27)) {
                    if (!(event.getSlotType() == InventoryType.SlotType.CONTAINER)) {
                        return;
                    }
                    if (event.getInventory().getType() == InventoryType.PLAYER) {
                        return;
                    }
                    if (slot > 26 || slot < 0) {
                        return;
                    }
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18 || slot == 19
                            || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                        player.setItemOnCursor(event.getCursor());
                        player.updateInventory();
                    } else if (!(event.isShiftClick())) {
                        if ((event.getCursor() == null || event.getCursor().getType() == Material.AIR && event.getCurrentItem() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")))) {
                            event.setCancelled(true);
                            ItemStack slotItem = tradeWindow.getItem(slot);
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            event.setCursor(slotItem);
                            player.updateInventory();
                        } else if ((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR && event.getCursor() != null)) {
                            if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("subType"))) {
                                event.setCancelled(true);
                                player.updateInventory();
                            }
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            tradeWindow.setItem(slot, currentItem);
                            event.setCursor(new ItemStack(Material.AIR));
                            player.updateInventory();
                        } else if (event.getCurrentItem() != null && event.getCursor() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton"))) {
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            ItemStack slotItem = event.getCurrentItem();
                            event.setCursor(slotItem);
                            event.setCurrentItem(currentItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot < 26) {
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18
                            || slot == 19 || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        if (tradeWindow.getItem(slot) != null && tradeWindow.getItem(slot).getType() != Material.AIR) {
                            tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                            player.updateInventory();
                        }
                    } else if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && !CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                        event.setCancelled(true);
                        ItemStack slotItem = event.getCurrentItem();
                        if (player.getInventory().firstEmpty() != -1) {
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            player.getInventory().setItem(player.getInventory().firstEmpty(), slotItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot >= 27 && !(event.isCancelled()) && !(event.getCurrentItem().getType() == Material.BOOK)) {
                    event.setCancelled(true);
                    ItemStack slotItem = event.getCurrentItem();
                    int localSlot = event.getSlot();
                    int x = -1;
                    while (x < 26) {
                        x++;
                        if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                            continue;
                        }
                        ItemStack itemStack = tradeWindow.getItem(x);
                        if (!(itemStack == null)) {
                            continue;
                        }
                        tradeWindow.setItem(x, slotItem);
                        if (tradeWindow.getItem(x) != null) {
                            tradeWindow.getItem(x).setAmount(slotItem.getAmount());
                        }
                        player.getInventory().setItem(localSlot, new ItemStack(Material.AIR));
                        player.updateInventory();
                        break;
                    }
                }
                List<ItemStack> player_Offer = new ArrayList<>();
                int x = -1;
                while (x < 26) {
                    x++;
                    if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                        continue;
                    }
                    ItemStack itemStack = tradeWindow.getItem(x);
                    if (itemStack == null || itemStack.getType() == Material.AIR || CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) {
                        continue;
                    }
                    player_Offer.add(itemStack);
                }
                List<ItemStack> new_Offer = TradeCalculator.calculateMerchantOffer(player_Offer);
                x = -1;
                while (x < 26) {
                    x++;
                    if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                            || x == 20 || x == 21)) {
                        continue;
                    }
                    tradeWindow.setItem(x, new ItemStack(Material.AIR));
                }
                x = -1;
                while (x < 26) {
                    x++;
                    if (new_Offer.size() > 0) {
                        if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                                || x == 20 || x == 21)) {
                            continue;
                        }
                        int index = new_Offer.size() - 1;
                        ItemStack itemStack = new_Offer.get(index);
                        tradeWindow.setItem(x, itemStack);
                        new_Offer.remove(index);
                    }
                }
                player.updateInventory();
                if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                    event.setCancelled(true);
                    if (event.getCurrentItem().getDurability() == 8) {
                        int player_Inv_Available = 0;
                        int inv_Needed = 0;
                        event.setCurrentItem(new ItemBuilder().setItem(Material.INK_SACK, (short) 10, ChatColor.GREEN + "Trade accepted.", new String[]{
                                ""
                        }).setNBTString("acceptButton", "whynot").build());
                        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1F, 2.F);

                        for (ItemStack itemStack : player.getInventory()) {
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                player_Inv_Available++;
                            }
                        }
                        int slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            inv_Needed++;
                        }
                        if (player_Inv_Available < inv_Needed) {
                            player.sendMessage(ChatColor.RED + "Inventory is full.");
                            player.sendMessage(ChatColor.GRAY + "You require " + ChatColor.BOLD + (inv_Needed - player_Inv_Available) + ChatColor.GRAY + " more free slots to complete this trade!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                InventoryCloseEvent closeEvent = new InventoryCloseEvent(player.getOpenInventory());
                                Bukkit.getServer().getPluginManager().callEvent(closeEvent);
                            }, 2L);
                            return;
                        }
                        slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (itemStack.getType() == Material.EMERALD) {
                                itemStack = BankMechanics.createBankNote(itemStack.getAmount());
                            }
//                                        if (Glyph.getInstance().isGlyph(itemStack)) {
//                                            int tier = Glyph.getInstance().getGlyphTier(itemStack);
//                                            if (new Random().nextBoolean()) {
//                                                itemStack = Glyph.getInstance().getBaseGlyph("Weapon Glyph", tier, Glyph.GlyphType.WEAPON);
//                                            } else {
//                                                itemStack = Glyph.getInstance().getBaseGlyph("Armor Glyph", tier, Glyph.GlyphType.ARMOR);
//                                            }
//                                        }
                            player.getInventory().setItem(player.getInventory().firstEmpty(), itemStack);
                        }
                        player.sendMessage(ChatColor.GREEN + "Trade Accepted.");

                        for (ItemStack stack : tradeWindow.getContents()) {
                            if (stack != null && stack.getType() == Material.MAGMA_CREAM) {
                                break;
                            }
                        }

                        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1F, 1F);
                        tradeWindow.clear();

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            player.updateInventory();
                            player.closeInventory();
                        }, 2L);

                        return;
                    }
                    player.updateInventory();
                }
                break;
        }

        /*
        Dungeoneer
         */
        if (name.equals("Dungeoneer")) {
            event.setCancelled(true);
            if (slot > 9) return;
            if (event.getCurrentItem().getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack == null) return;
                if (nmsStack.getTag() == null) return;
                if (nmsStack.getTag().hasKey("shardTier") && nmsStack.getTag().hasKey("shardCost")) {
                    if (API.removePortalShardsFromPlayer(player, nmsStack.getTag().getInt("shardTier"), nmsStack.getTag().getInt("shardCost"))) {
                        player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                        player.closeInventory();
                        //player.getInventory().addItem(SandS.getInstance().getScroll(SandS.ScrollType.WHITE_SCROLL, nmsStack.getTag().getInt("shardTier")));
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this scroll");
                        return;
                    }
                }
            }
        } else
        /*
        Friend Management
         */
            if (name.equals("Friend Management")) {
                event.setCancelled(true);
                if (slot >= 44) return;
                if (slot == 1) {
                    PlayerMenus.openFriendsMenu(player);
                }
                if (slot == 0) {
                    AnvilGUIInterface addFriendGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                anvilClick.setWillClose(true);
                                anvilClick.setWillDestroy(true);
                                if (Bukkit.getPlayer(anvilClick.getName()) != null) {
                                    FriendHandler.getInstance().sendRequest(player, Bukkit.getPlayer(anvilClick.getName()));
                                } else {
                                    player.sendMessage(ChatColor.RED + "Oops, I can't find that player!");
                                }
                                break;
                        }
                    });
                    addFriendGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    addFriendGUI.open();
                    return;
                }
                FriendHandler.getInstance().addOrRemove(player, event.getClick(), event.getCurrentItem());
            } else

        /*
        Friends List Menu
         */
                if (name.equals("Friends")) {
                    event.setCancelled(true);
                    if (slot >= 54) return;
                    if (slot == 0) {
                        PlayerMenus.openFriendInventory(player);
                    }
                    FriendHandler.getInstance().remove(player, event.getClick(), event.getCurrentItem());
                } else

        /*
        Mail Below
         */
                    if (name.equals("Mailbox")) {
                        event.setCancelled(true);
                        switch (slot) {
                            case 0:
                                player.sendMessage(ChatColor.RED + "You cannot send mail yet! It's coming soon! :-)");
                                break;
                        }
                        if (event.getCurrentItem() != null) {
                            ItemStack clickedItem = event.getCurrentItem();
                            MailHandler.getInstance().giveItemToPlayer(clickedItem, player);
                        }
                    } else
        /*
        Pets Below
         */
                        if (name.equalsIgnoreCase("Pet Selection")) {
                            event.setCancelled(true);
                            if (event.getCurrentItem().getType() == Material.BARRIER) {
                                PlayerMenus.openPlayerProfileMenu(player);
                                return;
                            }
                            if (event.getCurrentItem().getType() == Material.LEASH) {
                                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                                    if (entity.isAlive()) {
                                        entity.getBukkitEntity().remove();
                                    }
                                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                    }
                                    EntityAPI.removePlayerPetList(player.getUniqueId());
                                    player.sendMessage(ChatColor.AQUA + " Pet dismissed.");
                                } else {
                                    player.sendMessage(ChatColor.AQUA + " You currently do not have a pet in the world.");
                                }
                                return;
                            }
                            if (event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                                    if (entity.isAlive()) {
                                        entity.getBukkitEntity().remove();
                                    }
                                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                    }
                                    EntityAPI.removePlayerPetList(player.getUniqueId());
                                }
                                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                                    Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                                    if (entity.isAlive()) {
                                        entity.getBukkitEntity().remove();
                                    }
                                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                    }
                                    EntityAPI.removePlayerMountList(player.getUniqueId());
                                    player.sendMessage(ChatColor.AQUA + " Mount dismissed.");
                                }
                                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                                if (nmsStack.getTag() == null || nmsStack.getTag().getString("petType") == null) {
                                    player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [NBTTag]");
                                    player.closeInventory();
                                    return;
                                }
                                String particleType = "";
                                if (nmsStack.getTag().getString("particleType") != null) {
                                    particleType = nmsStack.getTag().getString("particleType");
                                }
                                PetUtils.spawnPet(player.getUniqueId(), nmsStack.getTag().getString("petType"), particleType);
                            }
                        } else

        /*
        Mounts Below
         */
                            if (name.equalsIgnoreCase("Mount Selection")) {
                                event.setCancelled(true);
                                if (event.getCurrentItem().getType() == Material.BARRIER) {
                                    PlayerMenus.openPlayerProfileMenu(player);
                                    return;
                                }
                                if (event.getCurrentItem().getType() == Material.LEASH) {
                                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                                        if (entity.isAlive()) {
                                            entity.getBukkitEntity().remove();
                                        }
                                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                        }
                                        EntityAPI.removePlayerMountList(player.getUniqueId());
                                        player.sendMessage(ChatColor.AQUA + " Mount dismissed.");
                                    } else {
                                        player.sendMessage(ChatColor.AQUA + " You currently do not have a mount in the world.");
                                    }
                                    return;
                                }
                                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                                        if (entity.isAlive()) {
                                            entity.getBukkitEntity().remove();
                                        }
                                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                        }
                                        EntityAPI.removePlayerMountList(player.getUniqueId());
                                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.AQUA + " Mount dismissed.");
                                    }
                                    if (EntityAPI.hasPetOut(player.getUniqueId())) {
                                        Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                                        if (entity.isAlive()) {
                                            entity.getBukkitEntity().remove();
                                        }
                                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                        }
                                        EntityAPI.removePlayerPetList(player.getUniqueId());
                                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.AQUA + " Pet dismissed.");
                                    }
                                    if (CombatLog.isInCombat(player)) {
                                        player.sendMessage(ChatColor.RED + "You cannot summon a mount while in Combat!");
                                        return;
                                    }
                                    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                                    if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                                        player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                                        player.closeInventory();
                                        return;
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Your Mount is being summoned into this world!");
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                        if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                                            if (!CombatLog.isInCombat(player)) {
                                                MountUtils.spawnMount(player.getUniqueId(), nmsStack.getTag().getString("mountType"));
                                            } else {
                                                player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "You just summoned your mount!");
                                        }
                                    }, 60L);
                                }
                            } else

        /*
        Particle Trails Below
         */
                                if (name.equalsIgnoreCase("Player Trail Selection")) {
                                    event.setCancelled(true);
                                    if (event.getCurrentItem().getType() == Material.BARRIER) {
                                        PlayerMenus.openPlayerProfileMenu(player);
                                        return;
                                    }
                                    if (event.getCurrentItem().getType() == Material.ARMOR_STAND) {
                                        if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                                            DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                                            player.sendMessage(ChatColor.AQUA + " You have disabled your trail.");
                                        } else {
                                            player.sendMessage(ChatColor.AQUA + " You don't have a Player trail enabled.");
                                        }
                                        return;
                                    }
                                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.ARMOR_STAND) {
                                        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                                        if (nmsStack.getTag() == null || nmsStack.getTag().getString("playerTrailType") == null) {
                                            player.sendMessage("Uh oh... Something went wrong with your Player trail! Please inform a staff member! [NBTTag]");
                                            player.closeInventory();
                                            return;
                                        }
                                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("playerTrailType")));
                                        player.sendMessage(ChatColor.AQUA + " Enabling " + ChatColor.RED + nmsStack.getTag().getString("playerTrailType") + ChatColor.AQUA + " trail.");
                                    }
                                } else

        /*
        Profile PlayerMenus Below
         */
                                    if (name.equals("Profile")) {
                                        event.setCancelled(true);
                                        switch (slot) {
                                            case 0:
                                                player.openInventory(StatsManager.getInventory(player));
                                                break;
                                            case 1:
                                                PlayerMenus.openFriendInventory(player);
                                                break;
                                            case 6:
                                                PlayerMenus.openPlayerParticleMenu(player);
                                                break;
                                            case 7:
                                                PlayerMenus.openPlayerMountMenu(player);
                                                break;
                                            case 8:
                                                PlayerMenus.openPlayerPetMenu(player);
                                                break;
                                            case 16: {
                                                List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                                                if (!playerMounts.contains("MULE")) {
                                                    player.sendMessage(ChatColor.RED + "Purchase a storage mule from the Animal Tamer.");
                                                    return;
                                                }
                                                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                                                    Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                                                    if (entity.isAlive()) {
                                                        entity.getBukkitEntity().remove();
                                                    }
                                                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                                    }
                                                    EntityAPI.removePlayerMountList(player.getUniqueId());
                                                    player.sendMessage(ChatColor.AQUA + " Mount dismissed.");
                                                }
                                                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                                                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                                                    if (entity.isAlive()) {
                                                        entity.getBukkitEntity().remove();
                                                    }
                                                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                                                    }
                                                    EntityAPI.removePlayerPetList(player.getUniqueId());
                                                    player.sendMessage(ChatColor.AQUA + " Pet dismissed.");
                                                }
                                                if (CombatLog.isInCombat(player)) {
                                                    player.sendMessage(ChatColor.RED + "You cannot summon a storage mule while in Combat!");
                                                    return;
                                                }
                                                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                                                if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                                                    player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                                                    player.closeInventory();
                                                    return;
                                                }
                                                player.sendMessage(ChatColor.GREEN + "Summoning storage mule.");
                                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                                    if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                                                        MountUtils.spawnMount(player.getUniqueId(), "MULE");
                                                    } else {
                                                        player.sendMessage(ChatColor.RED + "You just summoned your mount!");
                                                    }
                                                }, 60L);
                                                player.closeInventory();
                                                break;
                                                //SPAWN
                                            }
                                            case 18:
                                                NPCMenus.openECashPurchaseMenu(player);
                                                break;
                                            case 26:
                                                PlayerMenus.openToggleMenu(player);
                                                break;
                                            default:
                                                break;
                                        }
                                    } else

        /*Reset Stats Wizard*/
                                        if (name.equalsIgnoreCase("Wizard")) {
                                            GamePlayer gp = API.getGamePlayer(player);
                                            if (gp.getLevel() >= 10) {
                                                if (gp.getStats().resetAmounts > 0) {
                                                    player.sendMessage(ChatColor.GREEN + "ONE stat reset available.");
                                                    AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
                                                        if (e.getSlot() == AnvilSlot.OUTPUT) {
                                                            if (e.getName().equalsIgnoreCase("Yes") || e.getName().equalsIgnoreCase("y")) {
                                                                gp.getStats().freeResets -= 1;
                                                            } else {
                                                                e.destroy();
                                                            }
                                                        }
                                                    });
                                                    ItemStack stack = new ItemStack(Material.INK_SACK, 1, DyeColor.GREEN.getDyeData());
                                                    ItemMeta meta = stack.getItemMeta();
                                                    meta.setDisplayName("Use your ONE stat points reset?");
                                                    stack.setItemMeta(meta);
                                                    gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                                    Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> player.sendMessage("Opening stat reset confirmation"), 0, 20 * 3);
                                                    Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), gui::open, 20 * 5);
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "You have already used your free stat reset for your character.");
                                                    player.sendMessage(ChatColor.YELLOW + "You may purchase more resets from the E-Cash vendor!.");
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.RED + "You need to be level 10 to use your ONE reset.");
                                            }

                                        } else if (name.equalsIgnoreCase("Toggles")) {
                                            event.setCancelled(true);
                                            if (slot > 9) return;
                                            switch (slot) {
                                                case 0:
                                                    boolean bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_CHAOTIC_PREVENTION, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 1:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_DEBUG, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 2:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DUEL, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_DUEL, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 3:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_GLOBAL_CHAT, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 4:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_PVP, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 5:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_RECEIVE_MESSAGE, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 6:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_TRADE, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 7:
                                                    bool = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE_CHAT, player.getUniqueId());
                                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.TOGGLE_TRADE_CHAT, !bool, true);
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> PlayerMenus.openToggleMenu(player), 10L);
                                                    break;
                                                case 8:
                                                    PlayerMenus.openPlayerProfileMenu(player);
                                                    break;
                                            }
                                        } else if (name.equalsIgnoreCase("Food Vendor")) {
                                            if (event.isShiftClick()) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            if (event.getRawSlot() >= 18) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            event.setCancelled(true);
                                            ItemStack stack = event.getCurrentItem();
                                            if (stack == null || stack.getType() == Material.AIR) return;
                                            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                                            if (!nms.getTag().hasKey("worth")) {
                                                event.setCancelled(true);
                                                return;
                                            }
                                            int price = nms.getTag().getInt("worth");
                                            if (BankMechanics.getInstance().takeGemsFromInventory(price, player)) {
                                                ItemStack copy = stack.clone();
                                                ArrayList<String> lore = new ArrayList<>();
                                                ItemMeta meta = copy.getItemMeta();
                                                if (meta.hasLore()) {
                                                    lore = (ArrayList<String>) meta.getLore();
                                                }
                                                for (int i = 0; i < lore.size(); i++) {
                                                    String current = lore.get(i);
                                                    if (current.contains("Price")) {
                                                        lore.remove(i);
                                                        break;
                                                    }
                                                }
                                                meta.setLore(lore);
                                                copy.setItemMeta(meta);
                                                player.getInventory().addItem(copy);
                                            } else {
                                                player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.GREEN + price + "g");
                                            }
                                        }
    }
}
