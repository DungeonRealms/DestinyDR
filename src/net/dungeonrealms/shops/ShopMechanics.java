/**
 *
 */
package net.dungeonrealms.shops;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.mastery.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopMechanics {
    public static HashMap<UUID, Shop> shops = new HashMap<>();

    /**
     * setup new shop for player
     *
     * @param block
     * @param uniqueId
     */
    public static void setupShop(Block block, UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
            if (event.getSlot() == AnvilSlot.OUTPUT) {
                if (event.getName().equalsIgnoreCase("Shop Name?")) {
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                    player.sendMessage("Please enter a valid number");
                    return;
                }
                String shopName;
                try {
                    shopName = event.getName();
                    if (shopName.length() > 14) {
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        player.sendMessage(ChatColor.RED.toString() + "Shop name must be 14 characters.");
                        return;
                    }
                } catch (Exception exc) {
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                    Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
                    return;
                }
                Block b = player.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
                if (b.getType() == Material.AIR) {
                    b.setType(Material.CHEST);
                    Shop shop = new Shop(uniqueId, shopName, b);
                    shops.put(uniqueId, shop);
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                } else {
                    player.sendMessage("You can't place a shop there");
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                }
            }
        });

        ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("Shop Name?");
        stack.setItemMeta(meta);
        gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
        player.closeInventory();
        gui.open();

    }

    /**
     * gets the shop represented by this block.
     *
     * @param block
     */
    public static Shop getShop(Block block) {
        for (int i = 0; i < shops.values().size(); i++) {
            Shop current = (Shop) shops.values().toArray()[i];
            if (current.block.getX() == block.getX() && current.block.getY() == block.getY() && current.getBlock().getZ() == block.getZ()) {
                return current;
            }
        }
        return null;
    }

    /**
     * Very Dangerous Method..
     *
     * @since 1.0
     */
    public static void deleteAllShops() {
            while (shops.size() > 0) {
                shops.get(0).deleteShop();
                shops.remove(0);
                Utils.log.info("A Shop deleted");
            }

    }

    /**
     * Check a player for money
     *
     * @param uuid
     * @param price
     * @return
     * @since 1.0
     */
    public static boolean checkPlayerForMoney(UUID uuid, int price) {
        Player p = Bukkit.getPlayer(uuid);
        Inventory inv = p.getInventory();
        int gemWorth = 0;
        int noteWorth = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack current = inv.getItem(i);
            if (current != null && current.getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(current);
                p.sendMessage("0");
                if (nms.hasTag() && nms.getTag().hasKey("type")
                        && nms.getTag().getString("type").equalsIgnoreCase("money")) {
                    p.sendMessage("1");
                    if (current.getType() == Material.EMERALD) {
                        p.sendMessage("2");
                        gemWorth += (current.getAmount());
                    } else if (current.getType() == Material.PAPER) {
                        p.sendMessage("3");
                        if (nms.getTag().getInt("worth") == price)
                            noteWorth = (nms.getTag().getInt("worth"));
                    }
                }
            }
        }
        p.sendMessage(gemWorth + " gems");
        p.sendMessage(noteWorth + " notes");
        if (gemWorth >= price) {
            ItemStack gem = BankMechanics.gem.clone();
            gem.setAmount(price);
            p.getInventory().removeItem(gem);
            return true;
        } else if (noteWorth > 0) {
            ItemStack note = BankMechanics.createBankNote(price);
            p.getInventory().removeItem(note);
            return true;
        } else {
            return false;
        }
    }

}
