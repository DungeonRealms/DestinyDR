package net.dungeonrealms.game.world.items;

import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_9_R2.NBTTagCompound;

/**
 * Created by Nick on 9/19/2015.
 */
public class Attribute {

    private ItemStack item;
    private net.minecraft.server.v1_9_R2.ItemStack nmsStack;

    public Attribute(ItemStack item) {
        this.item = item;
        this.nmsStack = CraftItemStack.asNMSCopy(item);
    }

    public Item.ItemType getItemType() {
        NBTTagCompound tag = nmsStack.getTag();
        return Item.ItemType.getById(tag.getInt("itemType"));
    }

    public Item.ItemTier getItemTier() {
        NBTTagCompound tag = nmsStack.getTag();
        return Item.ItemTier.getByTier(tag.getInt("itemTier"));
    }

    public Item.ItemRarity getItemRarity() {
        NBTTagCompound tag = nmsStack.getTag();
        return Item.ItemRarity.getById(tag.getInt("itemRarity"));
    }

    public ItemStack getItem() {
        return item;
    }
}
