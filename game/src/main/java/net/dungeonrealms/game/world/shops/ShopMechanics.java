package net.dungeonrealms.game.world.shops;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chase on Nov 17, 2015
 */
public class ShopMechanics implements GenericMechanic {

    public static ConcurrentHashMap<String, Shop> ALLSHOPS = new ConcurrentHashMap<>();

    public static Shop getShop(Block block) {
        for (Shop shop : ALLSHOPS.values()) {
            if (shop.block1.getX() == block.getX() && shop.block1.getY() == block.getY()
                    && shop.block1.getZ() == block.getZ()
                    || shop.block2.getX() == block.getX() && shop.block2.getY() == block.getY()
                    && shop.block2.getZ() == block.getZ()) {
                return shop;
            }
        }
        return null;
    }

    public static void deleteAllShops(boolean shutDown) {
        for (Shop shop : ALLSHOPS.values()) {
            shop.deleteShop(shutDown);
        }
        Bukkit.getWorlds().get(0).save();
    }

    public static boolean isItemSellable(ItemStack i) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(i);
        return !API.isItemTradeable(i) || !API.isItemDroppable(i) || API.isItemSoulbound(i) || MountUtils.isMount(i) ||  nms.hasTag() && nms.getTag().hasKey("subtype") && nms.getTag().getString("subtype").equalsIgnoreCase("starter");
    }

    public static void setupShop(Block block, UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        player.sendMessage(ChatColor.YELLOW + "Please enter a " + ChatColor.BOLD + "SHOP NAME." + ChatColor.YELLOW + " [max. 12 characters]");
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        Chat.listenForMessage(player, event -> {
            String shopName = event.getMessage();
            if (shopName.length() > 12) {
                player.sendMessage(ChatColor.RED + "Shop name '" + ChatColor.BOLD + shopName + ChatColor.RED + "' exceeds the MAX character limit of 12.");
                return;
            } else if (shopName.length() <= 2) {
                player.sendMessage(ChatColor.RED.toString() + "Shop name must be at least 3 characters.");
                return;
            }
            if (shopName.contains("@")) {
                player.sendMessage(ChatColor.RED + "Invalid character '@' in name.");
                return;
            }

            Block b = player.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
            Block block2 = block.getWorld().getBlockAt(block.getLocation().add(1, 1, 0));
            if (b.getType() == Material.AIR && block2.getType() == Material.AIR) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    block2.setType(Material.CHEST);
                    b.setType(Material.CHEST);
                    Shop shop = new Shop(uniqueId, b.getLocation(), Chat.getInstance().checkForBannedWords(shopName));
                    DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.HASSHOP, true, true);
                    ALLSHOPS.put(player.getName(), shop);
                    player.sendMessage(ChatColor.YELLOW + "Shop name assigned.");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "YOU'VE CREATED A SHOP!");
                    player.sendMessage(ChatColor.YELLOW + "To stock your shop, simply drag items into your shop's inventory.");
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SHOP_CREATOR);
                }, 1L);
            } else {
                player.sendMessage("You can't place a shop there");
            }
        }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
        player.closeInventory();

    }

    /**
     * @param ownerName
     * @return
     */
    public static Shop getShop(String ownerName) {
        return ALLSHOPS.get(ownerName);
    }

    public static void stripPriceLore(ItemStack stackClicked) {
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

    public static void listenForPricing(Shop shop, Player clicker, net.minecraft.server.v1_9_R2.ItemStack nms, int playerSlot) {
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
    }

    /**
     * @param item
     * @param price
     * @return
     */
    public static ItemStack addPrice(ItemStack item, int price) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        String[] arr = lore.toArray(new String[lore.size()]);
        item = NPCMenus.editItem(item, item.getItemMeta().getDisplayName(), arr);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.getTag().setInt("worth", price);
        return CraftItemStack.asBukkitCopy(nms);
    }
}
