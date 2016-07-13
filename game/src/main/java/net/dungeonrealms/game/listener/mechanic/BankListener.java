package net.dungeonrealms.game.listener.mechanic;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chase, by fixed by Proxying and under inspection of xFinityPro.
 */
public class BankListener implements Listener {

    /**
     * Bank Inventory. When a player moves items
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            if (e.getClickedBlock().getType() != Material.ENDER_CHEST)
                return;

            e.setCancelled(true);
            Player p = e.getPlayer();
            if (BankMechanics.storage.get(p.getUniqueId()).collection_bin != null) {
                p.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
                p.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
                return;
            }

            int storage_lvl = (Integer) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, p.getUniqueId());
            if (storage_lvl >= 6) {
                p.sendMessage(ChatColor.RED + "You've reached the current Storage lvl cap!");
                return;
            }
            int upgrade_cost = BankMechanics.getPrice(storage_lvl + 1);

            p.sendMessage("");
            p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Bank Upgrade Confirmation" + ChatColor.DARK_GRAY + " ***");
            p.sendMessage(ChatColor.DARK_GRAY + "           CURRENT Slots: " + ChatColor.GREEN + storage_lvl * 9 + ChatColor.DARK_GRAY + "          NEW Slots: " + ChatColor.GREEN + (storage_lvl + 1) * 9);
            p.sendMessage(ChatColor.DARK_GRAY + "                  Upgrade Cost: " + ChatColor.GREEN + "" + upgrade_cost + " Gem(s)");
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Enter '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your upgrade.");
            p.sendMessage("");
            p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Bank upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
            p.sendMessage("");

            Chat.listenForMessage(p, event -> {
                if (event.getMessage().equalsIgnoreCase("confirm")) {
                    boolean tookGems = BankMechanics.getInstance().takeGemsFromInventory(upgrade_cost, p);
                    if (tookGems) {
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, (storage_lvl + 1), true);
                        p.sendMessage("");
                        p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** BANK UPGRADE TO LEVEL " + (storage_lvl + 1) + " COMPLETE ***");
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.25F);
                        p.closeInventory();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> BankMechanics.getInstance().getStorage(p.getUniqueId()).update(), 20L);
                    } else {
                        p.closeInventory();
                        p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
                        p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + upgrade_cost);
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Bank Upgrade Cancelled");
                }
            }, null);


        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
                e.setCancelled(true);
                e.getPlayer().openInventory(getBank(e.getPlayer().getUniqueId()));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickUp(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.EMERALD) {
            if (event.getItem().getItemStack().getAmount() <= 0) {
                event.setCancelled(true);
                event.getItem().remove();
            }
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem().getItemStack());
            if (nms.hasTag() && nms.getTag() != null && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, event.getPlayer().getUniqueId()).toString())) {
                    event.getPlayer().sendMessage("                      " + ChatColor.GREEN + "+" + event.getItem().getItemStack().getAmount() + ChatColor.BOLD + "G");
                }
                int gems = event.getItem().getItemStack().getAmount();
                GamePlayer gamePlayer = API.getGamePlayer(event.getPlayer());
                if (gamePlayer != null) {
                    gamePlayer.getPlayerStatistics().setGemsEarned(gamePlayer.getPlayerStatistics().getGemsEarned() + gems);
                }
                for (int i = 0; i < event.getPlayer().getInventory().getSize(); i++) {
                    ItemStack gemPouch = event.getPlayer().getInventory().getItem(i);
                    if (gemPouch == null || gemPouch.getType() == Material.AIR)
                        continue;
                    if (!BankMechanics.getInstance().isGemPouch(gemPouch))
                        continue;
                    net.minecraft.server.v1_9_R2.ItemStack nmsPouch = CraftItemStack.asNMSCopy(gemPouch);
                    int currentAmount = nmsPouch.getTag().getInt("worth");
                    int tier = nmsPouch.getTag().getInt("tier");
                    int max = BankMechanics.getInstance().getPouchMax(tier);
                    event.getItem().remove();
                    event.setCancelled(true);
                    if (currentAmount < max) {
                        while (currentAmount < max && gems > 0) {
                            currentAmount += 1;
                            gems -= 1;
                        }
                        event.getPlayer().getInventory().setItem(i, BankMechanics.getInstance().createGemPouch(tier, currentAmount));
                        break;
                    }
                }
                if (gems > 0) {
                    event.getItem().remove();
                    event.setCancelled(true);
                    event.getPlayer().getInventory().addItem(BankMechanics.createGems(gems));
                }
            }

        }
    }

    /**
     * Bank inventorys clicked.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBankClicked(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equalsIgnoreCase("Bank Chest")) {
            if (e.getCursor() != null) {
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(e.getCursor());
                if (e.getRawSlot() < 9) {
                    if (e.getRawSlot() == 8) {
                        e.setCancelled(true);
                        if (e.getCursor() != null) {
                            if (e.getClick() == ClickType.LEFT) {
                                int currentGems = getPlayerGems(player.getUniqueId());
                                player.closeInventory();
                                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
                                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " + ChatColor.GREEN + currentGems + " GEM(s)");
                                player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "How much would you like to WITHDRAW today, " + player.getDisplayName() + "?");
                                player.sendMessage(ChatColor.GRAY + "Please enter the amount you'd like To WITHDRAW. Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
                                Chat.listenForMessage(player, event -> {
                                    if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("c")) {
                                        player.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED");
                                        return;
                                    }
                                    int number = 0;
                                    try {
                                        number = Integer.parseInt(event.getMessage());
                                    } catch (Exception exc) {
                                        player.sendMessage(ChatColor.RED + "Please enter a valid number");
                                        return;
                                    }
                                    if (number <= 0) {
                                        player.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    } else if (number > currentGems) {
                                        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but you only have " + currentGems + " GEM(s) stored in our bank.");
                                        player.sendMessage(ChatColor.GRAY + "You cannot withdraw more GEM(s) than you have stored.");
                                    } else {
                                        ItemStack stack = BankMechanics.gem.clone();
                                        if (hasSpaceInInventory(player.getUniqueId(), number)) {
                                            DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC, EnumData.GEMS, -number, true);
                                            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "New Balance: " + ChatColor.GREEN + (currentGems - number) + " GEM(s)");
                                            player.sendMessage(ChatColor.GRAY + "You have withdrawn " + number + " GEM(s) from your bank account.");
                                            player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Here are your Gems, thank you for your business!");
                                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                                            while (number > 0) {
                                                while (number > 64) {
                                                    ItemStack item = stack.clone();
                                                    item.setAmount(64);
                                                    player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                                                    number -= 64;
                                                }
                                                ItemStack item = stack.clone();
                                                item.setAmount(number);
                                                player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                                                number = 0;
                                            }
                                        }
                                    }
                                }, p -> p.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED"));
                            } else if (e.getClick() == ClickType.RIGHT) {
                                int currentGems = getPlayerGems(player.getUniqueId());
                                player.closeInventory();
                                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
                                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " + ChatColor.GREEN + currentGems + " GEM(s)");
                                player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "How much would you like to CONVERT today, " + player.getDisplayName() + "?");
                                player.sendMessage(ChatColor.GRAY + "Please enter the amount you'd like To CONVERT into a gem note. Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
                                Chat.listenForMessage(player, event -> {
                                    if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("c")) {
                                        player.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED");
                                        return;
                                    }
                                    int number = 0;
                                    try {
                                        number = Integer.parseInt(event.getMessage());
                                    } catch (Exception exc) {
                                        player.sendMessage(ChatColor.RED + "Please enter a valid number");
                                        return;
                                    }
                                    if (number <= 0) {
                                        player.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                                    } else if (number > 100000) {
                                        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but I cannot create bank notes that large.");
                                    } else if (number > currentGems) {
                                        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "I'm sorry, but you only have " + currentGems + " GEM(s) stored in our bank.");
                                        player.sendMessage(ChatColor.GRAY + "You cannot withdraw more GEM(s) than you have stored.");
                                    } else {
                                        player.getInventory().addItem(BankMechanics.createBankNote(number));
                                        DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC, EnumData.GEMS, -number, true);
                                        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "New Balance: " + ChatColor.GREEN + (currentGems - number) + " GEM(s)");
                                        player.sendMessage(ChatColor.GRAY + "You have converted " + number + " GEM(s) from your bank account into a " + ChatColor.BOLD.toString() + "GEM NOTE.");
                                        player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Here are your Gems, thank you for your business!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                                    }
                                }, p -> p.sendMessage(ChatColor.RED + "Withdrawal operation - " + ChatColor.BOLD + "CANCELLED"));
                            }

                        }
                    } else if (e.getRawSlot() != 0 && e.getRawSlot() != 4 || e.getRawSlot() == 4 && e.getInventory().getItem(4) == null || e.getInventory().getItem(4) != null && e.getInventory().getItem(4).getType() == Material.AIR) {
                        if (nms == null)
                            return;
                        e.setCancelled(true);
                        if (BankMechanics.getInstance().isBankNote(e.getCursor()) || BankMechanics.getInstance().isGemPouch(e.getCursor()) || BankMechanics.getInstance().isGem(e.getCursor())) {
                            if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                                int size = 0;
                                if (e.isLeftClick()) {
                                    if (e.getCursor().getType() == Material.INK_SACK) {
                                        int type = nms.getTag().getInt("tier");
                                        size = nms.getTag().getInt("worth");
                                        e.setCursor(null);
                                        e.setCurrentItem(null);
                                        e.getWhoClicked().getInventory().addItem(BankMechanics.getInstance().createGemPouch(type, 0));
                                    } else if (e.getCursor().getType() == Material.EMERALD) {
                                        size = e.getCursor().getAmount();
                                        e.setCursor(null);
                                        e.setCurrentItem(null);
                                    } else if (e.getCursor().getType() == Material.PAPER) {
                                        size = e.getCursor().getAmount() * nms.getTag().getInt("worth");
                                        e.setCursor(null);
                                        e.setCurrentItem(null);
                                    }
                                } else if (e.isRightClick()) {

                                    if (e.getCursor().getType() == Material.EMERALD)
                                        size = 1;
                                    else
                                        size = nms.getTag().getInt("worth");

                                    if (e.getCursor().getAmount() > 1) {
                                        e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                                    } else {
                                        e.setCursor(null);
                                    }
                                }
                                int newBalance = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId()) + size;
                                BankMechanics.getInstance().addGemsToPlayerBank(player.getUniqueId(), size);
                                BankMechanics.getInstance().checkBankAchievements(player.getUniqueId(), newBalance);
                                player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "+" + ChatColor.GREEN + size + ChatColor.BOLD + "G, New Balance: " + ChatColor.GREEN + newBalance + " GEM(s)");
                                ItemStack bankItem = new ItemStack(Material.EMERALD);
                                ItemMeta meta = bankItem.getItemMeta();
                                meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                        + ChatColor.GREEN + " GEM(s)");
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to withdraw " + ChatColor.GREEN.toString() + ChatColor.BOLD + "RAW GEMS");
                                lore.add(ChatColor.GREEN + "Right Click " + ChatColor.GRAY + "to create " + ChatColor.GREEN.toString() + ChatColor.BOLD + "A GEM NOTE");
                                meta.setLore(lore);
                                bankItem.setItemMeta(meta);
                                net.minecraft.server.v1_9_R2.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                                nmsBank.getTag().setString("type", "bank");
                                e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            }
                        } else {
                            Storage storage = BankMechanics.getInstance().getStorage(e.getWhoClicked().getUniqueId());
                            if (e.isLeftClick()) {
                                if (storage.hasSpace()) {
                                    if (!API.isItemTradeable(e.getCursor()) || !API.isItemDroppable(e.getCursor())) {
                                        player.sendMessage(ChatColor.RED + "You can't store this item!");
                                        e.setCancelled(true);
                                        return;
                                    }

                                    storage.inv.addItem(e.getCursor());
                                    e.setCursor(null);
                                    player.sendMessage(ChatColor.GREEN + "Item added to storage!");

                                } else {
                                    player.sendMessage(ChatColor.RED + "You do not have space to add this item.");
                                }
                            } else if (e.getClick() == ClickType.RIGHT) {
                                if (storage.hasSpace()) {
                                    ItemStack stack = e.getCurrentItem();
                                    if (stack.getAmount() > 1) {
                                        ItemStack stackToAdd = e.getCursor().clone();
                                        stackToAdd.setAmount(1);
                                        storage.inv.addItem(stackToAdd);
                                        ItemStack newStack = stack.clone();
                                        newStack.setAmount(stack.getAmount() - 1);
                                        e.setCursor(newStack);
                                    } else {
                                        storage.inv.addItem(stack);
                                        e.setCursor(null);
                                        e.setCurrentItem(null);
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Item added to storage!");

                                } else {
                                    player.sendMessage(ChatColor.RED + "You do not have space to add this item.");
                                }
                            }
                        }
                    } else if (e.getRawSlot() == 0) {
                        e.setCancelled(true);
                        Storage storage = BankMechanics.getInstance().getStorage(player.getUniqueId());
                        if (e.isLeftClick()) {
                            // Open Storage
                            player.openInventory(storage.inv);
                        } else if (e.getClick() == ClickType.MIDDLE) {
                            Player p = (Player) e.getWhoClicked();
                            if (BankMechanics.storage.get(player.getUniqueId()).collection_bin != null) {
                                player.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
                                player.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
                                return;
                            }

                            int storage_lvl = (Integer) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, p.getUniqueId());
                            if (storage_lvl >= 6) {
                                p.sendMessage(ChatColor.RED + "You've reached the current Storage lvl cap!");
                                return;
                            }
                            int upgrade_cost = BankMechanics.getPrice(storage_lvl + 1);

                            p.sendMessage("");
                            p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Bank Upgrade Confirmation" + ChatColor.DARK_GRAY + " ***");
                            p.sendMessage(ChatColor.DARK_GRAY + "           CURRENT Slots: " + ChatColor.GREEN + storage_lvl * 9 + ChatColor.DARK_GRAY + "          NEW Slots: " + ChatColor.GREEN + (storage_lvl + 1) * 9);
                            p.sendMessage(ChatColor.DARK_GRAY + "                  Upgrade Cost: " + ChatColor.GREEN + "" + upgrade_cost + " Gem(s)");
                            p.sendMessage("");
                            p.sendMessage(ChatColor.GREEN + "Enter '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your upgrade.");
                            p.sendMessage("");
                            p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Bank upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
                            p.sendMessage("");

                            Chat.listenForMessage(p, event -> {
                                if (event.getMessage().equalsIgnoreCase("confirm")) {
                                    boolean tookGems = BankMechanics.getInstance().takeGemsFromInventory(upgrade_cost, p);
                                    if (tookGems) {
                                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, (storage_lvl + 1), true);
                                        p.sendMessage("");
                                        p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** BANK UPGRADE TO LEVEL " + (storage_lvl + 1) + " COMPLETE ***");
                                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.25F);
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> BankMechanics.getInstance().getStorage(p.getUniqueId()).update(), 20L);
                                    } else {
                                        p.closeInventory();
                                        p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
                                        p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + upgrade_cost);
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "Bank Upgrade Cancelled");
                                }
                            }, null);

                            p.closeInventory();

                            // Upgrade Storage
                        }
                    } else if (e.getRawSlot() == 4 && e.getInventory().getItem(4) != null && e.getInventory().getItem(4).getType() == Material.CHEST) {
                        //Collection Bin
                        e.setCancelled(true);
                        Storage storage = BankMechanics.getInstance().getStorage(player.getUniqueId());
                        if (storage.collection_bin != null) {
                            player.openInventory(storage.collection_bin);
                            e.setCancelled(true);
                        } else {
                            player.sendMessage(ChatColor.RED + "Collection Bin is empty.");
                        }
                    }
                } else {
                    if (e.isShiftClick()) {
                        if (!BankMechanics.getInstance().isBankNote(e.getCurrentItem()) && !BankMechanics.getInstance().isGem(e.getCurrentItem()) && !BankMechanics.getInstance().isGemPouch(e.getCurrentItem())) {
                            Storage storage = BankMechanics.getInstance().getStorage(e.getWhoClicked().getUniqueId());
                            if (storage.hasSpace()) {
                                if (!API.isItemTradeable(e.getCurrentItem()) || !API.isItemDroppable(e.getCurrentItem())) {
                                    player.sendMessage(ChatColor.RED + "You can't store this item!");
                                    e.setCancelled(true);
                                    return;
                                }

                                storage.inv.addItem(e.getCurrentItem());
                                e.setCurrentItem(null);
                                player.sendMessage(ChatColor.GREEN + "Item added to storage!");

                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have space to add this item.");
                            }
                            e.setCancelled(true);
                            return;
                        }

                        nms = CraftItemStack.asNMSCopy(e.getCurrentItem());
                        if (!nms.hasTag())
                            return;
                        int size = 0;
                        if (e.getCurrentItem().getType() == Material.EMERALD) {
                            size = e.getCurrentItem().getAmount();
                            e.setCurrentItem(null);
                        } else if (e.getCurrentItem().getType() == Material.PAPER) {
                            size = e.getCurrentItem().getAmount() * nms.getTag().getInt("worth");
                            e.setCurrentItem(null);
                        } else if (e.getCurrentItem().getType() == Material.INK_SACK) {
                            int tier = nms.getTag().getInt("tier");
                            size = nms.getTag().getInt("worth");
                            e.setCurrentItem(BankMechanics.getInstance().createGemPouch(tier, 0));
                        }
                        if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                            e.setCancelled(true);
                            ItemStack bankItem = new ItemStack(Material.EMERALD);
                            ItemMeta meta = bankItem.getItemMeta();
                            meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                    + ChatColor.GREEN + " GEM(s)");
                            ArrayList<String> lore = new ArrayList<>();
                            int newBalance = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId()) + size;
                            BankMechanics.getInstance().addGemsToPlayerBank(player.getUniqueId(), size);
                            BankMechanics.getInstance().checkBankAchievements(player.getUniqueId(), newBalance);
                            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "+" + ChatColor.GREEN + size + ChatColor.BOLD + "G, New Balance: " + ChatColor.GREEN + newBalance + " GEM(s)");
                            lore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to withdraw " + ChatColor.GREEN.toString() + ChatColor.BOLD + "RAW GEMS");
                            lore.add(ChatColor.GREEN + "Right Click " + ChatColor.GRAY + "to create " + ChatColor.GREEN.toString() + ChatColor.BOLD + "A GEM NOTE");
                            meta.setLore(lore);
                            bankItem.setItemMeta(meta);
                            net.minecraft.server.v1_9_R2.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                            nmsBank.getTag().setString("type", "bank");
                            e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    }
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("Collection Bin")) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            if (e.getRawSlot() > e.getInventory().getSize()) {
                e.setCancelled(true);
            } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                ItemStack stack = e.getCurrentItem();
                if (e.getWhoClicked().getInventory().firstEmpty() >= 0) {
                    e.setCurrentItem(new ItemStack(Material.AIR));
                    e.getWhoClicked().getInventory().addItem(stack);
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("container.crafting")) {
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                if (BankMechanics.getInstance().isBankNote(e.getCurrentItem()) && BankMechanics.getInstance().isBankNote(e.getCursor())) {
                    int note1Worth = BankMechanics.getInstance().getNoteValue(e.getCurrentItem());
                    int note2Worth = BankMechanics.getInstance().getNoteValue(e.getCursor());
                    int worth = note1Worth + note2Worth;
                    if (worth > 100000) {
                        player.sendMessage(ChatColor.RED + "You cannot create a banknote of this value.");
                        e.setCancelled(true);
                        return;
                    }
                    e.setCursor(null);
                    e.setCurrentItem(BankMechanics.createBankNote(worth));
                    player.sendMessage(ChatColor.GRAY + "You have combined bank notes " + ChatColor.ITALIC + note1Worth + "G + " + note2Worth + "G " + ChatColor.GRAY + "with the value of " + ChatColor.BOLD + worth + "G");
                }
            }


            if (!e.isRightClick())
                return;
            if (e.getCurrentItem() != null && BankMechanics.getInstance().isGemPouch(e.getCurrentItem()))

                if ((e.getCursor() == null || e.getCursor().getType() == Material.AIR)) {
                    e.setCancelled(true);
                    int pouchGems = BankMechanics.getInstance().getPouchAmount(e.getCurrentItem());
                    if (pouchGems <= 64) {
                        e.setCursor(BankMechanics.createGems(pouchGems));
                        e.setCurrentItem(BankMechanics.getInstance().makeNewPouch(e.getCurrentItem(), 0));
                    } else {
                        e.setCursor(BankMechanics.createGems(64));
                        e.setCurrentItem(BankMechanics.getInstance().makeNewPouch(e.getCurrentItem(), pouchGems - 64));
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("Storage Chest")) {
            Player p = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();
            ItemStack item = null;
            if (e.isShiftClick()) {
                if (slot > e.getInventory().getSize()) {
                    item = e.getCurrentItem();
                } else {
                    return;
                }
            } else {
                if (slot > e.getInventory().getSize()) {
                    return;
                } else {
                    item = e.getCursor();
                }
            }

            if (!API.isItemTradeable(item) || !API.isItemDroppable(item)) {
                p.sendMessage(ChatColor.RED + "You can't store this item!");
                e.setCancelled(true);
            }


        }

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void splitBankNote(PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();
        if (interactEvent.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (interactEvent.getPlayer().getInventory().getItemInMainHand() != null && BankMechanics.getInstance().isBankNote(interactEvent.getPlayer().getInventory().getItemInMainHand())) {
                int noteWorth = BankMechanics.getInstance().getNoteValue(player.getInventory().getItemInMainHand());
                player.sendMessage(ChatColor.GRAY + "This bank note is worth " + ChatColor.GREEN + noteWorth + " Gems." + ChatColor.GRAY + " Please enter the amount");
                player.sendMessage(ChatColor.GRAY + "you'd like to sign an additional bank note for. Alternatively,");
                player.sendMessage(ChatColor.GRAY + "type" + ChatColor.RED + " 'cancel' " + ChatColor.GRAY + "to stop this operation.");
                ItemStack heldItem = interactEvent.getPlayer().getEquipment().getItemInMainHand().clone();
                interactEvent.getPlayer().getInventory().setItemInMainHand(null);

                Chat.listenForMessage(player, event -> {
                    if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("c")) {
                        player.getInventory().addItem(heldItem);
                        player.sendMessage(ChatColor.RED + "Bank Note Split - " + ChatColor.BOLD + "CANCELLED");
                        return;
                    }
                    int number = 0;
                    try {
                        number = Integer.parseInt(event.getMessage());
                    } catch (Exception exc) {
                        player.sendMessage(ChatColor.RED + "Please enter a valid number");
                        player.getInventory().addItem(heldItem);
                        return;
                    }
                    if (number <= 0) {
                        player.sendMessage(ChatColor.RED + "You must enter a POSITIVE amount.");
                        player.getInventory().addItem(heldItem);
                    } else if (number > noteWorth) {
                        player.sendMessage(ChatColor.GRAY + "You cannot split a note more than what it's worth.");
                        player.getInventory().addItem(heldItem);
                    } else {
                        if (player.getInventory().firstEmpty() != -1) {
                            if (number == noteWorth) {
                                player.getInventory().addItem(heldItem);
                                return;
                            }
                            int newValue = noteWorth - number;
                            player.getInventory().addItem(BankMechanics.createBankNote(newValue));
                            player.getInventory().addItem(BankMechanics.createBankNote(number));
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have enough space in your inventory to perform this action.");
                        }
                    }
                }, p -> {
                    player.getInventory().addItem(heldItem);
                    p.sendMessage(ChatColor.RED + "Bank Note Split - " + ChatColor.BOLD + "CANCELLED");
                });

            }
        }
    }


    /**
     * Checks if player has room in inventory for amount of gems to withdraw.
     *
     * @param uuid
     * @param Gems_worth being added
     * @since 1.0
     */
    private boolean hasSpaceInInventory(UUID uuid, int Gems_worth) {
        if (Gems_worth > 64) {
            int space_needed = Math.round(Gems_worth / 64) + 1;
            int count = 0;
            ItemStack[] contents = Bukkit.getPlayer(uuid).getInventory().getContents();
            for (ItemStack content : contents) {
                if (content == null || content.getType() == Material.AIR) {
                    count++;
                }
            }
            int empty_slots = count;

            if (space_needed > empty_slots) {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED
                        + "You do not have enough space in your inventory to withdraw " + Gems_worth + " GEM(s).");
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + space_needed + " slots");
                return false;
            } else
                return true;
        }
        return Bukkit.getPlayer(uuid).getInventory().firstEmpty() != -1;
    }

    /**
     * Gets an Inventory specific for player.
     *
     * @param uuid
     * @since 1.0
     */
    private Inventory getBank(UUID uuid) {
        Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
        ItemStack bankItem = new ItemStack(Material.EMERALD);
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


        ItemMeta collectionMeta = storage.getItemMeta();
        collectionMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "COLLECTION BIN");
        ArrayList<String> collectionlore = new ArrayList<>();
        collectionlore.add(ChatColor.GREEN + "Left Click " + ChatColor.GRAY + "to open " + ChatColor.GREEN.toString() + ChatColor.BOLD + "COLLECTION BIN");
        collectionMeta.setLore(collectionlore);
        storage.setItemMeta(collectionMeta);
        net.minecraft.server.v1_9_R2.ItemStack collectionBin = CraftItemStack.asNMSCopy(storage);
        collectionBin.getTag().setString("type", "collection");
        if (BankMechanics.getInstance().getStorage(uuid).collection_bin != null) {
            inv.setItem(4, CraftItemStack.asBukkitCopy(collectionBin));
        } else if (DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, uuid).toString().length() > 1) {
            BankMechanics.getInstance().getStorage(uuid).update();
            inv.setItem(4, CraftItemStack.asBukkitCopy(collectionBin));
        }
        return inv;
    }

    /**
     * Get Player Gems.
     *
     * @param uuid
     * @since 1.0
     */
    private int getPlayerGems(UUID uuid) {
        return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
    }

}
