package net.dungeonrealms.game.miscellaneous;


import lombok.NonNull;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTWrapper {

    private ItemStack item;

    private net.minecraft.server.v1_9_R2.ItemStack nmsItem;

    public ItemStack build() {
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public NBTWrapper(@NonNull ItemStack item) {
        this.item = item;
        if (this.item != null)
            this.nmsItem = CraftItemStack.asNMSCopy(item);
    }

    public boolean hasTag(String tag) {
        return getCompound().hasKey(tag);
    }

    public NBTWrapper removeKey(String key) {
        NBTTagCompound compound = getCompound();
        compound.remove(key);
        nmsItem.setTag(compound);
        return this;
    }

    public NBTBase get(String string){ return getCompound().get(string);}
    public int getInt(String string) {
        return getCompound().getInt(string);
    }

    public String getString(String string) {
        return getCompound().getString(string);
    }

    public NBTWrapper set(String key, NBTBase base) {
        NBTTagCompound compound = getCompound();
        compound.set(key, base);
        nmsItem.setTag(compound);
        return this;
    }

    public NBTWrapper setString(String string, String value) {
        NBTTagCompound compound = getCompound();
        compound.setString(string, value);
        nmsItem.setTag(compound);
        return this;
    }

    public NBTWrapper setInt(String string, int value) {
        NBTTagCompound compound = getCompound();
        compound.setInt(string, value);
        nmsItem.setTag(compound);
        return this;
    }

    public NBTWrapper setLong(String string, long value) {
        NBTTagCompound compound = getCompound();
        compound.setLong(string, value);
        nmsItem.setTag(compound);
        return this;
    }
    public long getLong(String string) {
        return getCompound().getLong(string);
    }

    private NBTTagCompound getCompound() {
        return nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
    }
}

