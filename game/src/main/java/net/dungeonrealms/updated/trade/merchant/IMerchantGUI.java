package net.dungeonrealms.updated.trade.merchant;

import net.dungeonrealms.updated.trade.ItemTrade;
import net.dungeonrealms.updated.trade.gui.action.ClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClicker;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IMerchantGUI {

    MerchantTrade getCorrespondingTrade();

    Map<Integer, ItemStack> getItemsInScreen();

    /**
     * Handle a trade click
     *
     * @param clickAction The click action
     * @param event       The called event
     */
    void handleClick(ClickAction clickAction, InventoryClickEvent event);

    void handleButtonClick();

    /**
     * Get a list of disallowed slots
     *
     * @return List<Integer>
     */
    default List<Integer> disallowedSlots() {
        return Arrays.asList(0, 4, 8, 13, 22, 31);
    }

    /**
     * Get a list of allowed slots to click on based on the clicker
     *
     * @param clicker The clicker
     * @return List<Integer>
     */
    default List<Integer> allowedSlots(EnumClicker clicker) {
        switch (clicker) {
            case OWNER:
                return Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 26, 27, 28, 29);
            case PARTICIPATOR:
                return Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 22, 23, 24, 25, 32, 33, 34, 35);
            default:
                break;
        }
        return null;
    }

    default ItemStack getButton(boolean status) {
        if (status) {
            ItemStack itemStack = new ItemStack(Material.INK_SACK, (byte) 10);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + "TRADE ACCEPTED");
            itemMeta.setLore(Arrays.asList("", "Click to unready"));
            itemStack.setItemMeta(itemMeta);
            // Apply NBT
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("tradeActionButton", new NBTTagString("existent"));
            nmsStack.setTag(tag);
            return itemStack;
        } else {
            ItemStack itemStack = new ItemStack(Material.INK_SACK, (byte) 8);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + "CLICK TO ACCEPT");
            itemMeta.setLore(Arrays.asList("", "Click to accept trade"));
            itemStack.setItemMeta(itemMeta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("tradeActionButton", new NBTTagString("existent"));
            nmsStack.setTag(tag);
            return itemStack;
        }
    }
}
