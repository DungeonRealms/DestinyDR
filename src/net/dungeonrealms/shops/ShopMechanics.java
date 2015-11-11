package net.dungeonrealms.shops;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopMechanics implements GenericMechanic{
    public static ConcurrentHashMap<UUID, Shop> PLAYER_SHOPS = new ConcurrentHashMap<>();
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
                Block block2 = block.getWorld().getBlockAt(block.getLocation().add(1, 1, 0));
                if (b.getType() == Material.AIR && block2.getType() == Material.AIR) {
                    block2.setType(Material.CHEST);
                    b.setType(Material.CHEST);
                    Shop shop = new Shop(uniqueId, shopName, b);
                	DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.HASSHOP, true, true);
                    PLAYER_SHOPS.put(uniqueId, shop);
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
        for (Shop shop : PLAYER_SHOPS.values()) {
            if (shop.block.getX() == block.getX() && shop.block.getY() == block.getY() && shop.getBlock().getZ() == block.getZ() || shop.block2.getX() == block.getX() && shop.block2.getY() == block.getY() && shop.block2.getZ() == block.getZ()) {
                return shop;
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
        for(Shop shop : PLAYER_SHOPS.values()){
            shop.saveCollectionBin();
            shop.deleteShop();
        }
        Bukkit.getWorlds().get(0).save();
    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.NO_STARTUP;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {
        deleteAllShops();
    }
}
