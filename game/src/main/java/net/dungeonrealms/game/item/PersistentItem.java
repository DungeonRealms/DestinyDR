package net.dungeonrealms.game.item;

import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * PersistentItem - Items that contain a variety of data that need to be loaded or changed.
 *
 * @author Kneesnap
 */
public abstract class PersistentItem {

    protected ItemStack item;
    private NBTTagCompound tag;
    private boolean generating;

    public PersistentItem() {
        this(null);
    }

    public PersistentItem(ItemStack item) {
        this.item = item;
        if (item != null)
            loadItem();
    }

    public ItemStack getItem() {
        return item == null ? getStack() : item;
    }

    /**
     * Loads all the data stored in this item from NBT.
     * This will only be called if this instance has an item.
     */
    protected abstract void loadItem();

    /**
     * Gets a vanilla ItemStack for this item.
     * May not contain any DR data.
     */
    protected abstract ItemStack getStack();

    /**
     * Updates all the data for the current item.
     * Should be called after changes are made.
     * <p>
     * Bukkit data gets updated here. NBT Data gets set but NOT UPDATED HERE.
     * Only call this if you want to update the existing itemStack.
     * NBT data cannot be updated without creating a new ItemStack object.
     * Call generateItem and replace the item you're editting with the result.
     */
    public abstract void updateItem();

    /**
     * Generates an item based on specified parameters.
     * Overrides the current item, if any.
     */
    public ItemStack generateItem() {
        this.generating = true;
        this.item = getStack();
        updateItem();
        setTag(this.tag);
        // We are no longer generating the item.
        this.generating = false;
        return getItem();
    }

    /**
     * Is the item being completely generated right now or is it just doing a shallow update?
     */
    protected boolean isGenerating() {
        return this.generating;
    }

    /**
     * Set an enum value in NBT.
     *
     * @param key
     * @param val
     */
    public <T extends Enum<T>> void setEnum(String key, Enum<T> val) {
        setTagString(key, val.name());
    }

    /**
     * Load an enum from NBT.
     *
     * @param key
     * @param fallback - Default value. Please do not supply null.
     */
    public <T extends Enum<T>> T getEnum(String key, T fallback) {
        return getEnum(key, fallback.getClass(), fallback);
    }

    /**
     * Load an enum value from NBT.
     *
     * @param key
     * @param cls
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <T extends Enum<T>> T getEnum(String key, Class<? extends Enum> cls) {
        return getEnum(key, cls, null);
    }

    /**
     * Gets an enum value from NBT.
     *
     * @param key          - The NBT key to load from.
     * @param cls          - The enum's class.
     * @param defaultValue - The default value.
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Enum<T>> T getEnum(String key, Class<? extends Enum> cls, T defaultValue) {
        try {
            return (T) Enum.valueOf(cls, getTagString(key));
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Set the NBT tag of this item.
     * Only call if this has an item.
     */
    public void setTag(NBTTagCompound tag) {
        net.minecraft.server.v1_9_R2.ItemStack nms = getNMSCopy();
        if (nms != null)
            nms.setTag(tag);
        this.item = CraftItemStack.asBukkitCopy(nms);
    }

    /**
     * Does this item's NBT tag have this key?
     * Returns false if there is no tag.
     * Only call if this has an item.
     */
    public boolean hasTag(String s) {
        return getTag().hasKey(s);
    }

    /**
     * Gets the NBT tag of this item.
     * Only call if this has an item.
     */
    public NBTTagCompound getTag() {
        if (this.tag == null) {
            net.minecraft.server.v1_9_R2.ItemStack nms = getNMSCopy();
            this.tag = (nms != null && nms.hasTag()) ? nms.getTag() : new NBTTagCompound();
        }
        return tag;
    }

    /**
     * Removes a tag.
     */
    public void removeTag(String key) {
        NBTTagCompound tag = getTag();
        if (tag.hasKey(key))
            tag.remove(key);
    }

    /**
     * Sets the boolean nbt tag value.
     */
    public void setTagBool(String key, boolean val) {
        NBTTagCompound tag = getTag();
        if (!val && !tag.hasKey(key)) //Don't bother using extra space, since it will default to false.
            return;
        tag.setBoolean(key, val);
    }

    /**
     * Gets a boolean nbt tag value.
     */
    public boolean getTagBool(String key) {
        NBTTagCompound tag = getTag();
        return tag.hasKey(key) && tag.getBoolean(key);
    }

    /**
     * Gets an integer nbt value.
     */
    public int getTagInt(String key) {
        return hasTag(key) ? getTag().getInt(key) : 0;
    }

    /**
     * Sets an integer nbt value.
     */
    public void setTagInt(String key, int val) {
        NBTTagCompound tag = getTag();
        tag.setInt(key, val);
    }

    /**
     * Get a string type nbt tag value.
     */
    public String getTagString(String key) {
        NBTTagCompound tag = getTag();
        return tag.hasKey(key) ? tag.getString(key) : null;
    }

    /**
     * Sets a string type nbt tag value.
     */
    public void setTagString(String k, String v) {
        NBTTagCompound tag = getTag();
        tag.setString(k, v);
    }

    /**
     * Get a NMS copy of this item.
     * Only call if this has an item.
     */
    public net.minecraft.server.v1_9_R2.ItemStack getNMSCopy() {
        return CraftItemStack.asNMSCopy(getItem());
    }

    /**
     * Gets the ItemType for this item, if any.
     *
     * @param item
     * @return
     */
    public static ItemType getType(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || !nms.hasTag() || !nms.getTag().hasKey("type"))
            return null;
        return ItemType.getType(nms.getTag().getString("type"));
    }

    /**
     * Returns if a given itemstack is of a given type.
     * Null-Safe
     */
    public static boolean isType(ItemStack item, ItemType type) {
        return type.equals(getType(item));
    }

    public static PersistentItem constructItem(ItemStack item) {
        ItemType type = getType(item);
        if (type == null) {
            return new VanillaItem(item);
        }


        try {
            return type.getItemClass().getDeclaredConstructor(ItemStack.class).newInstance(item);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log.info("Failed to construct " + type.getItemClass().getName() + ". Is it missing a constructor accepting only ItemStack?");
        }

        return new VanillaItem(item);
    }
}
