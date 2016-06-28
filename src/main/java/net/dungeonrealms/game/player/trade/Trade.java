package net.dungeonrealms.game.player.trade;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

/**
 * Created by Chase on Nov 16, 2015
 */
public class Trade {

    public Player p1;
    public Player p2;
    public boolean p1Ready;
    public boolean p2Ready;
    public Inventory inv;

    public Trade(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        p1.sendMessage(ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + p2.getName() + "...");
        p2.sendMessage(ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + p1.getName() + "...");
        openInventory();
    }

    public void remove() {
        TradeManager.trades.remove(this);
    }

    /**
     * Opens Trade Window
     */
    private void openInventory() {
        inv = Bukkit.createInventory(null, 36, "Trade Window");
        Bukkit.getPlayer(p1.getUniqueId()).closeInventory();
        Bukkit.getPlayer(p2.getUniqueId()).closeInventory();
        ItemStack separator = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, " ", null, (short) 0);
        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP",
                null, DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "READY UP");
        inv.setItem(0, CraftItemStack.asBukkitCopy(nms));
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        inv.setItem(4, separator);
        inv.setItem(13, separator);
        inv.setItem(22, separator);
        inv.setItem(31, separator);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            p1.openInventory(inv);
            p2.openInventory(inv);
        }, 20);
    }

    // 0, 8 Confirm
    // 4, 13, 22, 27, 31 separator

    /**
     * Checks if specified slot is owned the the player on the left side.
     * <p>
     * LEFT ITEMS 1, 2, 3 9, 10, 11, 12, 18, 19, 20, 21
     *
     * @param slot
     * @return boolean
     * @since 1.0
     */
    public boolean isLeftSlot(int slot) {
        int[] left = new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20, 21, 30, 27, 28, 29};
        for (int aLeft : left)
            if (aLeft == slot)
                return true;
        return false;
    }

    /**
     * // RIGHT ITEMS 23, 24, 25, 26, 5, 6, 7, 14, 15, 16, 17
     *
     * @param slot
     * @return
     */
    public boolean isRightSlot(int slot) {
        int[] right = new int[]{23, 24, 25, 26, 5, 6, 7, 8, 14, 15, 16, 17, 32, 33, 34, 35};
        for (int aRight : right)
            if (aRight == slot)
                return true;
        return false;
    }

    /**
     * Handles if one player closes the trade inv before both players are ready.
     */
    public void handleClose() {
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;
            if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE)
                continue;
            if (i == 8)
                continue;
            if (isLeftSlot(i)) {
                p1.getInventory().addItem(item);
            } else if (isRightSlot(i)) {
                p2.getInventory().addItem(item);
            }
        }
        remove();
        p1.closeInventory();
        p2.closeInventory();
        p1.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Trade cancelled.");
        p2.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Trade cancelled.");
    }

    /**
     * @param uniqueId
     * @return
     */
    public boolean isLeftPlayer(UUID uniqueId) {
        return uniqueId.toString().equalsIgnoreCase(p1.getUniqueId().toString());
    }

    /**
     * Checks if both players are readyd up and then doTrade
     */
    public void checkReady() {
        if (p1Ready && p2Ready) {
            p1.closeInventory();
            p2.closeInventory();
            doTrade();
        }
    }

    /**
     * Finalize trade
     */
    private void doTrade() {
        for (int i = 1; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null)
                continue;
            if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE)
                continue;
            if (i == 8)
                continue;
            if (isLeftSlot(i)) {
                p2.getInventory().addItem(item);
            } else if (isRightSlot(i)) {
                p1.getInventory().addItem(item);
            }
        }
        p1.sendMessage(ChatColor.GREEN + "Trade successful.");
        p2.sendMessage(ChatColor.GREEN + "Trade successful.");
        remove();
    }

    public void changeReady() {
        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP",
                null, DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "READY UP");
        inv.setItem(0, CraftItemStack.asBukkitCopy(nms));
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        p1Ready = false;
        p2Ready = false;

    }

    /**
     * @param uniqueId
     */
    public void updateReady(UUID uniqueId) {
        if (uniqueId.toString().equalsIgnoreCase(p1.getUniqueId().toString())) {
            p1Ready = !p1Ready;
            if (p1Ready) {
                p1.sendMessage(ChatColor.YELLOW + "Trade accepted, waiting for " + ChatColor.BOLD + p2.getName() + "...");
                p2.sendMessage(ChatColor.GREEN + p1.getName() + " has accepted the trade.");
                p2.sendMessage(ChatColor.GRAY + "Click the gray button (dye) to accept.");
            } else {
                p1.sendMessage(ChatColor.RED + "Trade is pending your accept..");
                p2.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + p1.getName() + ChatColor.RED + " has unaccepted the trade");
            }
        } else {
            p2Ready = !p2Ready;
            if (p2Ready) {
                p2.sendMessage(ChatColor.YELLOW + "Trade accepted, waiting for " + ChatColor.BOLD + p1.getName() + "...");
                p1.sendMessage(ChatColor.GREEN + p2.getName() + " has accepted the trade.");
                p1.sendMessage(ChatColor.GRAY + "Click the gray button (dye) to accept.");
            } else {
                p2.sendMessage(ChatColor.RED + "Trade is pending your accept..");
                p1.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + p1.getName() + ChatColor.RED + " has unaccepted the trade");
            }
        }
    }
}
