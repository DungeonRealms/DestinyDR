package net.dungeonrealms.listeners;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
                Block b = e.getClickedBlock();
                ItemStack stack = new ItemStack(b.getType(), 1);
                NBTTagCompound nbt = CraftItemStack.asNMSCopy(stack).getTag();
                e.setCancelled(true);
                e.getPlayer().openInventory(getBank(e.getPlayer().getUniqueId()));
                e.getPlayer().playSound(e.getPlayer().getLocation(), "random.chestopen", 1, 1);
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
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(e.getCursor());
                if (e.getRawSlot() < 9) {
                    if (e.getRawSlot() == 8) {
                        e.setCancelled(true);
                        if (e.getCursor() != null) {
                            if (e.getClick() == ClickType.LEFT) {
                                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
                                    if (event.getSlot() == AnvilSlot.OUTPUT) {
                                        int number = 0;
                                        try {
                                            number = Integer.parseInt(event.getName());
                                        } catch (Exception exc) {
                                            event.setWillClose(true);
                                            event.setWillDestroy(true);
                                            Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
                                            return;
                                        }
                                        event.setWillClose(true);
                                        event.setWillDestroy(true);
                                        int currentGems = getPlayerGems(player.getUniqueId());
                                        if (number < 0) {
                                            player.getPlayer().sendMessage("You can't ask for negative money!");
                                        } else if (number > currentGems) {
                                            player.getPlayer().sendMessage("You only have " + currentGems);
                                        } else {
                                            ItemStack stack = BankMechanics.gem.clone();
                                            if (hasSpaceInInventory(player.getUniqueId(), number)) {
                                                Player p = player.getPlayer();
                                                DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(),
                                                        EnumOperators.$INC, "info.gems", -number, true);
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
                                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                            }
                                        }

                                    }
                                });
                                ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                                ItemMeta meta = stack.getItemMeta();
                                meta.setDisplayName("Withdraw?");
                                stack.setItemMeta(meta);
                                gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                player.closeInventory();
                                gui.open();
                            } else if (e.getClick() == ClickType.RIGHT) {
                                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
                                    if (event.getSlot() == AnvilSlot.OUTPUT) {
                                        int number = 0;
                                        try {
                                            number = Integer.parseInt(event.getName());
                                        } catch (Exception exc) {
                                            event.setWillClose(true);
                                            event.setWillDestroy(true);
                                            Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
                                            return;
                                        }
                                        event.setWillClose(true);
                                        event.setWillDestroy(true);
                                        int currentGems = getPlayerGems(player.getUniqueId());
                                        if (number < 0) {
                                            player.getPlayer().sendMessage("You can't ask for negative money!");
                                        } else if (number > currentGems) {
                                            player.getPlayer().sendMessage("You only have " + currentGems);
                                        } else {
                                            ItemStack stack = BankMechanics.banknote.clone();
                                            ItemMeta meta = stack.getItemMeta();
                                            ArrayList<String> lore = new ArrayList<>();
                                            lore.add(ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString()
                                                    + number);
                                            meta.setLore(lore);
                                            stack.setItemMeta(meta);
                                            net.minecraft.server.v1_8_R3.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
                                            nms1.getTag().setInt("worth", number);
                                            Player p = player.getPlayer();
                                            p.getInventory().addItem(CraftItemStack.asBukkitCopy(nms1));
                                            DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(),
                                                    EnumOperators.$INC, "info.gems", -number, true);
                                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

                                        }

                                    }
                                });
                                ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                                ItemMeta meta = stack.getItemMeta();
                                meta.setDisplayName("Withdraw?");
                                stack.setItemMeta(meta);
                                gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                player.closeInventory();
                                gui.open();
                            }

                        }
                    } else if (e.getRawSlot() != 0) {
                        if (nms == null)
                            return;
                        e.setCancelled(true);
                        if (nms.hasTag() && e.getCursor().getType() == Material.EMERALD
                                || nms.hasTag() && e.getCursor().getType() == Material.PAPER) {
                            if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                                Utils.log.info("Added Gem");
                                int size = 0;
                                if (e.isLeftClick()) {
                                    if (e.getCursor().getType() == Material.EMERALD)
                                        size = e.getCursor().getAmount();
                                    else
                                        size = e.getCursor().getAmount() * nms.getTag().getInt("worth");
                                    e.setCursor(null);
                                    e.setCurrentItem(null);
                                } else if (e.isRightClick()) {
                                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                                    if (e.getCursor().getType() == Material.EMERALD)
                                        size = 1;
                                    else
                                        size = nms.getTag().getInt("worth");
                                }
                                BankMechanics.addGemsToPlayer(player.getUniqueId(), size);
                                ItemStack bankItem = new ItemStack(Material.EMERALD);
                                ItemMeta meta = bankItem.getItemMeta();
                                meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                        + ChatColor.GREEN + " Gem(s)");
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
                                lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
                                meta.setLore(lore);
                                bankItem.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                                nmsBank.getTag().setString("type", "bank");
                                e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                                // checkOtherBankSlots(e.getInventory(),
                                // player.getUniqueId());
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                            }
                        }
                    } else {
                        e.setCancelled(true);
                        Storage storage = BankMechanics.getStorage(player.getUniqueId());
                        if (e.isLeftClick()) {
                            // Open Storage
                            player.openInventory(storage.inv);
                        } else if (e.isRightClick()) {
                            // Upgrade Storage
                        }
                    }
                } else {
                    if (e.isShiftClick()) {
                        nms = CraftItemStack.asNMSCopy(e.getCurrentItem());
                        int size = 0;
                        if (e.getCurrentItem().getType() == Material.EMERALD)
                            size = e.getCurrentItem().getAmount();
                        else if (e.getCurrentItem().getType() == Material.PAPER) {
                            size = e.getCurrentItem().getAmount() * nms.getTag().getInt("worth");
                        }
                        if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                            e.setCancelled(true);
                            BankMechanics.addGemsToPlayer(player.getUniqueId(), size);
                            e.setCurrentItem(null);
                            ItemStack bankItem = new ItemStack(Material.EMERALD);
                            ItemMeta meta = bankItem.getItemMeta();
                            meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
                                    + ChatColor.GREEN + " Gem(s)");
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
                            lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
                            meta.setLore(lore);
                            bankItem.setItemMeta(meta);
                            net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
                            nmsBank.getTag().setString("type", "bank");
                            e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
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
                        meta.setDisplayName("Withdraw " + finalNum + " Gems");
                        item.setItemMeta(meta);
                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                        nms.getTag().setInt("withdraw", finalNum);
                        e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
                    } else if (current.getType() == Material.INK_SACK) {
                        int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        if (number == 0) {
                            return;
                        }
                        int currentGems = getPlayerGems(player.getUniqueId());
                        try {
                            if (number < 0) {
                                player.getPlayer().sendMessage("You can't ask for negative money!");
                            } else if (number > currentGems) {
                                player.getPlayer().sendMessage("You only have " + currentGems);
                            } else {
                                ItemStack stack = BankMechanics.gem.clone();
                                if (hasSpaceInInventory(player.getUniqueId(), number)) {
                                    Player p = player.getPlayer();
                                    DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
                                            "info.gems", -number, true);
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
                                    player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
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
        } else if (e.getInventory().getTitle().equalsIgnoreCase("How much?")) {
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
                        ItemStack item = new ItemStack(Material.PAPER, 1);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("Withdraw " + finalNum + " Gems");
                        item.setItemMeta(meta);
                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                        nms.getTag().setInt("withdraw", finalNum);
                        e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
                    } else if (current.getType() == Material.INK_SACK) {
                        int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
                        if (number == 0) {
                            return;
                        }
                        int currentGems = getPlayerGems(player.getUniqueId());
                        try {
                            if (number < 0) {
                                player.getPlayer().sendMessage("You can't ask for negative money!");
                            } else if (number > currentGems) {
                                player.getPlayer().sendMessage("You only have " + currentGems);
                            } else {
                                ItemStack stack = BankMechanics.banknote.clone();
                                ItemMeta meta = stack.getItemMeta();
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString() + number);
                                meta.setLore(lore);
                                stack.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                                nms.getTag().setInt("worth", number);
                                Player p = player.getPlayer();
                                p.getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
                                DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
                                        "info.gems", -number, true);
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
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
     * Gets amount to add, or subtract for each slot clicked in How Many?
     * Inventory.
     *
     * @param slot
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

    /**
     * Checks if player has room in inventory for ammount of gems to withdraw.
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
        storagetMeta.setDisplayName(ChatColor.RED.toString() + "Storage");
        ArrayList<String> storelore = new ArrayList<>();
        storelore.add(ChatColor.GREEN.toString() + "Left Click to open your storage.");
        storelore.add(ChatColor.GREEN.toString() + "Right Click to upgrade your storage!");
        storagetMeta.setLore(storelore);
        storage.setItemMeta(storagetMeta);
        net.minecraft.server.v1_8_R3.ItemStack storagenms = CraftItemStack.asNMSCopy(storage);
        storagenms.getTag().setString("type", "storage");
        inv.setItem(0, CraftItemStack.asBukkitCopy(storagenms));

        ItemMeta meta = bankItem.getItemMeta();
        meta.setDisplayName(getPlayerGems(uuid) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
        ArrayList<String> lore = new ArrayList<>();
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
     * Get Player Gems.
     *
     * @param uuid
     * @since 1.0
     */
    private int getPlayerGems(UUID uuid) {
        return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
    }

}
