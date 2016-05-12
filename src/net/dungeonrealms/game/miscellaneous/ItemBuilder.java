package net.dungeonrealms.game.miscellaneous;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.itemnbtapi.NBTItem;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick on 10/31/2015.
 */
public class ItemBuilder {

	private NBTItem NBTitem;

    public ItemBuilder setItem(ItemStack item, String name, String[] lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        this.NBTitem = new NBTItem(item);
        return this;
    }

    public ItemBuilder setItem(Material material, short shortID, String name, String[] lore) {
        ItemStack tempItem = new ItemStack(material, 1, shortID);
        ItemMeta meta = tempItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        tempItem.setItemMeta(meta);
        this.NBTitem = new NBTItem(tempItem);
        return this;
    }

    public ItemBuilder setItem(ItemStack item) {
        this.NBTitem = new NBTItem(item);
        return this;
    }

    public ItemBuilder addLore(String lore) {
        ItemStack item = NBTitem.getItem();
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = meta.getLore();
        itemLore.add(lore);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        NBTitem = new NBTItem(item);
        return this;
    }

    public ItemBuilder setNBTString(String identifier, String content) {
        NBTItem temp = NBTitem;
        temp.setString(identifier, content);
        this.NBTitem = temp;
        return this;
    }

    public ItemBuilder setNBTInt(String identifier, int content) {
        NBTItem temp = NBTitem;
        temp.setInteger(identifier, content);
        this.NBTitem = temp;
        return this;
    }

    public ItemStack build() {
        return NBTitem.getItem();
    }

}