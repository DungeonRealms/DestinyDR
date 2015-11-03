package net.dungeonrealms.miscellaneous;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * Created by Nick on 10/31/2015.
 */
public class ShmozoNBT {

    private ItemStack itemStack;

    public ShmozoNBT setItem(ItemStack item, String name, String[] lore) {
        ItemStack tempItem = item;
        ItemMeta meta = tempItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        tempItem.setItemMeta(meta);
        this.itemStack = tempItem;
        return this;
    }

    public ShmozoNBT setItem(String ownerName, String name, String[] lore) {
        ItemStack tempItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) tempItem.getItemMeta();
        meta.setOwner(ownerName);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        tempItem.setItemMeta(meta);
        this.itemStack = tempItem;
        return this;
    }

    public ShmozoNBT setNBT(String identifier, String content) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        nbtTagCompound.set(identifier, new NBTTagString(content));
        nmsStack.setTag(nbtTagCompound);
        this.itemStack = CraftItemStack.asBukkitCopy(nmsStack);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

}
