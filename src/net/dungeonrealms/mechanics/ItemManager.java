package net.dungeonrealms.mechanics;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Nick on 9/18/2015.
 */
public class ItemManager {

    public static ItemStack getHearthStone(String name, String[] lore) {
        ItemStack rawStack = new ItemStack(Material.QUARTZ);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("wayshrine"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
}
