/**
 *
 */
package net.dungeonrealms.listeners;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
                e.setCancelled(true);
                e.getPlayer().openInventory(getBank(e.getPlayer()));
                e.getPlayer().playSound(e.getPlayer().getLocation(), "random.chestopen", 1, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBankClicked(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equalsIgnoreCase("Bank Chest")) {
            if (e.getCursor() != null) {
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(e.getCursor());
                if (e.getRawSlot() < 9) {
                    if (e.getRawSlot() == 8) {
                        e.setCancelled(true);
                        if (e.getCursor() != null) {
                            if (e.getRawSlot() == 8) {
                                if (e.getClick() == ClickType.LEFT) {
                                    openHowManyGems(player);
                                } else if (e.getClick() == ClickType.RIGHT) {

                                }
                            }
                        }
                    } else {
                        if (e.getCursor().getType() == Material.EMERALD) {
                            if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                                Utils.log.info("Added Gem");
                                BankMechanics.addGemsToPlayer(player, e.getCursor().getAmount());
                                e.setCursor(null);
                                ItemStack bankItem = new ItemStack(Material.EMERALD);
                                ItemMeta meta = bankItem.getItemMeta();
                                meta.setDisplayName(
                                        getPlayerGems(player) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
                                ArrayList<String> lore = new ArrayList<String>();
                                lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
                                lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
                                meta.setLore(lore);
                                bankItem.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                                nmsBank.getTag().setString("type", "bank");
                                e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                                checkOtherBankSlots(e.getInventory(), player);
                            }
                        }
                    }
                }
            }
        } else if (e.getInventory().getTitle().equalsIgnoreCase("How Many?")) {
            e.setCancelled(true);
            if (e.getRawSlot() < 27) {
                ItemStack current = e.getCurrentItem();
                if (current != null) {
                    if (current.getType() == Material.STAINED_GLASS_PANE) {
                        int number = getAmmount(e.getRawSlot());
                        int currentWith = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        int finalNum = 0;
                        finalNum = currentWith + number;
                        if (finalNum < 0)
                            finalNum = 0;
                        ItemStack item = new ItemStack(Material.EMERALD, 1);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(finalNum + " Gems");
                        item.setItemMeta(meta);
                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                        nms.getTag().setInt("withdraw", finalNum);
                        e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
                    } else if (current.getType() == Material.INK_SACK) {
                        int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        int currentGems = getPlayerGems(player.getPlayer());
                        try {
                            if (number < 0) {
                                player.getPlayer().sendMessage("You can't ask for negative money!");
                            } else if (number > currentGems) {
                                player.getPlayer().sendMessage("You only have " + currentGems);
                            } else {
                                ItemStack stack = BankMechanics.gem.clone();
                                if (hasSpaceInInventory(player.getPlayer(), number)) {
                                    Player p = player.getPlayer();
                                    DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC, "info.gems", -number);
                                    while (number > 0) {
                                        while (number > 64) {
                                            ItemStack item = stack.clone();
                                            item.setAmount(64);
                                            p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                            number -= 64;
                                        }
                                        ItemStack item = stack.clone();
                                        item.setAmount(number);
                                        p.getInventory().setItem(p.getInventory().firstEmpty(), item);
                                        number = 0;
                                    }
                                } else {
                                    player.getPlayer().sendMessage("You do not have space for all those gems");
                                }
                            }
                            player.closeInventory();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    /**
     * @param inventory
     */
    private void checkOtherBankSlots(Inventory inventory, Player player) {
        for (int i = 0; i < 8; i++) {
            if (inventory.getItem(i) != null) {
                Utils.log.info(i + " not null");
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(inventory.getItem(i));
                if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack != null) {
                        inventory.remove(stack);
                        inventory.getItem(8).getItemMeta().setDisplayName(getPlayerGems(player) + stack.getAmount() + " Gem(s)");
                        BankMechanics.addGemsToPlayer(player, stack.getAmount());
                    }
                }
            }
        }
    }

    /**
     * @param slot
     * @return
     * @since 1.0
     */
    private int getAmmount(int slot) {
        switch (slot) {
            case 0:
                return -1000;
            case 1:
                return -100;
            case 2:
                return -10;
            case 3:
                return -1;
            case 5:
                return 1;
            case 6:
                return 10;
            case 7:
                return 100;
            case 8:
                return 1000;
        }
        return 0;
    }

    public boolean hasSpaceInInventory(Player p, int Gems_worth) {
        if (Gems_worth > 64) {
            int space_needed = Math.round(Gems_worth / 64) + 1;
            int count = 0;
            ItemStack[] contents = p.getInventory().getContents();
            for (ItemStack content : contents) {
                if (content == null || content.getType() == Material.AIR) {
                    count++;
                }
            }
            int empty_slots = count;

            if (space_needed > empty_slots) {
                p.sendMessage(ChatColor.RED + "You do not have enough space in your inventory to withdraw " + Gems_worth
                        + " GEM(s).");
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + space_needed + " slots");
                return false;
            } else
                return true;
        }
        if (p.getInventory().firstEmpty() == -1)
            return false;
        return true;
    }

    public void openHowManyGems(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "How Many?");
        ItemStack item0 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1000", null,
                DyeColor.RED.getWoolData());
        ItemStack item1 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-100", null,
                DyeColor.RED.getWoolData());
        ItemStack item2 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-10", null,
                DyeColor.RED.getWoolData());
        ItemStack item3 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1", null,
                DyeColor.RED.getWoolData());
        ItemStack item4 = ItemManager.createItem(Material.EMERALD, "0 Gems", null);
        ItemStack item5 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1", null,
                DyeColor.LIME.getWoolData());
        ItemStack item6 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "10", null,
                DyeColor.LIME.getWoolData());
        ItemStack item7 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "100", null,
                DyeColor.LIME.getWoolData());
        ItemStack item8 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1000", null,
                DyeColor.LIME.getWoolData());
        ItemStack confimItem = ItemManager.createItemWithData(Material.INK_SACK, "Confirm", null, (short) 10);

        inv.setItem(0, item0);
        inv.setItem(1, item1);
        inv.setItem(2, item2);
        inv.setItem(3, item3);
        inv.setItem(4, item4);
        inv.setItem(5, item5);
        inv.setItem(6, item6);
        inv.setItem(7, item7);
        inv.setItem(8, item8);
        inv.setItem(26, confimItem);
        player.openInventory(inv);
    }

    /**
     * @param player
     * @return
     */
    private Inventory getBank(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
        ItemStack bankItem = new ItemStack(Material.EMERALD);
        ItemMeta meta = bankItem.getItemMeta();
        meta.setDisplayName(getPlayerGems(player) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GREEN.toString() + "Left Click to withdraw Raw Gems.");
        lore.add(ChatColor.GREEN.toString() + "Right Click to create a Bank Note.");
        meta.setLore(lore);
        bankItem.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(bankItem);
        nms.getTag().setString("type", "bank");
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        return inv;
    }

    /**
     * @param player
     * @return
     */
    private int getPlayerGems(Player player) {
        return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId());
    }

}
