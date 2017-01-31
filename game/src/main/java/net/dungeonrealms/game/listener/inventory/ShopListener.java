package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerOpenShopInventory(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        Player p = event.getPlayer();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        if (p.isSneaking()) return;
        Shop shop = ShopMechanics.getShop(block);
        if (shop == null) return;
        if (p.hasMetadata("pricing")) return;

        if (Chat.listened(p)) {
            p.sendMessage(ChatColor.RED + "You can't interact with inventories whilst being interactive with chat");
            event.setCancelled(true);
            return;
        }

        ItemStack[] inventory = p.getInventory().getContents();
        int freeInvSlots = 0;

        ItemStack[] armor = p.getEquipment().getArmorContents();
        int freeArmorSlots = 0;

        for (int i = 0; i < armor.length; i++) {
            ItemStack toCheck = armor[i];

            if (toCheck == null || toCheck.getType() == Material.AIR) {
                freeArmorSlots++;
            }
        }

        for (int i = 0; i < inventory.length; i++) {
            ItemStack toCheck = inventory[i];

            if (toCheck == null || toCheck.getType() == Material.AIR) {
                freeInvSlots++;
            }
        }

        if ((freeInvSlots - freeArmorSlots) < 2) {
            p.sendMessage(ChatColor.RED + "Please clear some inventory space before browsing this shop.");
            return;
        }

        if (shop.ownerName.equals(event.getPlayer().getName()) || Rank.isTrialGM(event.getPlayer()) || shop.isopen) {
            p.openInventory(shop.getInventory());
            p.playSound(event.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            p.setCanPickupItems(false);
        }
    }

    @EventHandler
    public void playerCloseShopInventory(InventoryCloseEvent event) {
        if (!event.getInventory().getTitle().contains("@")) return;
        event.getPlayer().setCanPickupItems(true);
    }

    /**
     * Handling Shops being Right clicked.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerAttemptShopUpgrade(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        Shop shop = ShopMechanics.getShop(block);
        if (shop == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!shop.isopen) {
            if (event.getPlayer().isSneaking()) {
                if (shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                    event.setCancelled(true);
                    shop.promptUpgrade();
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "You do not own this shop.");
                }
            }
        } else {
            if (event.getPlayer().isSneaking()) {
                if (shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You must close your shop to upgrade it.");
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerClickShopInventory(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().contains("@")) return;

        String ownerName = event.getInventory().getTitle().split("@")[1];
        if (ownerName == null) return;
        Shop shop = ShopMechanics.getShop(ownerName);
        if (shop == null) return;
        
        // Prevents Stealing from shops. //
        if (event.getAction() == InventoryAction.NOTHING){
        	if(event.getWhoClicked().getInventory().firstEmpty() == -1)
        		GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " + ChatColor.WHITE + "Player " + event.getWhoClicked().getName() + " has attempted to steal items on shard " + ChatColor.GOLD + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
        	event.setCancelled(true);
        	return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
            event.setCancelled(true); 
            return;
        }
        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            event.setCancelled(true);
            return;
        }
        if (clicker.getUniqueId().toString().equalsIgnoreCase(shop.ownerUUID.toString()) || Rank.isTrialGM(clicker)) {
            // Owner is Clicking
            if (event.getRawSlot() == (shop.getInvSize() - 1)) {
                event.setCancelled(true);
                clicker.playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                shop.updateStatus();
                return;
            }
            if (event.getRawSlot() == (shop.getInvSize() - 2)) {
                event.setCancelled(true);
                shop.deleteShop(false);
                return;
            }
            ItemStack itemHeld = event.getCursor();
            ItemStack stackInSlot = event.getCurrentItem();
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemHeld);
            if (shop.isopen) {
                clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
                event.setCancelled(true);
                return;
            }

            if (event.isShiftClick()) {
                if (event.getRawSlot() >= event.getInventory().getSize()) {

                    if (!ShopMechanics.isItemSellable(stackInSlot)) {
                        event.setCancelled(true);
                        clicker.sendMessage(ChatColor.RED + "You cannot sell this item!");
                        return;
                    }

                    ItemStack stackClicked = event.getCurrentItem().clone();
                    event.setCurrentItem(new ItemStack(Material.AIR));
                    if (BankMechanics.shopPricing.containsKey(clicker.getName())) {
                        clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                    }
                    BankMechanics.shopPricing.put(clicker.getName(), stackClicked);
                    clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "GEM" + ChatColor.GREEN + " value of [" + ChatColor.BOLD + "1x" + ChatColor.GREEN + "] of this item.");
                    Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> clicker.closeInventory());
                    Chat.listenForMessage(clicker, chat -> {
                        if (chat.getMessage().equalsIgnoreCase("Cancel") || chat.getMessage().equalsIgnoreCase("c")) {
                            clicker.sendMessage(ChatColor.RED + "Pricing of item - " + ChatColor.BOLD + "CANCELLED");
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                            return;
                        }
                        if (clicker.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                            clicker.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                            return;
                        }
                        int number = 0;
                        try {
                            number = Integer.parseInt(chat.getMessage());
                        } catch (Exception exc) {
                            clicker.sendMessage(ChatColor.RED + "Please enter a valid number");
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                            return;
                        }
                        
                        if(number >= Integer.MAX_VALUE / 64){
                        	clicker.sendMessage(ChatColor.RED + "You cannot charge this much for an item!");
                        	clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                        	return;
                        }
                        
                        if (number <= 0) {
                            clicker.sendMessage(ChatColor.RED + "You cannot request a NON-POSITIVE number.");
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                            clicker.updateInventory();
                            return;
                        } else {
                            if (BankMechanics.shopPricing.get(clicker.getName()) == null) return;
                            net.minecraft.server.v1_9_R2.ItemStack newNMS = CraftItemStack.asNMSCopy(stackClicked.clone());
                            NBTTagCompound tagCompound = newNMS.getTag() == null ? new NBTTagCompound() : newNMS.getTag();
                            tagCompound.setInt("Price", number);
                            newNMS.setTag(tagCompound);
                            if (shop.inventory.firstEmpty() >= 0) {
                                int slot = shop.inventory.firstEmpty();

                                ItemStack stack = CraftItemStack.asBukkitCopy(newNMS);
                                ItemMeta meta = stack.getItemMeta();
                                ArrayList<String> lore = new ArrayList<>();
                                if (meta.hasLore()) {
                                    lore = (ArrayList<String>) meta.getLore();
                                }
                                lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
                                        + ChatColor.WHITE.toString() + number + "g" + ChatColor.GREEN + " each");
                                meta.setLore(lore);
                                stack.setItemMeta(meta);
                                shop.inventory.setItem(slot, stack);


                                clicker.playSound(clicker.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);

                                clicker.sendMessage(new String[]{
                                        ChatColor.YELLOW.toString() + "Price set. Right-Click item to edit.",
                                        ChatColor.YELLOW + "Left Click the item to remove it from your shop."});
                                BankMechanics.shopPricing.remove(clicker.getName());
                                clicker.updateInventory();
                            } else {
                                clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                BankMechanics.shopPricing.remove(clicker.getName());
                                clicker.sendMessage("There is no room for this item in your Shop");
                                clicker.updateInventory();
                            }
                        }
                    }, player -> {
                        player.getInventory().addItem(BankMechanics.shopPricing.get(player.getName()));
                        BankMechanics.shopPricing.remove(player.getName());
                        clicker.updateInventory();
                    });
                } else {
                    ItemStack stackClicked = event.getCurrentItem();
                    ItemMeta meta = stackClicked.getItemMeta();
                    List<String> lore = meta.getLore();
                    if (lore != null)
                        for (int i = 0; i < lore.size(); i++) {
                            String current = lore.get(i);
                            if (current.contains("Price")) {
                                lore.remove(i);
                                break;
                            }
                        }
                    meta.setLore(lore);
                    stackClicked.setItemMeta(meta);

                }
                return;
            }

            if (stackInSlot != null && stackInSlot.getType() != Material.AIR && itemHeld.getType() != Material.AIR && itemHeld.getType() != stackInSlot.getType()) {
                clicker.sendMessage(ChatColor.RED.toString() + "Move item in slot first.");
                event.setCancelled(true);
            } else {
                if (event.getRawSlot() >= event.getInventory().getSize())
                    return;
                if (event.isLeftClick()) {

                    if (stackInSlot == null || stackInSlot.getType() == Material.AIR) {
                        //Setting new Item in SHop
                        if (itemHeld.getType() == Material.AIR)
                            return;
                        if (clicker.getInventory().firstEmpty() < 0) {
                            clicker.sendMessage(ChatColor.RED + "Make more room in your inventory");
                            event.setCancelled(true);
                            return;
                        }
                        if (!ShopMechanics.isItemSellable(itemHeld)) {
                            event.setCancelled(true);
                            clicker.sendMessage(ChatColor.RED + "You cannot sell this item!");
                            return;
                        }
                        event.setCancelled(true);
                        event.setCursor(new ItemStack(Material.AIR));
                        int playerSlot = clicker.getInventory().firstEmpty();
                        if (BankMechanics.shopPricing.containsKey(clicker.getName())) {
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                        }
                        BankMechanics.shopPricing.put(clicker.getName(), itemHeld);
                        clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "GEM" + ChatColor.GREEN + " value of [" + ChatColor.BOLD + "1x" + ChatColor.GREEN + "] of this item.");
                        clicker.closeInventory();
                        Chat.listenForMessage(clicker, chat -> {
                            if (chat.getMessage().equalsIgnoreCase("Cancel") || chat.getMessage().equalsIgnoreCase("c")) {
                                clicker.sendMessage(ChatColor.RED + "Pricing of item - " + ChatColor.BOLD + "CANCELLED");
                                clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                BankMechanics.shopPricing.remove(clicker.getName());
                                return;
                            }
                            if (clicker.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                                clicker.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
                                clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                BankMechanics.shopPricing.remove(clicker.getName());
                                return;
                            }
                            int number = 0;
                            try {
                                number = Integer.parseInt(chat.getMessage());
                            } catch (Exception exc) {
                                clicker.sendMessage(ChatColor.RED + "Please enter a valid number");
                                clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                BankMechanics.shopPricing.remove(clicker.getName());
                                return;
                            }
                            if (number <= 0) {
                                clicker.sendMessage(ChatColor.RED + "You cannot request a NON-POSITIVE number.");
                                clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                BankMechanics.shopPricing.remove(clicker.getName());
                                return;
                            } else {
                                if (BankMechanics.shopPricing.get(clicker.getName()) == null) return;
                                net.minecraft.server.v1_9_R2.ItemStack newNMS = CraftItemStack.asNMSCopy(BankMechanics.shopPricing.get(clicker.getName()).clone());
                                NBTTagCompound tag = newNMS.hasTag() ? nms.getTag() : new NBTTagCompound();
                                tag.setInt("Price", number);
                                newNMS.setTag(tag);
                                if (shop.inventory.firstEmpty() >= 0) {
                                    int slot = shop.inventory.firstEmpty();

                                    ItemStack stack = CraftItemStack.asBukkitCopy(newNMS);
                                    ItemMeta meta = stack.getItemMeta();
                                    ArrayList<String> lore = new ArrayList<>();
                                    if (meta.hasLore()) {
                                        lore = (ArrayList<String>) meta.getLore();
                                    }
                                    lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
                                            + ChatColor.WHITE.toString() + number + "g" + ChatColor.GREEN + " each");
                                    meta.setLore(lore);
                                    stack.setItemMeta(meta);
                                    shop.inventory.setItem(slot, stack);
                                    clicker.playSound(clicker.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);

                                    clicker.sendMessage(new String[]{
                                            ChatColor.GREEN.toString() + "Price set. Right-Click item to edit.",
                                            ChatColor.YELLOW + "Left Click the item to remove it from your shop."});
                                    clicker.getInventory().setItem(playerSlot, new ItemStack(Material.AIR));
                                    BankMechanics.shopPricing.remove(clicker.getName());
                                } else {
                                    clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                                    BankMechanics.shopPricing.remove(clicker.getName());
                                    clicker.sendMessage("There is no room for this item in your Shop");
                                }
                            }
                        }, player -> {
                            clicker.getInventory().addItem(BankMechanics.shopPricing.get(clicker.getName()));
                            BankMechanics.shopPricing.remove(clicker.getName());
                        });
                        clicker.closeInventory();
                        return;
                    }

                    if (event.getWhoClicked().getInventory().firstEmpty() < 0) {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Make room in your inventory.");
                        event.setCancelled(true);
                        return;
                    }

                    // Removing item from Shop
                    ItemStack stack = stackInSlot.clone();
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = meta.getLore();
                    if (lore != null)
                        for (int i = 0; i < lore.size(); i++) {
                            String current = lore.get(i);
                            if (current.contains("Price")) {
                                lore.remove(i);
                                break;
                            }
                        }
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                    event.setCancelled(true);
                    net.minecraft.server.v1_9_R2.ItemStack nms2 = CraftItemStack.asNMSCopy(stack);
                    nms2.getTag().remove("Price");
                    clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
                    event.getInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR, 1));
                } else if (event.isRightClick()) {
                    if (stackInSlot == null || stackInSlot.getType() == Material.AIR) {
                        clicker.sendMessage(ChatColor.RED + "Can't edit an empty shop!");
                        event.setCancelled(true);
                        return;
                    }
                    event.setCancelled(true);
                    clicker.closeInventory();
                    clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "GEM" + ChatColor.GREEN + " value of [" + ChatColor.BOLD + "1x" + ChatColor.GREEN + "] of this item.");
                    clicker.closeInventory();
                    Chat.listenForMessage(clicker, chat -> {
                        if (shop.inventory.getItem(event.getRawSlot()) == null || shop.inventory.getItem(event.getRawSlot()).getType() == Material.AIR) {
                            // The item has been removed from the shop, anti-dupe.
                            clicker.sendMessage(ChatColor.RED + "You cannot edit this item as it is longer in your shop.");
                            return;
                        }

                        if (chat.getMessage().equalsIgnoreCase("Cancel") || chat.getMessage().equalsIgnoreCase("c")) {
                            clicker.sendMessage(ChatColor.RED + "Pricing of item - " + ChatColor.BOLD + "CANCELLED");
                            return;
                        }
                        if (clicker.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                            clicker.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
                            return;
                        }
                        int number = 0;
                        try {
                            number = Integer.parseInt(chat.getMessage());
                        } catch (Exception exc) {
                            chat.getPlayer().sendMessage(ChatColor.RED + "Please enter a valid number");
                            return;
                        }
                        if (number < 0) {
                            clicker.sendMessage(ChatColor.RED + "You cannot request a NON-POSITIVE number.");
                        } else {
                            ItemStack stack = stackInSlot.clone();
                            ItemMeta meta = stackInSlot.getItemMeta();
                            ArrayList<String> lore = new ArrayList<>();
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
                            lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
                                    + ChatColor.WHITE.toString() + number + "g " + ChatColor.GREEN + "each");
                            meta.setLore(lore);
                            stack.setItemMeta(meta);
                            net.minecraft.server.v1_9_R2.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
                            nms1.getTag().setInt("Price", number);
                            shop.inventory.setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms1));
                            clicker.playSound(clicker.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);
                        }
                    }, player -> player.sendMessage(ChatColor.RED + "Action cancelled."));
                }
            }
        } else {
            event.setCancelled(true);
            // Not Owner Clicking
            if (!shop.isopen) {
                if (event.getCursor() != null) {
                    clicker.getInventory().addItem(event.getCursor());
                    event.setCursor(new ItemStack(Material.AIR));
                }
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.RED + "The shop has closed");
                return;
            }

            if (event.getRawSlot() >= (shop.getInvSize() - 1))
                return;

            final ItemStack itemClicked = event.getCurrentItem();
            if (itemClicked == null || itemClicked.getType() == Material.AIR) return;
            if (clicker.getInventory().firstEmpty() == -1) {
                clicker.sendMessage(ChatColor.RED + "No space available in inventory. Clear some room before attempting to purchase.");
                return;
            }
            boolean shiftClick = event.isShiftClick();
            if (!clicker.hasMetadata("pricing")) {
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemClicked);
                if (nms == null || !nms.hasTag() || !nms.getTag().hasKey("Price")) return;
                int itemPrice = nms.getTag().getInt("Price");
                clicker.closeInventory();
                if (!shiftClick) {
                    clicker.setMetadata("pricing", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    clicker.sendMessage(ChatColor.GRAY + "MAX: " + itemClicked.getAmount() + "X (" + itemPrice * itemClicked.getAmount() + "g), OR " + itemPrice + "g/each.");
                    Chat.listenForMessage(clicker, chat -> {
                        clicker.removeMetadata("pricing", DungeonRealms.getInstance());
                        if (chat.getMessage().equalsIgnoreCase("cancel") || chat.getMessage().equalsIgnoreCase("c")) {
                            clicker.sendMessage(ChatColor.RED + "Purchase of item " + ChatColor.BOLD + "CANCELLED");
                            return;
                        }
                        if (clicker.getInventory().firstEmpty() == -1) {
                            clicker.sendMessage(ChatColor.RED + "No space available in inventory. Type 'cancel' or clear some room.");
                            return;
                        }
                        if (!ShopMechanics.ALLSHOPS.containsKey(ownerName) || !shop.isopen ||
                                !(ShopMechanics.ALLSHOPS.get(ownerName).equals(shop))) {
                            clicker.sendMessage(ChatColor.RED + "The shop is no longer available.");
                            clicker.closeInventory();
                            return;
                        }
                        if (clicker.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                            clicker.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], purchase of item CANCELLED.");
                            return;
                        }
                        
                        if (shop.getInventory().getItem(event.getRawSlot()) == null || !shop.getInventory().getItem(event.getRawSlot()).equals(itemClicked)) {
                            clicker.sendMessage(ChatColor.RED + "That item is no longer available.");
                            return;
                        }
                        int quantity = 0;
                        try {
                            quantity = Integer.parseInt(chat.getMessage());
                            if (quantity <= 0) {
                                clicker.sendMessage(ChatColor.RED + "You cannot purchase a NON-POSITIVE number.");
                                return;
                            }
                            
                            if(quantity > 64){
                            	clicker.sendMessage(ChatColor.RED + "You cannot buy more than 64 items.");
                            	return;
                            }
                            
                            if (quantity > itemClicked.getAmount()) {
                                clicker.sendMessage(ChatColor.RED + "There are only [" + ChatColor.BOLD + itemClicked.getAmount() + ChatColor.RED + "] available.");
                                return;
                            }
                            int totalPrice = quantity * itemPrice;
                            if (totalPrice > 0 && (BankMechanics.getInstance().getTotalGemsInInventory(clicker) < totalPrice)) {
                                clicker.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
                                clicker.sendMessage(ChatColor.GRAY + "" + quantity + " X " + itemPrice + " gem(s)/ea = " + totalPrice + " gem(s).");
                                return;
                            }
                            BankMechanics.getInstance().takeGemsFromInventory(totalPrice, clicker);
                            ItemStack toGive = itemClicked.clone();
                            ItemMeta meta = toGive.getItemMeta();
                            List<String> lore = meta.getLore();
                            if (lore != null) {
                                for (int i = 0; i < lore.size(); i++) {
                                    String current = lore.get(i);
                                    if (current.contains("Price")) {
                                        lore.remove(i);
                                        break;
                                    }
                                }
                            }
                            meta.setLore(lore);
                            toGive.setItemMeta(meta);
                            toGive.setAmount(quantity);
                            clicker.getInventory().addItem(toGive);
                            clicker.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + totalPrice + ChatColor.BOLD + "G");
                            clicker.sendMessage(ChatColor.GREEN + "Transaction successful.");
                            clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                            clicker.updateInventory();
                            int remainingStock = itemClicked.getAmount() - quantity;
                            if (remainingStock > 0) {
                                itemClicked.setAmount(remainingStock);
                            } else {
                                event.getInventory().clear(event.getRawSlot());
                            }
                            DatabaseAPI.getInstance().update(shop.ownerUUID, EnumOperators.$INC, EnumData.GEMS, totalPrice, false);
                            if (shop.getOwner() != null) {
                                if (shop.hasCustomName(itemClicked)) {
                                    shop.getOwner().sendMessage(ChatColor.GREEN + "SOLD " + quantity + "x '" + itemClicked.getItemMeta().getDisplayName() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                                } else {
                                    shop.getOwner().sendMessage(ChatColor.GREEN + "SOLD " + quantity + "x '" + ChatColor.WHITE + itemClicked.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                                }
                                GamePlayer gamePlayer = GameAPI.getGamePlayer(shop.getOwner());
                                if (gamePlayer != null) {
                                    gamePlayer.getPlayerStatistics().setGemsEarned(gamePlayer.getPlayerStatistics().getGemsEarned() + totalPrice);
                                }
                                Achievements.getInstance().giveAchievement(shop.getOwner().getUniqueId(), Achievements.EnumAchievements.SHOP_MERCHANT);
                            } else {
                                if (shop.hasCustomName(itemClicked)) {
                                    BungeeUtils.sendPlayerMessage(ownerName, ChatColor.GREEN + "SOLD " + quantity + "x '" + itemClicked.getItemMeta().getDisplayName() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                                } else {
                                    BungeeUtils.sendPlayerMessage(ownerName, ChatColor.GREEN + "SOLD " + quantity + "x '" + ChatColor.WHITE + itemClicked.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                                }
                                DatabaseAPI.getInstance().update(shop.ownerUUID, EnumOperators.$INC, EnumData.GEMS_EARNED, totalPrice, true, doAfter -> {
                                    GameAPI.updatePlayerData(shop.ownerUUID);
                                });
                            }
                            int itemsLeft = 0;
                            for (ItemStack itemStack : event.getInventory().getContents()) {
                                if (itemStack != null && itemStack.getType() != Material.AIR) {
                                    if (itemStack.equals(event.getInventory().getItem(shop.getInvSize() - 1)) || itemStack.equals(event.getInventory().getItem(shop.getInvSize() - 2))) {
                                        continue;
                                    }
                                    itemsLeft++;
                                }
                            }
                            if (itemsLeft == 0) {
                                Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), shop::updateStatus, 10L);
                            }
                        } catch (NumberFormatException e) {
                            clicker.removeMetadata("pricing", DungeonRealms.getInstance());
                            clicker.sendMessage(ChatColor.RED + "Please enter a valid number.");
                        }
                    }, p -> {
                        p.sendMessage(ChatColor.RED + "Transaction cancelled.");
                        clicker.removeMetadata("pricing", DungeonRealms.getInstance());
                    });
                } else if (event.isShiftClick()) {
                    int totalPrice = itemPrice * itemClicked.getAmount();
                    if (totalPrice > 0 && (BankMechanics.getInstance().getTotalGemsInInventory(clicker) < totalPrice)) {
                        clicker.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
                        clicker.sendMessage(ChatColor.GRAY + "" + itemClicked.getAmount() + " X " + itemPrice + " gem(s)/ea = " + totalPrice + " gem(s).");
                        return;
                    }
                    if (BankMechanics.getInstance().takeGemsFromInventory(totalPrice, clicker)) {
                        event.getInventory().clear(event.getRawSlot());
                        ItemStack clickClone = itemClicked.clone();
                        ItemMeta meta = clickClone.getItemMeta();
                        List<String> lore = meta.getLore();
                        if (lore != null) {
                            for (int i = 0; i < lore.size(); i++) {
                                String current = lore.get(i);
                                if (current.contains("Price")) {
                                    lore.remove(i);
                                    break;
                                }
                            }
                        }
                        meta.setLore(lore);
                        clickClone.setItemMeta(meta);
                        DatabaseAPI.getInstance().update(shop.ownerUUID, EnumOperators.$INC, EnumData.GEMS, totalPrice, false);
                        if (shop.getOwner() != null) {
                            if (shop.hasCustomName(itemClicked)) {
                                shop.getOwner().sendMessage(ChatColor.GREEN + "SOLD " + itemClicked.getAmount() + "x '" + itemClicked.getItemMeta().getDisplayName() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                            } else {
                                shop.getOwner().sendMessage(ChatColor.GREEN + "SOLD " + itemClicked.getAmount() + "x '" + ChatColor.WHITE + itemClicked.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                            }
                            Achievements.getInstance().giveAchievement(shop.getOwner().getUniqueId(), Achievements.EnumAchievements.SHOP_MERCHANT);
                        } else {
                            if (shop.hasCustomName(itemClicked)) {
                                BungeeUtils.sendPlayerMessage(ownerName, ChatColor.GREEN + "SOLD " + itemClicked.getAmount() + "x '" + itemClicked.getItemMeta().getDisplayName() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                            } else {
                                BungeeUtils.sendPlayerMessage(ownerName, ChatColor.GREEN + "SOLD " + itemClicked.getAmount() + "x '" + ChatColor.WHITE + itemClicked.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + clicker.getName());
                            }
                        }
                        clickClone.setAmount(itemClicked.getAmount());
                        clicker.getInventory().addItem(clickClone);
                        clicker.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + totalPrice + ChatColor.BOLD + "G");
                        clicker.sendMessage(ChatColor.GREEN + "Transaction successful.");
                        clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                        int itemsLeft = 0;
                        for (ItemStack itemStack : event.getInventory().getContents()) {
                            if (itemStack != null && itemStack.getType() != Material.AIR) {
                                if (itemStack.equals(event.getInventory().getItem(shop.getInvSize() - 1)) || itemStack.equals(event.getInventory().getItem(shop.getInvSize() - 2))) {
                                    continue;
                                }
                                itemsLeft++;
                            }
                        }
                        if (itemsLeft == 0) {
                            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), shop::updateStatus, 10L);
                        }
                    } else {
                        clicker.closeInventory();
                        clicker.sendMessage(ChatColor.RED + "You don't have enough GEM(s) for " + itemClicked.getAmount() + "x of this item.");
                        clicker.sendMessage(ChatColor.RED + "COST: " + totalPrice);
                    }
                }
            } else {
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.RED + "Woah.. I think the banhammer is loading..");
                GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "BANHAMMER: " + clicker.getName() + " is cheating in shops on " + DungeonRealms.getInstance().bungeeName);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseShop(InventoryClickEvent event) {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerOpenShopInventory(InventoryOpenEvent event) {
        if (!event.getInventory().getTitle().contains("@")) return;
        String ownerName = event.getInventory().getTitle().split("@")[1];
        if (ownerName == null) return;
        Shop shop = ShopMechanics.getShop(ownerName);
        if (shop == null) return;
        if (!shop.isopen) return;
        if (event.getPlayer().getName().equalsIgnoreCase(ownerName)) return;
        if (shop.uniqueViewers.contains(event.getPlayer().getName())) return;
        shop.viewCount = shop.viewCount + 1;
        shop.uniqueViewers.add(event.getPlayer().getName());
        shop.hologram.removeLine(1);
        shop.hologram.insertTextLine(1, String.valueOf(shop.viewCount) + ChatColor.RED + " ❤");
    }

}