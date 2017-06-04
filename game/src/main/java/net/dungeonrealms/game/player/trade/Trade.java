package net.dungeonrealms.game.player.trade;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Nov 16, 2015
 */
public class Trade {

    public Player p1;
    public Player p2;
    public boolean p1Ready;
    public boolean p2Ready;
    public Inventory inv;

    private List<ItemStack> p1Items;

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
//        inv = Bukkit.createInventory(null, 36, generateTitle(p1.getName(), p2.getName()));
        inv = Bukkit.createInventory(null, 36, "Trade Window");
        if (!p1.isOnline() || p1 == null || !p2.isOnline() || p2 == null) {
            TradeManager.trades.remove(this);
            return;
        }
        Bukkit.getPlayer(p1.getUniqueId()).closeInventory();
        Bukkit.getPlayer(p2.getUniqueId()).closeInventory();
        p1.setCanPickupItems(false);
        p2.setCanPickupItems(false);
        ItemStack item = ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP");
        item.setDurability(DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "READY UP");
        inv.setItem(0, CraftItemStack.asBukkitCopy(nms));
        inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
        setDividerColor(DyeColor.WHITE);
        p1.openInventory(inv);
        p2.openInventory(inv);
    }

    public void setDividerColor(DyeColor dye) {
        ItemStack separator = ItemManager.createItem(Material.STAINED_GLASS_PANE, " ");
        separator.setDurability(dye.getData());
        inv.setItem(4, separator);
        inv.setItem(13, separator);
        inv.setItem(22, separator);
        inv.setItem(31, separator);
    }


    public static String generateTitle(String lPName, String rPName) {
        String title = lPName;

        int spacesLeft = 32 - (title.length() + rPName.length());

        for (int i = 0; i < spacesLeft; i++) {
            title += " ";
        }
        title += rPName;
        return title;
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
                GameAPI.giveOrDropItem(p1, item);
            } else if (isRightSlot(i)) {
                GameAPI.giveOrDropItem(p2, item);
            }
        }

        if (p1.getItemOnCursor() != null) {
            ItemStack item = p1.getItemOnCursor().clone();
            p1.setItemOnCursor(null);
            GameAPI.giveOrDropItem(p1, item);

        }
        if (p2.getInventory() != null) {
            ItemStack item = p2.getItemOnCursor().clone();
            p2.setItemOnCursor(null);
            GameAPI.giveOrDropItem(p2, item);
//            p2.getInventory().addItem(item);

        }
        p1.setCanPickupItems(true);
        p2.setCanPickupItems(true);
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
     * Checks if both players are readied up and then doTrade
     */
    public void checkReady() {
        if (p1Ready && p2Ready) {
            for (int i = 1; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null)
                    continue;
                if (item.getType() == Material.AIR || item.getType() == Material.STAINED_GLASS_PANE)
                    continue;
                if (i == 8)
                    continue;

                if (isLeftSlot(i)) {
                    if (p2.getInventory().firstEmpty() == -1) {
                        //CANT TRADE
                        p1.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the trade.");
                        p2.sendMessage(ChatColor.RED + p2.getName() + " does not have enough inventory space to accept the trade.");
                        changeReady();
                        return;
                    }
                } else if (isRightSlot(i)) {
                    if (p1.getInventory().firstEmpty() == -1) {
                        p1.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the trade.");
                        p2.sendMessage(ChatColor.RED + p1.getName() + " does not have enough inventory space to accept the trade.");
                        changeReady();
                        return;
                    }
                }
            }

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
        p1.setCanPickupItems(true);
        p2.setCanPickupItems(true);
        p1.sendMessage(ChatColor.GREEN + "Trade successful.");
        p2.sendMessage(ChatColor.GREEN + "Trade successful.");
        remove();
    }

    public void changeReady() {
//        ItemStack item = ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP");
//        item.setDurability(DyeColor.GRAY.getWoolData());
//        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
//        NBTTagCompound nbt = new NBTTagCompound();
//        nbt.setString("status", "notready");
//        nms.setTag(nbt);
//        nms.c(ChatColor.YELLOW + "READY UP");

        ItemStack item =
                new NBTWrapper(ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP", DyeColor.GRAY.getDyeData(), ChatColor.GRAY + "Click to accept trade"))
                        .setString("status", "notready").build();

        inv.setItem(0, item);
        inv.setItem(8, item);
        p1Ready = false;
        p2Ready = false;
        playSound(Sound.BLOCK_ANVIL_FALL, 1.8F);
    }

    public void playSound(Sound sound, float speed) {
        p1.playSound(p1.getLocation(), sound, .3F, speed);
        p2.playSound(p2.getLocation(), sound, .3F, speed);

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

    public Player getOppositePlayer(Player player) {
        if (p1.equals(player)) return p2;
        return p1;
    }
}
