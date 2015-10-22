package net.dungeonrealms.anticheat;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 10/1/2015.
 */
public class AntiCheat {

    static AntiCheat instance = null;

    public static AntiCheat getInstance() {
        if (instance == null) {
            instance = new AntiCheat();
        }
        return instance;
    }

    /**
     * Will be placed inside an eventlistener to make sure
     * the player isn't duplicating.
     *
     * @param event
     * @return
     * @since 1.0
     */
    public boolean watchForDupes(InventoryClickEvent event) {
        ItemStack checkItem = event.getCursor();
        if (checkItem == null) return false;
        if (!isRegistered(checkItem)) return false;
        String check = getUniqueEpochIdentifier(checkItem);
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == null || item.getType().equals(Material.AIR)) continue;
            if (check.equals(getUniqueEpochIdentifier(item))) return true;
        }
        return false;
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public String getUniqueEpochIdentifier(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || !tag.hasKey("u")) return null;
        return tag.getString("u");
    }

    /**
     * Check to see if item contains 'u' field.
     *
     * @param item
     * @return
     * @since 1.0
     */
    public boolean isRegistered(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return !(nmsStack == null || nmsStack.getTag() == null) && nmsStack.getTag().hasKey("u");
    }

    /**
     * Adds a (u) to the item. (u) -> UNIQUE IDENTIFIER
     *
     * @param item
     * @return
     * @since 1.0
     */
    public ItemStack applyAntiDupe(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || tag.hasKey("u")) return item;
        tag.set("u", new NBTTagString(System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability()));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

}
