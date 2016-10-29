package net.dungeonrealms.old.game.mastery;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
public class NBTItem {

    private ItemStack bukkitItem;

    public NBTItem(ItemStack item) {
        bukkitItem = item.clone();
    }

    public ItemStack getItem() {
        return bukkitItem;
    }

    public void setString(String key, String value) {
        bukkitItem = NBTReflection.setString(bukkitItem, key, value);
    }

    public String getString(String key) {
        return NBTReflection.getString(bukkitItem, key);
    }

    public void setInteger(String key, int value) {
        bukkitItem = NBTReflection.setInt(bukkitItem, key, value);
    }

    public Integer getInteger(String key) {
        return NBTReflection.getInt(bukkitItem, key);
    }

    public void setDouble(String key, double value) {
        bukkitItem = NBTReflection.setDouble(bukkitItem, key, value);
    }

    public double getDouble(String key) {
        return NBTReflection.getDouble(bukkitItem, key);
    }

    public void setBoolean(String key, boolean value) {
        bukkitItem = NBTReflection.setBoolean(bukkitItem, key, value);
    }

    public boolean getBoolean(String key) {
        return NBTReflection.getBoolean(bukkitItem, key);
    }

    public boolean hasKey(String key) {
        return NBTReflection.hasKey(bukkitItem, key);
    }

}
