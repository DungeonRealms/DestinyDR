package net.dungeonrealms.items;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/19/2015.
 */
public class Attribute {

    private ItemStack item;
    private net.minecraft.server.v1_8_R3.ItemStack nmsStack;

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
        return Item.ItemTier.getById(tag.getInt("itemTier"));
    }

    public Item.ItemModifier getItemModifier() {
        NBTTagCompound tag = nmsStack.getTag();
        return Item.ItemModifier.getById(tag.getInt("itemTier"));
    }

    public void addAttribute(Item.AttributeType type) {

    }

}
