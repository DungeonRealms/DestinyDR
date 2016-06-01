
package net.dungeonrealms.game.miscellaneous;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick on 10/31/2015.
 */
public class ItemBuilder {

    private ItemStack itemStack;

    public ItemBuilder setItem(ItemStack item, String name, String[] lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        this.itemStack = item;
        return this;
    }

    public ItemBuilder setItem(Material material, short shortID, String name, String[] lore) {
        ItemStack tempItem = new ItemStack(material, 1, shortID);
        ItemMeta meta = tempItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        tempItem.setItemMeta(meta);
        this.itemStack = tempItem;
        return this;
    }

    public ItemBuilder setItem(ItemStack item) {
        this.itemStack = item;
        return this;
    }

    public ItemBuilder addLore(String lore) {
        ItemStack item = itemStack;
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = meta.getLore();
        itemLore.add(lore);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        this.itemStack = item;
        return this;
    }

    public ItemBuilder setNBTString(String identifier, String content) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        nbtTagCompound.set(identifier, new NBTTagString(content));
        nmsStack.setTag(nbtTagCompound);
        this.itemStack = CraftItemStack.asBukkitCopy(nmsStack);
        return this;
    }

    public ItemBuilder setNBTInt(String identifier, int content) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        nbtTagCompound.set(identifier, new NBTTagInt(content));
        nmsStack.setTag(nbtTagCompound);
        this.itemStack = CraftItemStack.asBukkitCopy(nmsStack);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

}