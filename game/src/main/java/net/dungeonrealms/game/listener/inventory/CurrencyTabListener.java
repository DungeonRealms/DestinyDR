package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.miscellaneous.ScrapTier;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CurrencyTabListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().startsWith("Scrap Tab ") && !event.getInventory().getName().contains("@") && event.getInventory().getName().endsWith(")")) {

            boolean clickedTheirInventory = event.getRawSlot() >= event.getInventory().getSize() - 1;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                if (clickedTheirInventory && event.getCursor() != null && RepairAPI.isItemArmorScrap(event.getCursor()))
                    return;

                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                return;
            }

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            Player player = (Player) event.getWhoClicked();
            //Cant do anything with a cursor item.
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                player.sendMessage(ChatColor.RED + "Please empty your cursor item and try again.");
                return;
            }

            if (clickedTheirInventory) {
                //In their inventory?
                if (ItemManager.isScrap(clicked)) {
                    //Add the scrap to their max inventory.
                    CurrencyTab tab = BankMechanics.getInstance().getCurrencyTab().get(player.getUniqueId());

                    if (tab == null) {
                        player.closeInventory();
                        return;
                    }

                    int scrapTier = RepairAPI.getScrapTier(clicked);
                    if (scrapTier > 0) {

                        ScrapTier tier = ScrapTier.getScrapTier(scrapTier);
                        if (tier == null) return;
                        //Add this?
                        int amountToAdd = clicked.getAmount();

                        int currentAmount = tab.getScrapCount(tier);

                        int totalAmount = tab.getTotalScrapStored();
                        if (totalAmount + amountToAdd > 500) {
                            amountToAdd = 500 - totalAmount;

                            if (totalAmount == 500) {
                                player.sendMessage(ChatColor.RED + "Your Scrap Tab is " + ChatColor.UNDERLINE + "full" + ChatColor.RED + "!");
                                return;
                            }
                        }

                        if (currentAmount + amountToAdd > 250) {
                            if (currentAmount >= 250) {
                                player.sendMessage(ChatColor.RED + "That Scrap slot is " + ChatColor.UNDERLINE + "full" + ChatColor.RED + "!");
                                player.sendMessage(ChatColor.RED + "Each Scrap slot can hold up to 250 scrap of that type. ");
                                return;
                            }
                            int newAmount = 250 - currentAmount;
                            if (newAmount <= amountToAdd)
                                amountToAdd = newAmount;

                        }

                        if (amountToAdd < clicked.getAmount()) {
                            clicked.setAmount(clicked.getAmount() - amountToAdd);
                        } else {
                            player.getInventory().setItem(event.getSlot(), null);
                        }
                        tab.depositScrap(tier, amountToAdd);
                        tab.updateInventory(event.getInventory());
                        tab.updateWindowTitle(player);
                        player.updateInventory();
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.1F);
                        player.sendMessage(tier.getChatColor() + ChatColor.BOLD.toString() + "+ " + amountToAdd + " " + tier.getName() + " Scrap " + ChatColor.GREEN + "(" + ChatColor.BOLD + tab.getTotalScrapStored() + ChatColor.GREEN + " / " + ChatColor.BOLD + "500" + ChatColor.GREEN + ")");
                    }
                }
            } else {
                //Clicking ON TOP
                NBTWrapper wrapper = new NBTWrapper(clicked);
                if (wrapper.hasTag("scrapTier")) {
                    int tier = wrapper.getInt("scrapTier");

                    ScrapTier scrap = ScrapTier.getScrapTier(tier);

                    if (scrap == null) return;

                    CurrencyTab tab = BankMechanics.getInstance().getCurrencyTab().get(player.getUniqueId());

                    if (tab == null) {
                        player.closeInventory();
                        return;
                    }
                    int amountAvailable = tab.getScrapCount(scrap);
                    if (event.getClick() == ClickType.RIGHT) {
                        if (amountAvailable <= 0) {
                            player.sendMessage(ChatColor.RED + "No " + scrap.getName() + " Scrap" + ChatColor.RED + " stored.");
                            return;
                        }
                        //Enter amount into chat instead.
                        player.sendMessage("");
                        player.sendMessage(ChatColor.RED.toString() + "Please enter the amount of " + scrap.getName() + " Scrap " + ChatColor.RED + "you would like to withdraw.");
                        player.sendMessage("");
                        Location start = player.getLocation().clone();
                        Chat.listenForMessage(player, chat -> {
                            String msg = ChatColor.stripColor(chat.getMessage());
                            event.setCancelled(true);

                            int toWithdraw;
                            try {
                                toWithdraw = Integer.parseInt(msg);
                                if (toWithdraw <= 0) {
                                    player.sendMessage(ChatColor.RED + "Invalid amount entered.");
                                    return;
                                } else if (toWithdraw > 250) toWithdraw = 250;
                            } catch (Exception e) {
                                player.sendMessage(ChatColor.RED + "Invalid amount entered.");
                                return;
                            }

                            if (start.getWorld() != player.getWorld() || (start.distanceSquared(player.getLocation()) > 64)) {
                                player.sendMessage(ChatColor.RED + "You have moved > 6 blocks away.");
                                return;
                            }

                            int amountFoundNow = tab.getScrapCount(scrap);

                            if (amountFoundNow < toWithdraw) {
                                player.sendMessage(ChatColor.RED + "No " + scrap.getName() + " Scrap" + ChatColor.RED + " stored.");
                            } else {

                                player.closeInventory();
                                int stackSizes = Math.max(1, (int) Math.ceil(toWithdraw / 64D));
                                int emptySpaces = getEmptySlots(player);

                                if (emptySpaces < stackSizes) {
                                    player.sendMessage(ChatColor.RED + "You need " + ChatColor.BOLD + stackSizes + ChatColor.RED + " empty slots to withdraw that many scrap.");
                                    return;
                                }

                                tab.withdrawScrap(scrap, toWithdraw);
                                ItemStack item = ItemManager.createArmorScrap(scrap.getTier());

                                if (item != null) {
                                    item.setAmount(toWithdraw);
                                }

                                player.getInventory().addItem(item);
                                player.updateInventory();
                                player.sendMessage(scrap.getChatColor() + ChatColor.BOLD.toString() + "+ " + toWithdraw + " " + scrap.getName() + " Scrap");
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, .7F);
                            }
                        }, pl -> {
                            pl.sendMessage(ChatColor.RED + "Scrap withdrawl cancelled.");
                        });
                        return;
                    }

                    if (amountAvailable <= 0) {
                        player.sendMessage(ChatColor.RED + "You do not have any of that scrap stored.");
                        return;
                    }

                    int toGive = amountAvailable;
                    if (toGive > 64) {
                        toGive = 64;
                    }

                    //take the scrap
                    tab.withdrawScrap(scrap, toGive);
                    tab.updateWindowTitle(player);
                    player.updateInventory();

                    ItemStack is = ItemManager.createArmorScrap(scrap.getTier());
                    is.setAmount(toGive);
                    player.setItemOnCursor(is);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, .8F);
                    tab.updateInventory(event.getInventory());
                }

            }
        } else if (event.getInventory().getName().equalsIgnoreCase("Bank Chest") && (CurrencyTab.isEnabled() || Rank.isHeadGM((Player) event.getWhoClicked()))) {
            if (event.getRawSlot() == 1) {
                Player player = (Player) event.getWhoClicked();
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                //Clicking on
                CurrencyTab tab = BankMechanics.getInstance().getCurrencyTab().get(player.getUniqueId());
                if (tab == null || !tab.hasAccess) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                    player.sendMessage(ChatColor.RED + "You have not unlocked the Scrap Tab!");
                    player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + "http://dungeonrealms.net/store" + ChatColor.GRAY + "!");
                    player.closeInventory();
                    return;
                }

                Inventory inventory = tab.createCurrencyInventory();
                player.openInventory(inventory);
            }
        }
    }

    private int getEmptySlots(Player player) {
        int count = 0;
        for (ItemStack content : player.getInventory()) {
            if (content == null || content.getType() == Material.AIR) {
                count++;
            }
        }
        return count >= 1 ? count - 1 : count;
    }
}
